package de.jtem.halfedgetools.symmetry.plugin;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static java.awt.GridBagConstraints.WEST;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.View;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.FaceDragEvent;
import de.jreality.tools.FaceDragListener;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.ResourceClass;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.core.FiniteStateAutomaton;
import de.jtem.discretegroup.core.Platycosm;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricCoordinateAdapter;
import de.jtem.halfedgetools.symmetry.standard.SEdge;
import de.jtem.halfedgetools.symmetry.standard.SFace;
import de.jtem.halfedgetools.symmetry.standard.SHDS;
import de.jtem.halfedgetools.symmetry.standard.SVertex;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.StatusFlavor;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;


public class CompactifierPlugin extends ShrinkPanelPlugin implements StatusFlavor, ActionListener {

	private HalfedgeInterfacePlugin<SVertex,SEdge,SFace,SHDS>
		hedsConnector = null;
	private JSpinner
		leftIndexSpinner = null,
		rightIndexSpinner = null,
		leftIndexVertexSpinner = null,
		rightIndexVertexSpinner = null;
	private JButton
		identifyButton = null,
		pickLeftButton = null,
		pickRightButton = null,
		resetButton = null;
	private JCheckBox
		onlyTranslateCheckBox = null,
		flipOrientButton = null;
	
	private SHDS hds = new SHDS();
	
	private DragEventTool 
		tl = null,
		tr = null;
	
	private Content content;
	private StatusChangedListener statusChangedListener;

	Platycosm theGroup = null;
	private List<DiscreteGroupElement> generators = new LinkedList<DiscreteGroupElement>();
	private SymmetricCoordinateAdapter coordApt = new SymmetricCoordinateAdapter(AdapterType.VERTEX_ADAPTER);
	
	
	public void createControls() {
		Insets insets = new Insets(2, 2, 2, 2);
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.insets = insets;
		c.weighty = 0.0;
		c.anchor = WEST;
		
		leftIndexSpinner = new JSpinner(new SpinnerNumberModel());
		rightIndexSpinner = new JSpinner(new SpinnerNumberModel());
		leftIndexVertexSpinner = new JSpinner(new SpinnerNumberModel());
		rightIndexVertexSpinner = new JSpinner(new SpinnerNumberModel());
		
		identifyButton = new JButton("Identify");
		identifyButton.addActionListener(this);
		pickLeftButton = new JButton("Pick 1");
		pickLeftButton.addActionListener(this);
		pickRightButton = new JButton("Pick 2");
		pickRightButton.addActionListener(this);
		onlyTranslateCheckBox = new JCheckBox("Only translate", true);
		onlyTranslateCheckBox.addActionListener(this);
		flipOrientButton = new JCheckBox("Flip orientation", false);
		flipOrientButton.addActionListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(new JLabel("Face 1:"), c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(new JLabel("Vertex 1:"), c);
		
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(leftIndexSpinner, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(leftIndexVertexSpinner, c);


		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(new JLabel("Face 2:"), c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(new JLabel("Vertex 2:"), c);
		
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(rightIndexSpinner, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(rightIndexVertexSpinner, c);
		
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(flipOrientButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(onlyTranslateCheckBox, c);
		
		c.gridwidth = 1;
		c.weightx = 0.0;
		panel.add(resetButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		panel.add(identifyButton, c);
		

		
		shrinkPanel.add(panel, c);
	}
	
	public CompactifierPlugin() {
		createControls();
	}
	
	
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Compactifier";
		info.vendorName = "Kristoffer Josefsson";
		return info;
	}

	
	public void setStatusListener(StatusChangedListener scl) {
		statusChangedListener = scl;
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void install(Controller c) throws Exception {
		super.install(c);
		shrinkPanel.setHeaderColor(Color.orange);
		content = JRViewerUtility.getContentPlugin(c);
		hedsConnector = c.getPlugin(HalfedgeInterfacePlugin.class);
		
		tl = new DragEventTool(InputSlot.SHIFT_LEFT_BUTTON);

		tl.setDescription("identifying 1");
	    tl.addFaceDragListener(new FaceDragListener() {
	      public void faceDragStart(FaceDragEvent e) { 
	      }
	      public void faceDragged(FaceDragEvent e) {
	      };
	      public void faceDragEnd(FaceDragEvent e) {
	    	  statusChangedListener.statusChanged("Marked for + indentifying face nr. "+e.getIndex());
	    	  leftIndexSpinner.setValue(e.getIndex());
	      } 
	    });
	    tl.addPointDragListener(new PointDragListener() {
		      public void pointDragStart(PointDragEvent e) { 
		      }
		      public void pointDragged(PointDragEvent e) {
		      };
		      public void pointDragEnd(PointDragEvent e) {
		    	  statusChangedListener.statusChanged("Marked for + indentifying vertex nr. " + e.getIndex());
		    	  leftIndexVertexSpinner.setValue(e.getIndex());
		      } 
		    });
	    
		tr = new DragEventTool(InputSlot.SHIFT_RIGHT_BUTTON);

		tr.setDescription("identifying 2");
	    tr.addFaceDragListener(new FaceDragListener() {
	      public void faceDragStart(FaceDragEvent e) { 
	      }
	      public void faceDragged(FaceDragEvent e) {
	      };
	      public void faceDragEnd(FaceDragEvent e) {
	    	  statusChangedListener.statusChanged("Marked for - indentifying face nr. "+e.getIndex());
	    	  rightIndexSpinner.setValue(e.getIndex());
	      } 
	    });
	    tr.addPointDragListener(new PointDragListener() {
		      public void pointDragStart(PointDragEvent e) { 
		      }
		      public void pointDragged(PointDragEvent e) {
		      };
		      public void pointDragEnd(PointDragEvent e) {
		    	  statusChangedListener.statusChanged("Marked for - indentifying vertex nr. "+e.getIndex());
		    	  rightIndexVertexSpinner.setValue(e.getIndex());
		      } 
		    });
		    
		content.addContentTool(tl);
		content.addContentTool(tr);
	}

	
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		content.removeContentTool(tl);
		content.removeContentTool(tr);
	}
	
	
	public DiscreteGroup getGroup() {
		return theGroup;
	}
	
	public HalfEdgeDataStructure<SVertex, SEdge, SFace> getHDS(){
		return hds;
	}
	
	private void generateGroup() {
				
		theGroup = new Platycosm();
		theGroup.setGenerators(generators.toArray(new DiscreteGroupElement[0]));
		FiniteStateAutomaton fsa = FiniteStateAutomaton.fsaForName("test"+".wa", ResourceClass.class);
		if (fsa != null) {
			theGroup.setFsa(fsa);
			fsa.debugPrint();
		} 
		theGroup.setName("test");
	}
	
	
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == resetButton) {
			generators = new LinkedList<DiscreteGroupElement>();

		}
		
		if(e.getSource() == identifyButton) {
			
			SHDS hds = hedsConnector.getCachedHalfEdgeDataStructure();

			SFace f1 = null, f2 = null;
			SEdge e1 = null, e2 = null;
			SVertex v1 = null, v2 = null;
			
			v1 = hds.getVertex((Integer)leftIndexVertexSpinner.getValue());
			v2 = hds.getVertex((Integer)rightIndexVertexSpinner.getValue());
			
			f1 = hds.getFace((Integer)leftIndexSpinner.getValue());
			f2 = hds.getFace((Integer)rightIndexSpinner.getValue());
			
			for(SEdge ee : HalfEdgeUtilsExtra.getBoundary(f1)) {
				if(ee.getTargetVertex() == v1)
					e1 = ee;
			}
			
			for(SEdge ee : HalfEdgeUtilsExtra.getBoundary(f2)) {
				if(ee.getStartVertex() == v2)
					e2 = ee;
			}
			
			SEdge e3 = null;
			for(SEdge ee : HalfEdgeUtilsExtra.getBoundary(f2)) {
				if(ee.getTargetVertex() == v2)
					e3 = ee;
			}
			
			// create generator for this identification
			double[] generator = Rn.subtract(null, coordApt.getCoordinate(v2), coordApt.getCoordinate(v1));
			DiscreteGroupElement gen = new DiscreteGroupElement();

			
			
			// calc first frame
			double[] p0 = coordApt.getCoordinate(e1.getNextEdge().getTargetVertex());
			double[] p1 = coordApt.getCoordinate(v1);
			double[] p2 = coordApt.getCoordinate(e1.getPreviousEdge().getTargetVertex());
			
			double[] ee1 = Rn.normalize(null, Rn.subtract(null, p0, p1));
			double[] ee2 = Rn.normalize(null, Rn.subtract(null, p2, p1));
			double[] ee3 = Rn.crossProduct(null, ee1, ee2);
			
			// calc second frame
//			double[] q0 = coordApt.getCoordinate(e2.getTargetVertex());
//			double[] q1 = coordApt.getCoordinate(v2);
//			double[] q2 = coordApt.getCoordinate(e2.getPreviouSymmetricEdge().getStartVertex());
			double[] q2 = coordApt.getCoordinate(e3.getNextEdge().getTargetVertex());
			double[] q1 = coordApt.getCoordinate(v2);
			double[] q0 = coordApt.getCoordinate(e3.getPreviousEdge().getTargetVertex());
			
			
			double[] ff1 = Rn.normalize(null, Rn.subtract(null, q0, q1));
			double[] ff2 = Rn.normalize(null, Rn.subtract(null, q2, q1));
			double[] ff3 = null;
			if(flipOrientButton.isSelected()) {
				ff3 = Rn.crossProduct(null, ff2, ff1);
			} else {
				ff3 = Rn.crossProduct(null, ff1, ff2);
			}
			
//			double[] ee = new double[] {
//					ee1[0],ee1[1],ee1[2],
//					ee2[0],ee2[1],ee2[2],
//					ee3[0],ee3[1],ee3[2]
//					                  };
			
//			double[] ff = new double[] {
//					ff1[0],ff1[1],ff1[2],
//					ff2[0],ff2[1],ff2[2],
//					ff3[0],ff3[1],ff3[2]
//					                  };
			
			Color[] colors = new Color[3];
			colors[0] = Color.RED;
			colors[1] = Color.GREEN;
			colors[2] = Color.BLUE;
			
			int[][] indices = new int[3][];
			indices[0] = new int[] {0,1};
			indices[1] = new int[] {0,2};
			indices[2] = new int[] {0,3};
			
			double[][] coords1 = new double[4][];
			coords1[0] = p1;
			coords1[1] = Rn.add(null, p1, ee1);
			coords1[2] = Rn.add(null, p1, ee2);
			coords1[3] = Rn.add(null, p1, ee3);
			
			double[][] coords2 = new double[4][];
			coords2[0] = q1;
			coords2[1] = Rn.add(null, q1, ff1);
			coords2[2] = Rn.add(null, q1, ff2);
			coords2[3] = Rn.add(null, q1, ff3);
			
			IndexedLineSetFactory ilsF = new IndexedLineSetFactory();
			ilsF.setVertexCount(4);
			ilsF.setVertexCoordinates(coords1);
			ilsF.setEdgeCount(indices.length);
			ilsF.setEdgeIndices(indices);
			ilsF.setEdgeColors(colors);
			ilsF.update();
			
			IndexedLineSet frame1 = ilsF.getIndexedLineSet();
			
			ilsF = new IndexedLineSetFactory();
			ilsF.setVertexCount(4);
			ilsF.setVertexCoordinates(coords1);
			ilsF.setEdgeCount(indices.length);
			ilsF.setEdgeIndices(indices);
			ilsF.setEdgeColors(colors);
			ilsF.setVertexCoordinates(coords2);
			ilsF.update();
			
			IndexedLineSet frame2 = ilsF.getIndexedLineSet();
			
			SceneGraphComponent frames = SceneGraphUtility.createFullSceneGraphComponent("frames");
			GeometryMergeFactory gmf = new GeometryMergeFactory();
			IndexedLineSet framesMerged = gmf.mergeIndexedLineSets(new IndexedLineSet[] {frame1,frame2});
			
			frames.setGeometry(framesMerged);
			
//			content.setContent(frames);
			
			Matrix SFace = new Matrix();
			SFace.setColumn(0, ee1); SFace.setColumn(1, ee2); SFace.setColumn(2, ee3); SFace.setColumn(3, p1); SFace.setEntry(3, 3, 1.0);
			Matrix G = new Matrix();
			G.setColumn(0, ff1); G.setColumn(1, ff2); G.setColumn(2, ff3); G.setColumn(3, q1); G.setEntry(3, 3, 1.0);
			
			
			if(onlyTranslateCheckBox.isSelected()) {
				MatrixBuilder.euclidean().translate(generator[0],generator[1],generator[2]).assignTo(gen.getMatrix());
			} else {
				SFace.invert();
				G.multiplyOnRight(SFace);
				G.assignTo(gen.getMatrix());
			}				
			
			
//			Matrix t = DiscreteGroupExtra.generateIsometry(ee, p1, ff, q1);
//			t.assignTo(gen.getMatrix());
			
			System.err.println("Transformation matrix is:");
			System.err.println(Rn.matrixToJavaString(gen.getMatrix().getArray()));
			
			gen.setWord(DiscreteGroupUtility.genNames[generators.size()]);
			DiscreteGroupElement genInv = (DiscreteGroupElement) gen.getInverse();
			
			generators.add(gen);
			generators.add(genInv);
			
			Set<SEdge> symmetryCycle = HalfEdgeTopologyOperations.glueFacesAlongCycle(f1, f2, e1, e2);
			
			hds.getSymmetryCycles().paths.put(symmetryCycle,genInv);
			
			generateGroup();
			hds.setGroup(theGroup);
			
			hedsConnector.updateHalfedgeContentAndActiveGeometry(hds);
			
			leftIndexSpinner.setValue(0);
			leftIndexVertexSpinner.setValue(0);
			rightIndexSpinner.setValue(0);
			rightIndexVertexSpinner.setValue(0);
		}
		
//		if(e.getSource() == flipOrientButton) {
//			
//			if(flipOrientButton.isSelected()) {
//
//			} else {
//
//			}
//		}
//		
//		if(e.getSource() == onlyTranslateCheckBox) {
//			
//		}
		
	}

	
//	public static void main(String[] args) {
//
//
//		JRViewer viewer = new JRViewer();
//
//		viewer.addBasicUI();
//		viewer.addContentUI();
//		viewer.setShowPanelSlots(true, false, false, false);
//		viewer.setShowToolBar(true);
//		viewer.setPropertiesFile("working.jrw");
//		viewer.addContentSupport(ContentType.CenteredAndScaled);
//		viewer.registerPlugin(new CompactifierPlugin());
//		viewer.registerPlugin(new HalfedgeConnectorPlugin<SVertex, SEdge, SFace, SHDS>(new SHDS(),new SymmetricCoordinateAdapter(AdapterType.VERTEX_ADAPTER)));
////		viewer.registerPlugin(new SubdivisionPlugin());
////		viewer.registerPlugin(new HalfedgeDebuggerPlugin<SymmetricVertex, SymmetricEdge, SymmetricFace>());
//		viewer.registerPlugin(new LookAndFeelSwitch());
//		viewer.startup();
//		
//		try {
//			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//		} catch (Exception SEdge) {
//			SEdge.printStackTrace();
//		}
//		
//
//
//	}
	
	

}


