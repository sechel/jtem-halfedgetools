package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;

public class QuadGraphLinear {

	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS execute(
		HDS graph, 
		HDS quad, 
		Map<V, V> vertexVertexMap, 
		Map<F, V> faceVertexMap,
		VertexPositionCalculator vA,
		FaceBarycenterCalculator fA
	) {
		HashMap<E, E> leftQuadEdgeMap = new HashMap<E, E>();
		HashMap<E, F> edgeFaceMap = new HashMap<E, F>();
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			vA.set(newVertex, vA.get(v));
			vertexVertexMap.put(v, newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			vA.set(newVertex, fA.get(f));
			faceVertexMap.put(f, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
			F newFace = quad.addNewFace();
			edgeFaceMap.put(e, newFace);
			edgeFaceMap.put(e.getOppositeEdge(), newFace);
		}
		
		
		// create inner edges
		for (E e : graph.getPositiveEdges()){
			V v1 = vertexVertexMap.get(e.getStartVertex());
			V v2 = vertexVertexMap.get(e.getTargetVertex());
			V v3 = faceVertexMap.get(e.getRightFace());
			V v4 = faceVertexMap.get(e.getLeftFace());
			F f = edgeFaceMap.get(e);
			
			E e1 = quad.addNewEdge();
			E e2 = quad.addNewEdge();
			E e3 = quad.addNewEdge();
			E e4 = quad.addNewEdge();
			
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e3);
			e3.linkNextEdge(e4);
			e4.linkNextEdge(e1);
			
			e1.setTargetVertex(v2);
			e2.setTargetVertex(v4);
			e3.setTargetVertex(v1);
			e4.setTargetVertex(v3);
			
			e1.setLeftFace(f);
			e2.setLeftFace(f);
			e3.setLeftFace(f);
			e4.setLeftFace(f);
			
			leftQuadEdgeMap.put(e, e2);
			leftQuadEdgeMap.put(e.getOppositeEdge(), e4);
		}
		
		// connections inside
		for (E e : graph.getEdges()){
			E e1 = leftQuadEdgeMap.get(e);
			E e2 = leftQuadEdgeMap.get(e.getNextEdge()).getNextEdge();
			e1.linkOppositeEdge(e2);
		}
		
		return quad;
	}
	
}
