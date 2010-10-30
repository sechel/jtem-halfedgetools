package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.CurvatureField;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
@CurvatureField
public class EdgeVectorFielAdapter extends AbstractVectorFielAdapter {

	public EdgeVectorFielAdapter(
		Map<? extends Node<?, ?, ?>, double[]> vecMap, String name) {
		super(vecMap, name);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}
	
}
