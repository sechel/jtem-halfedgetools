package de.jtem.halfedgetools.plugin.visualizers;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class PositiveEdgeVisualizer extends VisualizerPlugin implements ChangeListener {

	private SceneGraphComponent 
		dirEdgesComponent = null;
	
	@Override
	public void stateChanged(ChangeEvent e) {
		manager.update();
	}
	
	@Override
	public String getName() {
		return "Positive Edges";
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		IndexedLineSetFactory ilf = new IndexedLineSetFactory();
		int numPoints = hds.numVertices();
		if(numPoints == 0) {
			dirEdgesComponent = null;
			return;
		}
		ilf.setVertexCount(numPoints);
		ilf.setEdgeCount(hds.numEdges()/2);
		int[][] edges = new int[hds.numEdges()/2][2];
		double[][] vertices = new double[numPoints][];
		int i = 0;
		for (V v : hds.getVertices()) {
			vertices[i] = a.get(Position.class, v, double[].class);
			i++;
		}
		int j = 0;
		for(E e : hds.getPositiveEdges()) {
			edges[j][0] = e.getStartVertex().getIndex();
			edges[j][1] = e.getTargetVertex().getIndex();
			j++;
		}
		ilf.setEdgeIndices(edges);
		ilf.setVertexCoordinates(vertices);
		ilf.update();
		
		BallAndStickFactory bsf = new BallAndStickFactory(ilf.getIndexedLineSet());
		// bsf.setBallRadius(.04);
		bsf.setShowBalls(false);
        bsf.setStickRadius(.005);
        bsf.setShowArrows(true);
        bsf.setArrowScale(.04);
        bsf.setArrowSlope(1.2);
        bsf.setArrowPosition(.5);
		bsf.update();
		dirEdgesComponent = bsf.getSceneGraphComponent(); 
	}
	
	@Override
	public SceneGraphComponent getComponent() {
		return dirEdgesComponent;
	}

}
