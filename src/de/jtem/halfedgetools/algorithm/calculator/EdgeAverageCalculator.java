package de.jtem.halfedgetools.algorithm.calculator;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Calculator;

public interface EdgeAverageCalculator extends Calculator {

	public abstract <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] get(E e);
	
	public void setAlpha(double alpha);
	public void setIgnore(boolean ignore);
	
}