package de.jtem.halfedgetools.plugin.algorithm.geometry;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import de.jreality.geometry.Primitives;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class PrimitivesGenerator extends AlgorithmDialogPlugin {

	private JPanel
		panel = new JPanel();
	private ButtonGroup
		primitivesGroup = new ButtonGroup();
	
	private JRadioButton
		triangleButton = new JRadioButton("triangle"),
		cubeButton = new JRadioButton("cube"),
		openCubeButton = new JRadioButton("open cube"),
		tetrahedronButton = new JRadioButton("tetrahedron");

	private ConverterJR2Heds
		converter = new ConverterJR2Heds();
	
	public PrimitivesGenerator() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = GridBagConstraints.RELATIVE;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		triangleButton.setSelected(true);
		primitivesGroup.add(triangleButton);
		primitivesGroup.add(openCubeButton);
		primitivesGroup.add(cubeButton);
		primitivesGroup.add(tetrahedronButton);
		
		panel.add(triangleButton,gbc2);
		panel.add(cubeButton,gbc2);
		panel.add(openCubeButton, gbc2);
		panel.add(tetrahedronButton,gbc2);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Primitives");
		info.icon = ImageHook.getIcon("cube.png", 16, 16);
		return info; 
	}
	
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		IndexedFaceSet ifs = null;
		if(triangleButton.isSelected()) {
			ifs = Primitives.regularPolygon(3);
		}
		if(cubeButton.isSelected()) {
			ifs = Primitives.cube();
		}
		if(openCubeButton.isSelected()) {
			ifs = Primitives.openCube();
		}
		if(tetrahedronButton.isSelected()) {
			ifs = Primitives.tetrahedron();
		}
		converter.ifs2heds(ifs, hds, hcp.getAdapters());
		hcp.update();
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public String getAlgorithmName() {
		return "Primitives";
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
}
