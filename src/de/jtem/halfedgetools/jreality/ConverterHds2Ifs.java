package de.jtem.halfedgetools.jreality;

import java.util.Map;

import de.jreality.scene.IndexedFaceSet;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;

public interface ConverterHds2Ifs {

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> IndexedFaceSet heds2ifs(HDS heds, AdapterSet adapters);

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> IndexedFaceSet heds2ifs(HDS hds, AdapterSet adapters, Map<Integer, Edge<?, ?, ?>> edgeMap);

}
