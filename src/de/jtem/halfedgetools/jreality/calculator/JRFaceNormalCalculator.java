package de.jtem.halfedgetools.jreality.calculator;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.FaceNormalCalculator;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;

public class JRFaceNormalCalculator implements FaceNormalCalculator {

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return JRFace.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(F f) {
		JRFace<?, ?, ?> jf = (JRFace<?, ?, ?>)f;
		if (jf.normal != null) {
			return jf.normal;
		} else {
			JREdge<?, ?, ?> e1 = jf.getBoundaryEdge();
			JREdge<?, ?, ?> e2 = jf.getBoundaryEdge().getNextEdge();
			double[] v1 = e1.getTargetVertex().position;
			double[] v2 = e2.getTargetVertex().position;
			double[] v3 = e1.getStartVertex().position;
			Rn.subtract(v1, v3, v1);
			Rn.subtract(v2, v3, v2);
			double[] normal = Rn.crossProduct(v3, v1, v2);
			Rn.normalize(normal, normal);
			return normal;
		}
	}

}
