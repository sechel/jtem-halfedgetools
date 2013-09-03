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

package de.jtem.halfedgetools.algorithm.triangulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.jreality.math.P2;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;

public class Triangulator {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> List<E> triangulateSingleSource(HDS hds) {
		List<E> newEdges = new ArrayList<E>();
		List<F> fList = new LinkedList<F>(hds.getFaces());
		for (F f : fList) {
			newEdges.addAll(triangulateSingleSource(f));
		}
		return newEdges;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> triangulateSingleSource(F f) {
		HalfEdgeDataStructure<V, E, F> hds = f.getHalfEdgeDataStructure();
		List<E> newEdges = new ArrayList<E>();
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		if (b.size() == 3) {
			return newEdges;
		} 
		if (b.size() < 3) {
			throw new RuntimeException("Face boundary with less than 3 edges not allowed in triangulate()");
		}
		E last = f.getBoundaryEdge();
		E first = last.getPreviousEdge();
		E opp = last.getNextEdge();
		V v0 = last.getStartVertex();
		while (opp.getNextEdge() != first) {
			E nextOpp = opp.getNextEdge();
			E newEdge = hds.addNewEdge();
			E newEdgeOpp = hds.addNewEdge();
			newEdge.linkOppositeEdge(newEdgeOpp);
			newEdge.linkNextEdge(last);
			newEdgeOpp.linkNextEdge(nextOpp);
			newEdgeOpp.linkPreviousEdge(first);
			opp.linkNextEdge(newEdge);

			V v2 = opp.getTargetVertex();
			newEdge.setTargetVertex(v0);
			newEdgeOpp.setTargetVertex(v2);
			
			newEdges.add(newEdge);
			newEdges.add(newEdgeOpp);
			
			F newFace = hds.addNewFace();
			last.setLeftFace(newFace);
			opp.setLeftFace(newFace);
			newEdge.setLeftFace(newFace);
			newEdgeOpp.setLeftFace(f);
			
			opp = nextOpp;
			last = newEdgeOpp;
		}
		return newEdges;
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> List<E> triangulateByCuttingCorners(HDS hds, AdapterSet a) {
		List<E> newEdges = new ArrayList<E>();
		List<F> fList = new LinkedList<F>(hds.getFaces());
		for (F f : fList) {
			newEdges.addAll(triangulateByCuttingCorners(f, a));
		}
		return newEdges;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> triangulateByCuttingCorners(F f, AdapterSet a) {
		HalfEdgeDataStructure<V, E, F> hds = f.getHalfEdgeDataStructure();
		List<E> newEdges = new ArrayList<E>();
		int numFaces = hds.numFaces();
		int numVerts = HalfEdgeUtils.boundaryVertices(f).size();
		for (int i = 0; i < numVerts - 3; i++) {
			E newEdge = cutCorner(f, a);
			newEdges.add(newEdge);
			newEdges.add(newEdge.getOppositeEdge());
		}
		numVerts = HalfEdgeUtils.boundaryVertices(f).size();
		assert numVerts == 3 : "the input face is triangle";
		assert numFaces + numVerts - 3 == hds.numFaces() : "number of faces has increased by the number of face vertices - 3";
		return newEdges;
	}
	
	static <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> boolean isDegenerate(F f, AdapterSet a) {
		double EPS = 1E-5;
		for (E be : HalfEdgeUtils.boundaryEdges(f)) {
			V v1 = be.getStartVertex();
			V v2 = be.getTargetVertex();
			V v3 = be.getNextEdge().getTargetVertex();
			double[] p1 = a.getD(Position3d.class, v1);
			double[] p2 = a.getD(Position3d.class, v2);
			double[] p3 = a.getD(Position3d.class, v3);
//			System.err.println("vertices = "+Rn.toString(new double[][]{p1,p2,p3}));
			double[] vec1 = Rn.subtract(null, p1, p2);
			double[] vec2 = Rn.subtract(null, p3, p2);
			double[] cross = Rn.crossProduct(null, vec1, vec2);
			double cl = Rn.euclideanNorm(cross);
			if (cl > EPS) {
				return false;
			}
		}
		return true;
	}

	static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> E cutCorner(F f, AdapterSet a) {
//		System.err.println("Cutting corner "+f.getIndex());
		double EPS = 1E-5;
		E cornerEdge = null;
		List<E> bedges = HalfEdgeUtils.boundaryEdges(f);
		for (E be : bedges) {
			V v1 = be.getStartVertex();
			V v2 = be.getTargetVertex();
			V v3 = be.getNextEdge().getTargetVertex();
			double[] p1 = a.getD(Position3d.class, v1);
			double[] p2 = a.getD(Position3d.class, v2);
			double[] p3 = a.getD(Position3d.class, v3);
//			System.err.println("vertices = "+Rn.toString(new double[][]{p1,p2,p3}));
			double[] vec1 = Rn.subtract(null, p1, p2);
			double[] vec2 = Rn.subtract(null, p3, p2);
			double[] cross = Rn.crossProduct(null, vec1, vec2);
			double cl = Rn.euclideanNorm(cross);
			if (cl > EPS) {
				E be2 = be.getNextEdge();
				double[][] pts = new double[bedges.size()-1][];
//				System.err.println((bedges.size()-1)+"pts");
				int i = 0;
				// collect remaining vertex positions into array
				do {
					V vt = be2.getTargetVertex();		// this is v3 at the beginning
//					System.err.println("index = "+vt.getIndex());
					pts[i] = a.getD(Position3d.class, vt);
					i++;
					be2 = be2.getNextEdge();
				} while(be2.getTargetVertex() != v2);
				if (isCollinear3d(pts))  continue;		// keep looking
				cornerEdge = be;
				V s = cornerEdge.getStartVertex();
				V t = cornerEdge.getNextEdge().getTargetVertex();
				cornerEdge = TopologyAlgorithms.splitFaceAt(f, s, t);
				break;
			}
		}
		if (cornerEdge == null) {
			throw new RuntimeException("could not find three non-colinear vertices in cutCorner()");
		}
		return cornerEdge;
	}
	
	static boolean isCollinear3d(double[][] pts)	{
		for (int i = 0; i<pts.length; ++i) pts[i][2] = 1.0;		// terrible hack: 
		double[] line = P2.lineFromPoints(null, pts[0], pts[1]);
		for (int i = 2; i < pts.length; ++i)	{
			if (Rn.innerProduct(line, pts[i]) > 10E-5) return false;
		}
		return true;
	}
	
}
