package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.PrincipalCurvatureMin;

@PrincipalCurvatureMin
public class EdgePrincipalCurvaturesMinAdapter extends AbstractDoubleMapAdapter {

	public EdgePrincipalCurvaturesMinAdapter(Map<? extends Node<?, ?, ?>, Double> valueMap, String name) {
		super(valueMap, name);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}

}
