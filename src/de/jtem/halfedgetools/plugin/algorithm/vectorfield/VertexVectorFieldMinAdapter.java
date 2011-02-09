package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.CurvatureField;
import de.jtem.halfedgetools.adapter.type.CurvatureFieldMin;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
@CurvatureField
@CurvatureFieldMin
public class VertexVectorFieldMinAdapter extends VertexVectorFieldAdapter {

	public VertexVectorFieldMinAdapter(Map<? extends Node<?, ?, ?>, double[]> vecMap, String name) {
		super(vecMap, name);
	}

}
