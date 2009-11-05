/**
 * 
 */
package de.jtem.halfedgetools.algorithm;

import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public interface Normal3DAdapter<N extends Node<?, ?, ?>> {
	public double[] getNormal(N v);
	public void setNormal(N v, double[] c);
}