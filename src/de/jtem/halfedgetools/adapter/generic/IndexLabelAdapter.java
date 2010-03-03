package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Label;

@Label
public class IndexLabelAdapter extends AbstractAdapter<String> {

	public IndexLabelAdapter() {
		super(String.class, true, false);
	}
	
	public IndexLabelAdapter(Class<? extends String> typeClass, boolean getter, boolean setter) {
		super(typeClass, getter, setter);
	}

	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return true;
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> String get(N n, AdapterSet a) {
		return "" + n.getIndex();
	}
	
}
