/**
 * 
 */
package de.jtem.halfedgetools.algorithm.subdivision.adapters;

import de.jtem.halfedge.Edge;

public interface SubdivisionEdge3DAdapter<E extends Edge<?, E, ?>> {
	public double[] getCoord(E e, double a, boolean ignore);
}