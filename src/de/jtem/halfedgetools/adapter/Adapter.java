package de.jtem.halfedgetools.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;


public abstract class Adapter<VAL> {
	  
	private boolean
		getter = false,
		setter = false;
	
	public Adapter(boolean getter, boolean setter) {
		this.getter = getter;
		this.setter = setter;
	}
	
	public abstract <
		N extends Node<?, ?, ?>
	> boolean canAccept(Class<N> nodeClass);
	
	public abstract boolean checkType(Class<?> typeClass); 
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> VAL get(N n, AdapterSet a) {
		if (n instanceof Vertex<?, ?, ?>) {
			return getV((V)n, a);
		}
		if (n instanceof Edge<?, ?, ?>) {
			return getE((E)n, a);
		}
		if (n instanceof Face<?, ?, ?>) {
			return getF((F)n, a);
		}
		return null;
	}
	protected abstract <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getV(V v, AdapterSet a);
	protected abstract  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getE(E e, AdapterSet a);	
	protected abstract  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getF(F f, AdapterSet a);
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> void set(N n, VAL value, AdapterSet a) {
		if (n instanceof Vertex<?, ?, ?>) {
			setV((V)n, value, a);
		}
		if (n instanceof Edge<?, ?, ?>) {
			setE((E)n, value, a);
		}
		if (n instanceof Face<?, ?, ?>) {
			setF((F)n, value, a);
		}
	}
	protected abstract  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, VAL value, AdapterSet a);
	protected abstract  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E v, VAL value, AdapterSet a);
	protected abstract  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F v, VAL value, AdapterSet a);
	
	public boolean isGetter() {
		return getter;
	}
	
	public boolean isSetter() {
		return setter;
	}
	
}
