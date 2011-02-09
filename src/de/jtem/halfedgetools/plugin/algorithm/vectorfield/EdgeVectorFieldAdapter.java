package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
public class EdgeVectorFieldAdapter extends AbstractVectorFieldMapAdapter {

	public EdgeVectorFieldAdapter(Map<? extends Node<?, ?, ?>, double[]> vecMap, String name) {
		super(vecMap, name);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}
	
}
