package de.jtem.halfedgetools.plugin;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public class HalfedgeSelection {

	private Set<Vertex<?, ?, ?>>
		vSet = new HashSet<Vertex<?,?,?>>();
	private Set<Edge<?, ?, ?>>
		eSet = new HashSet<Edge<?,?,?>>();
	private Set<Face<?, ?, ?>>
		fSet = new HashSet<Face<?,?,?>>();
	
	
	public HalfedgeSelection() {
	}
	
	public HalfedgeSelection(Collection<? extends Node<?,?,?>> nodes) {
		addAll(nodes);
	}
	
	public HalfedgeSelection(Node<?,?,?>... nodes) {
		this(Arrays.asList(nodes));
	}
	
	public HalfedgeSelection(HalfedgeSelection sel) {
		vSet = new HashSet<Vertex<?,?,?>>(sel.vSet);
		eSet = new HashSet<Edge<?,?,?>>(sel.eSet);
		fSet = new HashSet<Face<?,?,?>>(sel.fSet);
	}
	
	
	public Set<Vertex<?,?,?>> getVertices() {
		return Collections.unmodifiableSet(vSet);
	}
	public Set<Edge<?,?,?>> getEdges() {
		return Collections.unmodifiableSet(eSet);
	}
	public Set<Face<?,?,?>> getFaces() {
		return Collections.unmodifiableSet(fSet);
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> getVertices(HDS hds) {
		Set<V> result = new HashSet<V>();
		for (Vertex<?, ?, ?> v : vSet) {
			if (v.getHalfEdgeDataStructure() == hds) {
				result.add((V)v);
			}
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<E> getEdges(HDS hds) {
		Set<E> result = new HashSet<E>();
		for (Edge<?, ?, ?> e : eSet) {
			if (e.getHalfEdgeDataStructure() == hds) {
				result.add((E)e);
			}
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<F> getFaces(HDS hds) {
		Set<F> result = new HashSet<F>();
		for (Face<?, ?, ?> f : fSet) {
			if (f.getHalfEdgeDataStructure() == hds) {
				result.add((F)f);
			}
		}
		return result;
	}
	
	public List<Node<?,?,?>> getNodes() {
		List<Node<?,?,?>> result = new LinkedList<Node<?,?,?>>();
		for (Vertex<?,?,?> v : getVertices()) {
			result.add(v);
		}
		for (Edge<?,?,?> e : getEdges()) {
			result.add(e);
		}
		for (Face<?,?,?> f : getFaces()) {
			result.add(f);
		}
		return result;
	}
	
	public void clear() {
		vSet.clear();
		eSet.clear();
		fSet.clear();
	}
	
	public boolean isSelected(Node<?,?,?> n) {
		return vSet.contains(n) | eSet.contains(n) | fSet.contains(n);
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected) {
		if (selected == isSelected(n)) return;
		if (n instanceof Vertex<?,?,?>) {
			if (selected) {
				vSet.add((Vertex<?,?,?>)n);
			} else {
				vSet.remove(n);
			}
		}
		if (n instanceof Edge<?,?,?>) {
			if (selected) {
				eSet.add((Edge<?,?,?>)n);
			} else {
				eSet.remove(n);
			}
		}
		if (n instanceof Face<?,?,?>) {
			if (selected) {
				fSet.add((Face<?,?,?>)n);
			} else {
				fSet.remove(n);
			}
		}
	}
	
	
	public void add(Node<?,?,?> n) {
		if (n instanceof Vertex<?,?,?>) {
			vSet.add((Vertex<?,?,?>)n);
		}
		if (n instanceof Edge<?,?,?>) {
			eSet.add((Edge<?,?,?>)n);
		}
		if (n instanceof Face<?,?,?>) {
			fSet.add((Face<?,?,?>)n);
		}
	}
	
	public void remove(Node<?,?,?> n) {
		if (n instanceof Vertex<?,?,?>) {
			vSet.remove(n);
		}
		if (n instanceof Edge<?,?,?>) {
			eSet.remove(n);
		}
		if (n instanceof Face<?,?,?>) {
			fSet.remove(n);
		}
	}
	
	public void addAll(Collection<? extends Node<?,?,?>> nodes) {
		for (Node<?,?,?> n : nodes) {
			add(n);
		}
	}
	
	public void removeAll(Collection<? extends Node<?,?,?>> nodes) {
		for (Node<?,?,?> n : nodes) {
			remove(n);
		}
	}
	
}