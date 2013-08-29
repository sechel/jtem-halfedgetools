package de.jtem.halfedgetools.plugin.algorithm.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

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
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public class PrimitivesGenerator extends AlgorithmDialogPlugin implements ItemListener {

	private JPanel
		panel = new JPanel();
	private ButtonGroup
		primitivesGroup = new ButtonGroup();
	
	private JRadioButton
		triangleButton = new JRadioButton("Triangle"),
		cubeButton = new JRadioButton("Cube"),
		openCubeButton = new JRadioButton("Open Cube"),
		cylinderButton = new JRadioButton("Cylinder"),
		tetrahedronButton = new JRadioButton("Tetrahedron"),
		icosahedronButton = new JRadioButton("Icosahedron");

	private ShrinkPanel
		cylinderParametersPanel = new ShrinkPanel("Cyclinder parameters");
	
	private SpinnerNumberModel
		circlePoints = new SpinnerNumberModel(15, 3, 100, 1),
		heightPoints = new SpinnerNumberModel(15, 2, 100, 1),
		aspectRatio = new SpinnerNumberModel(1.0, 0.0001, 10000, 0.1);
	
	private JSpinner
		circleSpinner = new JSpinner(circlePoints),
		heightSpinner = new JSpinner(heightPoints),
		ratioSpinner = new JSpinner(aspectRatio);
	
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
		primitivesGroup.add(cylinderButton);
		cylinderButton.addItemListener(this);
		primitivesGroup.add(tetrahedronButton);
		primitivesGroup.add(icosahedronButton);
		
		cylinderParametersPanel.setLayout(new GridBagLayout());
		cylinderParametersPanel.add(new JLabel("Points on circle"),gbc1);
		cylinderParametersPanel.add(circleSpinner,gbc2);
		cylinderParametersPanel.add(new JLabel("Levels"),gbc1);
		cylinderParametersPanel.add(heightSpinner,gbc2);
		cylinderParametersPanel.add(new JLabel("Aspect ratio"),gbc1);
		cylinderParametersPanel.add(ratioSpinner,gbc2);
		cylinderParametersPanel.setShrinked(false);
		
		panel.add(triangleButton,gbc2);
		panel.add(cubeButton,gbc2);
		panel.add(openCubeButton, gbc2);
		panel.add(cylinderButton,gbc2);
		panel.add(cylinderParametersPanel,gbc2);		
		panel.add(tetrahedronButton,gbc2);
		panel.add(icosahedronButton, gbc2);
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
		if(cylinderButton.isSelected()) {
			int circle = circlePoints.getNumber().intValue();
			int levels = heightPoints.getNumber().intValue();
			double aspectRatioOfQuads = aspectRatio.getNumber().doubleValue();
			ifs = Primitives.cylinder(circle, 1, 1, 0, aspectRatioOfQuads*(levels-1)*2*Math.sin(Math.PI/circle), 2*Math.PI, levels);
		}
		if(tetrahedronButton.isSelected()) {
			ifs = Primitives.tetrahedron();
		}
		if (icosahedronButton.isSelected()) {
			ifs = Primitives.icosahedron();
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

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == cylinderButton) {
			if(cylinderButton.isSelected()) {
				cylinderParametersPanel.setShrinked(false);
			} else {
				cylinderParametersPanel.setShrinked(true);
			}
		}
	}
	
}
