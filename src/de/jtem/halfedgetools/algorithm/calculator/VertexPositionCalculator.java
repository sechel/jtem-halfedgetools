package de.jtem.halfedgetools.algorithm.calculator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Calculator;

public interface VertexPositionCalculator extends Calculator {
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] get(V v);
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void set(V v, double[] c);
	
}