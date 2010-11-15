package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
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
	
//	private double alpha = 0.5;
	
	public <	
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, Set<E>> subdivide(
			HDS oldHeds, 
			HDS newHeds, 
			TypedAdapterSet<double[]> a
	) {
		Map<V, F> oldVnewFMap = new HashMap<V,F>();
		Map<F, F> oldFnewFMap = new HashMap<F, F>();
		Map<E, F> oldpEnewFMap = new HashMap<E, F>();
		Map<E, V> oldEnewVMap = new HashMap<E, V>();
//		Map<E, Set<E>> oldEtonewEsMap = new HashMap<E, Set<E>>();
		Set<E> newEdges = new HashSet<E>();
		
		//faces
		// create new faces for old faces
		for (F f : oldHeds.getFaces()){
			F newFace = newHeds.addNewFace();
			oldFnewFMap.put(f, newFace);
		}
		
		// create new faces for old vertices	
		for (V v : oldHeds.getVertices()){
			F newFace = newHeds.addNewFace();
			oldVnewFMap.put(v, newFace);
		}
		
		// create new faces for old positiv edges
		// TODO: test ispositive 
		// not sure what positiv edges are!!
		for (E e : oldHeds.getEdges()){
			if (e.isPositive()){
				F newFace = newHeds.addNewFace();
				oldpEnewFMap.put(e, newFace);
			}
		}
		
		//vertices
		// create new vertices for old edge 		
		for (E e : oldHeds.getEdges()){
			V newVert = newHeds.addNewVertex();
			oldEnewVMap.put(e, newVert);
		}
		
		//combinatorics for oldFnewFMap
		for (F of : oldHeds.getFaces()){
			List <E> edges = new ArrayList<E>();
			E e = of.getBoundaryEdge();
			edges.add(e);
			E nextE = e.getNextEdge();
			while (nextE != e) {
				edges.add(nextE);
				nextE.getNextEdge();
			}
			//add new edges for new-face-old-face-map
			//set targetvertex
			List<E> newEs = new ArrayList<E>();
			for (E ed : edges){
				E newE = newHeds.addNewEdge();
				newEs.add(newE);
				newE.setTargetVertex(oldEnewVMap.get(ed));
			}
			//links nextEdge and face
			for (int i =0; i< newEs.size(); i++){
				E newE = newEs.get(i);
				newE.setLeftFace(oldFnewFMap.get(of));
				E nexted = newEs.get((i+1)%newEdges.size());
				newE.linkNextEdge(nexted);
			}
		}
		
		//combinatorics for oldVnewFMap
		for (V ov : oldHeds.getVertices())	{
			List <E> edges = new ArrayList<E>();
			E e = ov.getIncomingEdge();
			edges.add(e);
			E nextE = e.getNextEdge();
			E oppE = nextE.getOppositeEdge();
			while (oppE != e) {
				edges.add(oppE);
				nextE = oppE.getNextEdge();
				oppE = nextE.getOppositeEdge();
			}
			//add new edges for old-vertex-new-face-map
			//link faces
			List<E> newEs = new ArrayList<E>();
			for (E ed : edges){
				E newE = newHeds.addNewEdge();
				newEs.add(newE);
				newE.setTargetVertex(oldEnewVMap.get(ed));
			}
			//links nextEdge and face
			for (int i =0; i< newEs.size(); i++){
				E newE = newEs.get(i);
				newE.setLeftFace(oldVnewFMap.get(ov));
				E nexted = newEs.get((i+1)%newEdges.size());
				newE.linkNextEdge(nexted);
			}
			
		}
		
		//combinatorics for oldEnewFMap
		for (E olde : oldHeds.getPositiveEdges())	{
			E polde = olde.getPreviousEdge();
			E oolde = olde.getOppositeEdge();
			E poolde = oolde.getPreviousEdge();
			
			//add new edges
			E e1 = newHeds.addNewEdge();
			E e2 = newHeds.addNewEdge();
			E e3 = newHeds.addNewEdge();
			E e4 = newHeds.addNewEdge();
			
			// link edge
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e3);
			e3.linkNextEdge(e4);
			e4.linkNextEdge(e1);
			
			//set face
			F f = oldpEnewFMap.get(olde);
			e1.setLeftFace(f);
			e2.setLeftFace(f);
			e3.setLeftFace(f);
			e4.setLeftFace(f);
			
			//set targetvertex;
			e1.setTargetVertex(oldEnewVMap.get(olde));
			e2.setTargetVertex(oldEnewVMap.get(polde));
			e3.setTargetVertex(oldEnewVMap.get(oolde));
			e4.setTargetVertex(oldEnewVMap.get(poolde));
		}
		// TODO: opposite edges!!!
		
		
		
		
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
