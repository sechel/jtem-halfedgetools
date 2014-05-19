package de.jtem.halfedgetools.functional;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

public abstract class AbstractFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	@Override
	public abstract <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H);

	@Override
	public boolean hasHessian() {
		return false;
	}

	@Override
	public boolean hasGradient() {
		return false;
	}
	
	@Override
	public abstract <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int getDimension(HDS hds);

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}

}
