package de.jtem.halfedgetools.functional.electrostatic;

import java.util.Arrays;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

public class ElectrostaticFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>>
	void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null || G != null) {
			evaluateGradient(hds, x, E, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
		
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		//output
			Energy E,
			Gradient grad
	) {
		if(grad != null) {
			grad.setZero();
		}
		if(E != null) {
			E.setZero();
		}
		double energy = 0.0;
		double[] 
		       vv = new double[3],
		       nv = new double[3],
		       ne = new double[3];
		for(V v : hds.getVertices()) {
			
			FunctionalUtils.getPosition(v, x, vv);
			List<V> neighbors = HalfEdgeUtils.neighboringVertices(v);
			boolean[] nonNeighbors = new boolean[hds.numVertices()];
			nonNeighbors[v.getIndex()] = true;
			for(V n : neighbors) {
				nonNeighbors[n.getIndex()] = true;
			}
			for (int i = 0; i < nonNeighbors.length; i++) {
				if(!nonNeighbors[i]) {
					FunctionalUtils.getPosition(hds.getVertex(i), x, nv);
					Rn.subtract(ne, vv, nv);
					double r2 = Rn.innerProduct(ne, ne); 
					energy += 1/r2;
					if(grad != null) {
						Rn.times(ne, 2/(r2*r2), ne);
//						System.out.println(v.getIndex()+"-"+i + ":" + r2 + ":" + Arrays.toString(ne));
						FunctionalUtils.addVectorToGradient(grad, 3*i, ne);
						Rn.negate(ne, ne);
						FunctionalUtils.addVectorToGradient(grad, 3*v.getIndex(), ne);
					}
				}
			}
		}
		if(E != null) {
			E.set(energy);
		}
	}

	public void evaluateHessian(
		// input
			HalfEdgeDataStructure<V, E, F> G, DomainValue x,
		// output
			Hessian hess) {
		// TODO: Calculate the hessian for a given configuration x
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return 3*hds.numVertices();
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
