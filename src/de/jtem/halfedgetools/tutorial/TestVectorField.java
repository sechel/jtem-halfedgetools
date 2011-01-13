package de.jtem.halfedgetools.tutorial;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.VectorField;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@VectorField
public class TestVectorField extends AbstractTypedAdapter<VV, VE, VF, double[]> {

	public TestVectorField() {
		super(VV.class, null, null, double[].class, true, false);
	}
	
	@Override
	public double[] getVertexValue(VV v, AdapterSet a) {
		return a.getD(Position3d.class, v);
	}
	
}
