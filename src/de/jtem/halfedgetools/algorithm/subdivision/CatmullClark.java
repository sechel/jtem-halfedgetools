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
import static de.jtem.halfedge.util.HalfEdgeUtils.facesIncidentWithVertex;
import static de.jtem.halfedge.util.HalfEdgeUtils.incomingEdges;
import static de.jtem.halfedge.util.HalfEdgeUtils.isBoundaryVertex;
import static de.jtem.halfedge.util.HalfEdgeUtils.isInteriorEdge;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 * @author Charles Gunn
 * @author Stefan Sechelmann
 *
 * @param <V> Vertex class
 * @param <E> Edge class
 * @param <F> Face class
 */
public class CatmullClark {
	
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
		
		HalfEdgeUtilsExtra.clear(newHeds);
		// face vertices

		for (F f : oldHeds.getFaces()) {
			V v = newHeds.addNewVertex();
			oldFnewVMap.put(f, v);
			
			double[] sum = new double[3];
//			List<E> b = boundaryEdges(f);
//			int size = 0;
//			for (E e : b) {
//				V bv = e.getTargetVertex();
////				add(sum, sum, coord.getCoord(bv));
//				add(sum, sum, ec.getCoord(e));
//				size++;
//			}
//			times(sum, 1.0 / size, sum);
			sum = fc.get(f);
			vc.set(v, sum);
		}
		
		// edge vertices
		for (E e : oldHeds.getPositiveEdges()) {
			if (!isInteriorEdge(e)) {
				continue;
			}
			V v = newHeds.addNewVertex();
			oldEnewVMap.put(e, v);
			oldEnewVMap.put(e.getOppositeEdge(), v);
			
//			V leftV = fvMap.get(e.getLeftFace());
//			V rightV = fvMap.get(e.getRightFace());
			double[][] coords = new double[4][];
			coords[0] = fc.get(e.getLeftFace(),e);
			coords[1] = fc.get(e.getRightFace(),e);
			ec.setEdgeAlpha(1.0);
			ec.setEdgeIgnore(true);
			coords[2] = ec.get(e);
			coords[3] = ec.get(e.getOppositeEdge());
			vc.set(v, average(null, coords));
		}
	
		// vertex vertices

		for (V v : oldHeds.getVertices()) {
			if (isBoundaryVertex(v)) {
				continue;
			}
			V nv = newHeds.addNewVertex();
			oldVnewVMap.put(v, nv);
			
			List<E> star = incomingEdges(v);
			List<F> fStar = facesIncidentWithVertex(v);
			double[] faceSum = new double[3];
			for (F f : fStar) {
				V fv = oldFnewVMap.get(f);
				add(faceSum, faceSum, vc.get(fv));
			}
			times(faceSum, 1.0 / fStar.size(), faceSum);
			double[] edgeSum = new double[3];
			for (E e : star) {
//				add(edgeSum, coord.getCoord(e.getTargetVertex()), edgeSum);
//				add(edgeSum, coord.getCoord(e.getStartVertex()), edgeSum);
				ec.setEdgeIgnore(true);
				ec.setEdgeAlpha(1.0);
				add(edgeSum, ec.get(e), edgeSum);
				add(edgeSum, ec.get(e.getOppositeEdge()), edgeSum);
			}
			times(edgeSum, 1.0 / star.size(), edgeSum);
			
			int n = star.size();
			double[] vertexSum = times(null, n - 3, vc.get(v));
			
			double[] sum = add(null, add(null, faceSum, edgeSum), vertexSum);
			vc.set(nv, times(sum, 1.0 / n, sum));
		}

		// face vertex connections and linkage
		Map<E, E> edgeMap = new HashMap<E, E>();
		for (F f : oldHeds.getFaces()) {
			V fv = oldFnewVMap.get(f);
			E lastOut = null;
			E firstIn = null;
			F lastFace = null;
			for (E e : boundaryEdges(f)) {
				V ev = oldEnewVMap.get(e);
				if (ev == null) { // at the boundary
					lastFace = null;
					continue;
				}
				E in = newHeds.addNewEdge();
				E out = newHeds.addNewEdge();
				in.linkOppositeEdge(out);
				in.setTargetVertex(fv);
				out.setTargetVertex(ev);
				if (lastOut != null) {
					in.linkNextEdge(lastOut);
				}
				lastOut = out;
				if (firstIn == null) {
					firstIn = in;
				}
				edgeMap.put(e, in);
				
				// new faces
				if (lastFace != null) {
					in.setLeftFace(lastFace);
				}
				if (!isBoundaryVertex(e.getTargetVertex())) {
					lastFace = newHeds.addNewFace();
					out.setLeftFace(lastFace);
				} else {
					lastFace = null;
				}
			}
			if (firstIn != null) {
				firstIn.setLeftFace(lastFace);
				firstIn.linkNextEdge(lastOut);
			}
		}
		

		Map<E, Set<E>> oldEtoSubDivEs = new HashMap<E, Set<E>>();
		
		Map<E, E> tempEmap = new HashMap<E, E>();
		// vertex vertex connections and linkage
		for (V v : oldHeds.getVertices()) {
			if (isBoundaryVertex(v)) {
				continue;
			}
			V vv = oldVnewVMap.get(v);
			E lastIn = null;
			E firstOut = null;
			for (E e : incomingEdges(v)) {
				V ev = oldEnewVMap.get(e);
				E in = newHeds.addNewEdge();
				E out = newHeds.addNewEdge();
				in.linkOppositeEdge(out);
				in.setTargetVertex(vv);
				out.setTargetVertex(ev);
				if (lastIn != null) {
					out.linkPreviousEdge(lastIn);
				}
				lastIn = in;
				if (firstOut == null) {
					firstOut = out;
				}
				E linkIn = edgeMap.get(e).getOppositeEdge();
				E linkOut = edgeMap.get(e.getOppositeEdge());
				linkIn.linkNextEdge(in);
				out.linkNextEdge(linkOut);
				
				out.setLeftFace(linkOut.getLeftFace());
				in.setLeftFace(linkIn.getLeftFace());
				
				// boundary link
				if (linkIn.getOppositeEdge().getLeftFace() == null) {
					linkOut.getOppositeEdge().linkNextEdge(linkIn.getOppositeEdge());
				}
				tempEmap.put(e, in);
			}
			if (firstOut != null) {
				firstOut.linkPreviousEdge(lastIn);
			}
		}
		
		for(E e : oldHeds.getEdges()) {
			Set<E> newEs = new HashSet<E>();
			newEs.add(tempEmap.get(e));
			if (tempEmap.get(e.getOppositeEdge()) != null) {
				newEs.add(tempEmap.get(e.getOppositeEdge()).getOppositeEdge());
			}
			oldEtoSubDivEs.put(e, newEs);
		}

		HalfEdgeUtils.isValidSurface(newHeds, true);
		
		return oldEtoSubDivEs;
	}
	
	
	private static double[]  add(double[]  dst, double[]  src1, double[]  src2)	{
		if (dst == null) dst = new double[src1.length];
		int n = src1.length;
		if (src1.length != src2.length)
			n = Math.min(Math.min(dst.length, src1.length), src2.length);
		for (int i=0; i<n; ++i)	dst[i] = src1[i] + src2[i];
		return dst;
	}
	
	
	private static double[]  times(double[]  dst, double factor, double[]  src)	{
		if (dst == null) dst = new double[src.length];
		if (dst.length != src.length) {
			throw new IllegalArgumentException("Vectors must be same length");
		}
		int n = dst.length;
		for (int i=0; i<n; ++i)	dst[i] = factor * src[i];
		return dst;
	}
	
	public static double[]  average(double[]  dst, double[][]  vlist)	{
		// assert dim check
		if (dst == null) dst = new double[vlist[0].length];
		if (vlist.length == 0) return null;
		double[] tmp = new double[dst.length];
		for (int i=0; i<vlist.length; ++i)	{
			add(tmp, tmp, vlist[i]);
		}
		times(dst, 1.0/vlist.length, tmp);
		return dst;
	}
	
	
}
