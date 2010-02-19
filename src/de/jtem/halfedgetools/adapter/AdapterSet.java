package de.jtem.halfedgetools.adapter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class AdapterSet extends HashSet<Adapter<?>> {

	private static final long 
		serialVersionUID = 1L;


	public AdapterSet() {
	}
	
	public AdapterSet(Collection<? extends Adapter<?>> adapters) {
		super(adapters);
	}
	
	public AdapterSet(Adapter<?>... adapters) {
		super(Arrays.asList(adapters));
	}
	
	public AdapterSet(Adapter<?> a) {
		add(a);
	}
	
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		O
	> boolean contains(Class<A> type, Class<N> noteType, Class<O> out) {
		return query(type, noteType, out) != null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		A extends Adapter<VAL>,
		VAL
	> A query(Class<A> aClass) {
		for (Adapter<?> a : this) {
			if (aClass.isAssignableFrom(a.getClass())) {
				return (A)a;
			}
		}
		return null;	
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		O 
	> Adapter<O> query(Class<A> type, Class<N> noteType, Class<O> out) {
		Adapter<O> result = null;
		for (Adapter<?> a : this) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(noteType) && a.checkType(out)) {
				result = (Adapter<O>)a;
				break;
			}
		}
		return result;
	}
	

	public <
		O
	> TypedAdapterSet<O> query(Class<O> out) throws AdapterException {
		TypedAdapterSet<O> set = new TypedAdapterSet<O>(out);
		for (Adapter<?> a : this) {
			if (a.checkType(out)) {
				set.add(a);
			}
		}
		return set;		
	}

	public <
		N extends Node<?, ?, ?>,
		O
	> TypedAdapterSet<O> query(Class<N> noteType, Class<O> out) throws AdapterException {
		TypedAdapterSet<O> set = new TypedAdapterSet<O>(out);
		for (Adapter<?> a : this) {
			if (a.canAccept(noteType) && a.checkType(out)) {
				set.add(a);
			}
		}
		return set;		
	}
	
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		O 
	> boolean isAvailable(Class<A> type, Class<N> noteType, Class<O> out) {
		return query(type, noteType, out) != null;
	}
		
	
	public <
		A extends Adapter<VAL>,
		VAL
	> boolean isAvailable(Class<A> aClass) {
		return query(aClass) != null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <		
		A extends Annotation, 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,		
		N extends Node<V, E, F>,
		VAL
	> VAL get(Class<A> type, N n, Class<VAL> typeClass) {
		Adapter<VAL> a = query(type, n.getClass(), typeClass);
		if (a != null && a.isGetter()) {
			if (n instanceof Vertex<?, ?, ?>) {
				return a.getV((V)n, this);	
			}
			if (n instanceof Edge<?, ?, ?>) {
				return a.getE((E)n, this);
			}
			if (n instanceof Face<?, ?, ?>) {
				return a.getF((F)n, this);
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <		
		A extends Adapter<VAL>,
		T extends Annotation, 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,		
		N extends Node<V, E, F>,
		VAL
	> VAL get(Class<A> adapterClass, Class<T> type, N n, Class<VAL> typeClass) {
		Adapter<VAL> a = query(adapterClass);
		if (a == null) {
			a = query(type, n.getClass(), typeClass);
		}
		if (a != null && a.isGetter()) {
			if (n instanceof Vertex<?, ?, ?>) {
				return a.getV((V)n, this);	
			}
			if (n instanceof Edge<?, ?, ?>) {
				return a.getE((E)n, this);
			}
			if (n instanceof Face<?, ?, ?>) {
				return a.getF((F)n, this);
			}
		}
		return null;
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <		
		A extends Annotation, 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,		
		N extends Node<V, E, F>,
		VAL
	> VAL getDefault(Class<A> type, N n, VAL defaultValue) {
		Adapter<VAL> a = query(type, n.getClass(), (Class<VAL>)defaultValue.getClass());
		if (a != null) {
			if (n instanceof Vertex<?, ?, ?>) {
				return a.getV((V)n, this);	
			}
			if (n instanceof Edge<?, ?, ?>) {
				return a.getE((E)n, this);
			}
			if (n instanceof Face<?, ?, ?>) {
				return a.getF((F)n, this);
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <		
		A extends Annotation, 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,		
		N extends Node<V, E, F>,
		VAL
	> void set(Class<A> type, N n, VAL value) {
		Adapter<VAL> a = query(type, n.getClass(), (Class<VAL>)value.getClass());
		if (a != null && a.isSetter()) {
			if (n instanceof Vertex<?, ?, ?>) {
				a.setV((V)n, value, this);	
			}
			if (n instanceof Edge<?, ?, ?>) {
				a.setE((E)n, value, this);
			}
			if (n instanceof Face<?, ?, ?>) {
				a.setF((F)n, value, this);
			}
		}
	}
	
	
}
