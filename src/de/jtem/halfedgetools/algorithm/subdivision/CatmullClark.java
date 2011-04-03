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
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

/**
 * Catmull-Clark Subdivision
 * 
 * @author Charles Gunn
 * @author Stefan Sechelmann
 * @author seidel
 * 
 * @param <V>
 *            Vertex class
 * @param <E>
 *            Edge class
 * @param <F>
 *            Face class
 */

public class CatmullClark {

	/**
	 * Subdivides a given surface with the Catmull-Clark rule 
	 * Subdivides the boundary with the Lane-Riesenfeld rule
	 * Subdivides a given edge selection with the Lane-Riesenfeld rule
	 * 
	 * @param <HDS>
	 * @param oldHeds
	 *            the input surface
	 * @param newHeds
	 *            the output surface will be overwritten
	 * @param vc
	 *            a coordinates adapter
	 */

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, Set<E>> subdivide(
		HDS oldHeds, 
		HDS newHeds, 
		AdapterSet a,
		HalfedgeSelection sel, 
		boolean bSplineBoundary,
		boolean bSplineFeatureLines,
		boolean removeBoundary,
		boolean linearInterpolation,
		Map<F, V> oldFnewVMap,
		Map<E, V> oldEnewVMap,
		Map<V, V> oldVnewVMap
	) {
		HalfEdgeUtilsExtra.clear(newHeds);
		setNewCoordinates(oldHeds, newHeds, a, sel, oldFnewVMap, oldEnewVMap, oldVnewVMap, bSplineBoundary, bSplineFeatureLines, linearInterpolation);
		return createCombinatorics(oldHeds, newHeds, sel, oldFnewVMap, oldEnewVMap, oldVnewVMap,removeBoundary, bSplineFeatureLines);
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNewCoordinates(
		HDS oldHeds, 
		HDS newHeds, 
		AdapterSet a,
		HalfedgeSelection sel, 
		Map<F, V> oldFnewVMap,
		Map<E, V> oldEnewVMap, 
		Map<V, V> oldVnewVMap,
		boolean bSplineBoundary,
		boolean bSplineFeatureLines,
		boolean linearInterpolation
	) {
		HalfedgeSelection vertex = new HalfedgeSelection();
		setNewFaceCoordinates(oldHeds, newHeds, oldFnewVMap, a);
		setNewEdgeCoordinates(oldHeds, newHeds, oldFnewVMap, oldEnewVMap, oldVnewVMap, a, sel, bSplineFeatureLines, linearInterpolation);
		setNewVertexCoordinates(oldHeds, newHeds, oldFnewVMap, oldVnewVMap, sel, vertex, a, bSplineBoundary, bSplineFeatureLines, linearInterpolation);
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNewFaceCoordinates(
		HDS oldHeds, 
		HDS newHeds,
		Map<F, V> oldFnewVMap,
		AdapterSet a
	) {
		for (F f : oldHeds.getFaces()) {
			V v = newHeds.addNewVertex();
			oldFnewVMap.put(f, v);
			double[] sum = new double[3];
			a.setParameter("refEdge", (Object)null);
			sum = a.getD(BaryCenter4d.class, f);
			a.set(Position.class, v, sum);
		}
	}

	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNewEdgeCoordinates(
		HDS oldHeds, 
		HDS newHeds, 
		Map<F, V> oldFnewVMap,
		Map<E, V> oldEnewVMap, 
		Map<V, V> oldVnewVMap,
		AdapterSet a, 
		HalfedgeSelection sel, 
		boolean bSplineFeatureLines,
		boolean linearInterpolation
	) {
	
		for (E e : oldHeds.getPositiveEdges()) {
			if (!isInteriorEdge(e)||(bSplineFeatureLines && sel.isSelected(e)) || linearInterpolation) {
				V v = newHeds.addNewVertex();
				oldEnewVMap.put(e, v);
				oldEnewVMap.put(e.getOppositeEdge(), v);
				a.setParameter("alpha", 0.5);
				a.setParameter("ignore", true);
				a.set(Position.class, v, a.getD(BaryCenter4d.class, e));
			} else {
				V v = newHeds.addNewVertex();
				oldEnewVMap.put(e, v);
				oldEnewVMap.put(e.getOppositeEdge(), v);
				double[][] coords = new double[4][];
				a.setParameter("refEdge", e);
				coords[0] = a.getD(BaryCenter4d.class, e.getLeftFace());
				coords[1] = a.getD(BaryCenter4d.class, e.getRightFace());
				a.setParameter("alpha", 1.0);
				a.setParameter("ignore", true);
				coords[2] = a.getD(BaryCenter4d.class, e);
				coords[3] = a.getD(BaryCenter4d.class, e.getOppositeEdge());
				a.set(Position.class, v, average(null, coords));
			}
		}
	}
	
	private  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNewVertexCoordinates(
		HDS oldHeds, 
		HDS newHeds, 
		Map<F, V> oldFnewVMap,
		Map<V, V> oldVnewVMap,
		HalfedgeSelection sel,
		HalfedgeSelection vertex,
		AdapterSet a,
		boolean bSplineBoundary, 
		boolean bSplineFeatureLines,
		boolean useLinearInterpolation
	) {
		if (bSplineFeatureLines) {
			for (E e1 : sel.getEdges(oldHeds)) {
				int counter = 0;
				for (E e : incomingEdges(e1.getTargetVertex())) {
					if (sel.isSelected(e)) {
						counter++;
					}
				}
				if (counter > 2) {
					vertex.add(e1.getTargetVertex());
					continue;
				}
				for (E e2 : sel.getEdges(oldHeds)) {
					if (!isBoundaryVertex(e1.getTargetVertex()) && (e1.getOppositeEdge() != e2) && (e1.getTargetVertex() == e2.getStartVertex())) {
						sel.add(e1.getTargetVertex());
					}
				}
			}
		} // end if (b4)
		
		for (V v : oldHeds.getVertices()) {
			if (useLinearInterpolation) {
				V nv = newHeds.addNewVertex();
				oldVnewVMap.put(v, nv);
				a.set(Position.class, nv, a.getD(Position.class, v));
				continue;
			}
			if (isBoundaryVertex(v) && bSplineBoundary) {
				V nv= newHeds.addNewVertex();
				oldVnewVMap.put(v, nv);
				List<E> star= incomingEdges(v);
				double[][] vHelp = new double[2][];
				vHelp[0] = null;
				for (E e : star){
					if(!isInteriorEdge(e)){
						a.setParameter("alpha", 0.75);
						a.setParameter("ignore", true);
						if(vHelp[0] ==null){
							vHelp[0] = a.getD(BaryCenter4d.class, e);
						} else {
							vHelp[1] = a.getD(BaryCenter4d.class, e);
							a.set(Position.class, nv, average(null, vHelp));
						}
						
					}
				}
			} else if (isBoundaryVertex(v) && !bSplineBoundary){
				V nv= newHeds.addNewVertex();
				oldVnewVMap.put(v, nv);
				a.set(Position.class, nv, a.getD(Position.class, v));
			} else if(sel.isSelected(v)){
				V nv= newHeds.addNewVertex();
				oldVnewVMap.put(v, nv);
				List<E> star= incomingEdges(v);
				double[][] vHelp = new double[2][];
				vHelp[0] = null;
				for (E e : star){
					if(sel.isSelected(e)){
						a.setParameter("alpha", 0.75);
						a.setParameter("ignore", true);
						if(vHelp[0] ==null){
							vHelp[0] = a.getD(BaryCenter4d.class, e);
						} else {
							vHelp[1] = a.getD(BaryCenter4d.class, e);
							a.set(Position.class, nv, average(null, vHelp));
						}
						
					}
				}
			} else if (vertex.isSelected(v)) {
				V nv = newHeds.addNewVertex();
				oldVnewVMap.put(v, nv);
				a.set(Position.class, nv, a.getD(Position.class, v));
			} else {
				V nv = newHeds.addNewVertex();
				oldVnewVMap.put(v, nv);

				List<E> star = incomingEdges(v);
				List<F> fStar = facesIncidentWithVertex(v);
				double[] faceSum = new double[3];
				for (F f : fStar) {
					V fv = oldFnewVMap.get(f);
					add(faceSum, faceSum, a.getD(Position.class, fv));
				}
				times(faceSum, 1.0 / fStar.size(), faceSum);
				double[] edgeSum = new double[3];
				for (E e : star) {
					a.setParameter("alpha", 1.0);
					a.setParameter("ignore", true);
					add(edgeSum, a.getD(BaryCenter4d.class, e), edgeSum);
					add(edgeSum, a.getD(BaryCenter4d.class, e.getOppositeEdge()), edgeSum);
				}
				times(edgeSum, 1.0 / star.size(), edgeSum);

				int n = star.size();
				double[] vertexSum = times(null, n - 3, a.getD(Position.class, v));

				double[] sum = add(null, add(null, faceSum, edgeSum), vertexSum);
				a.set(Position.class, nv, times(sum, 1.0 / n, sum));
			}
		}
	}

	private <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, Set<E>> createCombinatorics(
		HDS oldHeds, 
		HDS newHeds,
		HalfedgeSelection sel, 
		Map<F, V> oldFnewVMap,
		Map<E, V> oldEnewVMap, 
		Map<V, V> oldVnewVMap,
		boolean removeBoundary, 
		boolean bSplineFeatureLines
	) {
		Map<E, E> evOutEmap = new HashMap<E, E>();
		Map<E, E> edgeMap = new HashMap<E, E>();
		Map<E, Set<E>> oldEtoSubDivEs = new HashMap<E, Set<E>>();
		HalfedgeSelection outSel = new HalfedgeSelection();

		// face vertex connections and linkage

		for (F f : oldHeds.getFaces()) {
			V fv = oldFnewVMap.get(f);
			E lastOut = null;
			E firstIn = null;
			F lastFace = null;
			for (E e : boundaryEdges(f)) {
				V ev = oldEnewVMap.get(e);
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
				lastFace = newHeds.addNewFace();
				out.setLeftFace(lastFace);
			}
			assert firstIn != null;
			firstIn.setLeftFace(lastFace);
			firstIn.linkNextEdge(lastOut);
		}

		// vertex vertex connections and linkage
		for (V v : oldHeds.getVertices()) {
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
				if (sel.isSelected(e) && bSplineFeatureLines && !(removeBoundary && isBoundaryVertex(v))) {
					outSel.add(in);
					outSel.add(out);
				}
				if (lastIn != null) {
					out.linkPreviousEdge(lastIn);
				}
				lastIn = in;
				if (firstOut == null) {
					firstOut = out;
				}
				evOutEmap.put(e, in);
			}
			if (firstOut != null) {
				firstOut.linkPreviousEdge(lastIn);
			}
		}

		// edge vertex linkage
		for (E e : oldHeds.getPositiveEdges()) {
			if (!edgeMap.containsKey(e)) {
				E out1 = evOutEmap.get(e.getOppositeEdge());
				E in1 = out1.getOppositeEdge();
				E out2 = edgeMap.get(e.getOppositeEdge());
				E in2 = out2.getOppositeEdge();
				E out3 = evOutEmap.get(e);
				E in3 = out3.getOppositeEdge();

				in1.linkNextEdge(out3);
				in2.linkNextEdge(out1);
				in3.linkNextEdge(out2);
				out1.setLeftFace(in2.getLeftFace());
				in3.setLeftFace(out2.getLeftFace());
				in1.setLeftFace(null);
				out3.setLeftFace(null);

			} else if (!edgeMap.containsKey(e.getOppositeEdge())) {
				E out1 = evOutEmap.get(e.getOppositeEdge());
				E in1 = out1.getOppositeEdge();
				E out2 = evOutEmap.get(e);
				E in2 = out2.getOppositeEdge();
				E out3 = edgeMap.get(e);
				E in3 = out3.getOppositeEdge();

				in1.linkNextEdge(out3);
				in2.linkNextEdge(out1);
				in3.linkNextEdge(out2);
				in1.setLeftFace(out3.getLeftFace());
				out2.setLeftFace(in3.getLeftFace());

			} else {
				E out1 = evOutEmap.get(e.getOppositeEdge());
				E in1 = out1.getOppositeEdge();
				E out2 = edgeMap.get(e.getOppositeEdge());
				E in2 = out2.getOppositeEdge();
				E out3 = evOutEmap.get(e);
				E in3 = out3.getOppositeEdge();
				E out4 = edgeMap.get(e);
				E in4 = out4.getOppositeEdge();

				in1.linkNextEdge(out4);
				in2.linkNextEdge(out1);
				in3.linkNextEdge(out2);
				in4.linkNextEdge(out3);
				in1.setLeftFace(out4.getLeftFace());
				out1.setLeftFace(in2.getLeftFace());
				in3.setLeftFace(out2.getLeftFace());
				out3.setLeftFace(in4.getLeftFace());
			}
		}
		if(removeBoundary){
			for(V v : HalfEdgeUtils.boundaryVertices(newHeds)){
				TopologyAlgorithms.removeVertex(v);
			}
		}
		for (E e : oldHeds.getEdges()) {
			Set<E> newEs = new HashSet<E>();
			newEs.add(evOutEmap.get(e));
			if (evOutEmap.get(e.getOppositeEdge()) != null) {
				newEs.add(evOutEmap.get(e.getOppositeEdge()).getOppositeEdge());
			}
			oldEtoSubDivEs.put(e, newEs);
		}

		HalfEdgeUtils.isValidSurface(newHeds, true);
		sel.clear();
		if (bSplineFeatureLines) {
			sel.addAll(outSel.getEdges());
		}
		return oldEtoSubDivEs;
	}

	private static double[] add(double[] dst, double[] src1, double[] src2) {
		if (dst == null)
			dst = new double[src1.length];
		int n = src1.length;
		if (src1.length != src2.length)
			n = Math.min(Math.min(dst.length, src1.length), src2.length);
		for (int i = 0; i < n; ++i)
			dst[i] = src1[i] + src2[i];
		return dst;
	}

	private static double[] times(double[] dst, double factor, double[] src) {
		if (dst == null)
			dst = new double[src.length];
		if (dst.length != src.length) {
			throw new IllegalArgumentException("Vectors must be same length");
		}
		int n = dst.length;
		for (int i = 0; i < n; ++i)
			dst[i] = factor * src[i];
		return dst;
	}

	public static double[] average(double[] dst, double[][] vlist) {
		// assert dim check
		if (dst == null)
			dst = new double[vlist[0].length];
		if (vlist.length == 0)
			return null;
		double[] tmp = new double[dst.length];
		for (int i = 0; i < vlist.length; ++i) {
			add(tmp, tmp, vlist[i]);
		}
		times(dst, 1.0 / vlist.length, tmp);
		return dst;
	}

}
