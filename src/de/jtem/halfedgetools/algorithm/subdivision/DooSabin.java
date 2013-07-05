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

package de.jtem.halfedgetools.algorithm.subdivision;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.algorithm.ProgressNotifier;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;

public final class DooSabin {
	
	/**
	 * Doo-Sabin
	 * 
	 * This is a dynamic implementation of DooSabin's algorithm, i.e. 
	 * the whole solution is generated via computation of small solutions, #iterations = #vertices.
	 * Maybe it is useful for parallelization or visualizing step-wise solution generation to an audience ;)
	 *
	 * @param oldHeds old half edge data structure of the graph to be refined
	 * @param newHeds new half edge data structure to create the new graph in 
	 * @param a TypedAdpaterSet
	 *
	 * @return the refinement of the given graph
	 * @throws SurfaceException
	 * 	 
	 * @author Jens-Peter Rohrlack (jens.peter.rohrlack@gmail.com)
	 * @return map of old to new edges
	 */

	public static enum BoundaryMode {
		FIXED, PROPOSED, CUSTOM
	}
	
	public final <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
		> 
	Map<E, E> subdivide(
			final HDS in, 
			HDS out, 
			final TypedAdapterSet<double[]> a,
			final HalfedgeInterface hcp,
			ArrayList<HDS> steps, 
			ProgressNotifier progress,
			BoundaryMode boundaryMode 
		) {
			
		return (subdivide(out, createNewState(in, a, hcp), steps, hcp, progress, boundaryMode));

	}
	
	/**
	 * 
	 * @param out half edge data structure representing the result in the end
	 * @param state initial state to start computing from
	 * @param steps list of iteration steps if desired
	 * @param hcp half edge interface
	 * @return map from old to new edges
	 */
	public final <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
		> 
		Map<E, E> subdivide(
				HDS out,
				AlgorithmState<V, E, F, HDS> currentState,
				ArrayList<HDS> steps,
				final HalfedgeInterface hcp, 
				ProgressNotifier progress, 
				BoundaryMode boundaryMode
	) {

		//add initial step
		if (steps != null) {
			steps.add(deepCopyHDS(currentState.getWorkingCopyHeds(), null, hcp, currentState.getA()));
		}
		
		//for each vertex expand all incoming-edge-opposite-edge-couples 
		//to a triangle or add only one inner edge,
		//resulting in #edges outer triangles or quadrilaterals and 
		//one #edges-gon formed of the new vertices, replacing this vertex 
		for (V v : currentState.getSourceHeds().getVertices()) {
			
			double prog = currentState.getVerticesDone().size() / (double)currentState.getSourceHeds().numVertices();
			progress.fireJobProgress(prog);
			//skip already expanded vertices
			if (currentState.getVerticesDone().contains(v)) continue;
			
			//list of generated outer edges of expanded vertex nGon
			List<E> innerNGonOuterEdges = new LinkedList<E>();
			
			//incoming edges
			List<E> halfStar = HalfEdgeUtils.incomingEdges(v);
			int numberOfNewVertices = halfStar.size();
			
			List<V> newVertices = currentState.getWorkingCopyHeds().addNewVertices(numberOfNewVertices);
			
			//counter to determine edge pairs of outer nGon edges via modulo n
			int incomingEdgeCounter = 0;
			
			for (int i=0; i < halfStar.size(); i++ ) {

				try {
				
				E edge = halfStar.get(i);
	
				//get edge working copy		
				E e = currentState.getOldEdgeToCopy().get(edge);

				double[] etv_new_position = null;
				
				//check for boundary edge
				if (edge.getLeftFace() == null) {

					switch (boundaryMode) {
					
						case FIXED : {
							etv_new_position = new double[]{
									currentState.getA().getD(Position.class, v)[0],
									currentState.getA().getD(Position.class, v)[1],
									currentState.getA().getD(Position.class, v)[2],
									currentState.getA().getD(Position.class, v)[3]
							};
							break;
						} 
						case PROPOSED : {
							
							//subdivision mask applied: http://www.cmlab.csie.ntu.edu.tw/~robin/courses/gm/note/subdivision-prn.pdf
							//page 80
							
							//find next boundary edge
							E temp = edge;
							int p = i;
							while (temp.getOppositeEdge().getLeftFace() != null) {
								temp = halfStar.get(p++ % halfStar.size());
							}

							//next boundary edge target vertex
							V nextV = temp.getOppositeEdge().getTargetVertex();
							
							//previous boundary edge target vertex
							V prevV = edge.getOppositeEdge().getTargetVertex();
							
							etv_new_position = new double[]{
									(3*currentState.getA().getD(Position.class, prevV)[0] + currentState.getA().getD(Position.class, nextV)[0])/4.,
									(3*currentState.getA().getD(Position.class, prevV)[1] + currentState.getA().getD(Position.class, nextV)[1])/4.,
									(3*currentState.getA().getD(Position.class, prevV)[2] + currentState.getA().getD(Position.class, nextV)[2])/4.,
									currentState.getA().getD(Position.class, v)[3]
							};
							break;
						}
						case CUSTOM : {
							
							//find next boundary edge
							E temp = edge;
							int p = i;
							while (temp.getOppositeEdge().getLeftFace() != null) {
								temp = halfStar.get(p++ % halfStar.size());
							}

							etv_new_position = new double[]{
									(2*currentState.getA().getD(Position.class, v)[0] + 3*currentState.getEdgeMidPoints().get(edge)[0].doubleValue() + 3*currentState.getEdgeMidPoints().get(temp)[0].doubleValue())/8.,
									(2*currentState.getA().getD(Position.class, v)[1] + 3*currentState.getEdgeMidPoints().get(edge)[1].doubleValue() + 3*currentState.getEdgeMidPoints().get(temp)[1].doubleValue())/8.,
									(2*currentState.getA().getD(Position.class, v)[2] + 3*currentState.getEdgeMidPoints().get(edge)[2].doubleValue() + 3*currentState.getEdgeMidPoints().get(temp)[2].doubleValue())/8.,
									currentState.getA().getD(Position.class, v)[3]
				
							};
							break;
						}
					}
				} else {
					
					//compute new target vertex position for e
					etv_new_position = new double[]{
							( currentState.getA().getD(Position.class, v)[0] 
							  + currentState.getFaceMidPoints().get(edge.getLeftFace())[0].doubleValue()
							  + currentState.getEdgeMidPoints().get(edge)[0].doubleValue()
							  + currentState.getEdgeMidPoints().get(edge.getNextEdge())[0].doubleValue()
							 )/4.,
							( currentState.getA().getD(Position.class, v)[1]
							  + currentState.getFaceMidPoints().get(edge.getLeftFace())[1].doubleValue()
							  + currentState.getEdgeMidPoints().get(edge)[1].doubleValue()
							  + currentState.getEdgeMidPoints().get(edge.getNextEdge())[1].doubleValue()
							)/4.,
							( currentState.getA().getD(Position.class, v)[2]
							  + currentState.getFaceMidPoints().get(edge.getLeftFace())[2].doubleValue()
							  + currentState.getEdgeMidPoints().get(edge)[2].doubleValue()
							  + currentState.getEdgeMidPoints().get(edge.getNextEdge())[2].doubleValue()
							)/4.,
							currentState.getA().getD(Position.class, v)[3]
					};
					
				}
				//this can be a new opposite edge, 
				//belonging to a previously created new face
				E eo = e.getOppositeEdge();
				
				//decide which mode
				if (currentState.getNewFaces().contains(eo.getLeftFace())) {
								
					//add only inner edge, which is an outer edge of the inner nGon
					
					V innerEdge_sv = newVertices.get(incomingEdgeCounter);
					V etv_new = newVertices.get( (incomingEdgeCounter + 1) % newVertices.size() );
					
					currentState.getA().set(Position.class, etv_new, etv_new_position);
					
					e.setTargetVertex(etv_new);
									
					E eop = eo.getNextEdge().getNextEdge();
					eop.setTargetVertex(innerEdge_sv);
					
					E innerEdge = currentState.getWorkingCopyHeds().addNewEdge();
					
					innerEdge.setTargetVertex(etv_new);
					innerEdge.setLeftFace(eo.getLeftFace());
					
					innerEdge.linkNextEdge(eo);
					eop.linkNextEdge(innerEdge);
					
					//remember the outer egde of the inner nGon
					innerNGonOuterEdges.add(innerEdge);			
				
					
				} else {
				
					//build new triangle face between e and eo, ...
					
					V eosv_new = newVertices.get(incomingEdgeCounter);
					V etv_new = newVertices.get( (incomingEdgeCounter + 1) % newVertices.size() );
					V eotv = eo.getTargetVertex();
					
					currentState.getA().set(Position.class, etv_new, etv_new_position);
					
					e.setTargetVertex(etv_new);
					
					E eo_new = currentState.getWorkingCopyHeds().addNewEdge();
					E eoo_new = currentState.getWorkingCopyHeds().addNewEdge();
					E eoo_new_n = currentState.getWorkingCopyHeds().addNewEdge();
					
					eo_new.setTargetVertex(eotv);
					eoo_new_n.setTargetVertex(etv_new);
					eoo_new.setTargetVertex(eosv_new);
										
					e.linkOppositeEdge(eo_new);
					eo.linkOppositeEdge(eoo_new);
					
					eo_new.linkNextEdge(eoo_new);
					eoo_new.linkNextEdge(eoo_new_n);
					eoo_new_n.linkNextEdge(eo_new);
					
					F triangleFace = currentState.getWorkingCopyHeds().addNewFace();
					
					eoo_new_n.setLeftFace(triangleFace);
					eoo_new.setLeftFace(triangleFace);
					eo_new.setLeftFace(triangleFace);
					
					//.. remember the outer egde of the inner nGon ...
					innerNGonOuterEdges.add(eoo_new_n);		
					
					//.. and the new added face.
					currentState.getNewFaces().add(triangleFace);				
					
				}
				
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				incomingEdgeCounter++;
			} //end, for all incoming edges 
	
			
			//after finishing the star, build the inner nGon face, replacing the old vertex
			F innerNgon = currentState.getWorkingCopyHeds().addNewFace();
			
			List<E> innerNGonEdges = new LinkedList<E>();
			for (int i=0; i < innerNGonOuterEdges.size(); i++) {	
				E e = innerNGonOuterEdges.get(i);
				E eo = currentState.getWorkingCopyHeds().addNewEdge();			
				eo.setTargetVertex(newVertices.get(i));
				eo.setLeftFace(innerNgon);
				e.linkOppositeEdge(eo);
				innerNGonEdges.add(eo);
			}
			
			for (int i=0; i < innerNGonEdges.size(); i++) {
				E e = innerNGonEdges.get(i);
				e.linkNextEdge(innerNGonEdges.get( (i - 1 + innerNGonEdges.size()) % innerNGonEdges.size()) );		
			}	
		
			//update done list
			currentState.getVerticesDone().add(v);
		
			//remove old vertex copy
			currentState.getWorkingCopyHeds().removeVertex(currentState.getOldVertexToCopy().get(v));
			
			//add step
			if (steps != null) {
				steps.add(deepCopyHDS(currentState.getWorkingCopyHeds(), null, hcp, currentState.getA()));
			}
						
		} //end, for all vertices
		
		//set output heds
		if (out != null) {
			deepCopyHDS(currentState.getWorkingCopyHeds(), out, hcp, currentState.getA());
		}
		
		return (currentState.getOldEdgeToCopy()); //no old edges were removed
		
	}
	
	public final static class AlgorithmState<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> { 
		private HDS sourceHeds, workingCopyHeds;
		
		//new faces
		private List<F> newFaces;	
		
		//old position data
		private Map<F, Double[]> faceMidPoints;
		private Map<E, Double[]> edgeMidPoints;

		//map old edges to copied edges
		private Map<E, E> oldEdgeToCopy;
	
		//map old vertices to copied vertices
		private Map<V, V> oldVertexToCopy;

		//vertices done so far
		private List<V> verticesDone;

		//adapter set
		private TypedAdapterSet<double[]> a;
		
		public AlgorithmState(
				HDS sourceHeds, 
				HDS workingCopyHeds,
				TypedAdapterSet<double[]> a,
				List<F> newFaces,
				Map<F, Double[]> faceMidPoints, Map<E, Double[]> edgeMidPoints,
				Map<E, E> oldEdgeToCopy, Map<V, V> oldVertexToCopy,
				List<V> verticesDone) {
			super();
			this.sourceHeds = sourceHeds;
			this.workingCopyHeds = workingCopyHeds;
			this.newFaces = newFaces;
			this.faceMidPoints = faceMidPoints;
			this.edgeMidPoints = edgeMidPoints;
			this.oldEdgeToCopy = oldEdgeToCopy;
			this.oldVertexToCopy = oldVertexToCopy;
			this.verticesDone = verticesDone;
			this.a = a;
		}

		public HDS getSourceHeds() {
			return sourceHeds;
		}

		public HDS getWorkingCopyHeds() {
			return workingCopyHeds;
		}

		public TypedAdapterSet<double[]> getA() {
			return a;
		}

		public List<F> getNewFaces() {
			return newFaces;
		}

		public Map<F, Double[]> getFaceMidPoints() {
			return faceMidPoints;
		}

		public Map<E, Double[]> getEdgeMidPoints() {
			return edgeMidPoints;
		}

		public Map<E, E> getOldEdgeToCopy() {
			return oldEdgeToCopy;
		}

		public Map<V, V> getOldVertexToCopy() {
			return oldVertexToCopy;
		}

		public List<V> getVerticesDone() {
			return verticesDone;
		}
		
	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> AlgorithmState<V, E, F, HDS> createNewState(
			final HDS oldHeds,
			final TypedAdapterSet<double[]> a,
			final HalfedgeInterface hcp) {

			//create deep copy of oldHeds
			HDS newHeds = deepCopyHDS(oldHeds, null, hcp, a);
	
			//remember new faces to decide which mode to apply
			List<F> newFaces = new LinkedList<F>();	
			
			Map<F, Double[]> faceMidPoints = new HashMap<F, Double[]>();
			Map<E, Double[]> edgeMidPoints = new HashMap<E, Double[]>();
			
			//map old faces to their mid-points
			for (F f : oldHeds.getFaces()) {
				
				List<E> boundingEdges = new LinkedList<E>();
				E curr = f.getBoundaryEdge();
				do {
					boundingEdges.add(curr);
					curr = curr.getNextEdge();
				} while(curr != f.getBoundaryEdge());
				
				Double[] midPoint = new Double[]{0., 0., 0., 1.};
				
				for (E e : boundingEdges){
					midPoint[0] += a.getD(Position.class, e.getTargetVertex())[0];
					midPoint[1] += a.getD(Position.class, e.getTargetVertex())[1];
					midPoint[2] += a.getD(Position.class, e.getTargetVertex())[2];
					
				}
				midPoint[0] /= boundingEdges.size();
				midPoint[1] /= boundingEdges.size();
				midPoint[2] /= boundingEdges.size();
				
				faceMidPoints.put(f, midPoint);
				
			}
			
			//map old edges to their mid-points
			for (E e : oldHeds.getEdges()) {
				
				if (edgeMidPoints.containsKey(e)) continue;
				
				Double[] midPoint = new Double[]{0., 0., 0., 1.};
				
				midPoint[0] = (a.getD(Position.class, e.getTargetVertex())[0] + a.getD(Position.class, e.getOppositeEdge().getTargetVertex())[0])/2.;
				midPoint[1] = (a.getD(Position.class, e.getTargetVertex())[1] + a.getD(Position.class, e.getOppositeEdge().getTargetVertex())[1])/2.;
				midPoint[2] = (a.getD(Position.class, e.getTargetVertex())[2] + a.getD(Position.class, e.getOppositeEdge().getTargetVertex())[2])/2.;			
	
	
				edgeMidPoints.put(e, midPoint);
				edgeMidPoints.put(e.getOppositeEdge(), midPoint);
			}
			
			//map old edges to copied edges
			Map<E, E> oldEdgeToCopy = new HashMap<E,E>();
			
			for (E e : oldHeds.getEdges()) {
				oldEdgeToCopy.put(e, newHeds.getEdge(e.getIndex()));
			}
			
			//map old vertices to copied vertices
			Map<V, V> oldVertexToCopy = new HashMap<V,V>();	
			
			for (V v : oldHeds.getVertices()) {
				oldVertexToCopy.put(v, newHeds.getVertex(v.getIndex()));
			}
			
			//create algorithm state
			return(new AlgorithmState<V, E, F, HDS>(
					oldHeds,
					newHeds,
					a,
					newFaces,
					faceMidPoints,
					edgeMidPoints,
					oldEdgeToCopy,
					oldVertexToCopy,
					new LinkedList<V>()
					));
		
	}
	
	private <V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>>
	HDS deepCopyHDS(HDS in, HDS out, HalfedgeInterface hcp, TypedAdapterSet<double[]> a) {
			
		if (out == null) {
			out = hcp.createEmpty(in);
		}
		in.createCombinatoriallyEquivalentCopy(out);
			
		//add position data
		for (V vv : in.getVertices()) {
			a.set(Position.class, out.getVertex(vv.getIndex()), a.getD(Position.class, vv));
		} 
		
		return (out);
	}
	
}

