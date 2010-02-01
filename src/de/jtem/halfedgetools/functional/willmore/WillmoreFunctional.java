package de.jtem.halfedgetools.functional.willmore;

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

/*
 * TODO: Calculate Hessian!!
 */

public class WillmoreFunctional<V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>>
		implements Functional<V, E, F> {

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
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
		double result = -hds.numVertices() * Math.PI;
		for (E e : hds.getPositiveEdges()) {
			double beta = 0.0;
			if (e.getRightFace() == null || e.getLeftFace() == null) {
				beta = BetaBnd(hds, x, e);
				double betaInf = BetaInfinity(hds,x,e);
				beta += betaInf;
			} else {
				beta = Beta(hds, x, e);
			}
			result += beta;
		}
		return result;
	}

	public void evaluateGradient(
		//input
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		//output
			Gradient G
	) {
		G.setZero();
		for (E e : hds.getPositiveEdges()) {
			if(e.getRightFace() == null || e.getLeftFace() == null) {
				//boundary edges
				BetaBndD(hds, x, e, G);
				BetaInfinityD(hds,x,e,G);
			} else {
				//interior edge
				BetaD(hds, x, e, G);
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
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(
			HDS hds) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasHessian() {
		return false;
	}

	// "copy" of Peter Schroeder's code
	// translate vi to the origin and apply a Moebius transformation M with
	// center vi
	// and radius |vj-vi| to the vertices.
	// output: a and b are the vectors connecting the images of the vertices
	// vk->vj and
	// vj->vl respectively.
	// return: square of the radius of the sphere used for the Moebius
	// transformation
	// Hence the angle between the circumcircles of the triangles
	// is now the angle between a and b!
	public double Moebius(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[] a, double[] b) 
	{
		// translate vi to origin
		vj = Rn.subtract(null, vj, vi);
		vk = Rn.subtract(null, vk, vi);
		vl = Rn.subtract(null, vl, vi);

		// calculate moebius transform
		double lj2 = Rn.innerProduct(vj, vj);
		double lk2 = Rn.innerProduct(vk, vk);
		double ll2 = Rn.innerProduct(vl, vl);
		Rn.subtract(a, vj, Rn.times(null, lj2 / lk2, vk));
		Rn.subtract(b, Rn.times(null, lj2 / ll2, vl), vj);
		return lj2;
	}

	// Scaled version of the above. no division needed. if only interested in
	// the angle between a and b this is perfectly fine.
	public double MoebiusScaled(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[] a, double[] b) 
	{
		// translate vi to origin
		vj = Rn.subtract(null, vj, vi);
		vk = Rn.subtract(null, vk, vi);
		vl = Rn.subtract(null, vl, vi);
		double lj2 = Rn.innerProduct(vj, vj);
		double lk2 = Rn.innerProduct(vk, vk);
		double ll2 = Rn.innerProduct(vl, vl);
		Rn.subtract(a, Rn.times(null, lk2, vj), Rn.times(null, lj2, vk));
		Rn.subtract(b, Rn.times(null, lj2, vl), Rn.times(null, ll2, vj));
		return lj2;
	}

	// Calculate the external angle of two circumcircles adjacent to the given
	// edge e in hds
	public double Beta(HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e) {
		double[] 
		       vi = new double[3], 
		       vj = new double[3], 
		       vk = new double[3], 
		       vl = new double[3], 
		       a_kj = new double[3], 
		       b_jl = new double[3];
		
		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
		FunctionalUtils.getPosition(e.getOppositeEdge().getNextEdge().getTargetVertex(), x, vl);
		MoebiusScaled(vi, vj, vk, vl, a_kj, b_jl);
		
		return FunctionalUtils.angle(a_kj, b_jl);
	}

	// Calculate beta for boundary edges
	public double BetaBnd(HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e) {
		double[] vi = new double[3], vj = new double[3], vk = new double[3];
		
		if (e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		} 

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);

		return Math.PI - FunctionalUtils.angle(vi,vk,vj);
	}

	// Calculate beta for edges connected to infinity
	public double BetaInfinity(HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x, E e) {
		double[] 
		       vi = new double[3], 
		       vj = new double[3], 
		       vk = new double[3];
		
		if (e.getRightFace() == null) {
			e = e.getOppositeEdge();
		} 
		
		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(),x,vk);
		
		return Math.PI - FunctionalUtils.angle(vi, vj, vk);
	}

	// Calculate the derivative of the Moebius transformation D (3x3 matrix)
	public void MoebiusD(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[][] D) 
	{
		double el = Rn.euclideanNorm(Rn.subtract(null, vj, vi));
		double[] tmp = Rn.subtract(null, vk, vi);
		double kl2 = Rn.innerProduct(tmp, tmp);
		double scaling = el * el / (kl2 * kl2);

		FunctionalUtils.outerProduct(tmp, D);
		Rn.times(D, -2.0, D);
		for (int i = 0; i < 3; i++) {
			D[i][i] += kl2;
		}
		Rn.times(D, scaling, D);
	}

	public void BetaD(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e,
		// output
			Gradient G) 
	{
		int 
			i = e.getStartVertex().getIndex(), 
			j = e.getTargetVertex().getIndex(), 
			k = e.getNextEdge().getTargetVertex().getIndex(), 
			l = e.getOppositeEdge().getNextEdge().getTargetVertex().getIndex();

		double[] 
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3], 
		       vl = new double[3];

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);
		FunctionalUtils.getPosition(e.getOppositeEdge().getNextEdge().getTargetVertex(), x, vl);
		
		double[] dk = new double[3];

		// derivative wrt i
		BetaDk(vl, vk, vi, vj, dk);
		for (int m = 0; m < dk.length; m++) {
			G.add(3 * i + m, dk[m]);
		}

		// derivative wrt j
		BetaDk(vk, vl, vj, vi, dk);
		for (int m = 0; m < dk.length; m++) {
			G.add(3 * j + m, dk[m]);
		}

		// derivative wrt k
		BetaDk(vi, vj, vk, vl, dk);
		for (int m = 0; m < dk.length; m++) {
			G.add(3 * k + m, dk[m]);
		}

		// derivative wrt l
		BetaDk(vj, vi, vl, vk, dk);
		for (int m = 0; m < dk.length; m++) {
			G.add(3 * l + m, dk[m]);
		}
	}

	public void BetaDk(
		// input
			double[] vi, double[] vj, double[] vk, double[] vl,
		// output
			double[] dk) 
	{
		double[] 
		       	a_kj = new double[3],
		       	b_jl = new double[3],
		       	angleGrad = new double[3];

		Moebius(vi, vj, vk, vl, a_kj, b_jl);
		
		FunctionalUtils.angleGradient(a_kj, b_jl, angleGrad);
		
		double[][] DpM = new double[3][3];
		MoebiusD(vi, vj, vk, vl, DpM);

		for (int i = 0; i < DpM.length; i++) {
			dk[i] = -Rn.innerProduct(angleGrad, DpM[i]);
		}
	}

	public void BetaBndD(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e,
		// output
			Gradient G) 
	{
		double[] 
		       vi = new double[3],
		       vj = new double[3],
		       vk = new double[3],
		       di = new double[3],
		       dj = new double[3],
		       dk = new double[3];
		
		if (e.getLeftFace() == null) {
			e = e.getOppositeEdge();
		} 
		
		int 
			i = e.getStartVertex().getIndex(),
			j = e.getTargetVertex().getIndex(),
			k = e.getNextEdge().getTargetVertex().getIndex();

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);

		FunctionalUtils.angleGradient(vj, vk, vi, dj, dk, di);
		//derivative with respect to vi
		for (int m = 0; m < di.length; m++) {
			G.add(3*i+m, -di[m]);
		}
		
		//derivative with respect to vj
		for (int m = 0; m < dj.length; m++) {
			G.add(3*j+m, -dj[m]);
		}
		
		//derivative with respect to vk
		for (int m = 0; m < di.length; m++) {
			G.add(3*k+m, -dk[m]);
		}
	}

	public void BetaInfinityD(
		// input
			HalfEdgeDataStructure<V, E, F> hds, DomainValue x, E e,
		// output
			Gradient G) 
	{
		double[] 
		       vi = new double[3], 
		       vj = new double[3], 
		       vk = new double[3],
		       di = new double[3],
		       dj = new double[3],
		       dk = new double[3];
		
		if (e.getRightFace() == null) {
			e = e.getOppositeEdge();
		} 
		
		int 
			i = e.getStartVertex().getIndex(),
			j = e.getTargetVertex().getIndex(),
			k = e.getNextEdge().getTargetVertex().getIndex();

		FunctionalUtils.getPosition(e.getStartVertex(), x, vi);
		FunctionalUtils.getPosition(e.getTargetVertex(), x, vj);
		FunctionalUtils.getPosition(e.getNextEdge().getTargetVertex(), x, vk);

		FunctionalUtils.angleGradient(vi, vj, vk, di, dj, dk);
		// Note: angleGradient points in the opposite direction since the angle 
		//       is Pi-angle(vi,vj,vk)!
		
		//derivative with respect to vi
		for (int m = 0; m < di.length; m++) {
			G.add(3*i+m, -di[m]);
		}
		
		//derivative with respect to vj
		for (int m = 0; m < di.length; m++) {
			G.add(3*j+m, -dj[m]);
		}

		//derivative with respect to vk
		for (int m = 0; m < dk.length; m++) {
			G.add(3*k+m, -dk[m]);
		}
	}
	
}
