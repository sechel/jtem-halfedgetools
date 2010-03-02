package de.jtem.halfedgetools.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;

public abstract class AbstractAdapter<VAL> extends Adapter<VAL> {

	private Class<? extends VAL>
		typeClass = null;
	
	public AbstractAdapter(Class<? extends VAL> typeClass, boolean getter, boolean setter) {
		super(getter, setter);
		this.typeClass = typeClass;
	}
	
	@Override
	public boolean checkType(Class<?> typeClass) {
		return this.typeClass.isAssignableFrom(typeClass);
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getV(V v, AdapterSet a) {
		throw new RuntimeException("getV not supported in this adapter: " + getClass().getSimpleName());
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getE(E e, AdapterSet a) {
		throw new RuntimeException("getE not supported in this adapter: " + getClass().getSimpleName());
	}	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getF(F f, AdapterSet a) {
		throw new RuntimeException("getF not supported in this adapter: " + getClass().getSimpleName());
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, VAL value, AdapterSet a) {
		throw new RuntimeException("setV not supported in this adapter: " + getClass().getSimpleName());
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, VAL value, AdapterSet a) {
		throw new RuntimeException("setE not supported in this adapter: " + getClass().getSimpleName());
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F f, VAL value, AdapterSet a) {
		throw new RuntimeException("setF not supported in this adapter: " + getClass().getSimpleName());
	}
	

}
