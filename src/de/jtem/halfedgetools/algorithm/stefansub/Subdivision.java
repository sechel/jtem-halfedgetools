/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.algorithm.stefansub;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdgeInterpolator;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.util.surfaceutilities.SurfaceException;

public class Subdivision {

	// EDGE QUAD SUBDIVIDE
	// combinatorially equivalent to catmull-clark!
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
			SubdivisionVertexAdapter<V> vA,
			SubdivisionEdgeInterpolator<E> eA,
			SubdivisionFaceBarycenter<F> fA)
			throws SurfaceException{
		
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, vA.getData(v));
			vertexVertexMap.put(v, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, eA.getData(e,0.5,true));
			edgeVertexMap.put(e, newVertex);
			edgeVertexMap.put(e.getOppositeEdge(), newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, fA.getData(f));
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
	// root-three without the flipped edges in the quads
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
			SubdivisionVertexAdapter<V> vA,
			SubdivisionFaceBarycenter<F> fA)
	throws SurfaceException{
		
		HashMap<E, E> leftQuadEdgeMap = new HashMap<E, E>();
		HashMap<E, F> edgeFaceMap = new HashMap<E, F>();
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, vA.getData(v));
			vertexVertexMap.put(v, newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, fA.getData(f));
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
	
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS createRootThree(
			HDS graph, 
			HDS quad, 
			HashMap<V, V> vertexVertexMap, 
			HashMap<F, V> faceVertexMap,
			SubdivisionVertexAdapter<V> vA,
			SubdivisionFaceBarycenter<F> fA)
	throws SurfaceException{
		
		HashMap<E, E> leftQuadEdgeMap = new HashMap<E, E>();
		HashMap<E, F> edgeFaceMap = new HashMap<E, F>();
		
		// vertices
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, vA.getData(v));
			vertexVertexMap.put(v, newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			vA.setData(newVertex, fA.getData(f));
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
				SubdivisionVertexAdapter<V> vA,
				SubdivisionEdgeInterpolator<E> eA
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
				vA.setData(v, eA.getData(e,0.5,true));
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
