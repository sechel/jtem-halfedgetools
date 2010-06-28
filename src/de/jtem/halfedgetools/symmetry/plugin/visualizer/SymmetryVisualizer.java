/**
 * 
 */
package de.jtem.halfedgetools.symmetry.plugin.visualizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.halfedgetools.symmetry.node.SEdge;
import de.jtem.halfedgetools.symmetry.node.SFace;
import de.jtem.halfedgetools.symmetry.node.SHDS;
import de.jtem.jrworkspace.plugin.Controller;

/**
 * @author josefsso
 *
 */
public class SymmetryVisualizer extends VisualizerPlugin implements ChangeListener{

	private final SceneGraphComponent body = SceneGraphUtility.createFullSceneGraphComponent("body");
	private JPanel panel = new JPanel();
	
	private JCheckBox edgesBox = new JCheckBox("Edges", true);
	private JCheckBox facesBox = new JCheckBox("Faces", true);
	

	public SymmetryVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(edgesBox, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(facesBox, c);
	}

	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
	
		SHDS shds = null;
		IndexedFaceSet eifse = new IndexedFaceSet();
		IndexedLineSet eilse = new IndexedLineSet();
		if(hds.getClass().isAssignableFrom(SHDS.class)) {
			shds = (SHDS)hds;
			
			if(shds.getGroup() != null) {
				
				// faces
				List<SFace> sFaces = new ArrayList<SFace>();
				for(SFace f : shds.getFaces()) {
					for(SEdge e : HalfEdgeUtils.boundaryEdges(f)) {
						if(e.isRightOfSymmetryCycle() != null) {
							sFaces.add(f);
						}
					}
				}
				
				double[][] extraCoords = new double[3*sFaces.size()][];
				int[][] extraIndices = new int[sFaces.size()][];
				double[][] extraColors = new double[sFaces.size()][];
				
				int j = 0;
				for(SFace f : sFaces) {
					
					extraCoords[j] = f.getEmbeddingOnBoundary(0, false);
					extraCoords[j+1] = f.getEmbeddingOnBoundary(1, false);
					extraCoords[j+2] = f.getEmbeddingOnBoundary(2, false);
					
					extraColors[j/3] = new double[] {0.7,0.7,0.7,1};
					
					extraIndices[j/3] = new int[] {j,j+1,j+2};
					
					j += 3;
				}
				
				IndexedFaceSetFactory ifsextra = new IndexedFaceSetFactory();
				ifsextra.setVertexCount(extraCoords.length);
				ifsextra.setVertexCoordinates(extraCoords);
				ifsextra.setFaceCount(extraIndices.length);
				ifsextra.setFaceIndices(extraIndices);
				ifsextra.setFaceColors(extraColors);
				ifsextra.update();
				if(facesBox.isSelected()) {
					eifse = ifsextra.getIndexedFaceSet();
				}
				
				// edges
				List<SEdge> symmetryEdges = new ArrayList<SEdge>();
				for(SEdge e : shds.getEdges()) {
					if(e.isRightIncomingOfSymmetryCycle() != null ) {
						symmetryEdges.add(e);
					}
				}
				
				double[][] extraCoords2 = new double[2*symmetryEdges.size()][];
				int[][] extraIndices2 = new int[symmetryEdges.size()][];
				double[][] extraColors2 = new double[symmetryEdges.size()][];
				int k = 0;
				for(SEdge e : symmetryEdges) {
					
					int n = e.getNr();
					SFace f = e.getLeftFace();
					extraCoords2[k] = f.getEmbeddingOnBoundary(n + 1, false);
					extraCoords2[k+1] = f.getEmbeddingOnBoundary(n + 2, false);
					
					extraColors2[k/2] = new double[] {1,1,1,1};
					
					extraIndices2[k/2] = new int[] {k,k+1};
					
					k += 2;
					
				}
				
				IndexedLineSetFactory ilsextra = new IndexedLineSetFactory();
				ilsextra.setVertexCount(extraCoords2.length);
				ilsextra.setVertexCoordinates(extraCoords2);
				ilsextra.setEdgeCount(extraIndices2.length);
				ilsextra.setEdgeIndices(extraIndices2);
				ilsextra.setEdgeColors(extraColors2);
				ilsextra.update();
				if(edgesBox.isSelected()) {
					eilse = ilsextra.getIndexedLineSet(); 
				}
				
			}
		}

		GeometryMergeFactory gmf = new GeometryMergeFactory();
		IndexedFaceSet ifs = gmf.mergeIndexedFaceSets(new PointSet[] {eifse,eilse});
		IndexedFaceSetUtility.calculateAndSetNormals(ifs);
		body.setGeometry(ifs);
	}

	@Override
	public String getName() {
		return "Symmetry visualizer";
	}
	
	@Override
	public SceneGraphComponent getComponent() {
		return body;
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	@Override
	public void updateContent() {
		manager.updateContent();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		manager.update();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		edgesBox.addChangeListener(this);
		facesBox.addChangeListener(this);
		super.install(c);
	}

}
