/**
 * 
 */
package de.jtem.halfedgetools.algorithm;

import de.jtem.halfedge.Edge;

public interface Edge3DAdapter<E extends Edge<?, E, ?>> {
	public double[] getCoord(E e, double a);
}