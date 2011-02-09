package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.CurvatureField;
import de.jtem.halfedgetools.adapter.type.CurvatureFieldMax;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
@CurvatureField
@CurvatureFieldMax
public class FaceVectorFieldMaxAdapter extends FaceVectorFieldAdapter {

	public FaceVectorFieldMaxAdapter(Map<? extends Node<?, ?, ?>, double[]> vecMap, String name) {
		super(vecMap, name);
	}

}
