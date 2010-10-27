package de.jtem.halfedgetools.util;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;

public class GeometryUtility {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double getMeanEdgeLength(HDS mesh, AdapterSet a) {
		double result = 0.0;
		for (E e : mesh.getPositiveEdges()) {
			double[] s = a.get(Position.class, e.getStartVertex(), double[].class);
			double[] t = a.get(Position.class, e.getTargetVertex(), double[].class);
			result += Rn.euclideanDistance(s, t);
		}
		return result / mesh.numEdges();
	}
	
}
