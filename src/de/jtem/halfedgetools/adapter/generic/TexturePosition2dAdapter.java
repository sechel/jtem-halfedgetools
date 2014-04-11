package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TextureBaryCenter2d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;

@TexturePosition2d
public class TexturePosition2dAdapter extends AbstractAdapter<double[]> {

	public TexturePosition2dAdapter() {
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
		double[] r = a.getDefault(TexturePosition.class, v, new double[] {0, 0});
		switch (r.length) {
		case 2:
			break;
		case 3:
			r = new double[] {r[0], r[1]};
			break;
		case 4:
			double[] ar = new double[2];
			ar[0] = r[0] / r[3];
			ar[1] = r[1] / r[3];
			r = ar;
			break;
		}
		return r;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		if (a.isAvailable(TexturePosition.class, e.getClass(), double[].class)) {
			double[] pos = a.getD(TexturePosition.class, e);
			return convertCoordinate(pos);
		} else {
			return a.getD(TextureBaryCenter2d.class, e);
		}
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		if (a.isAvailable(TexturePosition.class, f.getClass(), double[].class)) {
			double[] pos = a.getD(TexturePosition.class, f);
			return convertCoordinate(pos);
		} else {
			return a.getD(TextureBaryCenter2d.class, f);
		}
	}
	
	@Override
	public String toString() {
		return "Texture Position 2D";
	}
	
}
