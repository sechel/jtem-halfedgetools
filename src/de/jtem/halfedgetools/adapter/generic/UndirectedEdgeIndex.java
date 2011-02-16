package de.jtem.halfedgetools.adapter.generic;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.EdgeIndex;


@EdgeIndex
public class UndirectedEdgeIndex extends AbstractAdapter<Integer> {

	private Map<Edge<?,?,?>,Integer> 
		edgeMap = new HashMap<Edge<?,?,?>,Integer>();
	private boolean valid = false;
	
	public UndirectedEdgeIndex() {
		super(Integer.class, true, false);
	}

	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> Integer getE(E e, AdapterSet a) {
		HalfEdgeDataStructure<V, E, F> hds = e.getHalfEdgeDataStructure();
		if(!valid || edgeMap.size() != hds.numEdges()) {
			initEdgeMap(e.getHalfEdgeDataStructure());
			valid = true;
		}
		return edgeMap.get(e);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}

	@Override
	public void update() {
		valid = false;
	}
	
	private void initEdgeMap(HalfEdgeDataStructure<?, ?, ?> hds) {
		edgeMap.clear();
		int i = 0;
		for(Edge<?,?,?> e: hds.getPositiveEdges()) {
			edgeMap.put(e, i);
			edgeMap.put(e.getOppositeEdge(),i);
			++i;
		}
	}
	
}
