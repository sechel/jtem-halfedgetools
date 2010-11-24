package de.jtem.halfedgetools.adapter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.generic.BaryCenter3dAdapter;
import de.jtem.halfedgetools.adapter.generic.BaryCenter4dAdapter;
import de.jtem.halfedgetools.adapter.generic.BaryCenterAdapter;
import de.jtem.halfedgetools.adapter.generic.EdgeLengthAdapter;
import de.jtem.halfedgetools.adapter.generic.EdgeVectorAdapter;
import de.jtem.halfedgetools.adapter.generic.FaceAreaAdapter;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.adapter.generic.Position3dAdapter;
import de.jtem.halfedgetools.adapter.generic.Position4dAdapter;
import de.jtem.halfedgetools.adapter.generic.TexturePosition2dAdapter;
import de.jtem.halfedgetools.adapter.generic.TexturePosition3dAdapter;
import de.jtem.halfedgetools.adapter.generic.TexturePosition4dAdapter;

public class AdapterSet extends TreeSet<Adapter<?>> {

	private static final long 
		serialVersionUID = 1L;


	public AdapterSet() {
	}
	
	public AdapterSet(Collection<? extends Adapter<?>> adapters) {
		addAll(adapters);
	}
	
	public AdapterSet(Adapter<?>... adapters) {
		addAll(Arrays.asList(adapters));
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
	
	
	private Map<Long, Adapter<?>>
		queryCache1 = new HashMap<Long, Adapter<?>>();
	
	@SuppressWarnings("unchecked")
	public <
		A
	> A query(Class<A> aClass) {
		long hash = aClass.hashCode();
		Adapter<?> result = queryCache1.get(hash);
		if (result != null) return (A)result;
		for (Adapter<?> a : this) {
			if (aClass.isAssignableFrom(a.getClass())) {
				result = a;
				break;
			}
		}
		queryCache1.put(hash, result);
		return (A)result;	
	}
	
	private Map<Long, Object>
		queryCache2 = new HashMap<Long, Object>();
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		O 
	> Adapter<O> query(Class<A> type, Class<N> nodeType, Class<O> out) {
		long hash = type.hashCode() + nodeType.hashCode() + out.hashCode();
		Adapter<O> result = (Adapter<O>)queryCache2.get(hash);
		if (result != null) return result; 
		for (Adapter<?> a : this) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(nodeType) && a.checkType(out)) {
				result = (Adapter<O>)a;
				break;
			}
		}
		queryCache2.put(hash, result);
		return result;
	}
	
	
	private Map<Long, Object>
		queryCache3 = new HashMap<Long, Object>();
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation, 
		N extends Node<?, ?, ?>,
		O 
	> List<Adapter<O>> queryAll(Class<A> type, Class<N> nodeType, Class<O> out) {
		long hash = type.hashCode() + nodeType.hashCode() + out.hashCode();
		List<Adapter<O>> result = (List<Adapter<O>>)queryCache3.get(hash);
		if (result != null) return result;
		result = new LinkedList<Adapter<O>>();
		for (Adapter<?> a : this) {
			if (a.getClass().isAnnotationPresent(type) && a.canAccept(nodeType) && a.checkType(out)) {
				result.add((Adapter<O>)a);
			}
		}
		queryCache3.put(hash, result);
		return result;
	}
	

	private Map<Long, Object>
		queryCache4 = new HashMap<Long, Object>();
	
	@SuppressWarnings("unchecked")
	public <
		A extends Annotation,
		O 
	> List<Adapter<O>> queryAll(Class<A> type, Class<O> out) {
		long hash = type.hashCode() + out.hashCode();
		List<Adapter<O>> result = (List<Adapter<O>>)queryCache4.get(hash);
		if (result != null) return result;
		result = new LinkedList<Adapter<O>>();
		for (Adapter<?> a : this) {
			if (a.getClass().isAnnotationPresent(type) && a.checkType(out)) {
				result.add((Adapter<O>)a);
			}
		}
		queryCache4.put(hash, result);
		return result;
	}
	
	private Map<Long, List<Adapter<?>>>
		queryCache5 = new HashMap<Long, List<Adapter<?>>>();
	
	public <
		A extends Annotation
	> List<Adapter<?>> queryAll(Class<A> type) {
		long hash = type.hashCode();
		List<Adapter<?>> result = queryCache5.get(hash);
		if (result != null) return result;
		result = new LinkedList<Adapter<?>>();
		for (Adapter<?> a : this) {
			if (a.getClass().isAnnotationPresent(type)) {
				result.add(a);
			}
		}
		queryCache5.put(hash, result);
		return result;
	}
	
	
	
	private Map<Long, Object>
		queryCache6 = new HashMap<Long, Object>();
	
	public <
		O
	> TypedAdapterSet<O> querySet(Class<O> out) throws AdapterException {
		long hash = out.hashCode();
		@SuppressWarnings("unchecked")
		TypedAdapterSet<O> result = (TypedAdapterSet<O>)queryCache6.get(hash);
		if (result != null) return result;
		result = new TypedAdapterSet<O>(out);
		for (Adapter<?> a : this) {
			if (a.checkType(out)) {
				result.add(a);
			}
		}
		queryCache6.put(hash, result);
		return result;		
	}

	
	private Map<Long, Object>
		queryCache7 = new HashMap<Long, Object>();
	
	public <
		N extends Node<?, ?, ?>,
		O
	> TypedAdapterSet<O> querySet(Class<N> noteType, Class<O> out) throws AdapterException {
		long hash = noteType.hashCode() + out.hashCode();
		@SuppressWarnings("unchecked")
		TypedAdapterSet<O> result = (TypedAdapterSet<O>)queryCache7.get(hash);
		if (result != null) return result;
		result = new TypedAdapterSet<O>(out);
		for (Adapter<?> a : this) {
			if (a.canAccept(noteType) && a.checkType(out)) {
				result.add(a);
			}
		}
		queryCache7.put(hash, result);
		return result;		
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
			return a.get(n, this);
		}
		throw new AdapterException("AdapterSet.get()");
	}
	
	public <		
		A extends Annotation, 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,		
		N extends Node<V, E, F>,
		VAL
	> double[] getD(Class<A> type, N n) {
		Adapter<double[]> a = query(type, n.getClass(), double[].class);
		if (a != null && a.isGetter()) {
			return a.get(n, this);
		}
		throw new AdapterException("AdapterSet.get()");
	}
	
	
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
			return a.get(n, this);
		}
		throw new AdapterException("AdapterSet.get()");
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
		if (a != null && a.isGetter()) {
			VAL r = a.get(n, this);
			return r != null ? r : defaultValue;
		} else {
			return defaultValue;
		}
	}
	
	
	/**
	 * Sets a node value via a matching adapter. This operation might
	 * not do anything if there is no adapter.
	 * @param <A>
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <N>
	 * @param <VAL>
	 * @param type
	 * @param n
	 * @param value
	 */
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
			a.set(n, value, this);
		}
	}

	public void revalidateAdapters() {
		for(Adapter<?> a : this) {
			a.update();
		}
	}
	
	
	
	private class ParameterInvokation {
		
		public Adapter<?> a = null;
		public List<Method> m = null;
		
	}
	
	private HashMap<Long, List<ParameterInvokation>>
		paramCache = new HashMap<Long, List<ParameterInvokation>>();
	
	/**
	 * Sets a parameter value on all matching adapters 
	 * @param <A>
	 * @param type
	 * @param name
	 * @param value
	 */
	public <
		A extends Annotation
	> void setParameter(String name, Object... value) {
		long hash = name.hashCode();
		if (paramCache.containsKey(hash)) {
			List<ParameterInvokation> piList = paramCache.get(hash);
			for (ParameterInvokation pi : piList) {
				for (Method m : pi.m) {
					try {
						m.invoke(pi.a, value);
					} catch (Exception e) {
						throw new AdapterException(e);
					}
				}
			}
			return;
		}
		List<ParameterInvokation> piList = new LinkedList<AdapterSet.ParameterInvokation>();
		for (Adapter<?> a : this) {
			List<Method> mList = new LinkedList<Method>();
			for (Method m : a.getClass().getMethods()) {
				Parameter ap = m.getAnnotation(Parameter.class);
				if (ap != null && ap.name().equals(name)) {
					try {
						m.invoke(a, value);
						mList.add(m);
					} catch (Exception e) {
						throw new AdapterException(e);
					}
				}
				if (mList.size() != 0) {
					ParameterInvokation pi = new ParameterInvokation();
					pi.a = a;
					pi.m = mList;
					piList.add(pi);
				}
			}
		}
		paramCache.put(hash, piList);
	}
	
	
	public static AdapterSet createGenericAdapters() {
		AdapterSet aSet = new AdapterSet();
		aSet.add(new NormalAdapter());
		aSet.add(new BaryCenterAdapter());
		aSet.add(new BaryCenter3dAdapter());
		aSet.add(new BaryCenter4dAdapter());
		aSet.add(new FaceAreaAdapter());
		aSet.add(new Position3dAdapter());
		aSet.add(new Position4dAdapter());
		aSet.add(new TexturePosition2dAdapter());
		aSet.add(new TexturePosition3dAdapter());
		aSet.add(new TexturePosition4dAdapter());
		aSet.add(new EdgeVectorAdapter());
		aSet.add(new EdgeLengthAdapter());
		return aSet;
	}
	
	

	protected void resetQueryCache() {
		queryCache1.clear();
		queryCache2.clear();
		queryCache3.clear();
		queryCache4.clear();
		queryCache5.clear();
		queryCache6.clear();
		queryCache7.clear();
		paramCache.clear();
	}
	
	
	@Override
	public boolean add(Adapter<?> e) {
		resetQueryCache();
		return super.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends Adapter<?>> c) {
		resetQueryCache();
		return super.addAll(c);
	}
	
	@Override
	public void clear() {
		resetQueryCache();
		super.clear();
	}
	
	@Override
	public Adapter<?> pollFirst() {
		resetQueryCache();
		return super.pollFirst();
	}
	
	@Override
	public Adapter<?> pollLast() {
		resetQueryCache();
		return super.pollLast();
	}
	
	@Override
	public boolean remove(Object o) {
		resetQueryCache();
		return super.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c) {
		resetQueryCache();
		return super.removeAll(c);
	}
	
	@Override
	public boolean retainAll(Collection<?> c) {
		resetQueryCache();
		return super.retainAll(c);
	}
	
	
	
}
