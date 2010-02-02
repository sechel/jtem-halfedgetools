package de.jtem.halfedgetools.functional.geodesic;

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
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public class GeodesicAngleFunctional<
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
			if(!HalfEdgeUtils.isBoundaryVertex(v)) {
				FunctionalUtils.getPosition(v, x, vv);
				List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
				int nn = star.size();
				double[] angles = new double[nn/2];
				if(nn % 2 == 0) { // vertex has an even number of neighbors!
					for(int i = 0; i < nn; ++i){
						FunctionalUtils.getPosition(star.get(i), x, vs);
						FunctionalUtils.getPosition(star.get((i+1)%nn), x, vt);
						angles[i%(nn/2)] += ((i>=nn/2)?1.0:-1.0)*FunctionalUtils.angle(vs,vv,vt); 
					}
//					System.out.println("vertex " + v.getIndex());
//					System.out.println("angles " + Arrays.toString(angles));
					result += Rn.euclideanNormSquared(angles);
				} else { // interior vertex of odd degree.
					
				}	
			} else { // boundary vertex.
				
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
		       dt = new double[3];
		G.setZero();
		for (V v : hds.getVertices()) {
			if(!HalfEdgeUtils.isBoundaryVertex(v)) {
				FunctionalUtils.getPosition(v, x, vv);
				int vi = v.getIndex();
				List<V> star = HalfEdgeUtilsExtra.getVertexStar(v);
				int nn = star.size();
				double[] angles = new double[nn];
				if(nn % 2 == 0) { // vertex has an even number of neighbors!
					for(int i = 0; i < nn; ++i){
						V 	s = star.get(i),
							t = star.get((i+1)%nn);
						FunctionalUtils.getPosition(s, x, vs);
						FunctionalUtils.getPosition(t, x, vt);
						angles[i%(nn/2)] += ((i>=nn/2)?1.0:-1.0)*FunctionalUtils.angle(vs,vv,vt); 
					}
					for (int i = 0; i < nn; i++) {
						V 	s = star.get(i),
							t = star.get((i+1)%nn);
						int	si = s.getIndex(),
							ti = t.getIndex();
						
						FunctionalUtils.getPosition(s, x, vs);
						FunctionalUtils.getPosition(t, x, vt);
						FunctionalUtils.angleGradient(vs, vv, vt, ds, dv, dt);
						double scale = ((i>=(nn/2))?2.0:-2.0)*(angles[i%(nn/2)]);
						Rn.times(ds,scale, ds);
						Rn.times(dv,scale, dv);
						Rn.times(dt,scale, dt);
						
						for (int j = 0; j < ds.length; j++) {
							G.add(3*si+j, ds[j]);
						}
						for (int j = 0; j < dv.length; j++) {
							G.add(3*vi+j, dv[j]);
						}
						for (int j = 0; j < dt.length; j++) {
							G.add(3*ti+j, dt[j]);
						}
					}
				} else { // interior vertex of odd degree.
					
				}	
			} else { // boundary vertex.
				
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
