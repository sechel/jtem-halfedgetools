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
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

/**
 * Catmull-Clark Subdivision
 * @author Andre Heydt
 *
 * @param <V> Vertex class
 * @param <E> Edge class
 * @param <F> Face class
 */
public class CatmullClark3 {
	
	/**
	 * Subdivides a given surface with the Catmull-Clark rule
	 * @param <HDS> 
	 * @param oldHeds the input surface
	 * @param newHeds the output surface will be overwritten
	 * @param vA a coordinates adapter
	 */
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
		Map<F, V> oldFnewVMap = new HashMap<F, V>();
		Map<E, V> oldEnewVMap = new HashMap<E, V>();
		Map<V, V> oldVnewVMap = new HashMap<V, V>();
		Map<F, double[]> oldFtoPos = new HashMap<F, double[]>();
		Map<E, double[]> oldEtoPos = new HashMap<E, double[]>();
		Map<V, double[]> oldVtoPos = new HashMap<V, double[]>();
		Map<E, Set<E>> oldEtoNewEsMap = new HashMap<E,Set<E>>();

		HalfEdgeUtilsExtra.clear(newHeds);
		
		//calc coordinates for the new points at the "old point"
		// F = average of the facebarycenters
		// R = average of the edgemidpoints
		// P = oldpoint; N = newpoint
		// deg = star.size
		// N = (F + 2*R + (deg-3)*P)/deg
		//source: http://www.idi.ntnu.no/~fredrior/files/Catmull-Clark%201978%20Recursively%20generated%20surfaces.pdf
		for (V oldV: oldHeds.getVertices()){
			
			List<E> star = HalfEdgeUtils.incomingEdges(oldV);
			int deg = star.size();
			double[] posF = new double[3];
			double[] posE = new double[3];
			double[] pos = new double [3];
			for (E e : star){
				a.setParameter("alpha", 0.5);
				a.setParameter("ignore", false);
				Rn.add(posE, a.get(BaryCenter.class, e), posE);
				a.setParameter("refEdge", e);
				Rn.add(posF, a.get(BaryCenter.class, e.getLeftFace()), posF);
			}
			a.setParameter("alpha", 1.0);
			a.setParameter("ignore", true);
			pos = a.get(BaryCenter.class, star.get(0));
			Rn.times(pos, (deg-3.0), pos);
			//Rn.times(pos, (deg-3.0)/deg, pos);
			Rn.times(posE, 2.0/deg, posE);
			Rn.times(posF, 1./deg, posF);
			
			Rn.add(pos, posE, pos);
			Rn.add(pos, posF, pos);
			
			Rn.times(pos, 1.0/deg, pos);

			oldVtoPos.put(oldV, pos);
		}
		
		//calc coordinates for the new points at "face-barycenter"
		a.setParameter("refEdge", (Object)null);
		for(F oldF : oldHeds.getFaces()) {
			double [] pos = a.get(BaryCenter.class, oldF);
			oldFtoPos.put(oldF, pos);
		}
		
		//calc coordinates for the new points at "edge-midpoint" 
		for(E oldPosE : oldHeds.getPositiveEdges()) {
			double[] pos = new double[3];
			double[] pos1 = new double[3];
			// calc with edge midpoint
			a.setParameter("alpha", 1.0);
			a.setParameter("ignore", false);
			pos = a.get(BaryCenter.class, oldPosE);
			pos1= a.get(BaryCenter.class, oldPosE.getOppositeEdge());
			F fl = oldPosE.getLeftFace();
			F fr = oldPosE.getOppositeEdge().getLeftFace();
			a.setParameter("refEdge", oldPosE);
			double[] posfl = a.get(BaryCenter.class, fl);
			a.setParameter("refEdge", oldPosE.getOppositeEdge());
			double[] posfr = a.get(BaryCenter.class, fr);
			
			Rn.add(pos, pos1, pos);
			Rn.add(pos, pos, posfl);
			Rn.add(pos, pos, posfr);
			Rn.times(pos, 1.0/4.0 , pos);

			oldEtoPos.put(oldPosE, pos);
		}
		
		ccSubdiv(oldHeds, newHeds, oldVnewVMap, oldEnewVMap, oldFnewVMap, oldEtoNewEsMap);
		
		//set coordinates for the new points <-> old points
		for(V oV : oldVnewVMap.keySet()) {
			V nV = oldVnewVMap.get(oV);
			double[] pos = oldVtoPos.get(oV);
			a.set(Position.class, nV, pos);
		}
		
		//set coordinates for the new points <-> old faces
		for(F oF : oldFnewVMap.keySet()) {
			V nV = oldFnewVMap.get(oF);
			double[] pos = oldFtoPos.get(oF);
			a.set(Position.class, nV, pos);
		}
		
		//set coordinates for the new points <-> old edges
		for(E oE : oldEnewVMap.keySet()){
			V nV = oldEnewVMap.get(oE);
			double[] pos = oldEtoPos.get(oE);
			a.set(Position.class, nV, pos);
		}
		
		boolean validSurface = HalfEdgeUtils.isValidSurface(newHeds);
		System.out.println(validSurface);
		
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
		//struktur kopieren
		oldh.createCombinatoriallyEquivalentCopy(newh);
		
		for (V oldV : oldh.getVertices()){
			V v = newh.getVertex(oldV.getIndex());
			oldVnewVMap.put(oldV, v);
		}
		
		for(E oldPosE: oldh.getPositiveEdges()){
			E e = newh.getEdge(oldPosE.getIndex());
			
			V mv = newh.addNewVertex();
			oldEnewVMap.put(oldPosE,mv);
			
			V et = e.getTargetVertex();
			V es = e.getStartVertex();
			E eo = e.getOppositeEdge();
			
			E e2 = newh.addNewEdge();
			E eo2 = newh.addNewEdge();	
			E en = e.getNextEdge();
			E eon = eo.getNextEdge();		
			
			//re-link edges	
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
			
			//temporary faces linked
			e2.setLeftFace(e.getLeftFace());
			eo2.setLeftFace(eo.getLeftFace());
			
			Set<E> newEs = new HashSet<E>();
			newEs.add(e); 
			newEs.add(e2);
			
			oldEtoNewEs.put(oldPosE, newEs);

		}
		
		for (F oldF : oldh.getFaces()){
			V mv = newh.addNewVertex();
			oldFnewVMap.put(oldF, mv);
			
			F f = newh.getFace(oldF.getIndex());
			E e = f.getBoundaryEdge();
			e= e.getNextEdge();
			
			E prevOldE= e.getPreviousEdge().getPreviousEdge();
			E nextOldE = e.getNextEdge().getNextEdge();
			
			//first quad
			E ep = e.getPreviousEdge();
			E en = newh.addNewEdge();
			E enn = newh.addNewEdge();
			V tv = ep.getStartVertex();
			
			e.linkNextEdge(en);
			en.setTargetVertex(mv);
			en.linkNextEdge(enn);
			en.setLeftFace(f);
			enn.setTargetVertex(tv);
			enn.linkNextEdge(ep);
			enn.setLeftFace(f);
			
			//opposite
			E fOppE = en;
			E lOppE = enn;
			while (nextOldE!= prevOldE){
				E tempE = nextOldE;
				nextOldE = tempE.getNextEdge().getNextEdge();
				ep = tempE.getPreviousEdge();
				en = newh.addNewEdge();
				enn = newh.addNewEdge();
				tv = ep.getStartVertex();
				F nf = newh.addNewFace(); 
				
				ep.setLeftFace(nf);
				tempE.setLeftFace(nf);
				
				tempE.linkNextEdge(en);
				en.setTargetVertex(mv);
				en.linkNextEdge(enn);
				en.setLeftFace(nf);
				enn.setTargetVertex(tv);
				enn.linkNextEdge(ep);
				enn.setLeftFace(nf);
				
				//link opposites
				enn.linkOppositeEdge(fOppE);
				fOppE = en;				
			}
			
			ep = prevOldE.getPreviousEdge();
			en = newh.addNewEdge();
			enn = newh.addNewEdge();
			tv = ep.getStartVertex();
			F nf = newh.addNewFace(); 
			
			ep.setLeftFace(nf);
			prevOldE.setLeftFace(nf);
			
			prevOldE.linkNextEdge(en);
			en.setTargetVertex(mv);
			en.linkNextEdge(enn);
			en.setLeftFace(nf);
			enn.setTargetVertex(tv);
			enn.linkNextEdge(ep);
			enn.setLeftFace(nf);
			
			enn.linkOppositeEdge(fOppE);			
			lOppE.linkOppositeEdge(en);
		}
	}
}
