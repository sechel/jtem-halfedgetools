/**
 * 
 */
package de.jtem.halfedgetools.algorithm.subdivision.adapters;

import de.jtem.halfedge.Node;

public interface SubdivisionCoord3DAdapter<N extends Node<?, ?, ?>> {
	public double[] getCoord(N n);
	public void setCoord(N n, double[] c);
}