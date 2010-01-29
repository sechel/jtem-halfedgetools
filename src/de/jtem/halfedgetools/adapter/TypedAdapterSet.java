package de.jtem.halfedgetools.adapter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Iterator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class TypedAdapterSet <VAL> extends AdapterSet {

	private Class<VAL>
		typeClass = null;
	
	public TypedAdapterSet(Class<VAL> typeClass) {
		this.typeClass = typeClass;
	}
	
	public TypedAdapterSet(Class<VAL> typeClass, Collection<? extends Adapter<VAL>> adapters) {
		this(typeClass);
		for (Adapter<VAL> a : adapters) {
			add(a);
		}
	}
	
	public TypedAdapterSet(Class<VAL> typeClass, Adapter<VAL>... adapters) {
		this(typeClass);
		for (Adapter<VAL> a : adapters) {
			add(a);
		}
	}
	
	public TypedAdapterSet(Adapter<VAL> a) {
		add(a);
	}
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> Adapter<VAL> queryGetter(Class<A> type, Class<N> noteType) {
		Adapter<VAL> result = null;
		for (Adapter<?> a : adapters) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(noteType)) {
				result = (Adapter<VAL>)a;
				break;
			}
		}
		return result;
	}
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> boolean hasGetter(Class<A> type, Class<N> noteType) {
		return queryGetter(type, noteType) != null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> Adapter<VAL> querySetter(Class<A> type, Class<N> noteType) {
		Adapter<VAL> result = null;
		for (Adapter<?> a : adapters) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(noteType)) {
				result = (Adapter<VAL>)a;
				break;
			}
		}
		return result;
	}
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> boolean hasSetter(Class<A> type, Class<N> noteType) {
		return querySetter(type, noteType) != null;
	}
	
	
	public <
		N extends Node<?, ?, ?>
	> TypedAdapterSet<VAL> queryGetter(Class<N> noteType) throws AdapterException {
		TypedAdapterSet<VAL> set = new TypedAdapterSet<VAL>(typeClass);
		for (Adapter<?> a : adapters) {
			if (a.canAccept(noteType)) {
				set.add(a);
			}
		}
		return set;		
	}
	
	
	public <
		N extends Node<?, ?, ?>
	> TypedAdapterSet<VAL> querySetter(Class<N> noteType) throws AdapterException {
		TypedAdapterSet<VAL> set = new TypedAdapterSet<VAL>(typeClass);
		for (Adapter<?> a : adapters) {
			if (a.canAccept(noteType)) {
				set.add(a);
			}
		}
		return set;		
	}
	
	
	@SuppressWarnings("unchecked")
	public <		
		A extends Annotation, 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,		
		N extends Node<V, E, F>
	> VAL get(Class<A> type, N n) {
		Adapter<VAL> a = queryGetter(type, n.getClass());
		if (a != null) {
			if (n instanceof Vertex<?, ?, ?>) {
				return typeClass.cast(a.getV((V)n, this));	
			}
			if (n instanceof Edge<?, ?, ?>) {
				return typeClass.cast(a.getE((E)n, this));
			}
			if (n instanceof Face<?, ?, ?>) {
				return typeClass.cast(a.getF((F)n, this));
			}
		}
		return null;
	}
	

	public boolean add(Adapter<?> e) {
		return adapters.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends Adapter<?>> c) {
		return adapters.addAll(c);
	}
	
	public void addAll(Adapter<?>[] aArr) {
		for (Adapter<?> a : aArr) {
			add(a);
		}
	}

	@Override
	public void clear() {
		adapters.clear();
	}

	@Override
	public boolean contains(Object o) {
		return adapters.contains(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return adapters.containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return adapters.isEmpty();
	}

	@Override
	public Iterator<Adapter<?>> iterator() {
		return adapters.iterator();
	}

	@Override
	public boolean remove(Object o) {
		return adapters.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return adapters.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return adapters.retainAll(c);
	}

	@Override
	public int size() {
		return adapters.size();
	}

	@Override
	public Object[] toArray() {
		return adapters.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return adapters.toArray(a);
	}
	
	@Override
	public String toString() {
		return adapters.toString();
	}
	
	
}
