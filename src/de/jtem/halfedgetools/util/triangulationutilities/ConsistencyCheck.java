package de.jtem.halfedgetools.util.triangulationutilities;


import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;


/**
 * Basic triangulation consistency
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ConsistencyCheck {

	// Don't instatiate.
	private ConsistencyCheck() {}
	
	public static boolean isTriangulation(HalfEdgeDataStructure<?, ?, ?> heds){
		for (Face<?, ?, ?> face : heds.getFaces()){
			if (HalfEdgeUtilsExtra.getBoundary(face).size() != 3){
//				message("Face " + face + " has not 3 edges (it has " + HalfEdgeUtilsExtra.getBoundary(face).size() + ")");
				return false;
			}
		}
		return true;
	}
	
//	public static  <
//		V extends Vertex<V, E, F> & HasXYZW,
//		E extends Edge<V, E, F> & HasLength,
//		F extends Face<V, E, F>
//	> boolean checkEdgeLengths(HalfEdgeDataStructure<V, E, F> graph){
//		for (E e : graph.getEdges()){
//			E e1 = e.getNextEdge();
//			E e2 = e.getPreviousEdge();
//			if (e.getLength() > e1.getLength() + e2.getLength())
//				return false;
//		}
//		return true;
//	}
	
	
	/**
	 * Checks if the given heds is a valid sphere 
	 * @param heds
	 * @return the check result
	 */
	public static boolean isSphere(HalfEdgeDataStructure<?, ?, ?> heds){
		//check for border
		for (Edge<?,?,?> edge : heds.getEdges()){
			if (edge.getLeftFace() == null || edge.getOppositeEdge() == null){
//				message("triangulation has border at edge " + edge);
				return false;
			}
		}
		//check if it has only triangles
		if (!isTriangulation(heds))
			return false;
		//check euler characteristics
		if (heds.numVertices() - heds.numEdges() / 2 + heds.numFaces() != 2){
//			message("triangulation is no sphere: V-E+F!=2");
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks if the given heds is a valid disk 
	 * @param heds
	 * @return the check result
	 */
//	public static boolean isDisk(HalfEdgeDataStructure<?, ?, ?> heds){
//		//check if it has only triangles
//		if (!isTriangulation(heds))
//			return false;
//		//check euler characteristics
//		if (heds.numVertices() - heds.numEdges() / 2 + heds.numFaces() != 1){
//			message("triangulation is no sphere: V-E+F!=1");
//			return false;
//		}
//		return true;
//	}
	
	
	
//	public static boolean getDebug() {
//		return DBGTracer.isActive();
//	}

	
//	private static void message(String m) {
//		if (ConsistencyCheck.getDebug()) {
//			System.err.println(ConsistencyCheck.class.getSimpleName() + ": " + m);
//		}
//	}
}
