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
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition4d;

/**
 * @author Kristoffer Josefsson, Andre Heydt 
 */
public class LoopLinear {

	//return new HEDS approximated using dyadic scheme
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, Set<E>> subdivide(
		HDS oldHeds, 
		HDS newHeds, 
		TypedAdapterSet<double[]> a
	){
		Map<E, double[]> oldEtoPos = new HashMap<E, double[]>();
		Map<E, double[]> oldEtoTex = new HashMap<E, double[]>();
		Map<V,E> newVtoOldE = new HashMap<V,E>();
		Map<E, Set<E>> oldEtoNewEs = new HashMap<E,Set<E>>();
		
		int maxDeg = 0;
		for(V v : oldHeds.getVertices()) {
			maxDeg = Math.max(maxDeg, HalfEdgeUtils.incomingEdges(v).size());
		}
		
		HashMap<Integer, Double> alphaMap = new HashMap<Integer, Double>();
		for(int i = 1; i <= maxDeg; i++) {
			double alpha = 0.0;
			if(i == 6)
				alpha = 3.0/8.0;
			else
				alpha = 5.0/8.0 - StrictMath.pow(3.0/8.0 + 1.0/4.0 * StrictMath.cos(2*Math.PI/i),2);
			alphaMap.put(i, alpha);
		}
		
		for(E e : oldHeds.getPositiveEdges()) {
			a.setParameter("alpha", 0.5);
			a.setParameter("ignore", true);
			double[] m = a.get(BaryCenter4d.class, e);
			oldEtoPos.put(e, m);
			
			double[] sTex = a.get(TexturePosition4d.class, e.getStartVertex());
			double[] tTex = a.get(TexturePosition4d.class, e.getTargetVertex());
			double[] mt = Rn.linearCombination(null, 0.5, sTex, 0.5, tTex);
			oldEtoTex.put(e, mt);
		}
		
		dyadicSubdiv(oldHeds, newHeds, newVtoOldE, oldEtoNewEs);
		
		for(V ov : oldHeds.getVertices()) {
			double[] pos = a.get(BaryCenter4d.class, ov);
			double[] tex = a.get(TexturePosition4d.class, ov);
			V newV = newHeds.getVertex(ov.getIndex());
			a.set(Position.class, newV, pos);
			a.set(TexturePosition.class, newV, tex);
		}
		
		for(V nv : newVtoOldE.keySet()) {
			E oe = newVtoOldE.get(nv);
			double[] pos = oldEtoPos.get(oe);
			double[] tex = oldEtoTex.get(oe);
			a.set(Position.class, nv, pos);
			a.set(TexturePosition.class, nv, tex);
		}
		
		return oldEtoNewEs;

	};
	
	//return new HEDS subdivided
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void dyadicSubdiv(
		HDS oldh, 
		HDS newh,
		Map<V,E> newVtoOldE,
		Map<E, Set<E>> oldEtoNewEs
	){
		//struktur kopieren
		oldh.createCombinatoriallyEquivalentCopy(newh);

		
		for(E oe : oldh.getPositiveEdges()){
			E e = newh.getEdge(oe.getIndex());	

			V mv = newh.addNewVertex();
			newVtoOldE.put(mv, oe);
			
			V et = e.getTargetVertex();
			V es = e.getStartVertex();
			E eo = e.getOppositeEdge();
			
			E e2 = newh.addNewEdge();
			E eo2 = newh.addNewEdge();	
			E en = e.getNextEdge();
			E eon = eo.getNextEdge();		
			
		//	re-link edges	
			e.setTargetVertex(mv);
			e.linkNextEdge(e2);
			e.linkOppositeEdge(eo2);
			e2.linkNextEdge(en);
			e2.setTargetVertex(et);
			eo.setTargetVertex(mv);
			eo.linkNextEdge(eo2);
			eo.linkOppositeEdge(e2);
			eo2.linkNextEdge(eon);
			eo2.setTargetVertex(es);
			
			Set<E> newEs = new HashSet<E>();
			newEs.add(e); 
			newEs.add(e2);
			
			oldEtoNewEs.put(oe, newEs);

		}
		
		
	//	end : edge cut
		
	//	rearrange interior
		for(F of : oldh.getFaces()){
			F f = newh.getFace(of.getIndex());
			List<E> e = new ArrayList<E>(0);
			List<E> eb = new ArrayList<E>(0);
			List<F> fn = newh.addNewFaces(3);
			
			e.add(f.getBoundaryEdge());

			eb.add(e.get(0).getNextEdge());
			e.add(eb.get(0).getNextEdge());
			eb.add(e.get(1).getNextEdge());
			e.add(eb.get(1).getNextEdge());
			eb.add(e.get(2).getNextEdge());
			
			List<E> inner = newh.addNewEdges(3);
			List<E> outer = newh.addNewEdges(3);
			
			for (int i=0 ; i<3; i++){
				inner.get(i).setLeftFace(f);
				inner.get(i).setTargetVertex(e.get(i).getTargetVertex());
				inner.get(i).linkOppositeEdge(outer.get(i));
				inner.get(i).linkNextEdge(inner.get((i+1)%3));
				
				e.get(i).linkNextEdge(outer.get(i));
				outer.get(i).linkNextEdge(eb.get((i+2)%3));
				outer.get(i).setTargetVertex(e.get((i+2)%3).getTargetVertex());
				e.get(i).setLeftFace(fn.get(i));
				outer.get(i).setLeftFace(fn.get(i));
				eb.get((i+2)%3).setLeftFace(fn.get(i));
				
			};
			
		}
		
	};
	
}
