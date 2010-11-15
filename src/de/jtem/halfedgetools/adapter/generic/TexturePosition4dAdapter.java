package de.jtem.halfedgetools.adapter.generic;

import java.util.List;

import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition4d;

@TexturePosition4d
public class TexturePosition4dAdapter extends AbstractAdapter<double[]> {

	public TexturePosition4dAdapter() {
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
		double[] r = a.getDefault(TexturePosition.class, v, new double[] {0, 0, 0, 1});
		switch (r.length) {
		case 2:
			r = new double[] {r[0], r[1], 0, 1.0};
			break;
		case 3:
			r = new double[] {r[0], r[1], r[2], 1.0};
			break;
		case 4:
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
		double[] s = getV(e.getStartVertex(), a);
		double[] t = getV(e.getTargetVertex(), a);
		Pn.dehomogenize(s, s);
		Pn.dehomogenize(t, t);
		return Rn.linearCombination(null, 0.5, t, 0.5, s);
	}	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] pos = new double[4];
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		for (E e : b) {
			double[] tmp = getV(e.getTargetVertex(), a);
			Pn.dehomogenize(tmp, tmp);
			Rn.add(pos, pos, tmp);
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}
	
}
