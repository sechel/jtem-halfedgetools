package de.jtem.halfedgetools.algorithm.simplification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import javax.vecmath.Matrix3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.simplification.adapters.SimplificationAdapters.AreaAdapter;
import de.jtem.halfedgetools.algorithm.simplification.adapters.SimplificationAdapters.NormalAdapter;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class GarlandHeckbert<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> {
	HEDS activeMesh;
	int vertexCount;
	int edgeCount;
	double[][] vertexLocation;
	double[] edgeError;
	HashMap<E, EdgePQItem> edgeOperationMap = new HashMap<E, EdgePQItem>();

	private PriorityQueue<EdgePQItem> pq;
	boolean forceBoundary = false;

	class Quadric {
		Matrix3d A;
		double[] b;
		double c;

		public Quadric(Matrix3d A, double[] b, double c) {
			this.A = A;
			this.b = b;
			this.c = c;
		}

		public Quadric(double[] A, double[] b, double c) {
			this.A = new Matrix3d(A);
			this.b = b;
			this.c = c;
		}
	}

	HashMap<V, Quadric> quadric_map = new HashMap<V, Quadric>();
	private Coord3DAdapter<V> pa;
	private NormalAdapter<F> na;
	private AreaAdapter<F> aa;

	public boolean isValidEdgeOperation(E edge) {

		if (!edge.isValid()) {
			// System.out.println("Edge is not valid");
			return false;
		}
		// not on boundary
		if (forceBoundary
				&& (edge.getLeftFace() == null || edge.getOppositeEdge()
						.getLeftFace() == null)) {
			// System.out.println("Edge is on boundary");
			return false;
		}
		return checkLinkCondition(edge);
	}

	private boolean checkLinkCondition(E e) {
		V v1 = e.getStartVertex();
		V v2 = e.getTargetVertex();

		V o1 = e.getNextEdge().getTargetVertex();
		V o2 = e.getOppositeEdge().getNextEdge().getTargetVertex();

		List<V> s1 = HalfEdgeUtilsExtra.getVertexStar(v1);
		List<V> s2 = HalfEdgeUtilsExtra.getVertexStar(v2);
		List<V> s3 = new LinkedList<V>();

		for (V v : s1)
			if (s2.contains(v))
				s3.add(v);
		if (s3.contains(o1) && s3.contains(o2) && s3.size() == 2)
			return true;
		return false;
	}

	class EdgePQItem implements Comparable<EdgePQItem> {
		double error;
		E edge;
		double[] location;

		EdgePQItem(double error, E edge, double[] location) {
			// System.out.println("added " + edge + " error is " + error);
			this.error = error;
			this.edge = edge;
			this.location = location;
		}

		public int compareTo(EdgePQItem item) {
			return Double.compare(this.error, item.error);
		}

		public Set<E> collapse() {
			if (!isValidEdgeOperation(edge)) {
				// System.out.println("Edge is not valid "+edge.getIndex());
				return null;
			}
			// System.out.println("collapse edge: " + edge + " p.er=" + error);
			V v = null;

			// get edges with the same target vertex, those have to be relinked
			LinkedList<E> cTargetEdges = new LinkedList<E>();
			E o = edge.getOppositeEdge();
			E c = o.getNextEdge();
			E oc = c.getOppositeEdge();
			E actEdge = o;
			while (actEdge != oc) {
				actEdge = actEdge.getOppositeEdge().getPreviousEdge();
				cTargetEdges.add(actEdge);
			}

			if (checkLinkCondition(edge)) {
				// v = HalfEdgeTopologyOperations.collapseEdge(edge);
				v = HalfEdgeTopologyOperations.collapse(edge);
				if (v == null) { // topology not valid
					System.out
							.println("Skipping edge as link condition failed."); // this
																					// should
																					// not
																					// happen..
					return null;
				}
			}

			pa.setCoord(v, location);

			// get 2 ring (as sets)
			Set<E> ring2EdgeSet = new HashSet<E>();
			Set<V> ring2VertexSet = new HashSet<V>();

			List<V> ring1List = HalfEdgeUtilsExtra.getVertexStar(v);
			for (V ring1Vertex : ring1List) {
				ring2EdgeSet
						.addAll(HalfEdgeUtilsExtra.getEdgeStar(ring1Vertex));
				ring2VertexSet.addAll(HalfEdgeUtilsExtra
						.getVertexStar(ring1Vertex));
			}

			ring2VertexSet.addAll(ring1List);
			ring2EdgeSet.addAll(cTargetEdges);
			ring2EdgeSet.addAll(HalfEdgeUtilsExtra.getEdgeStar(v));

			// update weights within 2 ring
			for (V vr2 : ring2VertexSet) {
				computeQuadric(vr2);
			}

			// return edge set for further processing
			return ring2EdgeSet;
		}

	}

	private void prepareWeights() {
		List<V> vertices = activeMesh.getVertices();
		for (V v : vertices) {
			computeQuadric(v);
		}
		for (E e : activeMesh.getPositiveEdges()) {
			if (isValidEdgeOperation(e)) {
				EdgePQItem p = computeMinimum(e);
				edgeOperationMap.put(e, p);
				edgeOperationMap.put(e.getOppositeEdge(), p);
				pq.add(p);
			} else {
				// System.out.println("Skipped e.getIndex() = " + e.getIndex());
			}
		}
	}

	public void simplify(int numberOfSteps) {
		int i;
		System.out.println("Garland and Heckbert started with " + numberOfSteps
				+ " iterations... preparing weights");
		prepareWeights();
		int initalVertices = activeMesh.getVertices().size();
		for (i = 0; i < numberOfSteps; i++) {
			EdgePQItem p = pq.poll();
			if (p == null) {
				System.out.println("ERROR: There is nothing left to simplify!");
				return;
			}
			Set<E> l = p.collapse();
			if (l != null) {
				for (E e : l) {
					EdgePQItem edgePqItem = edgeOperationMap.get(e);
					edgeOperationMap.remove(e);
					int pqs = pq.size();
					pq.remove(edgePqItem);
					if (pq.size() == pqs) {
						// System.out.println("Removing failed: e.getIndex() = "
						// + e.getIndex());
					}
					if (isValidEdgeOperation(e)) {
						EdgePQItem pqi = computeMinimum(e);
						pq.add(pqi);
						edgeOperationMap.put(e, pqi);
					}
				}
				if (i % 5 == 0)
					System.out.println("Garland and Heckbert running, removed "
							+ i + " of " + numberOfSteps + " vertices");
			} else {
				i--; // enforce numberOfSteps
			}

		}
		int newVertices = activeMesh.getVertices().size();
		System.out.println("Reduced from " + initalVertices + " to "
				+ newVertices + " vertices");
	}

	public GarlandHeckbert(HEDS mesh, Coord3DAdapter<V> pa,
			NormalAdapter<F> na, AreaAdapter<F> aa) {
		activeMesh = mesh;
		vertexCount = mesh.numVertices();
		edgeCount = mesh.numEdges();
		this.pa = pa;
		this.na = na;
		this.aa = aa;
		pq = new PriorityQueue<EdgePQItem>();
	}

	private void computeQuadric(V v) {
		int j, k;
		double[] quadric_A = new double[9];
		double[] quadric_b = new double[3];
		double quadric_c = 0;

		double area;
		double d;

		List<F> faceStar = HalfEdgeUtilsExtra.getFaceStar(v);
		for (F f : faceStar) {
			double[] faceNormal = na.getNormal(f);
			area = aa.getArea(f); // weight by area maybe try 1 first...
			d = -Rn.innerProduct(faceNormal, pa.getCoord(v));
			quadric_c += area * d * d;
			for (j = 0; j < 3; j++) {
				quadric_b[j] += area * d * faceNormal[j];
				for (k = 0; k < 3; k++)
					// quadric_A = area * n * n^T
					quadric_A[j + 3 * k] += area * faceNormal[j]
							* faceNormal[k];
			}
		}
		quadric_map.remove(v);
		quadric_map.put(v, new Quadric(quadric_A, quadric_b, quadric_c));
	}

	private EdgePQItem computeMinimum(E e) {
		V v1 = e.getStartVertex();
		V v2 = e.getTargetVertex();

		Matrix3d A;
		Matrix3d Ainv;
		double[] b;
		double c;

		double[] v;
		double Qv;

		Quadric q1 = quadric_map.get(v1);
		Quadric q2 = quadric_map.get(v2);

		// sum up quadrics
		A = new Matrix3d(q1.A);
		A.add(q2.A);
		b = Rn.add(null, q1.b, q2.b);
		c = q1.c + q2.c;

		boolean v1ob = HalfEdgeUtils.isBoundaryVertex(v1);
		boolean v2ob = HalfEdgeUtils.isBoundaryVertex(v2);

		if (v1ob && !v2ob) {
			v = pa.getCoord(v1).clone();
		} else if (!v1ob && v2ob) {
			v = pa.getCoord(v2).clone();
		} else if (A.determinant() < Rn.TOLERANCE || (v1ob && v2ob)) {
			v = Rn
					.linearCombination(null, .5, pa.getCoord(v1), .5, pa
							.getCoord(v2));
		} else {
			Ainv = new Matrix3d(A);
			Ainv.invert();
			Tuple3d bT3d = new Vector3d(b);
			Tuple3d vT3d = new Vector3d();
			Ainv.transform(bT3d, vT3d);
			v = new double[3];
			vT3d.negate();
			vT3d.get(v);
		}
		// Q(v) = v^T A v + 2 b^T v + c

		double Av[] = new double[3];
		Tuple3d AvT3d = new Vector3d();
		Tuple3d vT3d = new Vector3d(v);
		A.transform(vT3d, AvT3d);
		AvT3d.get(Av);

		Qv = Math.abs(Rn.innerProduct(v, Av) + 2 * Rn.innerProduct(b, v) + c);
		return new EdgePQItem(Qv, e, v);
	}

}