/**
 * 
 */
package de.jtem.halfedgetools.functional.dirichlet;

import java.util.LinkedList;
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
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;

/**
 * @author josefsso
 *
 */
public class DirichletEnergyFunctional <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>> implements Functional<V, E, F> {

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
		
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		
	}

	public double evaluate(HalfEdgeDataStructure<V, E, F> hds, DomainValue x) {
		double result = 0.0;
		for (E e : hds.getPositiveEdges()) {
			
			result += w(e,x)*getLengthSquared(e, x);
			
		}
		return result;
	}


	public void evaluateGradient(HalfEdgeDataStructure<V, E, F> hds, DomainValue x, Gradient G) {

		G.setZero();
		
		double[] sum = new double[3];
		
		List<V> boundaryV = new LinkedList<V>();
		for(V v : hds.getVertices()) {
			if(HalfEdgeUtils.isBoundaryVertex(v)) {
				boundaryV.add(v);
			}
		}
		
		// interior vertices
		for (V v : hds.getVertices()) {
			
			sum = new double[] {0.0,0.0,0.0};
			int off = v.getIndex() * 3;
			
			if(boundaryV.contains(v)) {
				
			} else {
			
				for (E e : HalfEdgeUtils.incomingEdges(v)) {
					V w = e.getStartVertex();
					
					double weight = Δ_ij(v, w, x);
					
					if(boundaryV.contains(w)) {
						weight *= 2.0;
					}
					double[] pos = new double[3];
					getPosition(w, x, pos);
					
					Rn.times(pos, weight, pos);
					Rn.add(sum, sum, pos);
					
				}
				
				double weight = Δ_ij(v, v, x);
				double[] pos = new double[3];
				getPosition(v, x, pos);
				
				Rn.times(pos, weight, pos);
				Rn.add(sum, sum, pos);
			}
							
			
			// dimensions
			for (int d = 0; d < 3; d++) {
				G.add(off + d, sum[d]);
			}
			
		}

	}

	public double Δ_ij(V i, V j, DomainValue x) {

		if (i != j) {
			E e = null;
			e = HalfEdgeUtils.findEdgeBetweenVertices(i, j);
			// adjacent?
			if (e != null) {
				return -w(e, x);
			} else {
				return 0.0;
			}
		} else {
			Double sum = 0.0;
			for (E e : HalfEdgeUtils.incomingEdges(i)) {
					sum += Δ_ij(e.getTargetVertex(), e.getStartVertex(), x);
			}
			return -sum;
		}
	}
	
	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}
	
	public void getPosition(V v, DomainValue x, double[] pos) {
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
	}
	
	public static double cot(Double phi) {
		return 1.0 / StrictMath.tan(phi);
	}
	
	public double getLengthSquared(E e, DomainValue x) {
		double[] s = new double[3];
		double[] t = new double[3];
		getPosition(e.getStartVertex(), x, s);
		getPosition(e.getTargetVertex(), x, t);
		return Rn.euclideanDistanceSquared(s, t);
	}
	
	public double getAlpha(E e, DomainValue x){
		double a = getLengthSquared(e.getNextEdge(), x);
		double b = getLengthSquared(e.getPreviousEdge(), x);
		double c = getLengthSquared(e, x);
	
		return Math.acos((a + b - c) / (2.0 * Math.sqrt(a * b)));
	}
	

	private double w(E e, DomainValue x) {
		E e1 = e;
		E e2 = e.getOppositeEdge();
		double val = 0.0;
		double w = 0.5;
		
		if(e.getLeftFace() == null) {
			val = w * cot(getAlpha(e2,x));
		} else if(e.getRightFace() == null) {
			val = w * cot(getAlpha(e1,x));
		} else { // interior edge
			double alpha_ij = cot(getAlpha(e1,x));
			double alpha_ji = cot(getAlpha(e2,x));

			// optimize
			val = w * (cot(alpha_ij) + cot(alpha_ji));
		}
		
		return val;
	}

	@Override
	public boolean hasGradient() {
		return true;
	}

}
