package de.jtem.halfedgetools.tutorial;

import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;

@Position
public class TestPositionAdapter extends AbstractTypedAdapter<VV, VE, VF, double[]> {

	public TestPositionAdapter() {
		super(VV.class, null, null, double [].class, true, true);
	}
	
	@Override
	public double[] getVertexValue(VV v, AdapterSet a) {
		return v.p;
	}
	
	@Override
	public void setVertexValue(VV v, double[] value, AdapterSet a) {
		switch (value.length) {
		case 2:
			v.p[0] = value[0];
			v.p[1] = value[1];
			v.p[2] = 0.0;
			v.p[3] = 1.0;
			break;
		case 3:
			v.p[0] = value[0];
			v.p[1] = value[1];
			v.p[2] = value[2];
			v.p[3] = 1.0;
			break;
		case 4:
			System.arraycopy(value, 0, v.p, 0, 4);
			break;
		default:
			throw new IllegalArgumentException("Ilegal dimension in TestPositionAdapter.setVertexValue()");
		}
	}
	
}
