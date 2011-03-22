package de.jtem.halfedgetools.plugin.algorithm.geometry;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class PasteVertexPositions extends AlgorithmPlugin {

	private CopyVertexPositions
		copyPlugin = null;
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Geometry;
	}

	@Override
	public String getAlgorithmName() {
		return "Paste Vertex Positions";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		Map<Integer, double[]> posMap = copyPlugin.getPosMap();
		if (hds.numVertices() != posMap.size()) {
			throw new RuntimeException("Stored vertex positions don't fit current geometry");
		}
		for (V v : hds.getVertices()) {
			double[] p = posMap.get(v.getIndex());
			a.set(Position.class, v, p);
		}
		hi.update();
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		copyPlugin = c.getPlugin(CopyVertexPositions.class);
	}
	
	
}
