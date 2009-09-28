/**
 * 
 */
package de.jtem.halfedgetools.algorithm;

import de.jtem.halfedge.Vertex;

public interface Coord3DAdapter<V extends Vertex<V, ?, ?>> {
	public double[] getCoord(V v);
	public void setCoord(V v, double[] c);
}