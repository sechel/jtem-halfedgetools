package de.jtem.halfedgetools.algorithm.delaunay;


import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import javax.vecmath.Point2d;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.util.triangulationutilities.ConsistencyCheck;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;

/**
 * Construct a delaunay triangulation from a given triangulation
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a> <a
 * href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a>
 * 
 * @author Stefan Sechelmann
 */
public class Delaunay <V extends Vertex<V, E, F>, E extends Edge<V, E, F>  & IsFlippable, F extends Face<V, E, F>> {
	

	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F>  & HasLength, F extends Face<V, E, F>> boolean isObtuse(
			F f) throws TriangulationException {
		for (E e : HalfEdgeUtils.boundaryEdges(f)) {
			if (isObtuse(e))
				return true;
		}
		return false;
	}

	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasLength, F extends Face<V, E, F>> boolean isObtuse(
			E e) throws TriangulationException {
		return getAngle(e) > Math.PI;
	}

	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & IsFlippable, F extends Face<V, E, F>> Integer getNumFlips(
			HalfEdgeDataStructure<V, E, F> graph) {
		Integer result = 0;
		for (E e : graph.getEdges())
			result += e.getFlipCount();
		return result;
	}

	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & IsFlippable, F extends Face<V, E, F>> Integer getNumEffectiveFlips(
			HalfEdgeDataStructure<V, E, F> graph) {
		Integer result = 0;
		for (E e : graph.getEdges())
			if (e.getFlipCount() > 0 && (e.getFlipCount() % 2) != 0)
				result++;
		return result;
	}

	/**
	 * Calculates the angle between edge and edge.getNextEdge() Sets the angle
	 * property of edge to the result
	 * 
	 * @param edge
	 * @return the angle at edge
	 * @throws TriangulationException
	 */
	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasLength, F extends Face<V, E, F>> Double getAngle(
			E edge) throws TriangulationException {
		Double a = edge.getLength();
		Double b = edge.getNextEdge().getLength();
		Double c = edge.getPreviousEdge().getLength();
		if ((a*a + b*b - c*c) / (2*a*b) > 1)
			throw new TriangulationException("Triangle inequation doesn't hold for " + edge);
		Double result = Math.abs(StrictMath.acos((a*a + b*b - c*c) / (2*a*b)));
//		System.err.println("angle between " + edge + " and " + edge.getNextEdge() + " is " + result);
		return result;
		
	}

	/**
	 * Checks whether this edge is locally delaunay
	 * 
	 * @param edge
	 *            the edge to check
	 * @return the check result
	 * @throws TriangulationException
	 */
	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasLength, F extends Face<V, E, F>> boolean isDelaunay(
			E edge) throws TriangulationException {
		// added boundary check...
		// if(HalfEdgeUtils.isInteriorEdge(edge)) {
		Double gamma = getAngle(edge.getNextEdge());
		Double delta = getAngle(edge.getOppositeEdge().getNextEdge());
		return gamma + delta <= Math.PI;
		// } else {
		// return true;
		// }
	}

	/**
	 * Checks whether this edge is locally delaunay, except for supplied edges
	 * 
	 * @param edge
	 *            the edge to check
	 * @return the check result
	 * @throws TriangulationException
	 */
	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasLength, F extends Face<V, E, F>> boolean isDelaunay(
			E edge, List<E> interior) throws TriangulationException {
		// added boundary check...
		if (interior.contains(edge)) {
			Double gamma = getAngle(edge.getNextEdge());
			Double delta = getAngle(edge.getOppositeEdge().getNextEdge());
			return gamma + delta <= Math.PI;
		} else {
			return true;
		}
	}

	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & HasLength, F extends Face<V, E, F>> boolean isDelaunay(
			HalfEdgeDataStructure<V, E, F> graph) {
		for (E edge : graph.getEdges()) {
			try {
				if (!isDelaunay(edge))
					return false;
			} catch (TriangulationException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the positive edges belonging to the kite of the edge
	 */
	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & IsFlippable, F extends Face<V, E, F>> List<E> getPositiveKiteBorder(
			E edge) {
		LinkedList<E> result = new LinkedList<E>();
		E e1 = edge.getNextEdge();
		E e2 = edge.getPreviousEdge();
		E e3 = edge.getOppositeEdge().getNextEdge();
		E e4 = edge.getOppositeEdge().getPreviousEdge();
		if (!e1.isPositive())
			e1 = e1.getOppositeEdge();
		if (!e2.isPositive())
			e2 = e2.getOppositeEdge();
		if (!e3.isPositive())
			e3 = e3.getOppositeEdge();
		if (!e4.isPositive())
			e4 = e4.getOppositeEdge();
		result.add(e1);
		result.add(e2);
		result.add(e3);
		result.add(e4);
		return result;
	}

//	public static class Overlays
//		<V extends Vertex<V, E, F> & HasXYZW, 
//		E extends Edge<V, E, F> & IsFlippable, 
//		F extends Face<V, E, F>
//	> {
//		public LinkedHashMap<E, Double> edges;
//		public V startV;
//		public V targetV;
//
//		public Overlays() {
//			edges = new LinkedHashMap<E, Double>();
//		}
//		
//		public IndexedLineSet getAsILS() {
//			
//			IndexedLineSet ilss = new IndexedLineSet();
//
//			int deg = edges.size();
//			double[][] vertices = new double[deg+2][];
//
//			// start vertex
//			double[] start = new double[4]; startV.getXYZW().get(start);
//			vertices[0] = new double[]{start[0], start[1], start[2]};
//			
//			int i = 1;
//			
//			for(E oe : edges.keySet()) {
//				double[] mid = new double[4];
//				getPointAtOverlay(oe).get(mid);
//				vertices[i] = new double[]{mid[0], mid[1], mid[2]};
//				i++;
//			}
//			
//			// end vertex
//			double[] end = new double[4]; targetV.getXYZW().get(end);
//			vertices[deg+1] = new double[]{end[0], end[1], end[2]};
//			
//			ilss = IndexedLineSetUtility.createCurveFromPoints(vertices, false);
//
//			return ilss;
//		}
//		
//		public Point3d getPointAtParameter(double t) {
//			if(t <= 0)
//				return VecmathTools.p4top3(startV.getXYZW());
//			if(t >= 1)
//				return VecmathTools.p4top3(targetV.getXYZW());
//			else {
//
//				double total_t = 0.0;
//				LinkedHashMap<Double, Point3d> lengths = new LinkedHashMap<Double, Point3d>();
//				
//				Point3d p1 = getPointAtParameter(0);
//				lengths.put(0.0, p1);
//				
//				for(E e : edges.keySet()) {
//					
//					Point3d p2 = getPointAtOverlay(e);
//					double l = p1.distance(p2);
//					total_t += l;
//					lengths.put(total_t, p2);
//					p1 = p2;
//				}
//				total_t += p1.distance(getPointAtParameter(1));
//				lengths.put(total_t, p1);
//				
//				t*=total_t;
//				
//				System.err.println("total_t: " + total_t + " t: " + t);
//				
//				Point3d sel_pt = null;
//				double prev = 0.0;
//				double newt = 0.0;
//				for(Double check : lengths.keySet()) {
//					if(t <= check) {
//						sel_pt = lengths.get(check);
//						newt = check;
//						break;
//					}
//					prev = check;
//				}
//	
//				
//				double[] pos1 = new double[4]; lengths.get(prev).get(pos1);
//				double[] pos2 = new double[4]; sel_pt.get(pos2);
//				double[] mid = new double[4];
//	
//				double u = (t/total_t - prev)/(newt - prev);
//				
//				Rn.linearCombination(mid, u, pos1, 1-u, pos2);
//				
//				Point4d p = new Point4d(mid);
//				return VecmathTools.p4top3(p);
//			}
//			
//		}
//		
//		public Point3d getPointAtOverlay(E oe) {
//			double[] pos1 = new double[4]; oe.getStartVertex().getXYZW().get(pos1);
//			double[] pos2 = new double[4]; oe.getTargetVertex().getXYZW().get(pos2);
//			double[] mid = new double[4];
//			double t = edges.get(oe);
//			Rn.linearCombination(mid, t, pos1, 1-t, pos2);
//			
//			Point4d p = new Point4d(mid);
//			return VecmathTools.p4top3(p);
//			
//		}
//	
//	}
	
	public static
		<
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F> & IsFlippable & HasLength, 
			F extends Face<V, E, F>
		> Point2d[] getLocalQuadFromEdge(E ab) throws TriangulationException{
		// calculate the crossing parameter t on this edge to save for overlay
		// this could go into flip() but i don't want to change interfaces
		E a1 = ab.getOppositeEdge().getNextEdge();
		E a2 = a1.getNextEdge();
		E b1 = ab.getNextEdge();
		
		Double la1 = a1.getLength();
		Double la2 = a2.getLength();
		Double lb1 = b1.getLength();
		
		Double alpha = Delaunay.getAngle(ab) + Delaunay.getAngle(a2);
		Double beta = Delaunay.getAngle(a1);
		
	//	System.err.println("alpha = " + alpha + " beta = " + beta);
	
		// coordinates for calculating t
		Point2d p1 = new Point2d(StrictMath.cos(beta)*la1, StrictMath.sin(beta)*la1);
		Point2d p2 = new Point2d(la2, 0);
		Point2d p3 = new Point2d(0,0);
		Point2d p4 = new Point2d(la2+StrictMath.cos(Math.PI - alpha) * lb1, StrictMath.sin(Math.PI - alpha) *  lb1);
		
		return new Point2d[] {p1,p2,p3,p4};
	}
	
	
//	private static
//		<
//			V extends Vertex<V, E, F>, 
//			E extends Edge<V, E, F> & IsFlippable & HasLength, 
//			F extends Face<V, E, F>
//		> double getOverlayParameter(E ab, double ta, double tb) throws TriangulationException{
//	
//		Point2d[] q = getLocalQuadFromEdge(ab);
//	
//		Point2d p = QuadGeometry.diagonalIntersection(q, new double[] {0,2,ta,tb});
//		// TODO check for degeneracy here (vertical line)
//		// and then use y coordinate instead
//		double t = (p.x-q[1].x)/(q[0].x-q[1].x);
//	
//		return t;
//	}
	
//	private static
//		<
//			V extends Vertex<V, E, F>, 
//			E extends Edge<V, E, F> & IsFlippable & HasLength, 
//			F extends Face<V, E, F>
//		> double getRescaledOverlayParameter(E ab, double ta, double tb, E e) throws TriangulationException{
//		
//		System.err.println("ta: " + ta + " tb: " + tb);
//		double l = getOverlayParameter(ab, ta, tb);
//	
//		double totalLenght = ab.getLength();
//		double localLength = QuadGeometry.getLenghtOfOverlay(getLocalQuadFromEdge(ab), new double[] {ta,tb});
//		
//		
//		l = l * (localLength/totalLenght);
//		
//		System.err.println("l: " + l);
//		
//		return l;
//	}

//	/**
//	 * Constructs the delaunay triangulation of the given structure, and creates
//	 * overlay structure
//	 * 
//	 * @param graph
//	 *            must be a triangulation
//	 * @throws TriangulationException
//	 *             if the given graph is no triangulation or if the trangle
//	 *             inequation doesn't hold for some triangle
//	 */
//	@SuppressWarnings("unchecked")
//	public static 
//		<
//			V extends Vertex<V, E, F> & HasXYZW, 
//			E extends Edge<V, E, F> & IsFlippable & HasLength, 
//			F extends Face<V, E, F>
//		> 
//		HalfEdgeDataStructure<V, E, F> constructDelaunay(
//			HalfEdgeDataStructure<V, E, F> graph,
//			LinkedHashMap<E, Overlays<V, E, F>> eToOverlayMap) throws TriangulationException {
//		
//		System.err.println("Flipping delaunay...");
//		if (!ConsistencyCheck.isTriangulation(graph))
//			throw new TriangulationException("Graph is no triangulation!");
//
//		// create copy of original
//		HalfEdgeDataStructure<V, E, F> original = graph
//				.createCombinatoriallyEquivalentCopy(graph.getVertexClass(),
//						graph.getEdgeClass(), graph.getFaceClass());
//
//		// update metric
//		for(E e : original.getEdges())
//			e.setLength(graph.getEdge(e.getIndex()).getLength());
//		
//		// which edges to flip
//		HashSet<E> markSet = new HashSet<E>();
//		Stack<E> stack = new Stack<E>();
//		for (E positiveEdge : graph.getPositiveEdges()) {
//
//			F leftFace = positiveEdge.getLeftFace();
//			F rightFace = positiveEdge.getRightFace();
//			if (leftFace == rightFace) {
//				System.err.println("precaution: leftFace == rightFace");
////				return null;
//			} else {
//
//				markSet.add(positiveEdge);
//				stack.push(positiveEdge);
//			}
//		}
//		while (!stack.isEmpty()) {
//			E ab = stack.pop();
//			markSet.remove(ab);
//			if (!isDelaunay(ab)) {
//
//				// keep track of the edge that gets flipped, but on the original graph
//				V start = ab.getStartVertex();
//				V end = ab.getTargetVertex();
//				E originalBeforeFlip = HalfEdgeUtils.findEdgeBetweenVertices(original.getVertex(start.getIndex()), original.getVertex(end.getIndex()));
//
//				// actual flip
//				double t = getOverlayParameter(ab, 0, 2);
//				ab.flip();
//
//				if(ab.getFlipCount() > 1)
//					System.err.println("flipping edge " + ab.getIndex() + " for the " + ab.getFlipCount() + ":th time");
//
//				// store information about overlays
//				Overlays<V, E, F> overlay = new Overlays<V, E, F>();
//				overlay.startV = ab.getStartVertex();
//				overlay.targetV = ab.getTargetVertex();
//				
//				// this is our square on the (flipped) graph
//				List<V> vs = new LinkedList<V>();
//				vs.add(ab.getStartVertex()); // 0
//				vs.add(ab.getTargetVertex()); // 2
//				vs.add(start); // 1
//				vs.add(end);  // 3
//				
//				// this is the corresponding square on the original graph
//				List<V> ovs = new LinkedList<V>();
//				for(V v : vs)
//					ovs.add(original.getVertex(v.getIndex()));
//
//				// this is our edges of our square
//				LinkedList<E> es = new LinkedList<E>();
//				es.add(HalfEdgeUtils.findEdgeBetweenVertices(vs.get(2), vs.get(0))); //ostart-start
//				es.add(HalfEdgeUtils.findEdgeBetweenVertices(vs.get(3), vs.get(0))); //oend-start
//				es.add(HalfEdgeUtils.findEdgeBetweenVertices(vs.get(3), vs.get(1))); //oend-end
//				es.add(HalfEdgeUtils.findEdgeBetweenVertices(vs.get(2), vs.get(1))); //ostart-end
//
//				// DEBUG checkpoints
//				if(vs.get(2).getIndex() == 67 && vs.get(3).getIndex() == 61) {
//					System.err.println("missing e is " + ab.getIndex());
//				}
//				
//				if(vs.get(2).getIndex() == 66 && vs.get(3).getIndex() == 61) {
//					System.err.println("missing e is " + ab.getIndex());
//				}
//				
//				if(vs.get(0).getIndex() == 65 && vs.get(1).getIndex() == 67) {
//					System.err.println("missing e is " + ab.getIndex());
//				}
//				
//				LinkedList<LinkedHashMap<E, Double>> ees = new LinkedList<LinkedHashMap<E, Double>>();
//
//				// super lame hack
//				boolean globalOrder = false;
//				
//				// for each edge in the square
//				// if it already has overlays, they get added, because they need to be intersected too
//				int i = 1;
//				for (E e : es) {
//					if(e != null) 
//						if(eToOverlayMap.get(e) != null)
//							ees.add(eToOverlayMap.get(e).edges);
//						else
//							if(e.getOppositeEdge() != null)
//								if(eToOverlayMap.get(e.getOppositeEdge()) != null) {
//									ees.add(eToOverlayMap.get(e.getOppositeEdge()).edges);
//									globalOrder = true;
//								}
//
////					System.err.println("ee" + i + " not empty");
//					i++;
//				}
//
//				// the edges that get added, before and after the original-before-flip edge
//				LinkedHashMap<E, Double> preEdges = new LinkedHashMap<E, Double>();
//				LinkedHashMap<E, Double> postEdges = new LinkedHashMap<E, Double>();
//
//				// now go through all original edges to see if they are used as crossings
//				// in this quad
//				// these are the only ones that can be crossed
//				for (E e : original.getEdges()) {
//					
//					// check when original edge is connected to vertices of square (first flip)
//					for (LinkedHashMap<E, Double> checker : ees) {
//						if ( (ovs.contains(e.getStartVertex()) || ovs.contains(e.getTargetVertex()))
//								&& (checker.keySet().contains(e))) {
//							
//							double ta = checker.get(e); // the offset issue
//							ta += ees.indexOf(checker);
//							int tbi = ovs.contains(e.getStartVertex()) ? ovs.indexOf(e.getStartVertex()) : ovs.indexOf(e.getTargetVertex());
//							double tb = tbi == 2 ? 1 : 3; // this could be more nicely.. eh
//							
//							double l = getRescaledOverlayParameter(e, ta, tb, e);
//
//							if(l > 0 && l < 1) {
//								System.err.println("added nested overlay, from vertex: " + e + " " + l);
//								postEdges.put(e, l);
//							}
//						}
//					}
//					
//					// ugly check in square (for second flip and after)
//					// if an (original) edge turns up at least twice, it might be crossed
//					LinkedList<LinkedHashMap<E, Double>> checkers = new LinkedList<LinkedHashMap<E, Double>>();
//					for (LinkedHashMap<E, Double> checker : ees) {
//						if(checker.keySet().contains(e)) {
//							checkers.add(checker);
//						}
//					}
//					// then a proper tt should also be set
//					if(checkers.size() > 1) { // should never be > 2
//						
//						double ta = checkers.get(0).get(e); // the offset issue
//						ta += ees.indexOf(checkers.get(0));
//						double tb = checkers.get(1).get(e); // the offset issue
//						tb += ees.indexOf(checkers.get(1));
//						
//						double l = getRescaledOverlayParameter(e, ta, tb, e);
//						
//						if(l > 0 && l < 1) {
//							System.err.println("added nested overlay: " + l);
//							// FIXME, pre or post?
//							postEdges.put(e, l);
//						}
//					}
//
//				}
//
//				// this is all overlays in order that should be added
//				LinkedHashMap<E, Double> toAdd = new LinkedHashMap<E, Double>();
//				
////				Collections.reverse(preEdges);
//				toAdd.putAll(preEdges);
//				
//				if(!globalOrder && originalBeforeFlip != null) {
//					System.err.println("Adding orig.bf " + t + " global order = " + globalOrder + " " + originalBeforeFlip);
//					toAdd.put(originalBeforeFlip, t);
//				}
//					
////				Collections.reverse(postEdges);
//				toAdd.putAll(postEdges);
//				
//				if(globalOrder && originalBeforeFlip != null) {
//					System.err.println("Adding orig.bf " + (1-t) + " global order = " + globalOrder + " " + originalBeforeFlip);
//					toAdd.put(originalBeforeFlip, t); // 1-t ?
//				}
//				
////				Collections.reverse(toAdd);
//				
//				overlay.edges.clear();
//				overlay.edges.putAll(toAdd);
//
//				int n = (preEdges.size() + postEdges.size() + 1);
//				if(n > 1) {
//					System.err.println("Adding " + n + " overlays:");
//					for(E e : toAdd.keySet())
//						System.err.println(e);
//				}
//
//				eToOverlayMap.put(ab, overlay);
//				
//				
//				// get rest of edges to flip
//				for (E xy : getPositiveKiteBorder(ab)) {
//					if (!markSet.contains(xy)) {
//						markSet.add(xy);
//						stack.push(xy);
//					}
//				}
//			}
//		}
//
//		return original;
//	}

	/**
	 * Constructs the delaunay triangulation of the given structure.
	 * 
	 * @param graph
	 *            must be a triangulation
	 * @throws TriangulationException
	 *             if the given graph is no triangulation or if the trangle
	 *             inequation doesn't hold for some triangle
	 */
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F> & IsFlippable & HasLength, 
		F extends Face<V, E, F>
	> void constructDelaunay(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException {
		System.err.println("Flipping delaunay...");
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("Graph is no triangulation!");


//		E lastEdge = null;
//		E secondLastEdge = null;
		
		HashSet<E> markSet = new HashSet<E>();
		Stack<E> stack = new Stack<E>();
		for (E positiveEdge : graph.getPositiveEdges()) {

			F leftFace = positiveEdge.getLeftFace();
			F rightFace = positiveEdge.getRightFace();
			if (leftFace == rightFace) {
				System.err.println("precaution: leftFace == rightFace");
				return;
			} else {

				markSet.add(positiveEdge);
				stack.push(positiveEdge);
			}
		}
		while (!stack.isEmpty()) {
			E ab = stack.pop();
			markSet.remove(ab);
			if (!isDelaunay(ab)) {

//				if(secondLastEdge == ab) {
//					System.err.println("infinite loop: " + ab.getIndex());
//					System.err.println("edge info: length " + ab.getLength());
//					break;
//				} else {
					ab.flip();
//					secondLastEdge = lastEdge;
//					lastEdge = ab;
					System.err.println("flipping edge " + ab.getIndex()
							+ " for the " + ab.getFlipCount() + ":th time");
					for (E xy : getPositiveKiteBorder(ab)) {
						if (!markSet.contains(xy)) {
							markSet.add(xy);
							stack.push(xy);
						}
					}
//				}
			}
		}
	}

	/**
	 * Constructs the delaunay triangulation of the given structure, with
	 * boundary check
	 * 
	 * @param graph
	 *            must be a triangulation
	 * @throws TriangulationException
	 *             if the given graph is no triangulation or if the trangle
	 *             inequation doesn't hold for some triangle
	 */
//	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & IsFlippable, F extends Face<V, E, F>> void constructDelaunay(
//			HalfEdgeDataStructure<V, E, F> graph, List<E> interior)
//			throws TriangulationException {
//		System.err.println("Flipping delaunay...");
//		if (!ConsistencyCheck.isTriangulation(graph))
//			throw new TriangulationException("Graph is no triangulation!");
//
//		HashSet<E> markSet = new HashSet<E>();
//		Stack<E> stack = new Stack<E>();
//		for (E positiveEdge : graph.getPositiveEdges()) {
//			if (interior.contains(positiveEdge)) {
//				markSet.add(positiveEdge);
//				stack.push(positiveEdge);
//			}
//		}
//		while (!stack.isEmpty()) {
//			E ab = stack.pop();
//			markSet.remove(ab);
//			if (!isDelaunay(ab, interior)) {
//				ab.flip();
//				for (E xy : getPositiveKiteBorder(ab)) {
//					if (!markSet.contains(xy)) {
//						if (interior.contains(xy)) {
//							markSet.add(xy);
//							stack.push(xy);
//						}
//					}
//				}
//			}
//		}
//	}
}
