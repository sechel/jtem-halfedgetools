package de.jtem.halfedgetools.jreality.adapter;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Calculator;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.Position;

@BaryCenter
public class JRBaryCenterAdapter extends AbstractAdapter<double[]> implements Calculator {

	public JRBaryCenterAdapter() {
		super(double[].class, true, false);
	}
	
	@Override
	public <T extends Node<?, ?, ?>> boolean canAccept(Class<T> nodeClass) {
		return true;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		return a.getDefault(Position.class, v, new double[] {0, 0, 0});
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] s = a.getDefault(Position.class, e.getStartVertex(), new double[] {0, 0, 0});
		double[] t = a.getDefault(Position.class, e.getTargetVertex(), new double[] {0, 0, 0});
		return Rn.linearCombination(null, 0.5, t, 0.5, s);
	}	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] pos = new double[3];
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		for (E e : b) {
			double[] tmp = a.getDefault(Position.class, e.getTargetVertex(), new double[] {0,0,0});
			Rn.add(pos, pos, tmp);
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}
	
}
