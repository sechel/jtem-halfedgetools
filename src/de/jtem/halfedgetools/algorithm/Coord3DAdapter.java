/**
 * 
 */
package de.jtem.halfedgetools.algorithm;

import de.jtem.halfedge.Node;

public interface Coord3DAdapter<N extends Node<?, ?, ?>> {
	public double[] getCoord(N n);
	public void setCoord(N n, double[] c);
}