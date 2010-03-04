package de.jtem.halfedgetools.jreality.calculator;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class JRSubdivisionCalculator implements EdgeAverageCalculator, FaceBarycenterCalculator {

	private double
		alpha = 0.5;
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(E e) {
		JREdge<?, ?, ?> je = (JREdge<?, ?, ?>)e;
		double[] s = je.getStartVertex().position;
		double[] t = je.getTargetVertex().position;
		return Rn.linearCombination(null, alpha, t, 1 - alpha, s);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(F f) {
		double[] pos = new double[3];
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		for (E e : b) {
			JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)e.getTargetVertex();
			Rn.add(pos, pos, jv.position);
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		boolean result = false;
		result |= JREdge.class.isAssignableFrom(nodeClass);
		result |= JRFace.class.isAssignableFrom(nodeClass);
		return result;
	}

	@Override
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	@Override
	public void setIgnore(boolean ignore) {
	}

}
