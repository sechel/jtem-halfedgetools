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


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
	> E splitFaceAt(F f, V v1, V v2) {
		HalfEdgeDataStructure<V, E, F> hds = f.getHalfEdgeDataStructure();
		// edges
		E inv1 = null;
		E outv1 = null;
		for (E e : HalfEdgeUtils.incomingEdges(v1)) {
			if (e.getLeftFace() == f) {
				inv1 = e;
			}
			if (e.getRightFace() == f) {
				outv1 = e.getOppositeEdge();
			}
		}
		assert inv1 != null && outv1 != null;
		
		E inv2 = null;
		E outv2 = null;
		for (E e : HalfEdgeUtils.incomingEdges(v2)) {
			if (e.getLeftFace() == f) {
				inv2 = e;
			}
			if (e.getRightFace() == f) {
				outv2 = e.getOppositeEdge();
			}
		}
		assert inv2 != null && outv2 != null;
		
		// no degenerate split
		if (outv1 == inv2 || outv2 == inv1) {
			return null;
		}
		
		E ne1 = hds.addNewEdge();
		E ne2 = hds.addNewEdge();
		ne1.linkOppositeEdge(ne2);
		ne1.setTargetVertex(v1);
		ne2.setTargetVertex(v2);
		ne1.linkNextEdge(outv1);
		ne1.linkPreviousEdge(inv2);
		ne2.linkNextEdge(outv2);
		ne2.linkPreviousEdge(inv1);
		
		// faces
		F newface = hds.addNewFace();
		ne2.setLeftFace(f);
		E e = ne1;
		do {
			e.setLeftFace(newface);
			e = e.getNextEdge();
		} while (e != ne1);
		return ne2;
	}

	
	
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
					eopo.linkOppositeEdge(eono);				
				} else { // n-gon, n > 3
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
	
	// TODO a little tested :-)
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V collapseEdge(E e) {
		V vToKeep = e.getTargetVertex();
		return collapseEdge(e,vToKeep);	
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V collapseEdge(E e, V keep) {
		HalfEdgeDataStructure<V,E,F> graph = e.getHalfEdgeDataStructure();

		if(keep != e.getTargetVertex()) {
			e = e.getOppositeEdge();
		}
		
		E en = e.getNextEdge();
		E ep = e.getPreviousEdge();
		
		E eo = e.getOppositeEdge();
		E eon = eo.getNextEdge();
		E eop = eo.getPreviousEdge();
//		E eono = eon.getOppositeEdge();
		
		V vToRemove = e.getStartVertex();
		
		for(E ie : HalfEdgeUtils.incomingEdges(vToRemove)) {
			ie.setTargetVertex(keep);
		}
		
		ep.linkNextEdge(en);
		eop.linkNextEdge(eon);
		
		graph.removeEdge(e);
		graph.removeEdge(eo);
	
		graph.removeVertex(vToRemove);
		
		return keep;	
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F removeVertexFill(V vertex) {
		F f = null;
		if(HalfEdgeUtils.isBoundaryVertex(vertex)) {
			E be = null;
			for(E e : HalfEdgeUtils.incomingEdges(vertex)) {
				if(e.getLeftFace() == null) {
					be = e;
					break;
				}
			}
			assert be != null;
			if (be == null) {
				throw new RuntimeException("Cannot find a boundary edge in removeVertexFill()");
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
		E enew = graph.addNewEdge();
		E eonew = graph.addNewEdge();
		
		E eo = e.getOppositeEdge();
		V tv = e.getTargetVertex();
		
		E en = e.getNextEdge();
		E eop = eo.getPreviousEdge();
		
		F ef = e.getLeftFace();
		F eof = eo.getLeftFace();
		
		e.linkNextEdge(enew);
		eonew.linkNextEdge(eo);
		enew.linkNextEdge(en);
		eop.linkNextEdge(eonew);
		
		enew.setTargetVertex(tv);
		e.setTargetVertex(newV);
		eonew.setTargetVertex(newV);
		eop.setTargetVertex(tv);
		
		enew.linkOppositeEdge(eonew);
		eonew.linkOppositeEdge(enew);
		
		enew.setLeftFace(ef);
		eonew.setLeftFace(eof);
		
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

	public static <
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

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  void removeVertex(V vertex){
		try{
			HalfEdgeDataStructure<V, E, F> graph = vertex.getHalfEdgeDataStructure();
			List<E> edgeStar = HalfEdgeUtilsExtra.getEdgeStar(vertex);
			for (E e : edgeStar){
				if (e.getLeftFace() != null) {
					F f = e.getLeftFace();
					for (E be : HalfEdgeUtils.boundaryEdges(f)) {
						be.setLeftFace(null);
					}
					graph.removeFace(f);
				}
				if (e.getRightFace() != null) {
					F f = e.getRightFace();
					for (E be : HalfEdgeUtils.boundaryEdges(f)) {
						be.setLeftFace(null);
					}
					graph.removeFace(f);
				}
				
				E borderPre = e.getOppositeEdge().getNextEdge();
				E borderPost = e.getPreviousEdge();
				borderPost.linkNextEdge(borderPre);
				borderPost.setTargetVertex(e.getStartVertex());

				// remove the vertex
				graph.removeEdge(e.getOppositeEdge());
				graph.removeEdge(e);	
			}
			graph.removeVertex(vertex);
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	
	public static <
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
		if(en == e.getOppositeEdge()) {
			en = en.getNextEdge();
		}
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
