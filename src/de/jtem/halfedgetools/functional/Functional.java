package de.jtem.halfedgetools.functional;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

public interface Functional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> {

	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H);
	
	public boolean hasHessian();
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds);

	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds);
	
}
