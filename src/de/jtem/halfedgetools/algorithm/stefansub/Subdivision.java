package de.jtem.halfedgetools.algorithm.stefansub;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.functional.alexandrov.SurfaceUtility;
import de.jtem.halfedgetools.plugin.buildin.topology.TopologyOperations;
import de.jtem.halfedgetools.util.surfaceutilities.SurfaceException;

public class Subdivision {

	@SuppressWarnings("unchecked")
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS createStripSubdivision(
			HDS graph,
			HDS quad,
			HashMap<V, V> vertexVertexMap,
			HashMap<E, V> edgeVertexMap,
			HashMap<F, V> faceVertexMap,
			Coord3DAdapter<V> vA,
			Coord3DAdapter<E> eA,
			Coord3DAdapter<F> fA) 
			throws SurfaceException{
		
		for (V v : graph.getVertices()){
			V newV = quad.addNewVertex();
			vA.setCoord(newV, vA.getCoord(v));
			vertexVertexMap.put(v, newV);
		}
		for (E e : graph.getPositiveEdges()){
			V newV = quad.addNewVertex();
			vA.setCoord(newV, eA.getCoord(e));
			edgeVertexMap.put(e, newV);
			edgeVertexMap.put(e.getOppositeEdge(), newV);
		}
		for (F f : graph.getFaces()){
			if (HalfEdgeUtils.boundaryEdges(f).size() < 4) 
				continue;
			V newVertex = quad.addNewVertex();
			vA.setCoord(newVertex, fA.getCoord(f));
			faceVertexMap.put(f, newVertex);
		}
		
		FaceByFaceGenerator<V, E, F> g = new FaceByFaceGenerator<V, E, F>(quad);
		for (F f : graph.getFaces()){
			if (HalfEdgeUtils.boundaryEdges(f).size() >= 4){
				for (E b : HalfEdgeUtils.boundaryEdges(f)){
					V v1 = edgeVertexMap.get(b);
					V v2 = vertexVertexMap.get(b.getTargetVertex());
					V v3 = edgeVertexMap.get(b.getNextEdge());
					V v4 = faceVertexMap.get(f);
					g.addFace(v1, v2, v3, v4);
				}
			} else if (HalfEdgeUtils.boundaryEdges(f).size() == 3){
//				if (f.isInteriorFace())
//					throw new SurfaceException("Cannot subdivide inner triangles consistently, in createStripSubdivision()");
				E b = f.getBoundaryEdge();
//				for (E e : HalfEdgeUtils.boundaryEdges(f))
//					if (!e.isInteriorEdge())
//						b = e;
//				if (!b.getNextEdge().isInteriorEdge()){
//					b = b.getNextEdge();
//				}
//				 check if next in boundary is a triangle
//				if (!b.getPreviousEdge().isInteriorEdge()){
//					F nextInBoundary = b.getOppositeEdge().getPreviousEdge().getRightFace();
//					if (HalfEdgeUtils.boundaryEdges(nextInBoundary).size() != 3)
//						b = b.getPreviousEdge();
//				}
				V v1 = vertexVertexMap.get(b.getStartVertex());
				V v2 = edgeVertexMap.get(b);
				V v3 = vertexVertexMap.get(b.getTargetVertex());
				V v4 = edgeVertexMap.get(b.getNextEdge());
				V v5 = vertexVertexMap.get(b.getNextEdge().getTargetVertex());
				V v6 = edgeVertexMap.get(b.getPreviousEdge());
				g.addFace(v1, v2, v6);
				g.addFace(v2, v3, v4);
				g.addFace(v4, v5, v6, v2);
			} else {
				throw new SurfaceException("Cant handle face " + f + ", in createStripSubdivision()");
			}
		}
		SurfaceUtility.linkBoundary(quad);
		return quad;
	}
	

	// EDGE QUAD SUBDIVIDE
	//
	// x-----x-----x
	// |     |     |
	// |     |     |
	// x-----x-----x
	// |     |     |
	// |     |     |
	// x-----x-----x


	public static 
	<
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS createEdgeQuadGraph(
			HDS graph,
			HDS quad, 
			HashMap<V, V> vertexVertexMap, 
			HashMap<E, V> edgeVertexMap, 
			HashMap<F, V> faceVertexMap,
			Coord3DAdapter<V> vA,
			Coord3DAdapter<E> eA,
			Coord3DAdapter<F> fA)
			throws SurfaceException{
		
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			vA.setCoord(newVertex, vA.getCoord(v));
			vertexVertexMap.put(v, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
			V newVertex = quad.addNewVertex();
			vA.setCoord(newVertex, eA.getCoord(e));
			edgeVertexMap.put(e, newVertex);
			edgeVertexMap.put(e.getOppositeEdge(), newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			vA.setCoord(newVertex, fA.getCoord(f));
			faceVertexMap.put(f, newVertex);
		}
		
		int numLinks = 0;
		// edges vertex connections
		DualHashMap<V, V, E> quadEdgeMap = new DualHashMap<V, V, E>();
		for (E e : graph.getPositiveEdges()){
			V v = edgeVertexMap.get(e);
			V v1 = vertexVertexMap.get(e.getTargetVertex());
			V v3 = vertexVertexMap.get(e.getStartVertex());
			V v4 = faceVertexMap.get(e.getLeftFace());
			V v2 = faceVertexMap.get(e.getRightFace());
			
			E e1 = quad.addNewEdge();
			E e2 = quad.addNewEdge();
			E e3 = quad.addNewEdge();
			E e4 = quad.addNewEdge();
			E e5 = quad.addNewEdge();
			E e6 = quad.addNewEdge();
			E e7 = quad.addNewEdge();
			E e8 = quad.addNewEdge();
			
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v);
			e3.setTargetVertex(v2);
			e4.setTargetVertex(v);
			e5.setTargetVertex(v3);
			e6.setTargetVertex(v);
			e7.setTargetVertex(v4);
			e8.setTargetVertex(v);
			
			e2.linkNextEdge(e3);
			e4.linkNextEdge(e5);
			e6.linkNextEdge(e7);
			e8.linkNextEdge(e1);
			numLinks += 4;
		
			e1.linkOppositeEdge(e2);
			e3.linkOppositeEdge(e4);
			e5.linkOppositeEdge(e6);
			e7.linkOppositeEdge(e8);
			
			quadEdgeMap.put(v, v1, e1);
			quadEdgeMap.put(v1, v, e2);
			quadEdgeMap.put(v, v2, e3);
			quadEdgeMap.put(v2, v, e4);
			quadEdgeMap.put(v, v3, e5);
			quadEdgeMap.put(v3, v, e6);
			quadEdgeMap.put(v, v4, e7);
			quadEdgeMap.put(v4, v, e8);
		}
		
		// face vertex connections
		HashSet<F> readyFaces = new HashSet<F>();
		for (E bEdge : graph.getEdges()){
			F f = bEdge.getLeftFace();
			if (readyFaces.contains(f))
				continue;
			V v = faceVertexMap.get(f);
			V bVertex = edgeVertexMap.get(bEdge);
			E lastEdge = quadEdgeMap.get(bVertex, v);
			E actEdge = bEdge;
			do {
				actEdge = actEdge.getNextEdge();
				V vertex = edgeVertexMap.get(actEdge);
				E edge =  quadEdgeMap.get(vertex, v);
				edge.linkNextEdge(lastEdge.getOppositeEdge());
				numLinks++;
				lastEdge = edge;
			} while (actEdge != bEdge);
			readyFaces.add(f);
		}
		// vertex vertex connections
		for (V v : graph.getVertices()){
			V vertex = vertexVertexMap.get(v);
			Collection<E> vStar = quadEdgeMap.get(vertex);
			for (E edge : vStar){
				E linkEdge = edge.getNextEdge().getNextEdge().getNextEdge(); 
				linkEdge.linkNextEdge(edge);
				numLinks++;
			}
		}
		HalfEdgeUtils.fillAllHoles(quad);
		return quad;
	}

	// VERTEX QUAD SUBDIVIDE
	//
	// x-----------x
	// |\         /|
	// |   \   /   |
	// |     x     |
	// |   /   \   |
	// |/         \|
	// x-----------x
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS createVertexQuadGraph(
			HDS graph, 
			HDS quad, 
			HashMap<V, V> vertexVertexMap, 
			HashMap<F, V> faceVertexMap,
			Coord3DAdapter<V> vA,
			Coord3DAdapter<E> eA,
			Coord3DAdapter<F> fA)
	throws SurfaceException{
		
		HashMap<E, E> leftQuadEdgeMap = new HashMap<E, E>();
		HashMap<E, F> edgeFaceMap = new HashMap<E, F>();
		
		// vertices
		for (V v : graph.getVertices()){
			// quads only for inner edges
			boolean isNewVertex = false;
//			for (E e : HalfEdgeUtils.incomingEdges(v))
//				if (e.isInteriorEdge())
//				if(true)
//					isNewVertex = true;
//			if (!isNewVertex) continue;
			V newVertex = quad.addNewVertex();
			vA.setCoord(newVertex, vA.getCoord(v));
			vertexVertexMap.put(v, newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			vA.setCoord(newVertex, fA.getCoord(f));
			faceVertexMap.put(f, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
//			 quads only for inner edges
//			if (!e.isInteriorEdge())
//				continue;
			F newFace = quad.addNewFace();
			edgeFaceMap.put(e, newFace);
			edgeFaceMap.put(e.getOppositeEdge(), newFace);
		}
		
		
		// create inner edges
		for (E e : graph.getPositiveEdges()){
			// quads only for inner edges
//			if (!e.isInteriorEdge()) continue;
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
//			if (!e.isInteriorEdge()) continue;
			E e1 = leftQuadEdgeMap.get(e);
//			if (!e.getNextEdge().isInteriorEdge()) continue;
			E e2 = leftQuadEdgeMap.get(e.getNextEdge()).getNextEdge();
			e1.linkOppositeEdge(e2);
		}
		
		// create boundary edges
		for (int i = 0; i < quad.numEdges(); i++){
			E e = quad.getEdge(i);
			if (e.getOppositeEdge() != null) continue;
			E opp = quad.addNewEdge();
			
			opp.linkOppositeEdge(e);
		}
		
		// connect boundary
		for (E e : quad.getEdges()){
			if (e.getLeftFace() != null) continue;
			E prev = e;
			do {
				prev = prev.getOppositeEdge().getNextEdge();
			} while (prev.getRightFace() != null);
			prev = prev.getOppositeEdge();
			e.linkPreviousEdge(prev);
		}
		
		for (E e : quad.getEdges()){
			if (e.getLeftFace() != null) continue;
			e.setTargetVertex(e.getNextEdge().getStartVertex());
		}
		
		return quad;
	}

	
	
	/**
	 * Generates the medial graph for the given graph
	 * @param graph the graph
	 * @param vClass the vertex class type of the result
	 * @param eClass the edge class type of the result
	 * @param fClass the face class type of the result
	 * @param edgeTable1 this map maps edges of the graph onto edges of the result 
	 * @return the medial
	 * @throws SurfaceException
	 */
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	>  HDS createMedialGraph
			(
				HDS graph,
				HDS result,
				HashMap<V, F> vertexFaceMap,
				HashMap<E, V> edgeVertexMap,
				HashMap<F, F> faceFaceMap,
				HashMap<E, E> edgeEdgeMap1,
				Coord3DAdapter<V> vA,
				Coord3DAdapter<E> eA,
				Coord3DAdapter<F> fA
			) throws SurfaceException
	{
		vertexFaceMap.clear();
		edgeVertexMap.clear();
		faceFaceMap.clear();
		edgeEdgeMap1.clear();
		
		HashMap<E, E> edgeEdgeMap2 = new HashMap<E, E>();
	
		// create faces for faces and vertices
		for (F f : graph.getFaces()){
			F newFace = result.addNewFace();
			faceFaceMap.put(f, newFace);
		}
		for (V v : graph.getVertices()){
			F newFace = result.addNewFace();
			vertexFaceMap.put(v, newFace);
		}
		
		// make edges and vertices
		for (E e : graph.getEdges()){
			V v = edgeVertexMap.get(e);
			if (v == null) {
				v = result.addNewVertex();
				edgeVertexMap.put(e, v);
				edgeVertexMap.put(e.getOppositeEdge(), v);
				vA.setCoord(v, eA.getCoord(e));
			}
			E e1 = result.addNewEdge();
			E e2 = result.addNewEdge();
			e1.setTargetVertex(v);
			e2.setTargetVertex(v);
	
			edgeEdgeMap1.put(e, e1);
			edgeEdgeMap2.put(e, e2);
		}
		// link cycles
		for (E e : graph.getEdges()){
			E nextE = e.getNextEdge();
			if (nextE == null)
				throw new SurfaceException("No surface in MedialSurface.generate()");
			E e1 = edgeEdgeMap1.get(e);
			E e2 = edgeEdgeMap2.get(e);
			E e11 = edgeEdgeMap1.get(nextE);
			
			e11.linkOppositeEdge(e2);
			e1.linkNextEdge(e11);
			
			F face = faceFaceMap.get(e.getLeftFace());
			e1.setLeftFace(face);
		}
		// link cocycles
		for (V v : graph.getVertices()){
			E firstEdge = v.getIncomingEdge();
			E actEdge = firstEdge;
			F face = vertexFaceMap.get(v);
			do {
				E nextEdge = actEdge.getNextEdge().getOppositeEdge();
				E e2 = edgeEdgeMap2.get(actEdge);
				E e3 = edgeEdgeMap2.get(nextEdge);
				e3.linkNextEdge(e2);
				actEdge = nextEdge;
				
				e2.setLeftFace(face);
			} while (actEdge != firstEdge);
		}
		return result;
	}

}
