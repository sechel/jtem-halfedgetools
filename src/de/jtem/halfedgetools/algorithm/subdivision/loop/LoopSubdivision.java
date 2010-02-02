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

package de.jtem.halfedgetools.algorithm.subdivision.loop;

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
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdgeInterpolator;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;

/**
 * @author Kristoffer Josefsson, Andre Heydt 
 */
public class LoopSubdivision <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> ,
F extends Face<V, E, F>,
HEDS extends HalfEdgeDataStructure<V, E, F>>
 {

	private Map<E, double[]> oldEtoPos = new HashMap<E, double[]>();
	private HashMap<V, double[]> oldVtoPos = new HashMap<V, double[]>();
	private Map<V,E> newVtoOldE = new HashMap<V,E>();
	private Map<E, Set<E>> oldEtoNewEs = new HashMap<E,Set<E>>();
	
	
	//return new HEDS approximated using dyadic scheme
	public Map<E, Set<E>> subdivide(HEDS oldHeds, HEDS newHeds, SubdivisionVertexAdapter<V> vA, SubdivisionEdgeInterpolator<E> eA, SubdivisionFaceBarycenter<F> fA){
		
		int maxDeg = 0;
		for(V v : oldHeds.getVertices()) {
			maxDeg = Math.max(maxDeg, HalfEdgeUtils.incomingEdges(v).size());
		}
		
		
		//	Für reguläre alte Punkte p_alt -> α= 3/8
		//	Sonst α= 5/8 -(3/8 + 1/4* cos(2*π/d)
		// TODO check with a paper
		HashMap<Integer, Double> alphaMap = new HashMap<Integer, Double>();
		for(int i = 1; i <= maxDeg; i++) {
			double alpha = 0.0;
			if(i == 6)
				alpha = 3.0/8.0;
			else
				alpha = 5.0/8.0 - StrictMath.pow(3.0/8.0 + 1.0/4.0 * StrictMath.cos(2*Math.PI/i),2);
			alphaMap.put(i, alpha);
		}
		
		// Verschiebung der neuen Punkte p_neu = 1/8*(3*a+3*b+1*c+1*d)
		
		Set<E> checkE = new HashSet<E>();
		
		for(E e : oldHeds.getPositiveEdges()) {

			double[] pos = new double[] {0,0,0};
			// calc with edge midpoint
			pos = eA.getData(e, 0.5, true);
			
			// calc with original scheme
//			double[] a = eA.getData(e.getPreviousEdge(),1.0,true);
//			double[] b = eA.getData(e.getOppositeEdge().getPreviousEdge(),1.0,true);
//			double[] c = eA.getData(e.getOppositeEdge().getNextEdge(),1.0,true);
//			double[] d = eA.getData(e.getNextEdge().getOppositeEdge(),1.0,true);
//			
//			Rn.times(a,3.0,a);
//			Rn.times(b,3.0,b);
//			Rn.add(pos, b, a);
//			Rn.add(pos, c, pos);
//			Rn.add(pos, d, pos);
//			Rn.times(pos, 1.0/8.0, pos);
			
			// calc with mid of barycenters and edge midpoint
//			double[] b1 = fA.getData(e.getLeftFace());
//			double[] b2 = fA.getData(e.getRightFace());
//			double[] m = eA.getData(e, 0.5, true);
//			
//			Rn.times(b1, 3.0/8.0, b1);
//			Rn.times(b2, 3.0/8.0, b2);
//			Rn.times(m, 1.0/4.0, m);
//			
//			Rn.add(pos, b1, b2);
//			Rn.add(pos, m, pos);
			
			oldEtoPos.put(e, pos);
			
		}
		
		//	Verschiebung der alten Punkte p_alt = (1-α)p_alt+ α*m
		//	m= Mittelwert Nachbarn
		//	Falls der Grad d := #Nachbarn =6,
		//	ist der Punkt regulär.
		for(V v : oldHeds.getVertices()) {
			List<E> star = HalfEdgeUtils.incomingEdges(v);
			int deg = star.size();
			
			double[] mid = new double[]{0,0,0};
			for(E e : star) {
				Rn.add(mid, eA.getData(e,0.0, false), mid);
			}
			Rn.times(mid, 1.0 / deg, mid);	
			
			double[] newpos = new double[] {0,0,0};
			double alpha = alphaMap.get(deg);
			
			Rn.linearCombination(newpos, 1.0 - alpha, vA.getData(v), alpha, mid);
			
			oldVtoPos.put(v, newpos);			
		}
		
		dyadicSubdiv(oldHeds, newHeds);
		
		for(V ov : oldVtoPos.keySet()) {
			double[] pos = oldVtoPos.get(ov);
			V newV = newHeds.getVertex(ov.getIndex());
			vA.setData(newV, pos);
		}
		
		for(V nv : newVtoOldE.keySet()) {
			E oe = newVtoOldE.get(nv);
			double[] pos = oldEtoPos.get(oe);
			vA.setData(nv, pos);
		}
		
		
		return oldEtoNewEs;

	};
	
	//return new HEDS subdivided
	private void dyadicSubdiv(HEDS alt, HEDS neu){
		//struktur kopieren
		alt.createCombinatoriallyEquivalentCopy(neu);

		
		for(E oe : alt.getPositiveEdges()){
			E e = neu.getEdge(oe.getIndex());	

			V mv = neu.addNewVertex();
			newVtoOldE.put(mv, oe);
			
			V et = e.getTargetVertex();
			V es = e.getStartVertex();
			E eo = e.getOppositeEdge();
			
			E e2 = neu.addNewEdge();
			E eo2 = neu.addNewEdge();	
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
			newEs.add(e); newEs.add(e2);
			
			oldEtoNewEs.put(oe, newEs);

		}
		
		
	//	end : edge cut
		
	//	rearrange interior
		for(F of : alt.getFaces()){
			F f = neu.getFace(of.getIndex());
			List<E> e = new ArrayList<E>(0);
			List<E> eb = new ArrayList<E>(0);
			List<F> fn = neu.addNewFaces(3);
			
			e.add(f.getBoundaryEdge());

			eb.add(e.get(0).getNextEdge());
			e.add(eb.get(0).getNextEdge());
			eb.add(e.get(1).getNextEdge());
			e.add(eb.get(1).getNextEdge());
			eb.add(e.get(2).getNextEdge());
			
			List<E> inner = neu.addNewEdges(3);
			List<E> outer = neu.addNewEdges(3);
			
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
