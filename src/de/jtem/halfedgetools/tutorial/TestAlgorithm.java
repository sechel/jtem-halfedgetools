package de.jtem.halfedgetools.tutorial;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Area;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class TestAlgorithm {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double doSomething(HDS S, AdapterSet a) {
		double area = 0.0;
		for (F f : S.getFaces()) {
			area += a.get(Area.class, f, Double.class);
		}
		for (V v : S.getVertices()) {
			double[] p = a.getD(Position3d.class, v);
			Rn.times(p, area, p);
			a.set(Position.class, v, p);
		}
		return area;
	}
	
	
}
