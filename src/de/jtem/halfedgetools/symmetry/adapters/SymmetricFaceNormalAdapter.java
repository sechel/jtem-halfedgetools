package de.jtem.halfedgetools.symmetry.adapters;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.symmetry.node.SEdge;
import de.jtem.halfedgetools.symmetry.node.SFace;
import de.jtem.halfedgetools.symmetry.node.SVertex;

@Normal
public class SymmetricFaceNormalAdapter extends AbstractTypedAdapter<SVertex, SEdge, SFace, double[]> {

	public SymmetricFaceNormalAdapter() {
		super(SVertex.class, SEdge.class, SFace.class, double[].class, true, false);
	}
	
	@Override
	public double[] getFaceValue(SFace f, AdapterSet a) {
		double[] v1 = f.getEmbeddingOnBoundary(0, false);
		double[] v2 = f.getEmbeddingOnBoundary(1, false);
		double[] v3 = f.getEmbeddingOnBoundary(2, false);
		Rn.subtract(v1, v3, v1);
		Rn.subtract(v2, v3, v2);
		double[] normal = Rn.crossProduct(v3, v1, v2);
		Rn.normalize(normal, normal);
		return normal;
	}

	@Override
	public double getPriority() {
		return 1;
	}

}
