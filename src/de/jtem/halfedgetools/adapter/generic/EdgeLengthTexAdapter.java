package de.jtem.halfedgetools.adapter.generic;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.LengthTex;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition3d;

@LengthTex
public class EdgeLengthTexAdapter extends AbstractAdapter<Double> {

	public EdgeLengthTexAdapter() {
		super(Double.class, true, false);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getE(E e, AdapterSet a) {
		double[] s = a.getD(TexturePosition3d.class, e.getStartVertex());
		double[] t = a.getD(TexturePosition3d.class, e.getTargetVertex());
		return Rn.euclideanDistance(s, t);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}

}
