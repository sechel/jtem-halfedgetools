package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter2d;
import de.jtem.halfedgetools.adapter.type.generic.Position2d;

@Position2d
public class Position2dAdapter extends AbstractAdapter<double[]> {

	public Position2dAdapter() {
		super(double[].class, true, false);
	}
	
	@Override
	public <T extends Node<?, ?, ?>> boolean canAccept(Class<T> nodeClass) {
		return true;
	}
	
	@Override
	public double getPriority() {
		return -1;
	}
	
	public static double[] convertCoordinate(double[] c) {
		switch (c.length) {
		case 2: return c;
		case 3: return new double[] {c[0], c[1]};
		case 4: 
			// interpret c[3] as homgeneous coordinate
			return new double[] {c[0] / c[3], c[1] / c[3]};
		default:
			throw new IllegalArgumentException("cannot convert coordinate in Position2dAdapter");
		}
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		double[] r = a.getDefault(Position.class, v, new double[] {0, 0});
		return convertCoordinate(r);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		if (a.isAvailable(Position.class, e.getClass(), double[].class)) {
			double[] pos = a.getD(Position.class, e);
			return convertCoordinate(pos);
		} else {
			return a.getD(BaryCenter2d.class, e);
		}
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		if (a.isAvailable(Position.class, f.getClass(), double[].class)) {
			double[] pos = a.getD(Position.class, f);
			return convertCoordinate(pos);
		} else {
			return a.getD(BaryCenter2d.class, f);
		}
	}
	
	@Override
	public String toString() {
		return "Position 2D";
	}
	
}
