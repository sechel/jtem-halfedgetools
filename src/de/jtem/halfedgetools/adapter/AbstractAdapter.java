package de.jtem.halfedgetools.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class AbstractAdapter <VAL> implements Adapter<VAL> {

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return false;
	}

	@Override
	public boolean canInput(Class<?> typeClass) {
		return false;
	}

	@Override
	public boolean canOutput(Class<?> typeClass) {
		return false;
	}

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getV(V v, AdapterSet a) {
		return null;
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getE(E e, AdapterSet a) {
		return null;
	}	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getF(F f, AdapterSet a) {
		return null;
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, VAL value, AdapterSet a) {}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E v, VAL value, AdapterSet a) {}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F v, VAL value, AdapterSet a) {}

}
