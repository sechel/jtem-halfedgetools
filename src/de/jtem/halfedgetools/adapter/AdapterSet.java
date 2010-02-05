package de.jtem.halfedgetools.adapter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class AdapterSet implements Set<Adapter<?>> {

	protected Set<Adapter<?>>
		adapters = new HashSet<Adapter<?>>();
	
	public AdapterSet() {
	}
	
	public AdapterSet(Collection<? extends Adapter<?>> adapters) {
		for (Adapter<?> a : adapters) {
			add(a);
		}
	}
	
	public AdapterSet(Adapter<?>... adapters) {
		for (Adapter<?> a : adapters) {
			add(a);
		}
	}
	
	public AdapterSet(Adapter<?> a) {
		add(a);
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		A extends Adapter<VAL>,
		VAL
	> A queryAdapter(Class<A> aClass) {
		for (Adapter<?> a : adapters) {
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
	> Adapter<O> queryGetter(Class<A> type, Class<N> noteType, Class<O> out) {
		Adapter<O> result = null;
		for (Adapter<?> a : adapters) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(noteType) && a.checkType(out)) {
				result = (Adapter<O>)a;
				break;
			}
		}
		return result;
	}
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		O
	> boolean hasGetter(Class<A> type, Class<N> noteType, Class<O> out) {
		return queryGetter(type, noteType, out) != null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		I
	> Adapter<I> querySetter(Class<A> type, Class<N> noteType, Class<I> in) {
		Adapter<I> result = null;
		for (Adapter<?> a : adapters) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(noteType) && a.checkType(in)) {
				result = (Adapter<I>)a;
				break;
			}
		}
		return result;
	}
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		I
	> boolean hasSetter(Class<A> type, Class<N> noteType, Class<I> in) {
		return querySetter(type, noteType, in) != null;
	}
	
	
	public <
		N extends Node<?, ?, ?>,
		O
	> TypedAdapterSet<O> queryGetter(Class<N> noteType, Class<O> out) throws AdapterException {
		TypedAdapterSet<O> set = new TypedAdapterSet<O>(out);
		for (Adapter<?> a : adapters) {
			if (a.canAccept(noteType) && a.checkType(out)) {
				set.add(a);
			}
		}
		return set;		
	}
	
	
	public <
		N extends Node<?, ?, ?>,
		I
	> TypedAdapterSet<I> querySetter(Class<N> noteType, Class<I> in) throws AdapterException {
		TypedAdapterSet<I> set = new TypedAdapterSet<I>(in);
		for (Adapter<?> a : adapters) {
			if (a.canAccept(noteType) && a.checkType(in)) {
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
		N extends Node<V, E, F>,
		VAL
	> VAL get(Class<A> type, N n, Class<VAL> typeClass) {
		Adapter<VAL> a = queryGetter(type, n.getClass(), typeClass);
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
	> VAL getSafe(Class<A> type, N n, VAL defaultValue) {
		Adapter<VAL> a = queryGetter(type, n.getClass(), (Class<VAL>)defaultValue.getClass());
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
	> VAL set(Class<A> type, N n, VAL value) {
		Adapter<VAL> a = queryGetter(type, n.getClass(), (Class<VAL>)value.getClass());
		if (a != null) {
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
		return null;
	}

	@Override
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
