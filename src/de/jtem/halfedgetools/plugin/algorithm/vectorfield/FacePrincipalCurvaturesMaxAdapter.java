package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.PrincipalCurvatureMax;

@PrincipalCurvatureMax
public class FacePrincipalCurvaturesMaxAdapter extends AbstractDoubleMapAdapter {

	public FacePrincipalCurvaturesMaxAdapter(Map<? extends Node<?, ?, ?>, Double> valueMap, String name) {
		super(valueMap, name);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

}
