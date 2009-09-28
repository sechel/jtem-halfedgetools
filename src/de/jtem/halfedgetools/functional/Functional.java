package de.jtem.halfedgetools.functional;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

public interface Functional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	X extends DomainValue
> {

	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds, X x, Energy E, Gradient G, Hessian H);
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds);

	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds);
	
}
