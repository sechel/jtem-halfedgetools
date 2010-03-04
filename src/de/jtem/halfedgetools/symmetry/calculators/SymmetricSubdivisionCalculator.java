package de.jtem.halfedgetools.symmetry.calculators;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;

public class SymmetricSubdivisionCalculator implements EdgeAverageCalculator , VertexPositionCalculator, FaceBarycenterCalculator {

	private double
		alpha = 0.5;
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(E e) {
		SymmetricEdge<?, ?, ?> je = (SymmetricEdge<?, ?, ?>)e;
		double[] s = je.getStartVertex().getEmbedding();
		double[] t = je.getTargetVertex().getEmbedding();
		return Rn.linearCombination(null, alpha, t, 1 - alpha, s);
	}

	@Override
	public  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(V v) {
		SymmetricVertex<?, ?, ?> jv = (SymmetricVertex<?, ?, ?>)v;
		return jv.getEmbedding();
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> void set(V v, double[] c) {
		SymmetricVertex<?, ?, ?> jv = (SymmetricVertex<?, ?, ?>)v;
		jv.setEmbedding(c);
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
			SymmetricVertex<?, ?, ?> jv = (SymmetricVertex<?, ?, ?>)e.getTargetVertex();
			Rn.add(pos, pos, jv.getEmbedding());
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		boolean result = false;
		result |= SymmetricVertex.class.isAssignableFrom(nodeClass);
		result |= SymmetricEdge.class.isAssignableFrom(nodeClass);
		result |= SymmetricFace.class.isAssignableFrom(nodeClass);
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
