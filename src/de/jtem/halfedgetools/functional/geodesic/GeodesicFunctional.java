package de.jtem.halfedgetools.functional.geodesic;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.FunctionalUtils;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.halfedgetools.functional.LaplaceOperator;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class GeodesicFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>>
	void evaluate(HDS hds, DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
	}

	// Calculate the energy of a given configuration
	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		double[] 
		       vv = new double[3],
		       vs = new double[3],
		       vt = new double[3];
		for (V v : hds.getVertices()) {
			double[] normal = new double[3];
			LaplaceOperator.evaluate(hds, v, x, normal);
			List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
			int nn = star.size();
			if(nn % 2 == 0) { // vertex has an even number of neighbors!
				for(int i = 0; i < nn/2; ++i){
					V s = star.get(i);
					V t = star.get(nn/2+i);
					
					FunctionalUtils.getPosition(v, x, vv);
					FunctionalUtils.getPosition(s, x, vs);
					FunctionalUtils.getPosition(t, x, vt);
					Rn.subtract(vs, vs, vv);
					Rn.subtract(vt, vt, vv);
					Rn.projectOntoComplement(vs, vs, normal);
					Rn.projectOntoComplement(vt, vt, normal);
					
					result += Math.PI - FunctionalUtils.angle(vs,vt);
				}
				
			} else {
				
			}
			
		}
//		System.out.println(result);
		return result;
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		//output
			Gradient G
	) {
		double[] 
		       vv = new double[3],
		       vs = new double[3],
		       vt = new double[3],
		       ds = new double[3],
		       dv = new double[3],
		       dt = new double[3],
		       normal = new double[3];
		G.setZero();
		for (V v : hds.getVertices()) {
			LaplaceOperator.evaluate(hds, v, x, normal);
			List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
			int nn = star.size();
			if(nn % 2 == 0) { // vertex has an even number of neighbors!
				for(int i = 0; i < nn/2; ++i){
					V s = star.get(i);
					V t = star.get(nn/2+i);
					
					int
						is = s.getIndex(),
						iv = v.getIndex(),
						it = t.getIndex();
					
					FunctionalUtils.getPosition(v, x, vv);
					FunctionalUtils.getPosition(s, x, vs);
					FunctionalUtils.getPosition(t, x, vt);
					Rn.subtract(vs, vs, vv);
					Rn.subtract(vt, vt, vv);
					Rn.subtract(vv,vv,vv);
					
					Rn.projectOntoComplement(vs, vs, normal);
					Rn.projectOntoComplement(vt, vt, normal);
					
					FunctionalUtils.angleGradient(vs, vv, vt, ds, dv, dt);
					for (int j = 0; j < ds.length; j++) {
						G.add(is*3+j, -ds[j]);
					}
					for (int j = 0; j < dv.length; j++) {
						G.add(iv*3+j, -dv[j]);
					}
					for (int j = 0; j < dt.length; j++) {
						G.add(it*3+j, -dt[j]);
					}
				}
				
			} else {
				// TODO: do something with vertices of odd degree!
			}
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
