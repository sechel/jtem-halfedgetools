package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
public class FaceVectorFielAdapter extends AbstractVectorFielAdapter {

	public FaceVectorFielAdapter(
		Map<? extends Node<?, ?, ?>, double[]> vecMap, String name) {
		super(vecMap, name);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}
	
}
