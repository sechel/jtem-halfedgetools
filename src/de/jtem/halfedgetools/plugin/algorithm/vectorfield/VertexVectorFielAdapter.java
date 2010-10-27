package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
public class VertexVectorFielAdapter extends AbstractVectorFielAdapter {

	public VertexVectorFielAdapter(
		Map<? extends Node<?, ?, ?>, double[]> vecMap, String name) {
		super(vecMap, name);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass);
	}
	
}
