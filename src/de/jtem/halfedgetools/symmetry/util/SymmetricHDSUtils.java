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

package de.jtem.halfedgetools.symmetry.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public  class SymmetricHDSUtils
 {
	public static 
	<V extends SymmetricVertex<V,E,F>,
	E extends SymmetricEdge<V,E,F>,
	F extends SymmetricFace<V,E,F>
	> double[] getAverageFaceCurvatureVector(V v) {

		double[] normal = new double[3];
		double m = 0.0;
		List<F> faces = HalfEdgeUtils.facesIncidentWithVertex(v);
		for (F f : faces) {

			// calc n extrinsically
			List<E> boundary = HalfEdgeUtilsExtra.getBoundary(f);
			E e1 = boundary.get(0);
			E e2 = boundary.get(1);

			double[] v1 = e1.getDirection();
			double[] v2 = e2.getDirection();
			double[] n = Rn.crossProduct(null, v1, v2);

			// check if ok
			Rn.normalize(n, n);
			// n.cross(e1.getDirection(), e2.getDirection());
			// n.normalize();

			Rn.add(normal, normal, n);
			m += 1.0;

		}

		Rn.times(normal, 1/m, normal);
		return normal;
//		return Rn.normalize(null, normal);
	}
	
	// FIXME rewrite with getDirecton() and check for cones/boundary
	public static 
	<V extends SymmetricVertex<V,E,F>,
	E extends SymmetricEdge<V,E,F>,
	F extends SymmetricFace<V,E,F>
	> double getExtrinsicSurfaceArea(SymmetricHDS<V,E,F> heds) {
		double a = 0.0;
		
//		for (F f : heds.getFaces()) {
//		
//			E e1 = f.getBoundaryEdge();
//			E e2 = e1.getNextEdge();
//			
//			double[] n = new double[3];
//			
//			RVertex vv1 = intrinsicT.getVertex(e1.getStartVertex().getIndex());
//			RVertex vv2 = intrinsicT.getVertex(e1.getTargetVertex().getIndex());
//			RVertex vv3 = intrinsicT.getVertex(e2.getTargetVertex().getIndex());
//			
//			if (iConeVertices.contains(vv1) || iConeVertices.contains(vv2)
//					 || iConeVertices.contains(vv3)) {
//				// nothing because cone face
//			} else {
//			
//				double[] v1 = e1.getDirection();
//				double[] v2 = e1.getDirection();
//			
//				Rn.crossProduct(n, v1, v2);
//			
//				a += Rn.euclideanNorm(n);
//			}
//		}
//		System.err.println("area is: " + a*0.5);
//		return a * 0.5;
		
		return a;
	}
	
	// FIXME rewrite with getDirection()
	public static 
	<V extends SymmetricVertex<V,E,F>,
	E extends SymmetricEdge<V,E,F>,
	F extends SymmetricFace<V,E,F>
	> double getArea(F f) {
	double a = 0.0;

	double[] n = new double[3];
	E e1 = f.getBoundaryEdge();
	E e2 = e1.getNextEdge();
	V vv1 = e1.getStartVertex();
	V vv2 = e1.getTargetVertex();
	V vv3 = e2.getTargetVertex();
	double[] o = vv1.getEmbedding();
	double[] v1 = vv2.getEmbedding();
	double[] v2 = vv3.getEmbedding();

	double[] w1 = Rn.subtract(null, v1, o);
	double[] w2 = Rn.subtract(null, v2, o);

	Rn.crossProduct(n, w1, w2);

	a = Rn.euclideanNorm(n);
	
	return 0.5*a;
}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Set<E> mirrorFacesAlongCycle(F f1, F f2, E e1, E e2) {
		
//		HalfEdgeDataStructure<V,E,F> heds = f1.getHalfEdgeDataStructure();
		
		Set<E> cycle = new HashSet<E>();
		
		List<E> b1 = HalfEdgeUtils.boundaryEdges(f1);
		List<E> b2 = HalfEdgeUtils.boundaryEdges(f2);
		
		for(E e : b1) {
			cycle.add(e);
		}
		
		if(b1.size() != b2.size()) {
			System.err.println("Faces do not have matching boundaries.");
		}
		
		if(!b1.contains(e1) || !b2.contains(e2)) {
			System.err.println("Specified edges not contined in given boundaries");
		}
		
//		List<V> vsToRemove = new ArrayList<V>();
//		List<E> esToRemove = new ArrayList<E>();
//		
//		HashMap<E,E> esToLink = new LinkedHashMap<E,E>();
//		HashMap<E,E> e1OpPrevs = new LinkedHashMap<E,E>();
//		HashMap<E,E> e2OpPrevs = new LinkedHashMap<E,E>();
//		HashMap<E,E> e1OpNexts = new LinkedHashMap<E,E>();
//		HashMap<E,E> e2OpNexts = new LinkedHashMap<E,E>();
//		
//		HashMap<E,F> e1OpFaces = new LinkedHashMap<E,F>();
//		HashMap<E,F> e2OpFaces = new LinkedHashMap<E,F>();
//		
//		HashMap<List<E>, V> toRetarget = new LinkedHashMap<List<E>, V>();
//		
//		List<E> edgesToReturn = new LinkedList<E>();
		
		return null;
	}

	// TODO 1) also allow for two different HEDS
		// TODO 2) allow arbitrary (non-closed boundaries) talk to Stefan...
		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Set<E> glueFacesAlongCycle(F f1, F f2, E e1, E e2) {
			
			HalfEdgeDataStructure<V,E,F> heds = f1.getHalfEdgeDataStructure();
			
			Set<E> cycle = new HashSet<E>();
			
			List<E> b1 = HalfEdgeUtils.boundaryEdges(f1);
			List<E> b2 = HalfEdgeUtils.boundaryEdges(f2);
			
			for(E e : b1) {
				cycle.add(e);
			}
			
			if(b1.size() != b2.size()) {
				System.err.println("Faces do not have matching boundaries.");
			}
			
			if(!b1.contains(e1) || !b2.contains(e2)) {
				System.err.println("Specified edges not contined in given boundaries");
			}
			
			List<V> vsToRemove = new ArrayList<V>();
			List<E> esToRemove = new ArrayList<E>();
			
			HashMap<E,E> esToLink = new LinkedHashMap<E,E>();
			HashMap<E,E> e1OpPrevs = new LinkedHashMap<E,E>();
			HashMap<E,E> e2OpPrevs = new LinkedHashMap<E,E>();
			HashMap<E,E> e1OpNexts = new LinkedHashMap<E,E>();
			HashMap<E,E> e2OpNexts = new LinkedHashMap<E,E>();
			
			HashMap<E,F> e1OpFaces = new LinkedHashMap<E,F>();
			HashMap<E,F> e2OpFaces = new LinkedHashMap<E,F>();
			
			HashMap<List<E>, V> toRetarget = new LinkedHashMap<List<E>, V>();
			
			List<E> edgesToReturn = new LinkedList<E>();
			
			int n = b1.size();
			int i = 0;
			while(i < n) {
	//			System.err.println("i is " + i);
				E e2Next = e2.getNextEdge();
				E e1Prev = e1.getPreviousEdge();
				V vToRemove = e2.getTargetVertex();
				vsToRemove.add(vToRemove);
				V vToKeep = e1.getStartVertex();
				
	//			System.err.println("Vertex " + vToRemove + " will be replaced by " + vToKeep);
				
				List<E> eToRet = new LinkedList<E>();
				for(E in : HalfEdgeUtils.incomingEdges(vToRemove)) {
					eToRet.add(in);
					if(in != e2) {
						edgesToReturn.add(in);
					}
				}
				toRetarget.put(eToRet, vToKeep);
	
				
				e1OpPrevs.put(e1, e1.getOppositeEdge().getPreviousEdge());
				e2OpPrevs.put(e2, e2.getOppositeEdge().getPreviousEdge());
				
				e1OpNexts.put(e1, e1.getOppositeEdge().getNextEdge());
				e2OpNexts.put(e2, e2.getOppositeEdge().getNextEdge());
				
				e1OpFaces.put(e1, e2.getOppositeEdge().getLeftFace());
				e2OpFaces.put(e2, e1.getOppositeEdge().getLeftFace());
				
				esToRemove.add(e2.getOppositeEdge());
				esToRemove.add(e1.getOppositeEdge());
				
				esToLink.put(e1, e2);
				
				e2 = e2Next;
				e1 = e1Prev;
				
				i++;
			}
			
			for(List<E> list : toRetarget.keySet()) {
				for(E e : list) {
					e.setTargetVertex(toRetarget.get(list));
				}
			}
			
			for(E ee1 : esToLink.keySet()) {
				E ee2 = esToLink.get(ee1);
				
				e1OpPrevs.get(ee1).linkNextEdge(ee2);
				e2OpPrevs.get(ee2).linkNextEdge(ee1);
				
				ee2.linkNextEdge(e1OpNexts.get(ee1));
				ee1.linkNextEdge(e2OpNexts.get(ee2));
	
				ee1.setLeftFace(e1OpFaces.get(ee1));
				ee2.setLeftFace(e2OpFaces.get(ee2));
				
				ee1.linkOppositeEdge(ee2);
	
				
			}
			
			for(V v : vsToRemove) {
				heds.removeVertex(v);
			}
			
			for(E e : esToRemove) {
				heds.removeEdge(e);
			}
			
			heds.removeFace(f1);
			heds.removeFace(f2);
			
			return cycle;
			
		}

}
