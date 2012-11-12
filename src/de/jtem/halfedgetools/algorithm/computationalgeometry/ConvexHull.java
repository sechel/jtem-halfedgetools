package de.jtem.halfedgetools.algorithm.computationalgeometry;

import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.innerProduct;
import static de.jreality.math.Rn.normalize;
import static de.jreality.math.Rn.subtract;
import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;

/**
 * Implements the ConvexHull algorithm from 
 * de Berg et. Al Computational Geometry
 * It always produces a triangulation
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
		AdapterSet vp
	) { 
		convexHull(hds, vp, 1E-8);
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void convexHull(
		HDS hds,
		AdapterSet vp,
		double tolerance
	) { 
		if (hds.numFaces() > 0) {
			throw new IllegalArgumentException("HDS cannot have faces in convexHull()");
		}
		if (hds.numEdges() > 0) {
			throw new IllegalArgumentException("HDS cannot have edges in convexHull()");
		}
		if (hds.numVertices() < 4) {
			throw new IllegalArgumentException("HDS must have at least 4 points in convexHull()");
		}
		
		Map<F, Set<V>> fMap = new HashMap<F, Set<V>>();
		Map<V, Set<F>> pMap = new HashMap<V, Set<F>>();
		Map<F, Plane> planeMap = new HashMap<F, Plane>();
		
		Set<V> initVertices = createInitialTetrahedron(hds, vp, tolerance);
		
		// initialize conflict graph
		for (V v : hds.getVertices()) {
			if (v.getIncomingEdge() != null) continue;
			for (F f : hds.getFaces()) {
				if (isFaceVisibleFrom(f, v, planeMap, vp, tolerance)) {
					Set<F> conflictFaces = getConflictFaces(v, pMap);
					Set<V> conflictVertices = getConflictVertices(f, fMap);
					conflictFaces.add(f);
					conflictVertices.add(v);
				}
			}
		}

		Set<V> randomSet = new HashSet<V>(hds.getVertices());
		randomSet.removeAll(initVertices);
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
						Set<V> conflicts = new HashSet<V>();
						conflicts.addAll(getConflictVertices(e.getLeftFace(), fMap));
						conflicts.addAll(getConflictVertices(e.getRightFace(), fMap));
						conflicts.remove(pr);
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
				e.setLeftFace(null);
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
			if (horizon == null) {
				throw new RuntimeException("Cannot find a horizon edge in ConvexHull()");
			}
			
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
						if (isFaceVisibleFrom(f, v, planeMap, vp, tolerance)) {
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
		}
		for (V v : new HashSet<V>(hds.getVertices())) {
			if (v.getIncomingEdge() == null) {
				hds.removeVertex(v);				
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
		AdapterSet vp
	) {
		if (!planeMap.containsKey(f)) {
			V v1 = f.getBoundaryEdge().getPreviousEdge().getTargetVertex();
			V v2 = f.getBoundaryEdge().getTargetVertex();
			V v3 = f.getBoundaryEdge().getNextEdge().getTargetVertex();
			double[] p1 = vp.getD(Position3d.class, v1);
			double[] p2 = vp.getD(Position3d.class, v2);
			double[] p3 = vp.getD(Position3d.class, v3);
			Plane result = new Plane(p1, p2, p3);
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
		AdapterSet vp,
		double eps
	) {
		Plane plane = getPlaneForFace(f, planeMap, vp);
		double[] pos = vp.getD(Position3d.class, v); 
		return plane.isAbove(pos, eps);
	}

	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> createInitialTetrahedron(
		HDS hds,
		AdapterSet pos,
		double tolerance
	) {
		 Set<V> result = new HashSet<V>();
		 V v1 = hds.getVertex(rnd.nextInt(hds.numVertices()));
		 double[] v1p = pos.getD(Position3d.class, v1);
		 V v2 = null;
		 double[] v2p = null;
		 for (V v : hds.getVertices()) {
			 double[] vp = pos.getD(Position3d.class, v);
			 if (!Rn.equals(vp, v1p, tolerance)) {
				 v2 = v;
				 v2p = vp;
				 break;
			 }
		 }
		 if (v2 == null || v2p == null) {
			 throw new IllegalArgumentException("All points are the same in convexHull()");
		 }
		 V v3 = null;
		 V v4 = null;
		 for (V v : hds.getVertices()) {
			 double[] vp = pos.getD(Position3d.class, v);
			 for (V vv : hds.getVertices()) {
				 double[] vvp = pos.getD(Position3d.class, vv);
				 double[][] m = {
					{v1p[0], v1p[1], v1p[2], 1}, 
					{v2p[0], v2p[1], v2p[2], 1}, 
					{vp[0],  vp[1],  vp[2], 1},
					{vvp[0], vvp[1], vvp[2], 1}
				};
				 double det = Rn.determinant(m);
				 if (det < -tolerance) {
					 v3 = v;
					 v4 = vv;
					 break;
				 }
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
		
		public boolean isAbove(double[] p, double eps){
			return Rn.innerProduct(n, p) > -d + eps;
		}
		
		@Override
		public String toString() {
			return "Plane n: " + Arrays.toString(n) + " d: " + d;
		}

	}
	
	
	
	
	public static void main(String[] args) {
		// Construction of an error
		DefaultJRHDS hds = new DefaultJRHDS();
		int numPoints = 50;
		
		Matrix T = MatrixBuilder.euclidean().rotate(0.3, 1, 1, 1).getMatrix();
		
		for (int i = 0; i < numPoints; i++) {
			DefaultJRVertex v = hds.addNewVertex();
			double phi = i * Math.PI * 2 / numPoints;
			v.position = new double[] {cos(phi), sin(phi), 0};
			T.transformVector(v.position);
			v = hds.addNewVertex();
			v.position = new double[] {cos(phi), sin(phi), 1};
			T.transformVector(v.position);
		}

		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new JRPositionAdapter());
		convexHull(hds, a, 1E-15);
		
		System.out.println(HalfEdgeUtils.getGenus(hds));
		
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.addBasicUI();
		v.registerPlugin(HalfedgeInterface.class);
		v.startup();
		v.getPlugin(HalfedgeInterface.class).set(hds);
	}
	
	
	
	
	
	
	
	
}
