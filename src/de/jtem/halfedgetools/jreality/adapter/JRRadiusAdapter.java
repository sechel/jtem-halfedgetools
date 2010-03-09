package de.jtem.halfedgetools.jreality.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Radius;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

@Radius
public class JRRadiusAdapter extends Adapter<Double> {

	public JRRadiusAdapter() {
		super(true, true);
	}
	
	@Override
	public <T extends Node<?, ?, ?>> boolean canAccept(Class<T> nodeClass) {
		boolean result = false;
		result |= JRVertex.class.isAssignableFrom(nodeClass);
		result |= JREdge.class.isAssignableFrom(nodeClass);
		result |= JRFace.class.isAssignableFrom(nodeClass);
		return result;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public boolean checkType(Class<?> typeClass) {
		return Double.class.isAssignableFrom(typeClass);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getV(V v, AdapterSet a) {
		JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)v;
		return jv.radius;
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getE(E e, AdapterSet a) {
		JREdge<?, ?, ?> je = (JREdge<?, ?, ?>)e;
		return je.radius;
	}	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getF(F f, AdapterSet a) {
		JRFace<?, ?, ?> jf = (JRFace<?, ?, ?>)f; 
		return jf.radius;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, Double value, AdapterSet a) {
		JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)v;
		jv.radius = value;
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, Double value, AdapterSet a) {
		JREdge<?, ?, ?> je = (JREdge<?, ?, ?>)e;
		je.radius = value;
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F f, Double value, AdapterSet a) {
		JRFace<?, ?, ?> jf = (JRFace<?, ?, ?>)f; 
		jf.radius = value;
	}


}
