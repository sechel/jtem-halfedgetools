package de.jtem.halfedgetools.symmetry2;

import java.util.HashMap;
import java.util.Map;

import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

@GroupElement
public class MappedGroupElementAdapter extends AbstractAdapter<DiscreteGroupElement> {
	
	private Map<Object, DiscreteGroupElement>
		map = new HashMap<Object, DiscreteGroupElement>();
	
	public MappedGroupElementAdapter() {
		super(DiscreteGroupElement.class, true, true);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass) || Vertex.class.isAssignableFrom(nodeClass);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> DiscreteGroupElement getE(E e, AdapterSet a) {
		return map.get(e);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, DiscreteGroupElement value, AdapterSet a) {
		map.put(e, value);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> DiscreteGroupElement getV(V e, AdapterSet a) {
		return map.get(e);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V e, DiscreteGroupElement value, AdapterSet a) {
		map.put(e, value);
	}
	

}	
