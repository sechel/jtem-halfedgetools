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

package de.jtem.halfedgetools.algorithm.topology;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class TopologyAlgorithms {

	public static <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> V collapseFace(F face) {
		
		HalfEdgeDataStructure<V,E,F> hds = face.getHalfEdgeDataStructure();
	
		V w = hds.addNewVertex();
	
		List<E> boundary = HalfEdgeUtils.boundaryEdges(face);
		
		List<V> vsToRemove = HalfEdgeUtils.boundaryVertices(face);
		List<E> esToRemove = new LinkedList<E>();
		List<F> fsToRemove = new LinkedList<F>();
		fsToRemove.add(face);
		esToRemove.addAll(boundary);
		
		List<E> incomingEdges = new LinkedList<E>();
		for(V v : vsToRemove) {
			incomingEdges.addAll(HalfEdgeUtils.incomingEdges(v));
		}
		
		for(E e : boundary) {
			E eo = e.getOppositeEdge();
			E eon = eo.getNextEdge();
			E eop = eo.getPreviousEdge();
			F erf = e.getRightFace();
			
			if(erf == null) { // TODO: handle boundary case
				esToRemove.add(eo);
			} else {
				
				int n = HalfEdgeUtils.boundaryEdges(erf).size();
				
				if(n <= 3) { // triangle
					fsToRemove.add(erf);
					esToRemove.add(eo);
					esToRemove.add(eon);
					esToRemove.add(eop);
					
					E eopo = eop.getOppositeEdge();
					E eono = eon.getOppositeEdge();
					eopo.linkOppositeEdge(eono);				} else { // n-gon, n > 3
					esToRemove.add(eo);
					eop.linkNextEdge(eon);
					eop.setTargetVertex(w);
				}
				
			}
			
		}
		
		for(E e : incomingEdges) {
			e.setTargetVertex(w);
		}
		
		for(E e : esToRemove) {
			hds.removeEdge(e);
		}
		for(F f : fsToRemove) {
			hds.removeFace(f);
		}
		for(V v : vsToRemove) {
			hds.removeVertex(v);
		}
		
		return w;
		
	}
	
	// TODO untested
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V collapseEdge(E e) {
		
		HalfEdgeDataStructure<V,E,F> graph = e.getHalfEdgeDataStructure();
		
		E en = e.getNextEdge();
		E ep = e.getPreviousEdge();
		
		E eo = e.getOppositeEdge();
		E eon = eo.getNextEdge();
		E eop = eo.getPreviousEdge();
		E eono = eon.getOppositeEdge();
		
		V vToKeep = e.getTargetVertex();
		V vToRemove = e.getStartVertex();
		
		ep.linkNextEdge(en);
		eop.linkNextEdge(eon);
		
		ep.setTargetVertex(vToKeep);
		eono.setTargetVertex(vToKeep);
		
		graph.removeEdge(e);
		graph.removeEdge(eo);
	
		graph.removeVertex(vToRemove);
		
		return vToKeep;
		
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F collapseVertex(V vertex) {
		F f = null;
		if(HalfEdgeUtils.isBoundaryVertex(vertex)) {
			E be = null;
			for(E e : HalfEdgeUtils.incomingEdges(vertex)) {
				if(e.getLeftFace() == null) {
					be = e;
					break;
				}
			}
			V v1 = be.getStartVertex();
			V v2 = be.getNextEdge().getTargetVertex();
			E e1int = be.getOppositeEdge().getNextEdge();
			E e2int = be.getNextEdge().getOppositeEdge().getPreviousEdge();
			E e1bd = be.getPreviousEdge();
			E e2bd = be.getNextEdge().getNextEdge();
			
			HalfEdgeDataStructure<V, E, F> hds = be.getHalfEdgeDataStructure();

			removeVertex(vertex);

			E e12 = hds.addNewEdge();
			E e21 = hds.addNewEdge();
			
			e12.linkOppositeEdge(e21);
			
			e21.linkNextEdge(e1int);
			e21.linkPreviousEdge(e2int);
			e21.setTargetVertex(v1);
			
			e12.linkNextEdge(e2bd);
			e12.linkPreviousEdge(e1bd);
			e12.setTargetVertex(v2);
			
			f = HalfEdgeUtils.fillHole(e21);
		} else {
			E e = vertex.getIncomingEdge().getPreviousEdge();
			removeVertex(vertex);
			f = HalfEdgeUtils.fillHole(e);
		}
		return f;
	}
	

	// FIXME something seems to be wrong with the order of the linking?
	// possible solution: removeVertex() and then fillHole()
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F collapseVertexBug(V vertex) {
		
		F f = null;
		
		try{
			HalfEdgeDataStructure<V, E, F> graph = vertex.getHalfEdgeDataStructure();
			f = graph.addNewFace();
			List<E> edgeStar = HalfEdgeUtilsExtra.getEdgeStar(vertex);
			
			List<E> esToReface = new LinkedList<E>();
			List<E> esToDelete = new LinkedList<E>();
			HashMap<E,E> esToLinkNext = new HashMap<E,E>();
			
			for (E e : edgeStar){
				E borderPre = e.getOppositeEdge().getNextEdge();
				E borderPost = e.getPreviousEdge();
				for(E epre : HalfEdgeUtils.boundaryEdges(borderPre.getLeftFace())) {
					esToReface.add(epre);
				}
				for(E epost : HalfEdgeUtils.boundaryEdges(borderPost.getLeftFace())) {
					esToReface.add(epost);
				}
				esToLinkNext.put(borderPost, borderPre);
				esToDelete.add(e);
				esToDelete.add(e.getOppositeEdge());

			}
			
			for(E e : esToLinkNext.keySet()) {
				e.linkNextEdge(esToLinkNext.get(e));
			}
			
			while(esToReface.size() > 0) {
				esToReface.get(0).setLeftFace(f);
				esToReface.remove(0);
			}
			
			while(esToDelete.size() > 0) {
				graph.removeEdge(esToDelete.get(0));
				esToDelete.remove(0);
			}
			graph.removeVertex(vertex);
		} catch (Exception e){
			e.printStackTrace();
	}
		
	return f;
		
	}
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>
	> V splitFace(F f) {
		
		// maybe not the fastet but quick to code!
		F nf = scaleFace(f);
		V nv = collapseFace(nf);
		return nv;
	}

	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>
	> V barycentricSubdFace(F f) {
		for(E e : HalfEdgeUtils.boundaryEdges(f)) {
			splitEdge(e);
		}
		V nv = splitFace(f);
		return nv;
	}

	
	
	// TODO UNTESTED
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V splitEdge(E e) {
		
		HalfEdgeDataStructure<V,E,F> graph = e.getHalfEdgeDataStructure();
		
		V newV = graph.addNewVertex();
		E en = graph.addNewEdge();
		E eon = graph.addNewEdge();
		
		E eo = e.getOppositeEdge();
		V et = e.getTargetVertex();
		V es = e.getStartVertex();
		
		E enn = e.getNextEdge();
		E eonn = eo.getNextEdge();
		E ep = e.getPreviousEdge();
		E eop = eo.getPreviousEdge();
		
		F fl = e.getLeftFace();
		F fr = eo.getLeftFace();
		
		e.linkNextEdge(en);
		en.linkNextEdge(enn);
		ep.linkNextEdge(e);
		eo.linkNextEdge(eon);
		eon.linkNextEdge(eonn);
		eop.linkNextEdge(eo);
		
		e.setTargetVertex(newV);
		eo.setTargetVertex(newV);
		en.setTargetVertex(et);
		eon.setTargetVertex(es);
		
		e.linkOppositeEdge(eon);
		eo.linkOppositeEdge(en);
		
		e.setLeftFace(fl);
		en.setLeftFace(fl);
		eo.setLeftFace(fr);
		eon.setLeftFace(fr);
		
		
		return newV;
	}
	
	public  <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> void split(E e) {
	       F leftFace = e.getLeftFace();
	       F rightFace = e.getRightFace();
	       // dont split if we are the boundary
	       // TODO: implement for the boundary
	       if (leftFace == null || rightFace == null)
	               return;
	       
	       HalfEdgeDataStructure<V,E,F> graph = e.getHalfEdgeDataStructure();

	       V E = graph.addNewVertex();
	       E ne1 = graph.addNewEdge();
	       E ne2 = graph.addNewEdge();
	       E ne3 = graph.addNewEdge();
	       E ne4 = graph.addNewEdge();
	       E ne5 = graph.addNewEdge();
	       E ne6 = graph.addNewEdge();

	       F f3 = graph.addNewFace();
	       F f4 = graph.addNewFace();

	       E o = e.getOppositeEdge();
	       E a = e.getNextEdge();
	       E b = a.getNextEdge();
	       E c = o.getNextEdge();
	       E d = c.getNextEdge();
	       F f1 = e.getLeftFace();
	       F f2 = o.getLeftFace();

	       V A = e.getTargetVertex();
	       V B = a.getTargetVertex();
	       V C = b.getTargetVertex();
	       V D = c.getTargetVertex();

	       // face 1
	       b.linkNextEdge(e);
	       e.linkNextEdge(ne4);
	       ne4.linkNextEdge(b);
	       b.setLeftFace(f1);
	       e.setLeftFace(f1);
	       ne4.setLeftFace(f1);
	       e.linkOppositeEdge(ne1);
	       ne4.linkOppositeEdge(ne3);

	       // face 2
	       ne1.linkNextEdge(c);
	       c.linkNextEdge(ne6);
	       ne6.linkNextEdge(ne1);
	       ne1.setLeftFace(f2);
	       c.setLeftFace(f2);
	       ne6.setLeftFace(f2);
	       ne6.linkOppositeEdge(ne5);

	       // face 3
	       o.linkNextEdge(ne5);
	       ne5.linkNextEdge(d);
	       d.linkNextEdge(o);
	       o.setLeftFace(f3);
	       ne5.setLeftFace(f3);
	       d.setLeftFace(f3);
	       o.linkOppositeEdge(ne2);

	       // face 4
	       ne2.linkNextEdge(a);
	       a.linkNextEdge(ne3);
	       ne3.linkNextEdge(ne2);
	       ne2.setLeftFace(f4);
	       a.setLeftFace(f4);
	       ne3.setLeftFace(f4);

	       // target vertex
	       b.setTargetVertex(C);
	       e.setTargetVertex(E);
	       ne4.setTargetVertex(B);

	       c.setTargetVertex(D);
	       ne6.setTargetVertex(E);
	       ne1.setTargetVertex(C);

	       d.setTargetVertex(A);
	       o.setTargetVertex(E);
	       ne5.setTargetVertex(D);

	       a.setTargetVertex(B);
	       ne3.setTargetVertex(E);
	       ne2.setTargetVertex(A);

	   }
	
	  // @SuppressWarnings("unchecked")
	   public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  V collapse(E e) {

	       F leftFace = e.getLeftFace();
	       F rightFace = e.getRightFace();
	       // dont collapse if we are the boundary
	       // TODO: implement for the boundary
	       if (leftFace == null || rightFace == null)
	               return null;

	       HalfEdgeDataStructure<V,E,F> heds = e.getHalfEdgeDataStructure();

	       E o = e.getOppositeEdge();
	       E a = e.getNextEdge();
	       E b = a.getNextEdge();
	       E c = o.getNextEdge();
	       E d = c.getNextEdge();

	       E oa = a.getOppositeEdge();
	       E ob = b.getOppositeEdge();
	       E oc = c.getOppositeEdge();
	       E od = d.getOppositeEdge();

	       F f1 = e.getLeftFace();
	       F f2 = o.getLeftFace();

	       V A = e.getTargetVertex();
	       V C = b.getTargetVertex();
//	       V B = a.getTargetVertex();
//	       V D = c.getTargetVertex();

	       // get edges with target C, those have to be relinked
	       LinkedList<E> cTargetEdges = new LinkedList<E>();
	       E actEdge = o;

	       while (actEdge != oc){
	           actEdge = actEdge.getOppositeEdge().getPreviousEdge();
	           cTargetEdges.add(actEdge);
	       }

	       // remove vertices
	       heds.removeEdge(a);
	       heds.removeEdge(b);
	       heds.removeEdge(c);
	       heds.removeEdge(d);
	       heds.removeEdge(e);
	       heds.removeEdge(o);
	       heds.removeFace(f1);
	       heds.removeFace(f2);
	       heds.removeVertex(C);
//	       A.setConnectedEdge(oa);
//	       B.setConnectedEdge(ob);
//	       D.setConnectedEdge(od);

	       // relink
	       oa.linkOppositeEdge(ob);
	       od.linkOppositeEdge(oc);

	       for (E ee : cTargetEdges)
	           if (ee.isValid())
	               ee.setTargetVertex(A);

	       return A;
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

	
		// TODO 1) also allow for two different HEDS
		// TODO 2) allow arbitrary (non-closed boundaries) talk to Stefan...
		public static <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> List<E> glueFaces(F f1, F f2, E e1, E e2) {
			
			HalfEdgeDataStructure<V,E,F> heds = f1.getHalfEdgeDataStructure();
			
			List<E> b1 = HalfEdgeUtils.boundaryEdges(f1);
			List<E> b2 = HalfEdgeUtils.boundaryEdges(f2);
			
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
			
			List<E> toRetEdges = new LinkedList<E>();
			
			int n = b1.size();
			int i = 0;
			while(i < n) {
	//			System.err.println("i is " + i);
				E e2Next = e2.getNextEdge();
				E e1Prev = e1.getPreviousEdge();
				V vToRemove = e2.getTargetVertex();
				vsToRemove.add(vToRemove);
				V vToKeep = e1.getStartVertex();
				
				System.err.println("Vertex " + vToRemove + " will be replaced by " + vToKeep);
				
				List<E> eToRet = new LinkedList<E>();
				for(E in : HalfEdgeUtils.incomingEdges(vToRemove)) {
					eToRet.add(in);
					if(in != e2) {
						toRetEdges.add(in);
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
			
			return toRetEdges;
			
		}

	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void removeAllFaces(HalfEdgeDataStructure<V, E, F> graph){
		while (graph.numFaces() > 0){
			F face = graph.getFace(0);
			graph.removeFace(face);
		}
	}

	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void removeAllEdges(HalfEdgeDataStructure<V, E, F> graph){
		while (graph.numEdges() > 0){
			E edge = graph.getEdge(0);
			graph.removeEdge(edge);
		}
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void removeFace(F face){
		HalfEdgeDataStructure<V, E, F> graph = face.getHalfEdgeDataStructure();
		List<E> boundary = null;
		try {
			boundary  = HalfEdgeUtilsExtra.getBoundary(face);
			for (E e : boundary) {
				e.setLeftFace(null);
			}
		} catch (Exception e) {}
		graph.removeFace(face);
	}

	public static 	
		<
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		>  void removeVertex(V vertex){
		
			try{
				HalfEdgeDataStructure<V, E, F> graph = vertex.getHalfEdgeDataStructure();
				List<E> edgeStar = HalfEdgeUtilsExtra.getEdgeStar(vertex);
				for (E e : edgeStar){
					E borderPre = e.getOppositeEdge().getNextEdge();
					E borderPost = e.getPreviousEdge();
					borderPost.linkNextEdge(borderPre);
					borderPost.setTargetVertex(e.getStartVertex());
					
					if (e.getLeftFace() != null) {
						graph.removeFace(e.getLeftFace());
					}
					if (e.getRightFace() != null) {
						graph.removeFace(e.getRightFace());
					}

					// remove the vertex
					graph.removeEdge(e.getOppositeEdge());
					graph.removeEdge(e);	
				}
				graph.removeVertex(vertex);
			} catch (Exception e){
				e.printStackTrace();
			}
		}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void removeEdge(E edge){
		HalfEdgeDataStructure<V, E, F> graph = edge.getHalfEdgeDataStructure();
		if (edge.getLeftFace() != null) {
			graph.removeFace(edge.getLeftFace());
		}
		if (edge.getRightFace() != null) {
			graph.removeFace(edge.getRightFace());
		}

		edge.getPreviousEdge().linkNextEdge(edge.getOppositeEdge().getNextEdge());

		edge.getOppositeEdge().getPreviousEdge().linkNextEdge(edge.getNextEdge());
		// remove the vertex
		graph.removeEdge(edge.getOppositeEdge());
		graph.removeEdge(edge);	
	}
	
	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F removeEdgeFill(E e){
		E en = e.getNextEdge();
		removeEdge(e);
		return HalfEdgeUtils.fillHole(en);
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F scaleFace(F f) {
		HalfEdgeDataStructure<V, E, F> graph = f.getHalfEdgeDataStructure();
		
		List<E> Ns = new LinkedList<E>();
		List<E> Ps = new LinkedList<E>();
		List<E> OOs = new LinkedList<E>();
		
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		int n = boundary.size();
		
		F midFace = graph.addNewFace();
		
		for(E e : boundary) {
			 E eN = graph.addNewEdge();
			 E eP = graph.addNewEdge();
			 E eO = graph.addNewEdge();
			 E eOO = graph.addNewEdge();
			 
			 F ff = graph.addNewFace();
			 
			 V vN = graph.addNewVertex();
			 
			 V vS = e.getStartVertex();
			 
			 eN.linkNextEdge(eO); eO.linkNextEdge(eP); eP.linkNextEdge(e); e.linkNextEdge(eN);
			 
			 e.setLeftFace(ff); eN.setLeftFace(ff); eP.setLeftFace(ff); eO.setLeftFace(ff);
			 
			 eO.linkOppositeEdge(eOO);
			 eOO.setLeftFace(midFace);
			 
			 eN.setTargetVertex(vN); 
			 eP.setTargetVertex(vS);
			 
			 Ns.add(eN); Ps.add(eP); OOs.add(eOO);
			 
		}
		
		int i = 0;
		for(E en : Ns) {
			E eno = Ps.get((i+1)%n);
			en.linkOppositeEdge(eno);
			i++;
		}
		
		for(E eoo : OOs) {
			E eo = eoo.getOppositeEdge();
			
			V vt = eo.getNextEdge().getOppositeEdge().getTargetVertex();
			eoo.getOppositeEdge().setTargetVertex(vt);
			
			E eoon = eo.getPreviousEdge().getOppositeEdge().getPreviousEdge().getOppositeEdge();
			eoo.linkNextEdge(eoon);
			
			eoo.setTargetVertex(eo.getPreviousEdge().getTargetVertex());
		}
		
		graph.removeFace(f);
		return midFace;
		
	}

	public static  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void flipEdge(E e) {
			F leftFace = e.getLeftFace();
			F rightFace = e.getRightFace();
			if (leftFace == rightFace) {
				System.err.println("leftFace == rightFace");
				return;
			}
				
			E a1 = e.getOppositeEdge().getNextEdge();
			E a2 = a1.getNextEdge();
			E b1 = e.getNextEdge();
			E b2 = b1.getNextEdge();
			
			V v3 = a1.getTargetVertex();
			V v4 = b1.getTargetVertex();

			//new connections
			e.linkNextEdge(a2);
			e.linkPreviousEdge(b1);
			e.getOppositeEdge().linkNextEdge(b2);
			e.getOppositeEdge().linkPreviousEdge(a1);
			e.setTargetVertex(v3);
			e.getOppositeEdge().setTargetVertex(v4);
			
			a2.linkNextEdge(b1);
			b2.linkNextEdge(a1);
			
			//set faces
			b2.setLeftFace(rightFace);
			a2.setLeftFace(leftFace);
			
			
		}
	

}
