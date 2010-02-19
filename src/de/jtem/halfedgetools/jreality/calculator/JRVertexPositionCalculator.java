package de.jtem.halfedgetools.jreality.calculator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class JRVertexPositionCalculator implements VertexPositionCalculator {

	@Override
	public  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(V v) {
		JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)v;
		return jv.position;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> void set(V v, double[] c) {
		JRVertex<?, ?, ?> jv = (JRVertex<?, ?, ?>)v;
		jv.position = c;
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return JRVertex.class.isAssignableFrom(nodeClass);
	}

}
