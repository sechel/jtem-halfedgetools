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
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;

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
	> Map<E, Set<E>> subdivide(
		HDS oldHeds, 
		HDS newHeds, 
		VertexPositionCalculator vc,
		EdgeAverageCalculator ec,
		FaceBarycenterCalculator fc
	) {
		Map<V,F> newVtoOldF = new HashMap<V,F>();;
		Map<V, double[]> oldVtoPos = new HashMap<V, double[]>();;
		Map<F, double[]> oldFtoPos = new HashMap<F, double[]>();
		Map<E, Set<E>> oldEtoNewEs = new HashMap<E,Set<E>>();
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
			oldFtoPos.put(oldF, fc.get(oldF));
		}
		
		
		//calc coordinates for the old points
		for(V v : oldHeds.getVertices()) {
			List<E> star = HalfEdgeUtils.incomingEdges(v);
			int deg = star.size();
			
			double[] mid = new double[]{0,0,0};
			for(E e : star) {
				ec.setAlpha(0.0);
				ec.setIgnore(false);
				Rn.add(mid, ec.get(e), mid);
			}
			Rn.times(mid, 1.0 / deg, mid);	
			
			double[] newpos = new double[] {0,0,0};
			double alpha = alphaMap.get(deg);
			
			Rn.linearCombination(newpos, 1.0 - alpha, vc.get(v), alpha, mid);
			
			oldVtoPos.put(v, newpos);			
		}
		
		
		for(E oldE : oldHeds.getEdges()) {
			Set<E> newEs = new HashSet<E>();
			newEs.add(newHeds.getEdge(oldE.getIndex()));
			oldEtoNewEs.put(oldE, newEs);
		}
		
		trian(oldHeds, newHeds, newVtoOldF);
		
		//set coordinates for the old points
		for(V ov : oldVtoPos.keySet()) {
			double[] pos = oldVtoPos.get(ov);
			V newV = newHeds.getVertex(ov.getIndex());
			vc.set(newV, pos);
		}
		
		//set coordinates for the new points
		for(V nv : newVtoOldF.keySet()) {
			F of = newVtoOldF.get(nv);
			double[] pos = oldFtoPos.get(of);
			vc.set(nv, pos);
		}		
	
		return oldEtoNewEs;
	}


	// return new HEDS triangulated
	public  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void trian (
		HDS alt, 
		HDS neu,
		Map<V,F> newVtoOldF
	){
		alt.createCombinatoriallyEquivalentCopy(neu);
		
		for(F of : alt.getFaces()){
			V mv = neu.addNewVertex();
			newVtoOldF.put(mv,of);
			
			F f1 = neu.getFace(of.getIndex());		
			E e1 = f1.getBoundaryEdge();
			E e2 = e1.getNextEdge();
			E e3 = e2.getNextEdge();
			
			//first triangle
			E en1 = neu.addNewEdge();
			E enn1 = neu.addNewEdge();
			V sv = e1.getStartVertex();
			e1.linkNextEdge(en1);
			en1.setTargetVertex(mv);
			en1.linkNextEdge(enn1);
			en1.setLeftFace(f1);
			enn1.setTargetVertex(sv);
			enn1.linkNextEdge(e1);
			enn1.setLeftFace(f1);
			
			//second triangle
			F f2 = neu.addNewFace();
			E en2 = neu.addNewEdge();
			E enn2 = neu.addNewEdge();
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
			F f3 = neu.addNewFace();
			E en3 = neu.addNewEdge();
			E enn3 = neu.addNewEdge();
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
	
	/*// return new HEDS triangulated , edge flipped
	public HEDS trianflip (HEDS alt, HEDS neu){
	
		alt.createCombinatoriallyEquivalentCopy(neu);
		
		//koordinaten der alten punkte uebernehmen
		for(int i=0;i<alt.numVertices();i++){
			neu.getVertex(i).position = alt.getVertex(i).position.clone();
		};		

		List <V> vlist= neu.addNewVertices(neu.numFaces());
		
		int i = 0;
		for (V v : vlist) {
			F oldFace = neu.getFace(i);
			v.position = midpoint(oldFace);

			List <E> elist = HalfEdgeUtils.boundaryEdges(neu.getFace(i));
			int l = elist.size();
			int k = 1;
			
			E efirst = neu.addNewEdge();
			E elast = null;
			
			for(E e : elist){
								
				F fn  = neu.addNewFace();
				E en = neu.addNewEdge();
				if (k==l) {
					neu.removeEdge(en);
					en = efirst;
				};
				
				E en2 = neu.addNewEdge();				
				V se = e.getStartVertex();
				
				en.setTargetVertex(v);
				en.setLeftFace(fn);
				en.linkNextEdge(en2);

				e.linkNextEdge(en);
				e.setLeftFace(fn);
				//e.setTargetVertex(bleibt);
				
				en2.setTargetVertex(se);
				en2.linkNextEdge(e);
				en2.setLeftFace(fn);
				
				if (k==1) {
					en2.linkOppositeEdge(efirst);
				}
				else{
					en2.linkOppositeEdge(elast);
				}
				k++;
				elast = en;
			}
			
		neu.removeFace(oldFace);
		}
		
//		flip edges
		boolean[] done = new boolean[(alt.getEdges().size())];
		for(int j =0;j<alt.getEdges().size();j++){
				done[j]=false;
		}
		for(E oe : alt.getEdges()){
			E e= neu.getEdge(oe.getIndex());
			
			if (done[e.getIndex()]==false){
				V u=e.getStartVertex();
				V w=e.getTargetVertex();
				V a=e.getNextEdge().getTargetVertex();
				V b=e.getOppositeEdge().getNextEdge().getTargetVertex();
				E ope=e.getOppositeEdge();
				E wa =e.getNextEdge();
				E au =wa.getNextEdge();
				E ub =ope.getNextEdge();
				E bw =ub.getNextEdge();
				F fb =e.getLeftFace();
				F fa =ope.getLeftFace();
				
				e.linkNextEdge(au);
				e.setTargetVertex(a);
				ub.linkNextEdge(e);
				ub.setLeftFace(fb);
				au.linkNextEdge(ub);
				
				ope.linkNextEdge(bw);
				ope.setTargetVertex(b);
				wa.linkNextEdge(ope);
				wa.setLeftFace(fa);
				bw.linkNextEdge(wa);
														
				done[ope.getIndex()]=true;
				done[e.getIndex()]=true;
			}
		}
		
		return neu;
	}*/

	
	
	/*// return face midpoint position
	private static double[] midpoint(F f){
	
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		double[] mid = new double[3];
		for (E e : b) {
			double[] pos = e.getTargetVertex().position.clone();
			Rn.add(mid, pos, mid);
		}
		return Rn.times(mid, 1.0 / b.size(), mid);
	}*/


	/*//return HEDS neu with vertices from oldheds moved towards 'average'
	private HEDS moveOldVert(V oldv, HEDS neu, double alpha){
			
		//alte nachbarn bestimmen
			List <V> nbs = HalfEdgeUtils.neighboringVertices(oldv);				
		//deren mittelwert berechnen
			double[] mid = new double[]{0,0,0};
			for (V nb : nbs){
				Rn.add(mid, nb.position, mid); 
			};
			Rn.times(mid, 1.0 / nbs.size(), mid);	
		//neue position berechnen
			double[] newpos = new double[3];			
			Rn.linearCombination(newpos, 1 - alpha, oldv.position, alpha, mid);
		//position in neuer heds zuweisen
			neu.getVertex(oldv.getIndex()).position=newpos;
			
		return neu;
	}*/
	
	
	/*//return new HEDS subdivided using trian & Kobbelt scheme
	public HEDS root3 (HEDS oldheds){
		
		//struktur kopieren
		HEDS neu = oldheds.createCombinatoriallyEquivalentCopy(new HEDS());
		for(int i=0;i<oldheds.numVertices();i++){
			neu.getVertex(i).position = oldheds.getVertex(i).position.clone();
		};

		neu = trianflip(neu);		
		for (V v : oldheds.getVertices()){
			neu = moveOldVert(v,neu, alphaKobbelt(v,HalfEdgeUtils.neighboringVertices(v).size()));	
			}
						
		return neu;
		
	};*/
	

 }