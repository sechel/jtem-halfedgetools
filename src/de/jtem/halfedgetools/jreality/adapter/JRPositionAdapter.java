package de.jtem.halfedgetools.jreality.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

@Position
public class JRPositionAdapter extends Adapter<double[]> {

	public JRPositionAdapter() {
		super(true, true);
	}
	
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
		return double[].class.isAssignableFrom(typeClass);
	}

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)v;
		return jv.position;
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		JREdge<?, ?, ?> je = (JREdge<?, ?, ?>)e;
		return je.position;
	}	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		JRFace<?, ?, ?> jf = (JRFace<?, ?, ?>)f; 
		return jf.position;
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, double[] value, AdapterSet a) {
		JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)v;
		jv.position = value;
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, double[] value, AdapterSet a) {
		JREdge<?, ?, ?> je = (JREdge<?, ?, ?>)e;
		je.position = value;
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F f, double[] value, AdapterSet a) {
		JRFace<?, ?, ?> jf = (JRFace<?, ?, ?>)f; 
		jf.position = value;
	}

}
