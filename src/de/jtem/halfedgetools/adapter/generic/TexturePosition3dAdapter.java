package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TextureBaryCenter3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition3d;

@TexturePosition3d
public class TexturePosition3dAdapter extends AbstractAdapter<double[]> {

	public TexturePosition3dAdapter() {
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
			throw new IllegalArgumentException("cannot convert coordinate in Position3dAdapter");
		}
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		double[] r = a.getDefault(TexturePosition.class, v, new double[] {0, 0, 0});
		switch (r.length) {
		case 2:
			r = new double[] {r[0], r[1], 0};
			break;
		case 3:
			break;
		case 4:
			double[] ar = new double[3];
			ar[0] = r[0] / r[3];
			ar[1] = r[1] / r[3];
			ar[2] = r[2] / r[3];
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
			return a.getD(TextureBaryCenter3d.class, e);
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
			return a.getD(TextureBaryCenter3d.class, f);
		}
	}
	
	@Override
	public String toString() {
		return "Texture Position 3D";
	}
	
}
