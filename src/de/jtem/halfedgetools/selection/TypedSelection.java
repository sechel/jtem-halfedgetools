package de.jtem.halfedgetools.selection;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class TypedSelection <N extends Node<?,?,?>> implements Set<N>, Serializable {

	private static final long 
		serialVersionUID = 5502813571615856546L;
	protected Map<N, Integer>
		M = new LinkedHashMap<N, Integer>();
	public static final Integer
		CHANNEL_DEFAULT = 0,
		CHANNEL_NOT_SELECTED = null;
	
	public TypedSelection() {
	}
	public TypedSelection(Collection<? extends N> c) {
		addAll(c);
	}
	public TypedSelection(TypedSelection<? extends N> c) {
		M.putAll(c.M);
	}
	public <NN extends N> TypedSelection(NN... nArr) {
		addAll(Arrays.asList(nArr));
	}
	
	@Override
	public String toString() {
		return M.keySet().toString();
	}
	
	public VertexSelection getVertices() {
		VertexSelection r = new VertexSelection();
		for (N n : this) {
			if (n instanceof Vertex<?,?,?>) {
				r.M.put((Vertex<?,?,?>)n, M.get(n));
			}
		}
		return r;
	}
	public EdgeSelection getEdges() {
		EdgeSelection r = new EdgeSelection();
		for (N n : this) {
			if (n instanceof Edge<?,?,?>) {
				r.M.put((Edge<?,?,?>)n, M.get(n));
			}
		}
		return r;
	}
	public FaceSelection getFaces() {
		FaceSelection r = new FaceSelection();
		for (N n : this) {
			if (n instanceof Face<?,?,?>) {
				r.M.put((Face<?,?,?>)n, M.get(n));
			}
		}
		return r;
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> TypedSelection<V> getVertices(HDS hds) {
		TypedSelection<V> r = new TypedSelection<V>();
		for (Vertex<?,?,?> v : getVertices()) {
			if (v.getHalfEdgeDataStructure() == hds) {
				r.M.put((V)v, M.get(v));
			}
		}
		return r;
	}
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> TypedSelection<E> getEdges(HDS hds) {
		TypedSelection<E> r = new TypedSelection<E>();
		for (Edge<?,?,?> e : getEdges()) {
			if (e.getHalfEdgeDataStructure() == hds) {
				r.M.put((E)e, M.get(e));
			}
		}
		return r;
	}
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> TypedSelection<F> getFaces(HDS hds) {
		TypedSelection<F> r = new TypedSelection<F>();
		for (Face<?,?,?> f : getFaces()) {
			if (f.getHalfEdgeDataStructure() == hds) {
				r.M.put((F)f, M.get(f));
			}
		}
		return r;
	}
	
	public Selection getChannel(Integer channel) {
		Selection r = new Selection();
		for (N n : this) {
			Integer c = getChannel(n);
			if (c.equals(channel)) {
				r.M.put(n, c);
			}
		}
		return r;
	}
	
	public Integer getChannel(N n) {
		return M.get(n);
	}
	
	public Set<Integer> getChannels() {
		return new TreeSet<Integer>(M.values());
	}
	
	
	@Override
	public boolean add(N n) {
		return add(n, CHANNEL_DEFAULT);
	}
	public boolean add(N n, Integer channel) {
		if (n == null) throw new NullPointerException("cannot add null to selection");
		boolean r = !M.containsKey(n);
		M.put(n, channel);
		return r;
	}

	@Override
	public boolean addAll(Collection<? extends N> c) {
		return addAll(c, CHANNEL_DEFAULT);
	}
	public boolean addAll(TypedSelection<? extends N> c) {
		boolean r = !M.keySet().containsAll(c);
		M.putAll(c.M);
		return r;
	}
	public boolean addAll(Collection<? extends N> c, Integer channel) {
		boolean r = false;
		for (N n : c) {
			r |= add(n, channel);
		}
		return r;
	}

	@Override
	public void clear() {
		M.clear();
	}
	public void clear(Integer channel) {
		for (N n : new HashSet<N>(this)) {
			if (channel.equals(getChannel(n))) {
				remove(n);
			}
		}
	}

	@Override
	public boolean contains(Object o) {
		return M.containsKey(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return M.keySet().containsAll(c);
	}

	@Override
	public boolean isEmpty() {
		return M.isEmpty();
	}

	@Override
	public Iterator<N> iterator() {
		return M.keySet().iterator();
	}

	@Override
	public boolean remove(Object o) {
		boolean r = M.containsKey(o);
		M.remove(o);
		return r;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean r = false;
		for (Object o : c) {
			r |= remove(o);
		}
		return r;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		Set<Object> toRemove = new HashSet<Object>(M.keySet());
		toRemove.removeAll(c);
		return removeAll(toRemove);
	}

	@Override
	public int size() {
		return M.size();
	}

	@Override
	public Object[] toArray() {
		return M.keySet().toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return M.keySet().toArray(a);
	}

}
