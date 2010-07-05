package de.jtem.halfedgetools.algorithm.computationalgeometry;

import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.innerProduct;
import static de.jreality.math.Rn.normalize;
import static de.jreality.math.Rn.subtract;
import static java.lang.Math.abs;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;

/**
 * Implements the ConvexHull algorithm from 
 * de Berg et. Al Computational Geometry
 * @author Stefan Sechelmann
 * @see {@link http://www.cs.uu.nl/geobook/}
 */
public class ConvexHull {

	private static Random
		rnd = new Random();
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void convexHull(
		HDS hds,
		VertexPositionCalculator vp,
		double tolerance
	) { 
		if (hds.numFaces() > 0) {
			new IllegalArgumentException("HDS cannot have faces in convexHull()");
		}
		if (hds.numEdges() > 0) {
			new IllegalArgumentException("HDS cannot have edges in convexHull()");
		}
		if (hds.numVertices() < 4) {
			new IllegalArgumentException("HDS must have at least 4 points in convexHull()");
		}
		
		Map<F, Set<V>> fMap = new HashMap<F, Set<V>>();
		Map<V, Set<F>> pMap = new HashMap<V, Set<F>>();
		Map<F, Plane> planeMap = new HashMap<F, Plane>();
		
		Set<V> initVertices = createInitialTetrahedron(hds, vp, tolerance);
		
		// initialize conflict graph
		for (V v : hds.getVertices()) {
			if (v.getIncomingEdge() != null) continue;
			for (F f : hds.getFaces()) {
				if (isFaceVisibleFrom(f, v, planeMap, vp)) {
					Set<F> conflictFaces = getConflictFaces(v, pMap);
					Set<V> conflictVertices = getConflictVertices(f, fMap);
					conflictFaces.add(f);
					conflictVertices.add(v);
				}
			}
		}
		
		Set<V> randomSet = new HashSet<V>(hds.getVertices());
		randomSet.removeAll(initVertices);
		int i = 0;
		for (V pr : randomSet) {
			Set<F> Fconflict = getConflictFaces(pr, pMap);
			if (Fconflict.isEmpty()) {
				continue;
			}
			Set<E> L = new HashSet<E>();
			Set<E> deleteMe = new HashSet<E>();
			Map<E, Set<V>> PconflictMap = new HashMap<E, Set<V>>();
			for (F f : Fconflict) {
				for (E e : HalfEdgeUtils.boundaryEdges(f)) {
					if (!Fconflict.contains(e.getRightFace())) {
						L.add(e);
						e.setLeftFace(null);
						Set<V> conflicts = new HashSet<V>();
						conflicts.addAll(getConflictVertices(e.getLeftFace(), fMap));
						conflicts.addAll(getConflictVertices(e.getRightFace(), fMap));
						PconflictMap.put(e, conflicts);
					} else {
						deleteMe.add(e);
					}
				}
			}
			for (F f : Fconflict) {
				hds.removeFace(f);
			}
			for (E e : deleteMe) { 
				hds.removeEdge(e);
			}
			
			E horizon = null;
			for (E e : L) { // repair boundary
				horizon = e;
				E next = e.getOppositeEdge();
				while (!L.contains(next)) {
					next = next.getPreviousEdge().getOppositeEdge();
				}
				assert next != e;
				e.linkNextEdge(next);
			}
			assert horizon != null;

			E lastHorizon = horizon.getPreviousEdge();
			E firstEdge = null;
			E lastEdge = null;
			
			do {
				E next = horizon.getNextEdge();
				V vs = horizon.getStartVertex();
				F f = hds.addNewFace();
				E e2 = hds.addNewEdge();
				E e3 = hds.addNewEdge();
				horizon.setLeftFace(f);
				e2.setLeftFace(f);
				e3.setLeftFace(f);
				horizon.linkNextEdge(e2);
				e2.linkNextEdge(e3);
				e3.linkNextEdge(horizon);
				if (lastEdge != null) {
					e3.linkOppositeEdge(lastEdge);
				}
				lastEdge = e2;
				if (firstEdge == null) {
					firstEdge = e3;
				}
				if (horizon == lastHorizon) {
					e2.linkOppositeEdge(firstEdge);
				}
				e2.setTargetVertex(pr);
				e3.setTargetVertex(vs);
				
				
//				Plane p1 = getPlaneForFace(f, planeMap, vp);
//				Plane p2 = getPlaneForFace(horizon.getRightFace(), planeMap, vp);
//				if (Rn.equals(p1.n, p2.n, tolerance)) {
//					Set<V> Pconflicts = getConflictVertices(horizon.getRightFace(), fMap);
//					f = TopologyAlgorithms.removeEdgeFill(horizon);
//					fMap.put(f, Pconflicts); // :TODO fix me here
//				} else {
					for (V v : PconflictMap.get(horizon)) {
						if (isFaceVisibleFrom(f, v, planeMap, vp)) {
							getConflictFaces(v, pMap).add(f);
							getConflictVertices(f, fMap).add(v);
						}
					}
//				}
				horizon = next;
			} while (horizon != null);
			
			pMap.remove(pr);
			for (V v : pMap.keySet()) {
				Set<F> faces = pMap.get(v);
				faces.removeAll(Fconflict);
			}
			for (F f : Fconflict) {
				fMap.remove(f);
			}
			for (F f : fMap.keySet()) {
				Set<V> vertices = fMap.get(f);
				vertices.remove(pr);
			}
			if (i++ >= 1) {
				return;
			}
		}
	}

	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Set<V> getConflictVertices(
		F f,
		Map<F, Set<V>> fMap
	) {
		if (!fMap.containsKey(f)) {
			fMap.put(f, new HashSet<V>());
		}
		return fMap.get(f);
	}
	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Set<F> getConflictFaces(
		V v,
		Map<V, Set<F>> pMap
	) {
		if (!pMap.containsKey(v)) {
			pMap.put(v, new HashSet<F>());
		}
		return pMap.get(v);
	}
	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Plane getPlaneForFace(
		F f,
		Map<F, Plane> planeMap,
		VertexPositionCalculator vp
	) {
		if (!planeMap.containsKey(f)) {
			V v1 = f.getBoundaryEdge().getPreviousEdge().getTargetVertex();
			V v2 = f.getBoundaryEdge().getTargetVertex();
			V v3 = f.getBoundaryEdge().getNextEdge().getTargetVertex();
			Plane result = new Plane(vp.get(v1), vp.get(v2), vp.get(v3));
			planeMap.put(f, result);
		}
		return planeMap.get(f);
	}
	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isFaceVisibleFrom(
		F f,
		V v,
		Map<F, Plane> planeMap,
		VertexPositionCalculator vp
	) {
		Plane plane = getPlaneForFace(f, planeMap, vp);
		double[] pos = vp.get(v); 
		return plane.isAbove(pos);
	}

	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> createInitialTetrahedron(
		HDS hds,
		VertexPositionCalculator pos,
		double tolerance
	) {
		 Set<V> result = new HashSet<V>();
		 V v1 = hds.getVertex(rnd.nextInt(hds.numVertices()));
		 double[] v1p = pos.get(v1);
		 V v2 = null;
		 double[] v2p = null;
		 for (V v : hds.getVertices()) {
			 double[] vp = pos.get(v);
			 if (!Rn.equals(vp, v1p, tolerance)) {
				 v2 = v;
				 v2p = vp;
				 break;
			 }
		 }
		 if (v2 == null || v2p == null) {
			 throw new IllegalArgumentException("All points are the same in convexHull()");
		 }
		 double[] lvec = Rn.subtract(null, v1p, v2p);
		 Rn.normalize(lvec, lvec);
		 V v3 = null;
		 double[] v3p = null;
		 for (V v : hds.getVertices()) {
			 double[] vp = pos.get(v);
			 double[] vpvec = Rn.subtract(null, v1p, vp);
			 Rn.normalize(vpvec, vpvec);
			 double dot = Rn.innerProduct(lvec, vpvec);
			 if (Math.abs(dot - 1) > tolerance) {
				 v3 = v;
				 v3p = vp;
				 break;
			 }
		 }
		 if (v3 == null || v3p == null) {
			 throw new IllegalArgumentException("All points lie on a line in convexHull()");
		 }
		 V v4 = null;
		 for (V v : hds.getVertices()) {
			 double[] vp = pos.get(v);
			 double[][] m = {
				{v1p[0], v1p[1], v1p[2], 1}, 
				{v2p[0], v2p[1], v2p[2], 1}, 
				{v3p[0], v3p[1], v3p[2], 1},
				{vp[0], vp[1], vp[2], 1}
			};
			 double det = Rn.determinant(m);
			 if (det*det > tolerance) {
				 v4 = v;
				 break;
			 }
		 }
		 if (v4 == null) {
			 throw new IllegalArgumentException("All points lie in one plane convexHull()");
		 }
		 HalfEdgeUtils.constructFaceByVertices(hds, v1, v2, v3);
		 HalfEdgeUtils.constructFaceByVertices(hds, v2, v1, v4);
		 HalfEdgeUtils.constructFaceByVertices(hds, v3, v4, v1);
		 HalfEdgeUtils.constructFaceByVertices(hds, v4, v3, v2);
		 result.add(v1);
		 result.add(v2);
		 result.add(v3);
		 result.add(v4);
		 return result;
	}
	
	
	
	private static class Plane {

		protected double[]
		    n = new double[3];
		protected double 
			d = 0.0;
		
		
		public Plane(double[] p1, double[] p2, double[] p3) {
			double[] xy = subtract(null, p1, p2);
			double[] xz = subtract(null, p3, p2);
			double[] n = crossProduct(null, xy, xz);
			normalize(n, n);
			double d = -Rn.innerProduct(n, p2);
			this.n = n;
			this.d = d;
		}
		
		
		public boolean isInPlane(double[] p, double eps) {
			return abs(innerProduct(n, p) + d) < eps;
		}
		
		@SuppressWarnings("unused")
		public boolean isInPlane(double[] p) {
			return isInPlane(p, 1E-8);
		}
		
		public boolean isAbove(double[] p){
			return Rn.innerProduct(n, p) <= -d;
		}
		
		@Override
		public String toString() {
			return "Plane n: " + Arrays.toString(n) + " d: " + d;
		}

	}
	
	
}
