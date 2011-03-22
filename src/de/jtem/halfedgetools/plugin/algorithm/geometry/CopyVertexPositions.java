package de.jtem.halfedgetools.plugin.algorithm.geometry;

import java.util.HashMap;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class CopyVertexPositions extends AlgorithmPlugin {

	private HashMap<Integer, double[]>
		posMap = new HashMap<Integer, double[]>();
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Geometry;
	}

	@Override
	public String getAlgorithmName() {
		return "Copy Vertex Positions";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		posMap.clear();
		for (V v : hds.getVertices()) {
			posMap.put(v.getIndex(), a.getD(Position3d.class, v));
		}
	}
	
	protected HashMap<Integer, double[]> getPosMap() {
		return posMap;
	}

}
