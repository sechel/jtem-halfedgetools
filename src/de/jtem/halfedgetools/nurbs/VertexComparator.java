package de.jtem.halfedgetools.nurbs;

import java.util.Comparator;

import de.jtem.halfedge.Vertex;

public class VertexComparator implements Comparator<Vertex<?,?,?>> {
	
	public int curveIndex;
	
	public int compare(Vertex<?,?,?> v1, Vertex<?,?,?> v2) {
		return (int)Math.signum(v1.getIndex() - v2.getIndex());
	} 

}
