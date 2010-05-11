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

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryEdges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

/**
 * Catmull-Clark Subdivision
 * @author Andre Heydt
 *
 * @param <V> Vertex class
 * @param <E> Edge class
 * @param <F> Face class
 */
public class CatmullClark2 {
	
	/**
	 * Subdivides a given surface with the Catmull-Clark rule
	 * @param <HDS> 
	 * @param oldHeds the input surface
	 * @param newHeds the output surface will be overwritten
	 * @param vc a coordinates adapter
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
		Map<F, V> oldFnewVMap = new HashMap<F, V>();
		Map<E, V> oldEnewVMap = new HashMap<E, V>();
		Map<V, V> oldVnewVMap = new HashMap<V, V>();
		Map<F, double[]> oldFtoPos = new HashMap<F, double[]>();
		Map<E, double[]> oldEtoPos = new HashMap<E, double[]>();
		Map<V, double[]> oldVtoPos = new HashMap<V, double[]>();
		Map<E, Set<E>> oldEtoNewEsMap = new HashMap<E,Set<E>>();

		HalfEdgeUtilsExtra.clear(newHeds);
		
		//calc coordinates for the new points at the "old point"
		for (V oldV: oldHeds.getVertices()){
			List<E> star = HalfEdgeUtils.incomingEdges(oldV);
			int deg = star.size();
			double[] pos = new double[]{0,0,0};
			for (E e : star){
				F f = e.getLeftFace();
				Rn.add(pos, pos, fc.get(f));
			}
			Rn.times(pos, 1.0/deg, pos);
			V nv = newHeds.addNewVertex();
			oldVnewVMap.put(oldV, nv);
			oldVtoPos.put(oldV, pos);
			vc.set(nv, pos);
		}
		
		//calc coordinates for the new points at "face-barycenter"
		for(F oldF : oldHeds.getFaces()) {
			oldFtoPos.put(oldF, fc.get(oldF));
			V nv = newHeds.addNewVertex();
			oldFnewVMap.put(oldF, nv);
			//System.out.println(fc.get(oldF));
			vc.set(nv, fc.get(oldF));
		}

		//calc coordinates for the new points at "edge-midpoint" 
		for(E oldPosE : oldHeds.getPositiveEdges()) {

			double[] pos = new double[]{0,0,0};
			// calc with edge midpoint
			ec.setEdgeAlpha(.5);
			ec.setEdgeIgnore(true);
			pos = ec.get(oldPosE);
			F fl = oldPosE.getLeftFace();
			F fr = oldPosE.getOppositeEdge().getLeftFace();
			double[] posfl = fc.get(fl);
			double[] posfr = fc.get(fr);
			
			Rn.add(pos, pos, posfl);
			Rn.add(pos, pos, posfr);
			Rn.times(pos, 1.0/3.0 , pos);
			
			oldEtoPos.put(oldPosE, pos);
			V nv = newHeds.addNewVertex();
			oldEnewVMap.put(oldPosE, nv);
			vc.set(nv, pos);
		}
		
		System.out.println("ois guad");
		ccSubdiv(oldHeds, newHeds, oldVnewVMap, oldEnewVMap, oldFnewVMap, oldEtoNewEsMap);
		System.out.println("ois guad 2");
		//set coordinates for the new points <-> old points
		for(V oV : oldVnewVMap.keySet()) {
			V nV = oldVnewVMap.get(oV);
			double[] pos = oldVtoPos.get(oV);
			System.out.println(pos[0]);
			vc.set(nV, pos);
		}
		System.out.println("");
		
		//set coordinates for the new points <-> old faces
		for(F oF : oldFnewVMap.keySet()) {
			V nV = oldFnewVMap.get(oF);
			double[] pos = oldFtoPos.get(oF);
			System.out.println(pos[0]);
			vc.set(nV, pos);
		}
		System.out.println("");
		
		//set coordinates for the new points <-> old edges
		for(E oE : oldEnewVMap.keySet()){
			V nV = oldEnewVMap.get(oE);
			double[] pos = oldEtoPos.get(oE);
			System.out.println(pos[0]);
			vc.set(nV, pos);
		}
		
		System.out.println(oldHeds.getEdges().size());
		System.out.println(newHeds.getEdges().size());
		System.out.println(oldHeds.getVertices().size());
		System.out.println(newHeds.getVertices().size());
		System.out.println(oldHeds.getFaces().size());
		System.out.println(newHeds.getFaces().size());
		
		boolean validSurface = newHeds.isValidSurface();
		System.out.println(validSurface);
		System.err.println("catmullclark2-algo returns nothing yet! still under contruction");
		return oldEtoNewEsMap;
			
		
	}
	
	//return new HEDS subdivided
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void ccSubdiv(
		HDS oldh, 
		HDS newh,
		Map<V,V> oldVnewVMap,
		Map<E,V> oldEnewVMap,
		Map<F,V> oldFnewVMap,
		Map<E, Set<E>> oldEtoNewEs
	){
		
		//add new edges for old egde
		for(E oldE : oldh.getEdges()){
			Set<E> newEs = new HashSet<E>();
			E e = newh.addNewEdge();
			newEs.add(e);
			E e1 = newh.addNewEdge();
			newEs.add(e1);
			oldEtoNewEs.put(oldE, newEs);
		}
		// link opposite edges and target vertices
		for(E oldPosE: oldh.getPositiveEdges()){
			Set<E> posEdges = oldEtoNewEs.get(oldPosE);
			Set<E> negEdges = oldEtoNewEs.get(oldPosE.getOppositeEdge());
			List<E> pe = new ArrayList<E>();
			List<E> ne = new ArrayList<E>();
			for(E e: posEdges){
				pe.add(e);
			}
			for(E e: negEdges){
				ne.add(e);
			}
			
			E e0 = pe.get(0);
			System.out.println("E0: " + e0);
			E e1 = pe.get(1);
			System.out.println("E1: " + e1);
			E oe0 = ne.get(0);
			System.out.println("oE0: " + oe0);
			E oe1 = ne.get(1);
			System.out.println("oE1: " + oe1);
			
			//opposite (Attention to the HashSet-order)
			if (e0.getIndex()>e1.getIndex()){
				if (oe0.getIndex()>oe1.getIndex()){
					e0.linkOppositeEdge(oe0);
					e1.linkOppositeEdge(oe1);
				} else {
					e0.linkOppositeEdge(oe1);
					e1.linkOppositeEdge(oe0);
				}
			} else {
				if (oe0.getIndex()>oe1.getIndex()){
					e0.linkOppositeEdge(oe1);
					e1.linkOppositeEdge(oe0);
				} else {
					e0.linkOppositeEdge(oe0);
					e1.linkOppositeEdge(oe1);
				}
			}
			
			//targetvertex (Attention to the HashSet-order)
			V tv = oldVnewVMap.get(oldPosE.getTargetVertex());
			V sv = oldVnewVMap.get(oldPosE.getOppositeEdge().getTargetVertex());
			V nv = oldEnewVMap.get(oldPosE);
			if(e0.getIndex()>e1.getIndex()){
				e0.setTargetVertex(nv);
				e1.setTargetVertex(tv);
			} else {
				e0.setTargetVertex(tv);
				e1.setTargetVertex(nv);
			}
			if(oe0.getIndex()>oe1.getIndex()){
				oe0.setTargetVertex(nv);
				oe1.setTargetVertex(sv);
			} else {
				oe0.setTargetVertex(sv);
				oe1.setTargetVertex(nv);
			}
		}
		
		for(F of : oldh.getFaces()){
			List <E> newEdges = new ArrayList<E>();
			for(E e : boundaryEdges(of)){
				newEdges.add(newh.addNewEdge());
				newEdges.add(newh.addNewEdge());
			}
			int i= 0;
			
			for (E e : boundaryEdges(of)) {
				Set<E> es = oldEtoNewEs.get(e);
				Set<E> pes = oldEtoNewEs.get(e.getPreviousEdge());
				List<E> edges = new ArrayList<E>();
				List<E> pedges= new ArrayList<E>();
				for(E ed: es){
					edges.add(ed);
					}
				for(E ed: pes){
					pedges.add(ed);
				}
				//keep Attention to Hashset-order
				E e0 = edges.get(0);
				E e1 = edges.get(1);
				E e2 = pedges.get(0);
				E e3 = pedges.get(1);
				
				if (e0.getIndex()<e1.getIndex()){
					e0 = edges.get(1);
				}
				if (e2.getIndex()>e3.getIndex()){
					e3 = pedges.get(1);
				}
				e1 = newEdges.get(i);
				i++;
				e2 = newEdges.get(i);
				i++;

			
				//link edges
				e0.linkNextEdge(e1);
				e1.linkNextEdge(e2);
				e2.linkNextEdge(e3);
				e3.linkNextEdge(e0);
				
				//set target vertex
				e1.setTargetVertex(oldFnewVMap.get(of));
				if(e.getPreviousEdge().isPositive()){
					e2.setTargetVertex(oldEnewVMap.get(e.getPreviousEdge()));
				}
				e2.setTargetVertex(oldEnewVMap.get(e.getPreviousEdge().getOppositeEdge()));
				
				//link face
				F f = newh.addNewFace();
				e0.setLeftFace(f);
				e1.setLeftFace(f);
				e2.setLeftFace(f);
				e3.setLeftFace(f);
			}
			//opposite edges
			for(int j=0;j<newEdges.size();j+=2){
				int k=(j+3)%newEdges.size();
				E e = newEdges.get(j);
				E oe = newEdges.get(k);
				e.linkOppositeEdge(oe);
			}
			
		}
		
		
	}
	
	
}
