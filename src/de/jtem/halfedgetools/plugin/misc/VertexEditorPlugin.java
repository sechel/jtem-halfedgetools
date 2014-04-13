package de.jtem.halfedgetools.plugin.misc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.MarqueeSelectionPlugin;
import de.jtem.halfedgetools.plugin.data.VisualizationInterface;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class VertexEditorPlugin extends ShrinkPanelPlugin implements PointDragListener, HalfedgeListener {

	private HalfedgeInterface hif = null;
	private VisualizationInterface vif = null;
	private SwingDragEventTool tool = new SwingDragEventTool(
			InputSlot.SHIFT_LEFT_BUTTON);
	private JCheckBox xBox = new JCheckBox("X"), yBox = new JCheckBox("Y"),
			zBox = new JCheckBox("Z");
	private JPanel panel = new JPanel();
	private MarqueeSelectionPlugin
		marqee = null;
	private boolean
		marqeeWasActive = false;

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
		panel.add(xBox, gbc1);
		panel.add(yBox, gbc1);
		panel.add(zBox, gbc2);
		panel.add(new JLabel("(Shift-LeftClick to Drag)"), gbc2);

		shrinkPanel.add(panel);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
		vif = c.getPlugin(VisualizationInterface.class);
		marqee = c.getPlugin(MarqueeSelectionPlugin.class);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	private AdapterSet adapters;
	private Set<Vertex<?, ?, ?>> selectedVertices = new HashSet<Vertex<?, ?, ?>>();
	private double[] pointerpos = null;
	private Map<Vertex<?, ?, ?>, double[]> differences = new HashMap<Vertex<?, ?, ?>, double[]>();

	@Override
	public void pointDragStart(PointDragEvent e) {
		selectedVertices = hif.getSelection().getVertices();
		adapters = hif.getAdapters();
		pointerpos = new double[] { xBox.isSelected() ? 0 : e.getX(),
				yBox.isSelected() ? 0 : e.getY(),
				zBox.isSelected() ? 0 : e.getZ(), 0 };

		double[] vertexpos = null;
		for (Vertex<?, ?, ?> v : selectedVertices) {
			vertexpos = Pn.dehomogenize(null, adapters.getD(Position.class, v));
			double[] pos = Rn.subtract(null, vertexpos, pointerpos);
			differences.put(v, pos);
		}
		marqeeWasActive = marqee.isActivated();
		marqee.setActivated(false);
	}

	@Override
	public void pointDragged(PointDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		if (hds == null)
			return;
		if (hds.numVertices() <= e.getIndex())
			return;
		if (e.getIndex() < 0)
			return;

		pointerpos = new double[] { xBox.isSelected() ? 0 : e.getX(),
				yBox.isSelected() ? 0 : e.getY(),
				zBox.isSelected() ? 0 : e.getZ(), 0 };

		double[] vertexpos = null;
		for (Vertex<?, ?, ?> v : selectedVertices) {
			vertexpos = differences.get(v);
			double[] pos = Rn.add(null, vertexpos, pointerpos);
			adapters.set(Position.class, v, pos);
		}

		hif.getActiveLayer().updateNoUndo();
		Selection sel = hif.getSelection();
		for (Vertex<?, ?, ?> v : selectedVertices) {
			sel.add(v);
		}
		hif.setSelection(sel);
		vif.updateActiveVisualizations();
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
		differences.clear();
		marqee.setActivated(marqeeWasActive);
		hif.update();
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void dataChanged(HalfedgeLayer layer) {
		addTool(layer);
	}

	private void addTool(HalfedgeLayer layer) {
		SceneGraphComponent comp = layer.getGeometryRoot();
		if (!comp.getTools().contains(tool)) {
			comp.addTool(tool);
		}
	}

	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}

	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		addTool(active);
	}

	@Override
	public void layerCreated(HalfedgeLayer layer) {
		addTool(layer);
	}

	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}
	
}
