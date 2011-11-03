package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;

public class Sqrt3Linear {

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS subdivide(
		HDS graph, 
		HDS quad, 
		Map<V, V> vertexVertexMap, 
		Map<F, V> faceVertexMap,
		AdapterSet a
	) {
		HashMap<E, E> leftQuadEdgeMap = new HashMap<E, E>();
		HashMap<E, F> edgeFaceMap = new HashMap<E, F>();
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			a.set(Position.class, newVertex, a.getD(BaryCenter3d.class, v));
			vertexVertexMap.put(v, newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			a.set(Position.class, newVertex, a.getD(BaryCenter3d.class, f));
			faceVertexMap.put(f, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
			F newFace = quad.addNewFace();
			F newFace2 = quad.addNewFace();
			edgeFaceMap.put(e, newFace);
			edgeFaceMap.put(e.getOppositeEdge(), newFace2);
		}
		
		
		// create inner edges
		for (E e : graph.getPositiveEdges()){
			// quads only for inner edges
//			if (!e.isInteriorEdge()) continue;
			V v1 = vertexVertexMap.get(e.getStartVertex());
			V v2 = vertexVertexMap.get(e.getTargetVertex());
			V v3 = faceVertexMap.get(e.getRightFace());
			V v4 = faceVertexMap.get(e.getLeftFace());
			F fl = edgeFaceMap.get(e);
			F fr = edgeFaceMap.get(e.getOppositeEdge());
			
			E e1 = quad.addNewEdge();
			E e2 = quad.addNewEdge();
			E e3 = quad.addNewEdge();
			E e4 = quad.addNewEdge();
			E e5 = quad.addNewEdge();
			E e6 = quad.addNewEdge();
			
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e3);
			e3.linkNextEdge(e1);
			
			e4.linkNextEdge(e5);
			e5.linkNextEdge(e6);
			e6.linkNextEdge(e4);
			
			e1.setTargetVertex(v2);
			e2.setTargetVertex(v4);
			e3.setTargetVertex(v3);
			
			e4.setTargetVertex(v4);
			e5.setTargetVertex(v1);
			e6.setTargetVertex(v3);
			
			e3.linkOppositeEdge(e4);
		
			e1.setLeftFace(fr);
			e2.setLeftFace(fr);
			e3.setLeftFace(fr);
			
			e4.setLeftFace(fl);
			e5.setLeftFace(fl);
			e6.setLeftFace(fl);
			
			leftQuadEdgeMap.put(e, e2);
			leftQuadEdgeMap.put(e.getOppositeEdge(), e6);
		}
		
		// connections inside
		for (E e : graph.getEdges()){
			E e1 = leftQuadEdgeMap.get(e);
			E e2 = leftQuadEdgeMap.get(e.getNextEdge()).getNextEdge().getOppositeEdge().getNextEdge();
			e1.linkOppositeEdge(e2);
		}
		
//		// create boundary edges
//		for (int i = 0; i < quad.numEdges(); i++){
//			E e = quad.getEdge(i);
//			if (e.getOppositeEdge() != null) continue;
//			E opp = quad.addNewEdge();
//			
//			opp.linkOppositeEdge(e);
//		}
//		
//		// connect boundary
//		for (E e : quad.getEdges()){
//			if (e.getLeftFace() != null) continue;
//			E prev = e;
//			do {
//				prev = prev.getOppositeEdge().getNextEdge();
//			} while (prev.getRightFace() != null);
//			prev = prev.getOppositeEdge();
//			e.linkPreviousEdge(prev);
//		}
//		
//		for (E e : quad.getEdges()){
//			if (e.getLeftFace() != null) continue;
//			e.setTargetVertex(e.getNextEdge().getStartVertex());
//		}
		
		return quad;
	}
	
}
