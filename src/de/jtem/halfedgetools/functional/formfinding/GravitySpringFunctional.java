package de.jtem.halfedgetools.functional.formfinding;

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
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.Length;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.WeightFunction;

public class GravitySpringFunctional<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>> 
	implements Functional<V, E, F> {

	private Length<E>
		l0 = null;
	private WeightFunction<E> 
		w = null;
	private double
		g = 9.81;

	private double[] 
	    dir = new double[] {0.0,1.0,0.0};
	
	public GravitySpringFunctional(Length<E> l0, WeightFunction<E> w, double g, double[] dir) {
		this.l0 = l0;
		this.w = w;
		this.g = g;
		Rn.times(this.dir, g, Rn.normalize(null, dir));
	}
	
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
		double[] s = new double[3];
		double[] t = new double[3];
		double result = 0.0;
		for (E e : hds.getPositiveEdges()) {
			if(e.getLeftFace() != null && e.getRightFace() != null) {
				FunctionalUtils.getPosition(e.getStartVertex(), x, s);
				FunctionalUtils.getPosition(e.getTargetVertex(), x, t);
				double length = Rn.euclideanDistance(s, t);
				result += w.getWeight(e)*(length-l0.getTargetLength(e))*(length-l0.getTargetLength(e));
			}
		}
		for(V v : hds.getVertices()) {
			if(!HalfEdgeUtils.isBoundaryVertex(v)) {
				double[] vv = new double[3];
				FunctionalUtils.getPosition(v, x, vv);
				result += Rn.innerProduct(dir,vv);
			}
		}
		return result;
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> G,
			DomainValue x,
		//output
			Gradient grad
	) {
		grad.setZero();
		double[] vk = new double[3];
		double[] vj = new double[3];
		double[] smt = new double[3];
		for (V v : G.getVertices()) {
			FunctionalUtils.getPosition(v, x, vk);
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				double l = l0.getTargetLength(e);
				FunctionalUtils.getPosition(e.getStartVertex(), x, vj);
				Rn.subtract(smt, vk, vj);
				double factor = (1-l/Rn.euclideanDistance(vk, vj));
				int off = v.getIndex() * 3;
				for (int d = 0; d < 3; d++) {
					grad.add(off + d, 2*(vk[d] - vj[d]) * factor * w.getWeight(e));
				}
			}
			if(!HalfEdgeUtils.isBoundaryVertex(v)){
				FunctionalUtils.addVectorToGradient(grad,3*v.getIndex(),dir);
			}
			
		}
//		for (int i = 0; i < 3*G.numVertices(); i++) {
//			System.out.println(grad.get(i));
//		}
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
