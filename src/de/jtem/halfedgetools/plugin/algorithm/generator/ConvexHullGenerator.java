package de.jtem.halfedgetools.plugin.algorithm.generator;

import java.util.LinkedList;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class ConvexHullGenerator extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public String getAlgorithmName() {
		return "Convex Hull";
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		// remove faces and edges
		for (F f : new LinkedList<F>(hds.getFaces())) {
			hds.removeFace(f);
		}
		for (E e : new LinkedList<E>(hds.getEdges())) {
			hds.removeEdge(e);
		}
		// convex hull of vertices
		TypedAdapterSet<double[]> da = a.querySet(double[].class);
		ConvexHull.convexHull(hds, da, 1E-8);
		hi.update();
	}

}
