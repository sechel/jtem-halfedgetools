package de.jtem.halfedgetools.symmetry2;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import de.discretization.halfedge.hds.DEdge;
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
			if (g != null) System.err.println(e.getIndex()+":"+e.getStartVertex().getIndex()+":"+e.getTargetVertex().getIndex()+" dge = "+g.getWord());
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
		for (E e : hds.getEdges()) {
			System.err.println(e.getIndex()+":"+e.getStartVertex().getIndex()+":"+e.getTargetVertex().getIndex());
		}

		// pull back all vertices into the fund dom
		for (V v : hds.getVertices())	{
			double[] p0 = a.getD(Position3d.class, v);
			DiscreteGroupElement dge0 = new DiscreteGroupElement();
			
			// find the group element which brings the point into the fundamental domain
			double[] cp0 = DiscreteGroupUtility.getCanonicalRepresentative2(null, p0, dge0, fundDom, group);
			a.set(CanonicalPosition.class, v, cp0);
//			if (Rn.isIdentityMatrix(dge0.getArray(), 10E-8)) continue;
			// we are interested in the inverse, which brings the canon. repn. to its original position in covering space
			DiscreteGroupElement idge0 = dge0.getInverse();
			if (dge0.getWord().length() != 0) {
				System.err.println("vertex "+v.getIndex()+" "+idge0.getWord()+" "+Rn.toString(p0)+" :: "+Rn.toString(cp0));
				a.set(GroupElement.class, v, idge0);
			}
		}
		// store off group elements for edges based on vertex results above
		// we've already found the inverse elements; edge elements are determined by product of start/end vertex elements
		for (E e: hds.getEdges()) {
			V v0 = e.getStartVertex(), v1 = e.getTargetVertex();
			DiscreteGroupElement g0 = a.get(GroupElement.class, v0, DiscreteGroupElement.class);
			g0 = (g0 == null) ? new DiscreteGroupElement() : g0;
			DiscreteGroupElement g1 = a.get(GroupElement.class, v1, DiscreteGroupElement.class);
			g1 = (g1== null) ? new DiscreteGroupElement() : g1;
			DiscreteGroupElement dge = new DiscreteGroupElement( g1);
			dge.multiplyOnRight(g0.getInverse());
			a.set(GroupElement.class, e, dge);
		}
		Collection<V> bv = HalfEdgeUtils.boundaryVertices(hds);
		for (E e0: HalfEdgeUtils.boundaryEdges(hds) ){
			for (E e1: HalfEdgeUtils.boundaryEdges(hds) ){
				if (!e0.isValid() || !e1.isValid() || e0 == e1) continue;
				V 		v00 = e0.getStartVertex(),
						v01 = e0.getTargetVertex(),
						v10 = e1.getTargetVertex(),
						v11 = e1.getStartVertex();
				double[] cp00 = a.getD(CanonicalPosition.class, v00),
						cp01 = a.getD(CanonicalPosition.class, v01),
						cp10 = a.getD(CanonicalPosition.class, v10),
						cp11 = a.getD(CanonicalPosition.class, v11);
				if (Rn.euclideanDistanceSquared(cp00, cp10) > 10E-8  ||
						Rn.euclideanDistanceSquared(cp01, cp11) > 10E-8) {
					continue;
				}
				System.err.println("Vertices "+v00.getIndex()+":"+v01.getIndex()+"::"+v10.getIndex()+":"+v11.getIndex());
				DiscreteGroupElement g00, g01, g10, g11;
				g00 = a.get(GroupElement.class, v00, DiscreteGroupElement.class);
				g01 = a.get(GroupElement.class, v01, DiscreteGroupElement.class);
				g10 = a.get(GroupElement.class, v10, DiscreteGroupElement.class);
				g11 = a.get(GroupElement.class, v11, DiscreteGroupElement.class);
				g00 = (g00 == null) ? new DiscreteGroupElement() : g00;
				g01 = (g01 == null) ? new DiscreteGroupElement() : g01;
				g10 = (g10 == null) ? new DiscreteGroupElement() : g10;
				g11 = (g11 == null) ? new DiscreteGroupElement() : g11;
				DiscreteGroupElement prod = new DiscreteGroupElement(g00);
				prod = prod.getInverse();
				prod.multiplyOnRight(g10);
				prod.multiplyOnRight(g11.getInverse());
				prod.multiplyOnRight(g01);
				if (Rn.isIdentityMatrix(prod.getArray(), 10E-4))	{
					System.err.println("found matching edges "+e0.getIndex()+" "+e1.getIndex());
					// remove these edges and relink
					identifyBoundaryVertices(hds, v00, v10);
					if (v00 != v01 || v10 != v11) identifyBoundaryVertices(hds, v01, v11);
					E eo0 = e0.getOppositeEdge();
					E eo1 = e1.getOppositeEdge();
					eo0.linkOppositeEdge(eo1);
					eo1.linkOppositeEdge(eo0);
					if (e0.getPreviousEdge() != e0) e0.getPreviousEdge().linkNextEdge(e1.getNextEdge());
					if (e1.getPreviousEdge() != e1) e1.getPreviousEdge().linkNextEdge(e0.getNextEdge());
					hds.removeEdge(e0);
					hds.removeEdge(e1);
				}
				
				
			}
			System.err.println("hds has "+hds.getEdges().size()+" edges");
		}
		// remove duplicate vertices
		if (false)
		for (V v0: bv) {
			for (V v1:  bv)	{
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
					identifyBoundaryVertices(hds, v0, v1);
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
		List<E> edges1 = HalfEdgeUtils.incomingEdges(v1);
		for (E e: edges1)	{
			e.setTargetVertex(v0);
		}
//		edges1 = HalfEdgeUtils.outgoingEdges(v1);
//		for (E e: edges1)	{
//			e.setStartVertex(v0);
//		}
		hds.removeVertex(v1);
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
