package de.jtem.halfedgetools.jreality.calculator;

import static java.lang.Math.sqrt;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.FaceAreaCalculator;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;

public class JRFaceAreaCalculator implements FaceAreaCalculator {

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return JRFace.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double get(F f) {
		JRFace<?, ?, ?> jf = (JRFace<?, ?, ?>)f;
		JREdge<?, ?, ?> e1 = jf.getBoundaryEdge();
		JREdge<?, ?, ?> e2 = jf.getBoundaryEdge().getNextEdge();
		double[] v1 = e1.getTargetVertex().position;
		double[] v2 = e2.getTargetVertex().position;
		double[] v3 = e1.getStartVertex().position;
		double a = 0,b = 0,c = 0;
		for (int k=0; k<3; k++) {
           a += (v1[k] - v2[k]) * (v1[k] - v2[k]);
           b += (v1[k] - v2[k]) * (v3[k] - v2[k]);
           c += (v3[k] - v2[k]) * (v3[k] - v2[k]);
		}
		double area = a*c-b*b;
		if (area <= 0.0) {
           return 0.0;
		} else {
           return sqrt(area) / 2.0;
		}
	}

}
