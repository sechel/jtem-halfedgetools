package de.jtem.halfedgetools.util;


import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;

public class Consistency {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean checkConsistency(HalfEdgeDataStructure<V, E, F> heds){
		for (V v : heds.getVertices()){
			List<E> cocycle1 = HalfEdgeUtilsExtra.getEdgeStar(v); 
			List<E> cocycle2 = HalfEdgeUtilsExtra.findEdgesWithTarget(v);
			for (E e : cocycle1)
				if (!cocycle2.contains(e) || cocycle1.size() != cocycle2.size()){
					System.err.println("Consistency: " + "e.getEdgeStar != he.findEdgesWithTarget");
					return false;
				}
			if (v.getIncomingEdge() != null && v.getIncomingEdge().getTargetVertex() != v){
				System.err.println("Consistency: " + "e.getEdgeStar != he.findEdgesWithTarget");
				return false;
			}
		}
		for (F f : heds.getFaces()){
			List<E> boundary1 = HalfEdgeUtilsExtra.getBoundary(f);
			List<E> boundary2 = HalfEdgeUtils.boundaryEdges(f);
			for (E e : boundary1)
				if (!boundary2.contains(e) || boundary1.size() != boundary2.size()){
					System.err.println("Consistency: " + "f.getBoundary != he.boundary()");
					return false;
				}	
		}
			
		return true;
	}
	
	
	
}

