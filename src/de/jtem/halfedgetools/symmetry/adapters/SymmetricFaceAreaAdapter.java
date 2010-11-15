package de.jtem.halfedgetools.symmetry.adapters;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Area;
import de.jtem.halfedgetools.symmetry.node.SEdge;
import de.jtem.halfedgetools.symmetry.node.SFace;
import de.jtem.halfedgetools.symmetry.node.SVertex;

@Area
public class SymmetricFaceAreaAdapter extends AbstractTypedAdapter<SVertex, SEdge, SFace, Double> {

	public SymmetricFaceAreaAdapter() {
		super(SVertex.class, SEdge.class, SFace.class, Double.class, true, false);
	}
	
	@Override
	public Double getFaceValue(SFace f, AdapterSet a) {		
		double[] v1 = f.getEmbeddingOnBoundary(0, false);
		double[] v2 = f.getEmbeddingOnBoundary(1, false);
		double[] v3 = f.getEmbeddingOnBoundary(2, false);
		Rn.subtract(v1, v3, v1);
		Rn.subtract(v2, v3, v2);
		double[] normal = Rn.crossProduct(v3, v1, v2);
		return Rn.euclideanNorm(normal)/2.0;
	}

	@Override
	public double getPriority() {
		return 1;
	}

}
