package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TextureBaryCenter;
import de.jtem.halfedgetools.adapter.type.generic.TextureBaryCenter4d;

@TextureBaryCenter4d
public class TextureBaryCenter4dAdapter extends AbstractAdapter<double[]> {

	public TextureBaryCenter4dAdapter() {
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
		case 2: return new double[] {c[0], c[1], 0, 1};
		case 3: return new double[] {c[0], c[1], c[2], 1};
		case 4: return c;
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
		double[] r = a.getDefault(TextureBaryCenter.class, v, new double[] {0, 0, 0, 1});
		return convertCoordinate(r);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] r = a.getDefault(TextureBaryCenter.class, e, new double[] {0, 0, 0, 1});
		return convertCoordinate(r);
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] r = a.getDefault(TextureBaryCenter.class, f, new double[] {0, 0, 0, 1});
		return convertCoordinate(r);
	}
	
	@Override
	public String toString() {
		return "Barycenter 4D";
	}
	
}
