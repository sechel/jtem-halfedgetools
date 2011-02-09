package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.type.PrincipalCurvatureMax;

@PrincipalCurvatureMax
public class VertexPrincipalCurvaturesMaxAdapter extends AbstractDoubleMapAdapter {

	public VertexPrincipalCurvaturesMaxAdapter(Map<? extends Node<?, ?, ?>, Double> valueMap, String name) {
		super(valueMap, name);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass);
	}

}
