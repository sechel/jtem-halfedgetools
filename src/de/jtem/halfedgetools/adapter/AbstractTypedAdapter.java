package de.jtem.halfedgetools.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public abstract class AbstractTypedAdapter<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	VAL
> extends Adapter<VAL> {

	private Class<? extends Vertex<?,?,?>>
		vClass = null;
	private Class<? extends Edge<?,?,?>>
		eClass = null;
	private Class<? extends Face<?,?,?>>
		fClass = null;
	private Class<? extends VAL>
		typeClass = null;
	
	
	protected AbstractTypedAdapter(
		Class<? extends Vertex<?,?,?>> vClass, 
		Class<? extends Edge<?,?,?>> eClass, 
		Class<? extends Face<?,?,?>> fClass,
		Class<? extends VAL> typeClass,
		boolean getter,
		boolean setter
	) {
		super(getter, setter);
		this.vClass = vClass;
		this.eClass = eClass;
		this.fClass = fClass;
		this.typeClass = typeClass;
	}
	
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		boolean accept = false;
		accept |= vClass != null && nodeClass.isAssignableFrom(vClass);
		accept |= eClass != null && nodeClass.isAssignableFrom(eClass);
		accept |= fClass != null && nodeClass.isAssignableFrom(fClass);
		return accept;
	}
	
	
	@Override
	public boolean checkType(Class<?> typeClass) {
		return this.typeClass.isAssignableFrom(typeClass);
	}
	

	public VAL getVertexValue(V v, AdapterSet a) {
		throw new RuntimeException("getVertexValue not supported in this adapter");
	}
	public VAL getEdgeValue(E e, AdapterSet a) {
		throw new RuntimeException("getEdgeValue not supported in this adapter");
	}
	public VAL getFaceValue(F f, AdapterSet a) {
		throw new RuntimeException("getFaceValue not supported in this adapter");
	}
	public void setVertexValue(V v, VAL value, AdapterSet a) {
		throw new RuntimeException("setVertexValue not supported in this adapter");
	}
	public void setEdgeValue(E e, VAL value, AdapterSet a) {
		throw new RuntimeException("setEdgeValue not supported in this adapter");
	}
	public void setFaceValue(F f, VAL value, AdapterSet a) {
		throw new RuntimeException("setFaceValue not supported in this adapter");
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public final <
		V1 extends Vertex<V1, E1, F1>,
		E1 extends Edge<V1, E1, F1>,
		F1 extends Face<V1, E1, F1>
	> VAL getV(V1 v, AdapterSet a) {
		return getVertexValue((V)v, a);
	}
	@Override
	@SuppressWarnings("unchecked")
	public final <
		V1 extends Vertex<V1, E1, F1>,
		E1 extends Edge<V1, E1, F1>,
		F1 extends Face<V1, E1, F1>
	> VAL getE(E1 e, AdapterSet a) {
		return getEdgeValue((E)e, a);
	}	
	@Override
	@SuppressWarnings("unchecked")
	public final <
		V1 extends Vertex<V1, E1, F1>,
		E1 extends Edge<V1, E1, F1>,
		F1 extends Face<V1, E1, F1>
	> VAL getF(F1 f, AdapterSet a) {
		return getFaceValue((F)f, a);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public final <
		V1 extends Vertex<V1, E1, F1>,
		E1 extends Edge<V1, E1, F1>,
		F1 extends Face<V1, E1, F1>
	> void setV(V1 v, VAL value, AdapterSet a) {
		setVertexValue((V)v, value, a);
	}
	@Override
	@SuppressWarnings("unchecked")
	public final <
		V1 extends Vertex<V1, E1, F1>,
		E1 extends Edge<V1, E1, F1>,
		F1 extends Face<V1, E1, F1>
	> void setE(E1 e, VAL value, AdapterSet a) {
		setEdgeValue((E)e, value, a);
	}
	@Override
	@SuppressWarnings("unchecked")
	public final <
		V1 extends Vertex<V1, E1, F1>,
		E1 extends Edge<V1, E1, F1>,
		F1 extends Face<V1, E1, F1>
	> void setF(F1 f, VAL value, AdapterSet a) {
		setFaceValue((F)f, value, a);
	}

	@Override
	public double getPriority() {
		return 0;
	}
	
}
