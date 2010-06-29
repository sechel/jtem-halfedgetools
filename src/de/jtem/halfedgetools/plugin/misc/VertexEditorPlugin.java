package de.jtem.halfedgetools.plugin.misc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.View;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class VertexEditorPlugin extends ShrinkPanelPlugin implements PointDragListener {

	private HalfedgeInterface
		hif = null;
	
	private DragEventTool
		tool = new DragEventTool(InputSlot.SHIFT_LEFT_BUTTON);
	
	private JCheckBox
		xBox = new JCheckBox("X"),
		yBox = new JCheckBox("Y"),
		zBox = new JCheckBox("Z");
	
	private JPanel
		panel = new JPanel();
	
	public VertexEditorPlugin() {
		tool.addPointDragListener(this);
		tool.setDescription("Vertex Editor Tool");
		
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
		JRViewerUtility.getContentPlugin(c).addContentTool(tool);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		JRViewerUtility.getContentPlugin(c).removeContentTool(tool);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
	}
	
	@Override
	public void pointDragStart(PointDragEvent e) {
	}
	
	@Override
	public void pointDragged(PointDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		if (hds == null) return;
		if (hds.numVertices() <= e.getIndex()) return;
		if (e.getIndex() < 0) return;
		Vertex<?,?,?> v = hds.getVertex(e.getIndex());
		AdapterSet adapters = hif.getAdapters();
		double[] xyz = adapters.get(Position.class,v,double[].class);
		double[] newPos = new double[]{
				(xBox.isSelected())?xyz[0]:e.getX(),
				(yBox.isSelected())?xyz[1]:e.getY(),
				(zBox.isSelected())?xyz[2]:e.getZ()};
		adapters.set(Position.class, v, newPos);
		hif.update();
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
}
