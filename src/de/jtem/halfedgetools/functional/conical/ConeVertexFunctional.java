package de.jtem.halfedgetools.functional.conical;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class ConeVertexFunctional  <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> implements Functional<V, E, F> {

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasHessian() {
		// TODO Auto-generated method stub
		return false;
	}

}
