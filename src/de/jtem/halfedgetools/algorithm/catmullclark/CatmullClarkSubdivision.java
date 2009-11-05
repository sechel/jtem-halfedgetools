package de.jtem.halfedgetools.algorithm.catmullclark;

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
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
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
public class CatmullClarkSubdivision  <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> {
	
	/**
	 * Subdivides a given surface with the Catmull-Clark rule
	 * @param <HDS> 
	 * @param hds the input surface
	 * @param r the output surface will be overwritten
	 * @param vc a coordinates adapter
	 */
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Map<E, Set<E>> subdivide(
		HDS hds,
		HDS r,
		Coord3DAdapter<V> vc,
		Coord3DAdapter<E> ec,
		Coord3DAdapter<F> fc
	) {
		
		
		HalfEdgeUtilsExtra.clear(r);
		// face vertices
		Map<F, V> fvMap = new HashMap<F, V>();
		for (F f : hds.getFaces()) {
			V v = r.addNewVertex();
			fvMap.put(f, v);
			
			double[] sum = {0, 0, 0};
//			List<E> b = boundaryEdges(f);
//			int size = 0;
//			for (E e : b) {
//				V bv = e.getTargetVertex();
////				add(sum, sum, coord.getCoord(bv));
//				add(sum, sum, ec.getCoord(e));
//				size++;
//			}
//			times(sum, 1.0 / size, sum);
			sum = fc.getCoord(f);
			vc.setCoord(v, sum);
		}
		
		// edge vertices
		Map<E, V> evMap = new HashMap<E, V>();
		for (E e : hds.getPositiveEdges()) {
			if (!isInteriorEdge(e)) {
				continue;
			}
			V v = r.addNewVertex();
			evMap.put(e, v);
			evMap.put(e.getOppositeEdge(), v);
			
			V leftV = fvMap.get(e.getLeftFace());
			V rightV = fvMap.get(e.getRightFace());
			double[][] coords = new double[4][];
//			coords[0] = vc.getCoord(leftV);
//			coords[1] = vc.getCoord(rightV);
//			coords[2] = coord.getCoord(e.getStartVertex());
//			coords[3] = coord.getCoord(e.getTargetVertex());
			coords[0] = fc.getCoord(e.getLeftFace());
			coords[1] = fc.getCoord(e.getRightFace());
			coords[2] = ec.getCoord(e);
			coords[3] = ec.getCoord(e.getOppositeEdge());
			vc.setCoord(v, average(null, coords));
		}
	
		// vertex vertices
		Map<V, V> vvMap = new HashMap<V, V>();
		for (V v : hds.getVertices()) {
			if (isBoundaryVertex(v)) {
				continue;
			}
			V nv = r.addNewVertex();
			vvMap.put(v, nv);
			
			List<E> star = incomingEdges(v);
			List<F> fStar = facesIncidentWithVertex(v);
			double[] faceSum = {0, 0, 0};
			for (F f : fStar) {
				V fv = fvMap.get(f);
				add(faceSum, faceSum, vc.getCoord(fv));
			}
			times(faceSum, 1.0 / fStar.size(), faceSum);
			double[] edgeSum = {0, 0, 0};
			for (E e : star) {
//				add(edgeSum, coord.getCoord(e.getTargetVertex()), edgeSum);
//				add(edgeSum, coord.getCoord(e.getStartVertex()), edgeSum);
				add(edgeSum, ec.getCoord(e), edgeSum);
				add(edgeSum, ec.getCoord(e.getOppositeEdge()), edgeSum);
			}
			times(edgeSum, 1.0 / star.size(), edgeSum);
			
			int n = star.size();
			double[] vertexSum = times(null, n - 3, vc.getCoord(v));
			
			double[] sum = add(null, add(null, faceSum, edgeSum), vertexSum);
			vc.setCoord(nv, times(sum, 1.0 / n, sum));
			
		}

		// face vertex connections and linkage
		Map<E, E> edgeMap = new HashMap<E, E>();
		for (F f : hds.getFaces()) {
			V fv = fvMap.get(f);
			E lastOut = null;
			E firstIn = null;
			F lastFace = null;
			for (E e : boundaryEdges(f)) {
				V ev = evMap.get(e);
				if (ev == null) { // at the boundary
					lastFace = null;
					continue;
				}
				E in = r.addNewEdge();
				E out = r.addNewEdge();
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
					lastFace = r.addNewFace();
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
		for (V v : hds.getVertices()) {
			if (isBoundaryVertex(v)) {
				continue;
			}
			V vv = vvMap.get(v);
			E lastIn = null;
			E firstOut = null;
			for (E e : incomingEdges(v)) {
				V ev = evMap.get(e);
				E in = r.addNewEdge();
				E out = r.addNewEdge();
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
		
		for(E e : hds.getEdges()) {
			Set<E> newEs = new HashSet<E>();
			newEs.add(tempEmap.get(e));
			newEs.add(tempEmap.get(e.getOppositeEdge()).getOppositeEdge());
			
			oldEtoSubDivEs.put(e, newEs);
		}
		
		HalfEdgeUtils.isValidSurface(r, true);
		
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
