package de.jtem.halfedgetools.symmetry2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.discretization.halfedge.hds.DVertex;
import de.jreality.math.Matrix;
import de.jreality.math.P3;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class QuotientMeshUtility {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	// assign coordinates in the universal cover to the vertices: store in the half-edge as 
	// target vertex.
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void assignCoveringSpaceCoordinates(HDS hds, AdapterSet a) {
		
		Queue<E> edgesTodo = new LinkedList<E>();
		Set<E> edgesDone = new HashSet<E>();
		Map<V, Matrix> vertexMatrices = new HashMap<V, Matrix>();
		E e0 = hds.getEdge(0);		// start
		// startup
		edgesTodo.add(e0);
		edgesTodo.add(e0.getOppositeEdge());
		while (!edgesTodo.isEmpty())	{
			E e = edgesTodo.poll();
			// process this triangle
			edgesDone.add(e);
			edgesDone.add(e.getNextEdge());
			edgesDone.add(e.getPreviousEdge());
			System.err.println(e.toString());
			unwrapTargetVertex(e, vertexMatrices, a);
			unwrapTargetVertex(e.getNextEdge(), vertexMatrices, a);
			unwrapTargetVertex(e.getPreviousEdge(), vertexMatrices, a);
			// do combinatorics
			E next = e.getNextEdge();
			if (!edgesDone.contains(next.getOppositeEdge())) {
				edgesTodo.add(next.getOppositeEdge());
			}
			E prev = e.getPreviousEdge();
			if (!edgesDone.contains(prev.getOppositeEdge())) {
				edgesTodo.add(prev.getOppositeEdge());
			}
		}
//		for (F f : hds.getFaces())	{
//			double[] accumulatedMatrix = Rn.identityMatrix(4);
//			// TODO: fix this to follow the edges around
//			System.err.println("next face");
//			for (E e : HalfEdgeUtils.boundaryEdges(f))	{
//				DiscreteGroupElement g = a.get(GroupElement.class, e, DiscreteGroupElement.class);
//				// accumulate the transformation
//				accumulatedMatrix = Rn.times(null, g.getArray(), accumulatedMatrix);
//				V v = e.getTargetVertex();
//			}
//		}
		for (E e : hds.getEdges()) {
			DiscreteGroupElement g = a.get(GroupElement.class, e, DiscreteGroupElement.class);
			if (g != null) System.err.println(e.getIndex()+" dge = "+g.getWord());
		}
		
	}

	static final Matrix identity = new Matrix();
	protected static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void unwrapTargetVertex(E e, Map<V, Matrix> vertexMatrices, AdapterSet a) {
		V v0 = e.getStartVertex();
		V v1 = e.getTargetVertex();
		Matrix accumulated = vertexMatrices.get(v0);
		accumulated = (accumulated == null) ? new Matrix() : accumulated;
		DiscreteGroupElement g = a.get(GroupElement.class, e, DiscreteGroupElement.class);
		// first do group element, then accumulated tform
		accumulated.multiplyOnRight(g != null ? g.getMatrix() : identity);
		double[] canonicalCoordV =  a.getD(CanonicalPosition.class, v1);
		double[] universalCoverV = Rn.matrixTimesVector(null, accumulated.getArray(), canonicalCoordV);
		a.set(Position.class, e, universalCoverV);
		vertexMatrices.put(v1, accumulated);
		System.err.println("position = "+Rn.toString(universalCoverV));
	}
	/**
	 * The input should be a mesh in the covering space which is also a fundamental domain.  
	 * @param hds
	 * @param a
	 */
	public static <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> void  assignCanonicalCoordinates( HDS hds, AdapterSet a, WallpaperGroup group, IndexedFaceSet fundDom) {
		// pull back all vertices into the fund dom
		for (V v : hds.getVertices())	{
			double[] p0 = a.getD(Position3d.class, v);
			DiscreteGroupElement dge0 = new DiscreteGroupElement();
			double[] cp0 = DiscreteGroupUtility.getCanonicalRepresentative2(null, p0, dge0, fundDom, group);
			a.set(CanonicalPosition.class, v, cp0);
//			if (Rn.isIdentityMatrix(dge0.getArray(), 10E-8)) continue;
			DiscreteGroupElement idge0 = dge0.getInverse();
			if (dge0.getWord().length() != 0) {
				System.err.println("vertex "+v.getIndex()+" "+idge0.getWord()+" "+Rn.toString(p0)+" :: "+Rn.toString(cp0));
				a.set(GroupElement.class, v, idge0);
			}
		}
		// remove duplicate vertices
		for (V v0: HalfEdgeUtils.boundaryVertices(hds)) {
			for (V v1: HalfEdgeUtils.boundaryVertices(hds))	{
				if (! v0.isValid() || !v1.isValid() || v0.equals(v1)) continue;
				System.err.println("checking "+v0.getIndex()+" against "+v1.getIndex());
				double[] cp0 = a.getD(CanonicalPosition.class, v0);
				double[] cp1 = a.getD(CanonicalPosition.class, v1);
				// TODO: make this metric neutral
				double d = Rn.euclideanDistance(cp0, cp1);
				if (d < 10E-8) {
					double[] p0 = a.getD(Position.class, v0);
					double[] p1 = a.getD(Position.class, v1);
					System.err.println("identifying "+Rn.toString(p0)+" and "+Rn.toString(p1));
					DiscreteGroupElement dge = a.get(GroupElement.class, v0, DiscreteGroupElement.class);
					if (dge == null) identifyBoundaryVertices(hds, v0, v1);
					else identifyBoundaryVertices(hds, v1,  v0);
				}
			}
		}
	}
	public static <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
>  void identifyBoundaryVertices(HDS hds, V v0, V v1)	{
		List<E> edges0 = HalfEdgeUtils.incomingEdges(v0),
				edges1 = HalfEdgeUtils.incomingEdges(v1);
		E in0 = null, in1 = null, in0Next = null, in1Next = null;
		for (E e: edges0)	{
			if (e.getLeftFace() == null)	{
				in0 = e;
				in0Next = in0.getNextEdge();
			}
		}
		for (E e: edges1)	{
			if (e.getLeftFace() == null)	{
				in1 = e;
				in1Next = in1.getNextEdge();
			}
			// replace v1 by v0
			e.setTargetVertex(v0);
		}
		hds.removeVertex(v1);
		in0.linkNextEdge(in1Next);
		in1.linkNextEdge(in0Next);
		// now test if there's more to identify
		if (in0.getStartVertex() == in1Next.getTargetVertex() && !(in0.getStartVertex() == v1)) {
			in0.getOppositeEdge().linkOppositeEdge(in1Next.getOppositeEdge());
			hds.removeEdge(in0);
			hds.removeEdge(in1Next);
		}
		if (in1.getStartVertex() == in0Next.getTargetVertex() && !(in1.getStartVertex() == v0)) {
			in1.getOppositeEdge().linkOppositeEdge(in0Next.getOppositeEdge());
			hds.removeEdge(in1);
			hds.removeEdge(in0Next);
		}
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void explodeFaces(HDS exploded, HDS hds, AdapterSet a) {
		for (F f : hds.getFaces())	{
			F fc = exploded.addNewFace();
			E e0 = exploded.addNewEdge(); 
			E e0o = exploded.addNewEdge(); 
			E e1 = exploded.addNewEdge(); 
			E e1o = exploded.addNewEdge(); 
			E e2 = exploded.addNewEdge(); 
			E e2o = exploded.addNewEdge();
			V v0 = exploded.addNewVertex();
			V v1 = exploded.addNewVertex();
			V v2 = exploded.addNewVertex();
			e0.setLeftFace(fc);
			e1.setLeftFace(fc);
			e2.setLeftFace(fc);
			e0.linkOppositeEdge(e0o);
			e1.linkOppositeEdge(e1o);
			e2.linkOppositeEdge(e2o);
			e0.linkNextEdge(e1);
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e0);
			e0o.linkNextEdge(e2o);
			e1o.linkNextEdge(e0o);
			e2o.linkNextEdge(e1o);
			e0.setTargetVertex(v0);
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v2);
			e0o.setTargetVertex(v2);
			e1o.setTargetVertex(v1);
			e2o.setTargetVertex(v0);
			
			E ee0 = f.getBoundaryEdge();
			E ee1 = ee0.getNextEdge();
			E ee2 = ee1.getNextEdge();
			double[] p0 = a.getD(Position.class, ee0);
			double[] p1 = a.getD(Position.class, ee1);
			double[] p2 = a.getD(Position.class, ee2);
			a.set(Position.class, v0, p0);
			a.set(Position.class, v1, p1);
			a.set(Position.class, v2, p2);
		}
	}

}
