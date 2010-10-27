package de.jtem.halfedgetools.bsp;


import static de.jtem.halfedge.util.HalfEdgeUtils.facesIncidentWithVertex;
import static de.jtem.halfedge.util.HalfEdgeUtils.incomingEdges;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public final class KdUtility {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> Collection<F> collectFacesInRadius(KdTree<V, E, F> kdTree, double[] p, double radius) {
		Collection<V> vertresult = kdTree.collectInRadius(p, radius);
		HashSet<F> faceSet = new HashSet<F>(vertresult.size() * 2);
		for (V v : vertresult) {
			faceSet.addAll(facesIncidentWithVertex(v));
		}
		Vector<F> result = new Vector<F>();
		result.addAll(faceSet);
		return result;
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Collection<E> collectEdgesInRadius(KdTree<V, E, F> kdTree, double[] p, double radius) {
		Collection<V> vertresult = kdTree.collectInRadius(p, radius);
		HashSet<E> edgeSet = new HashSet<E>(vertresult.size() * 3);
		for (V v : vertresult) {
			edgeSet.addAll(incomingEdges(v));
		}
		Vector<E> result = new Vector<E>();
		result.addAll(edgeSet);
		return result;
	}
	
}
