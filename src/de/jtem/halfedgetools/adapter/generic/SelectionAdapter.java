package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.selection.Selection;

@de.jtem.halfedgetools.adapter.type.Selection
public class SelectionAdapter extends AbstractAdapter<Integer> {

	private HalfedgeInterface
		hif = null;
	
	public SelectionAdapter(HalfedgeInterface hif) {
		super(Integer.class, true, true);
		this.hif = hif;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return true;
	}

	@Override
	public boolean checkType(Class<?> typeClass) {
		return Integer.class == typeClass;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		N extends Node<V, E, F>
	> Integer get(N n, AdapterSet a) {
		return hif.getSelection().getChannel(n);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		N extends Node<V, E, F>
	> void set(N n, Integer value, AdapterSet a) {
		Selection s = hif.getSelection();
		s.add(n, value);
	}
	
	@Override
	public String toString() {
		return "Selection";
	}
	
}
