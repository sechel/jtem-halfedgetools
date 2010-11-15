package de.jtem.halfedgetools.adapter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class TypedAdapterSet <VAL> extends AdapterSet {

	private static final long 
		serialVersionUID = 1L;
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
	
	private Map<Long, Object>
		queryCache1 = new HashMap<Long, Object>();
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> Adapter<VAL> query(Class<A> type, Class<N> nodeType) {
		long hash = type.hashCode() + nodeType.hashCode();
		Adapter<VAL> result = (Adapter<VAL>)queryCache1.get(hash);
		if (result != null) {
			return result;
		}
		for (Adapter<?> a : this) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(nodeType)) {
				result = (Adapter<VAL>)a;
				break;
			}
		}
		queryCache1.put(hash, result);
		return result;
	}
	
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> boolean hasGetter(Class<A> type, Class<N> noteType) {
		return query(type, noteType) != null;
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>
	> Adapter<VAL> querySetter(Class<A> type, Class<N> noteType) {
		Adapter<VAL> result = null;
		for (Adapter<?> a : this) {
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
		for (Adapter<?> a : this) {
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
		for (Adapter<?> a : this) {
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
		Adapter<VAL> a = query(type, n.getClass());
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
		throw new AdapterException("AdapterSet.get()");
	}
	
	@Override
	protected void resetQueryCache() {
		super.resetQueryCache();
		queryCache1.clear();
	}
	
}
