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
import java.util.List;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;

/**
 * @author Kristoffer Josefsson, Andre Heydt 
 * UNTESTED!!!!!
 */
public class Sqrt3 {
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, E> subdivide(
		HDS oldHeds, 
		HDS newHeds, 
		AdapterSet a
	) {
		Map<V,F> newVtoOldF = new HashMap<V,F>();
		Map<V, double[]> oldVtoPos = new HashMap<V, double[]>();
		Map<F, double[]> oldFtoPos = new HashMap<F, double[]>();
		Map<E, E> oldEtoNewE = new HashMap<E,E>();
		// TODO
		// 1 calc new coordinates into maps
		// 2 create/change combinatorics of the new heds (including flips i.e. e.flip()	
		// 3 set new coordingate from the maps of step 1
		// 4 create and return map from old edges to set of new edges (to keep cycles)
		
		//
		
		int maxDeg = 0;
		for(V v : oldHeds.getVertices()) {
			maxDeg = Math.max(maxDeg, HalfEdgeUtils.incomingEdges(v).size());
		}		
		//	Für reguläre alte Punkte p_alt -> α= 1/3
		//	Sonst α= 2/9 *(2- cos(2*π/deg)
		// source: Leif Kobbelt 
		// http://www-i8.informatik.rwth-aachen.de/uploads/media/sqrt3.pdf
		// site 4
		HashMap<Integer, Double> alphaMap = new HashMap<Integer, Double>();
		for(int i = 1; i <= maxDeg; i++) {
			double alpha = 0.0;
			if(i == 6)
				alpha = 1.0/3.0;
			else
				alpha = 2.0/9.0*(2.0-StrictMath.cos(2.0*Math.PI / i));
			alphaMap.put(i, alpha);
		}
		
		//calc coordinates for the new points
		for(F oldF : oldHeds.getFaces()) {
			oldFtoPos.put(oldF, a.getD(Position4d.class, oldF));
		}
		
		
		//calc coordinates for the old points
		for(V v : oldHeds.getVertices()) {
			List<E> star = HalfEdgeUtils.incomingEdges(v);
			int deg = star.size();
			
			double[] mid = new double[]{0,0,0};
			for(E e : star) {
				a.setParameter("alpha", 0.0);
				a.setParameter("ignore", false);
				Rn.add(mid, a.getD(BaryCenter3d.class, e), mid);
			}
			Rn.times(mid, 1.0 / deg, mid);	
			
			double[] newpos = new double[] {0,0,0};
			double alpha = alphaMap.get(deg);
			
			Rn.linearCombination(newpos, 1.0 - alpha, a.getD(BaryCenter3d.class, v), alpha, mid);
			
			oldVtoPos.put(v, newpos);			
		}
				
		trian(oldHeds, newHeds, newVtoOldF);
		
		//mark symmetry-edges for the adapters, 
		//ATTENTION: after trian/before flip!
		for(E oldE : oldHeds.getEdges()) {
			E flipE = newHeds.getEdge(oldE.getIndex());
			oldEtoNewE.put(oldE, flipE);
			if (flipE.isPositive()) {
				flip(flipE);
			}
		}
		
		//set coordinates for the old points
		for(V ov : oldVtoPos.keySet()) {
			double[] pos = oldVtoPos.get(ov);
			V newV = newHeds.getVertex(ov.getIndex());
			a.set(Position.class, newV, pos);
		}
		
		//set coordinates for the new points
		for(V nv : newVtoOldF.keySet()) {
			F of = newVtoOldF.get(nv);
			double[] pos = oldFtoPos.get(of);
			a.set(Position.class, nv, pos);
		}		
		
		return oldEtoNewE;
	}


	// return new HEDS triangulated
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void trian (
		HDS oldh, 
		HDS newh,
		Map<V,F> newVtoOldF
	){
		oldh.createCombinatoriallyEquivalentCopy(newh);
		
		for(F of : oldh.getFaces()){
			V mv = newh.addNewVertex();
			newVtoOldF.put(mv,of);
			
			F f1 = newh.getFace(of.getIndex());		
			E e1 = f1.getBoundaryEdge();
			E e2 = e1.getNextEdge();
			E e3 = e2.getNextEdge();
			
			//first triangle
			E en1 = newh.addNewEdge();
			E enn1 = newh.addNewEdge();
			V sv = e1.getStartVertex();
			e1.linkNextEdge(en1);
			en1.setTargetVertex(mv);
			en1.linkNextEdge(enn1);
			en1.setLeftFace(f1);
			enn1.setTargetVertex(sv);
			enn1.linkNextEdge(e1);
			enn1.setLeftFace(f1);
			
			//second triangle
			F f2 = newh.addNewFace();
			E en2 = newh.addNewEdge();
			E enn2 = newh.addNewEdge();
			sv = e2.getStartVertex();
			e2.linkNextEdge(en2);
			e2.setLeftFace(f2);
			en2.setTargetVertex(mv);
			en2.linkNextEdge(enn2);
			en2.setLeftFace(f2);
			enn2.setTargetVertex(sv);
			enn2.linkNextEdge(e2);
			enn2.setLeftFace(f2);
			
			//third triangle
			F f3 = newh.addNewFace();
			E en3 = newh.addNewEdge();
			E enn3 = newh.addNewEdge();
			sv = e3.getStartVertex();
			e3.linkNextEdge(en3);
			e3.setLeftFace(f3);
			en3.setTargetVertex(mv);
			en3.linkNextEdge(enn3);
			en3.setLeftFace(f3);
			enn3.setTargetVertex(sv);
			enn3.linkNextEdge(e3);
			enn3.setLeftFace(f3);
			
			//link opposite edges
			en1.linkOppositeEdge(enn2);
			en2.linkOppositeEdge(enn3);
			en3.linkOppositeEdge(enn1);
		}
	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	>void flip (E e){
		System.out.println("flip edge " + e);
			//edges
			E e1 = e.getNextEdge();
			E e2 = e1.getNextEdge();
			E oe = e.getOppositeEdge();
			E oe1 = oe.getNextEdge();
			E oe2 = oe1.getNextEdge();
			//faces
			F f = e.getLeftFace();
			F of = oe.getLeftFace();
			//vertices
			V vs = oe1.getTargetVertex();
			V vt = e1.getTargetVertex();
			
			//face f
			oe1.setLeftFace(f);
			e.setTargetVertex(vt);
			e.linkNextEdge(e2);
			e2.linkNextEdge(oe1);
			oe1.linkNextEdge(e);
			
			//face of
			e1.setLeftFace(of);
			oe.setTargetVertex(vs);
			oe.linkNextEdge(oe2);
			oe2.linkNextEdge(e1);
			e1.linkNextEdge(oe);
	}

 }