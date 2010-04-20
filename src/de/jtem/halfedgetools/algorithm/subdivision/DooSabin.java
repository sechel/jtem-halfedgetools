package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.util.SurfaceException;

public class DooSabin {

	/**
	 * Doo-Sabin
	 *
	 * Generates the medial graph for the given graph
	 * @param graph the graph
	 * @param vClass the vertex class type of the result
	 * @param eClass the edge class type of the result
	 * @param fClass the face class type of the result
	 * @param edgeTable1 this map maps edges of the graph onto edges of the result 
	 * @return the medial
	 * @throws SurfaceException
	 * TODO write it as symmetric
	 */
	public <	
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, Set<E>> subdivide(
			HDS oldHeds, 
			HDS newHeds, 
			VertexPositionCalculator vc,
			EdgeAverageCalculator ec,
			FaceBarycenterCalculator fc
	) {
		Map<V, Set<V>> oldVtoNewVs = new HashMap<V,Set<V>>();
		
		
		
		for (V v : oldHeds.getVertices()){
			
		}
		
		
//		eA.setAlpha(0.5);
//		eA.setIgnore(true);
//		vertexFaceMap.clear();
//		edgeVertexMap.clear();
//		faceFaceMap.clear();
//		edgeEdgeMap1.clear();
//		
//		HashMap<E, E> edgeEdgeMap2 = new HashMap<E, E>();
//	
//		// create faces for faces and vertices
//		for (F f : graph.getFaces()){
//			F newFace = result.addNewFace();
//			faceFaceMap.put(f, newFace);
//		}
//		for (V v : graph.getVertices()){
//			F newFace = result.addNewFace();
//			vertexFaceMap.put(v, newFace);
//		}
//		
//		// make edges and vertices
//		for (E e : graph.getEdges()){
//			V v = edgeVertexMap.get(e);
//			if (v == null) {
//				v = result.addNewVertex();
//				edgeVertexMap.put(e, v);
//				edgeVertexMap.put(e.getOppositeEdge(), v);
//				vA.set(v, eA.get(e));
//			}
//			E e1 = result.addNewEdge();
//			E e2 = result.addNewEdge();
//			e1.setTargetVertex(v);
//			e2.setTargetVertex(v);
//	
//			edgeEdgeMap1.put(e, e1);
//			edgeEdgeMap2.put(e, e2);
//		}
//		// link cycles
//		for (E e : graph.getEdges()){
//			E nextE = e.getNextEdge();
//			E e1 = edgeEdgeMap1.get(e);
//			E e2 = edgeEdgeMap2.get(e);
//			E e11 = edgeEdgeMap1.get(nextE);
//			
//			e11.linkOppositeEdge(e2);
//			e1.linkNextEdge(e11);
//			
//			F face = faceFaceMap.get(e.getLeftFace());
//			e1.setLeftFace(face);
//		}
//		// link cocycles
//		for (V v : graph.getVertices()){
//			E firstEdge = v.getIncomingEdge();
//			E actEdge = firstEdge;
//			F face = vertexFaceMap.get(v);
//			do {
//				E nextEdge = actEdge.getNextEdge().getOppositeEdge();
//				E e2 = edgeEdgeMap2.get(actEdge);
//				E e3 = edgeEdgeMap2.get(nextEdge);
//				e3.linkNextEdge(e2);
//				actEdge = nextEdge;
//				
//				e2.setLeftFace(face);
//			} while (actEdge != firstEdge);
//		}
//		return result;
//	}
//
//	public <
//		V extends Vertex<V, E, F>,
//		E extends Edge<V, E, F>,
//		F extends Face<V, E, F>,
//		HDS extends HalfEdgeDataStructure<V, E, F>
//	> Map<E, Set<E>> subdivide(
//			HDS oldHeds, 
//			HDS newHeds, 
//			VertexPositionCalculator vc,
//			EdgeAverageCalculator ec,
//			FaceBarycenterCalculator fc
//	) {
		
		System.err.println("doosabin-algo returns nothing yet! still under contruction");
		return null;
		
	}
	
}
