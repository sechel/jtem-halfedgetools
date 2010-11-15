package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;

@BaryCenter4d
public class BaryCenter4dAdapter extends AbstractAdapter<double[]> {

	public BaryCenter4dAdapter() {
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
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		double[] r = a.getDefault(BaryCenter.class, v, new double[] {0, 0, 0, 1});
		if (r.length == 3) {
			double[] ar = {r[0], r[1], r[2], 1};
			r = ar;
		}
		return r;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] r = a.getDefault(BaryCenter.class, e, new double[] {0, 0, 0, 1});
		if (r.length == 3) {
			double[] ar = {r[0], r[1], r[2], 1};
			r = ar;
		}
		return r;
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] r = a.getDefault(BaryCenter.class, f, new double[] {0, 0, 0, 1});
		if (r.length == 3) {
			double[] ar = {r[0], r[1], r[2], 1};
			r = ar;
		}
		return r;
	}
	
}
