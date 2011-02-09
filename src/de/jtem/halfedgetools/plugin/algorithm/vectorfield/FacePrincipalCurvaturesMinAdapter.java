package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.PrincipalCurvatureMin;

@PrincipalCurvatureMin
public class FacePrincipalCurvaturesMinAdapter extends AbstractDoubleMapAdapter {

	public FacePrincipalCurvaturesMinAdapter(Map<? extends Node<?, ?, ?>, Double> valueMap, String name) {
		super(valueMap, name);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

}
