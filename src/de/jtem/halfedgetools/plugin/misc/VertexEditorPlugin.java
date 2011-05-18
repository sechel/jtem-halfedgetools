package de.jtem.halfedgetools.plugin.misc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.scene.PointSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class VertexEditorPlugin extends ShrinkPanelPlugin implements PointDragListener, HalfedgeListener {

	private HalfedgeInterface
		hif = null;
	private SwingDragEventTool
		tool = new SwingDragEventTool(InputSlot.SHIFT_LEFT_BUTTON);
	private JCheckBox
		xBox = new JCheckBox("X"),
		yBox = new JCheckBox("Y"),
		zBox = new JCheckBox("Z");
	private JPanel
		panel = new JPanel();

	private Set<Vertex<?,?,?>> 
		selectedVertices = new HashSet<Vertex<?,?,?>>();
	private double[] 
	    position = null;
	
	
	public VertexEditorPlugin() {
		tool.addPointDragListener(this);
		tool.setDescription("Vertex Editor Tool");
		shrinkPanel.setTitle("Vertex Coordinate Editor");
		
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = 1;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		panel.add(new JLabel("Fix direction"));
		panel.add(xBox,gbc1);
		panel.add(yBox,gbc1);
		panel.add(zBox,gbc2);
		panel.add(new JLabel("(Shift-LeftClick to Drag)"),gbc2);
		
		shrinkPanel.add(panel);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		AdapterSet adapters = hif.getAdapters();
		final Vertex<?,?,?> v = hds.getVertex(e.getIndex());
		double[] oldPos = adapters.getD(Position3d.class, v);
		double[] translation = Rn.subtract(null, position, oldPos);
		TranslatedPositionAdapter posAdapter = new TranslatedPositionAdapter(translation, selectedVertices);
		hif.getActiveLayer().updateGeometry(posAdapter);
	}
	
	@Override
	public void pointDragStart(PointDragEvent e) {
		HalfedgeSelection hes = new HalfedgeSelection(hif.getSelection());
		selectedVertices = new HashSet<Vertex<?,?,?>>(hes.getVertices());
	}
	
	@Override
	public void pointDragged(PointDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		if (hds == null) return;
		if (hds.numVertices() <= e.getIndex()) return;
		if (e.getIndex() < 0) return;
		PointSet ps = e.getPointSet();
		position = e.getPosition();
		Pn.dehomogenize(position, position);
		double[][] coords = ps.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		double[] xyz = coords[e.getIndex()];
		position[0] = (xBox.isSelected())?xyz[0]:position[0];
		position[1] = (yBox.isSelected())?xyz[1]:position[1];
		position[2] = (zBox.isSelected())?xyz[2]:position[2];
		double[] translation = Rn.subtract(null, position, xyz);
		selectedVertices.add(hds.getVertex(e.getIndex()));
		TranslatedPositionAdapter posAdapter = new TranslatedPositionAdapter(translation, selectedVertices);
		hif.updateGeometry(posAdapter);
	}
	
	@Position
	public static class TranslatedPositionAdapter extends AbstractAdapter<double[]> {

		private double[]
			translation = null;
		private Set<Vertex<?,?,?>>
			effected = null;

		public TranslatedPositionAdapter(double[] translation, Set<Vertex<?,?,?>> effected) {
			super(double[].class, true, false);
			this.translation = translation;
			this.effected = effected;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> double[] getV(V v, AdapterSet a) {
			double[] pos = a.get(Position.class, v, double[].class);
			if (effected.contains(v)) {
				pos[0] += translation[0];
				pos[1] += translation[1];
				pos[2] += translation[2];
			}
			return pos;
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return true;
		}
		
	}
	

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		old.getLayerRoot().removeTool(tool);
		active.getLayerRoot().addTool(tool);
	}
	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}
	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}
	
}
