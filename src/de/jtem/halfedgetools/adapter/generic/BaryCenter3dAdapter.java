package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.BeadPosition;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;

@BaryCenter3d
@BeadPosition
public class BaryCenter3dAdapter extends AbstractAdapter<double[]> {

	public BaryCenter3dAdapter() {
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
		case 2: return new double[] {c[0], c[1], 0};
		case 3: return c;
		case 4: 
			// interpret c[3] as homgeneous coordinate
			return new double[] {c[0] / c[3], c[1] / c[3], c[2] / c[3]};
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
		double[] r = a.getDefault(BaryCenter.class, v, new double[] {0, 0, 0});
		return convertCoordinate(r);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] r = a.getDefault(BaryCenter.class, e, new double[] {0, 0, 0});
		return convertCoordinate(r);
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] r = a.getDefault(BaryCenter.class, f, new double[] {0, 0, 0});
		return convertCoordinate(r);
	}
	
	@Override
	public String toString() {
		return "Barycenter 3D";
	}
	
}
