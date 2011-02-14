package de.jtem.halfedgetools.algorithm.triangulation;

import java.util.HashMap;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@Length
public class DelaunayLengthAdapter extends AbstractAdapter<Double> {

	private Map<Edge<?, ?, ?>, Double>
		lMap = new HashMap<Edge<?,?,?>, Double>();
	
	public DelaunayLengthAdapter() {
		super(Double.class, true, true);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getE(E e, AdapterSet a) {
		if (lMap.containsKey(e)) {
			return lMap.get(e);
		} else {
			double[] s = a.get(Position3d.class, e.getStartVertex(), double[].class);
			double[] t = a.get(Position3d.class, e.getTargetVertex(), double[].class);
			double l = Rn.euclideanDistance(s, t);
			lMap.put(e, l);
			lMap.put(e.getOppositeEdge(), l);
			return l;
		}
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, Double value, AdapterSet a) {
		lMap.put(e, value);
		lMap.put(e.getOppositeEdge(), value);
	}
	
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public double getPriority() {
		return 10;
	}
	
}