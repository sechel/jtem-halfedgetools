package de.jtem.halfedgetools.functional;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;

public class LaplaceOperator  {

	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>>
	void evaluate(HalfEdgeDataStructure<V,E,F> hds, V v, DomainValue x, double[] normal) {
		double[] 
		       sv = new double[3],
		       tv = new double[3], 
		       ev = new double[3];
		FunctionalUtils.getPosition(v,x,tv);
		for (E e : HalfEdgeUtils.incomingEdges(v)) {
			FunctionalUtils.getPosition(e.getStartVertex(),x,sv);
			Rn.subtract(ev, tv, sv);
			double ew = w(e,x);
			Rn.add(normal,normal,Rn.times(null, ew, ev));
		}
	}
	
	private static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> >
	double w(E e, DomainValue x) {
		
		E e1 = e;
		E e2 = e.getOppositeEdge();
		
		double val = 0.0;
		double w = 0.5;
		
		if(e.getLeftFace() == null) 
		{
			val = w * cot(getAlpha(e2,x));
		} else if(e.getRightFace() == null) 
		{
			val = w * cot(getAlpha(e1,x));
		} else 
		{ // interior edge
			double alpha_ji = cot(getAlpha(e1,x));
			double alpha_ij = cot(getAlpha(e2,x));
			// optimize
			val = w * (alpha_ij + alpha_ji);
		}
		
		return val;
	}
	
	public static <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> >
	double getAlpha(E e, DomainValue x){
		double[]
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3];
		
		FunctionalUtils.getPosition(e.getTargetVertex(),x,vi);
		FunctionalUtils.getPosition(e.getStartVertex(),x,vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
		return  FunctionalUtils.angle(vi, vk, vj);
	}
	
	public static double cot(Double phi) {
		return 1.0 / StrictMath.tan(phi);
	}
	
}
