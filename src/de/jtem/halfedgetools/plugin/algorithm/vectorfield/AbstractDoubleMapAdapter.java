package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

public abstract class AbstractDoubleMapAdapter extends AbstractAdapter<Double> {

	private Map<? extends Node<?, ?, ?>, Double> 
		valueMap = null;
	private String
		name = "";
	
	public AbstractDoubleMapAdapter(
		Map<? extends Node<?, ?, ?>, Double> vecMap, 
		String name
	) {
		super(Double.class, true, false);
		this.valueMap = vecMap;
		this.name = name;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> Double get(N n, AdapterSet a) {
		return valueMap.get(n);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
