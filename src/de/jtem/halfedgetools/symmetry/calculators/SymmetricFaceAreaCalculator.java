package de.jtem.halfedgetools.symmetry.calculators;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.FaceAreaCalculator;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;

public class SymmetricFaceAreaCalculator implements FaceAreaCalculator {

	@Override
	public <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>> double get(
			F f) {
		
		SymmetricFace<?,?,?> sf = (SymmetricFace<?,?,?>)f;
		double[] v1 = sf.getEmbeddingOnBoundary(0, false);
		double[] v2 = sf.getEmbeddingOnBoundary(1, false);
		double[] v3 = sf.getEmbeddingOnBoundary(2, false);
		Rn.subtract(v1, v3, v1);
		Rn.subtract(v2, v3, v2);
		double[] normal = Rn.crossProduct(v3, v1, v2);
		return Rn.euclideanNorm(normal)/2.0;
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return SymmetricFace.class.isAssignableFrom(nodeClass);
	}

	@Override
	public double getPriority() {
		return 1;
	}

}
