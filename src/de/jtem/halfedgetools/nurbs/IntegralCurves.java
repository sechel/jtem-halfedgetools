package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;
import java.util.LinkedList;
import de.jreality.math.Rn;

public class IntegralCurves {
	
	public static double[] getMaxMinCurv(NURBSSurface ns,double u ,double v,boolean max){
		if(max){
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[1];
		}else{
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[0];
		}
	}

	public static LinkedList<double[]> rungeKutta(NURBSSurface ns, double[] tspan, double[] y0, double tol, boolean max) {
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
		double[] b = { 0, 0.5, 0.75, 1 };
		LinkedList<Double> t = new LinkedList<Double>();
		LinkedList<double[]> u = new LinkedList<double[]>();
		int dim = y0.length;
		double h = (tspan[1] - tspan[0]) / 1000;
		double tau;
		double vau;
		t.add(tspan[0]);
		u.add(y0);
		double[] Udomain = { ns.U[0], ns.U[ns.U.length - 1] };
		double[] Vdomain = { ns.V[0], ns.V[ns.V.length - 1] };
		double u1 = Udomain[0];
		double u2 = Udomain[1];
		double v1 = Vdomain[0];
		double v2 = Vdomain[1];
		double[] orientation = IntegralCurves.getMaxMinCurv(ns, y0[0],y0[1], max);

		while ((t.getLast() < tspan[1]) && (t.getLast() + h > t.getLast())) {
			double[] v = new double[dim];
			double[] sumA = new double[dim];
			for (int i = 0; i < dim; i++) {
				v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
			}
			double[][] k = new double[b.length][2];
			if (v[0] >= u2 || v[0] <= u1 || v[1] >= v2 || v[1] <= v1) {
				return u;
			}
			if (Rn.innerProduct(orientation, IntegralCurves.getMaxMinCurv(ns, v[0],v[1], max)) > 0) {
				k[0] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0],v[1], max));
			} else {
				k[0] = Rn.times(null, -1, Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns, v[0],v[1], max)));
			}
			for (int l = 1; l < b.length; l++) {
				sumA = Rn.times(null, A[l][0], k[0]);
				for (int m = 1; m < l - 1; m++) {
					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
				}
				if (v[0] + h * sumA[0] >= u2 || v[0] + h * sumA[0] <= u1
						|| v[1] + h * sumA[1] >= v2 || v[1] + h * sumA[1] <= v1) {
					System.out.println("out of domain");
					// return u;
				}
				if (v[0] + h * sumA[0] >= u2 && v[1] + h * sumA[1] > v1 && v[1] + h * sumA[1] < v2) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,u2 - 0.00000001, v[1] + h * sumA[1],max));
				}
				// 2. case
				else if (v[0] + h * sumA[0] >= u2 && v[1] + h * sumA[1] >= v2) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,u2 - 0.00000001, v2 - 0.00000001,max));
				}
				// 3. case
				else if (v[1] + h * sumA[1] >= v2 && v[0] + h * sumA[0] > u1 && v[0] + h * sumA[0] < u2) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v2 - 0.00000001,max));
				}
				// 4. case
				else if (v[0] + h * sumA[0] <= u1 && v[1] + h * sumA[1] >= v2) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,u1 + 0.00000001, v2 - 0.00000001,max));
				}
				// 5. case
				else if (v[0] + h * sumA[0] <= u1 && v[1] + h * sumA[1] > v1 && v[1] + h * sumA[1] < v2) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,u1 + 0.00000001, v[1] + h * sumA[1],max));
				}
				// 6. case
				else if (v[0] + h * sumA[0] <= u1 && v[1] + h * sumA[1] <= v1) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,u1 + 0.00000001, v1 + 0.00000001,max));
				}
				// 7. case
				else if (v[1] + h * sumA[1] <= v1 && v[0] + h * sumA[0] > u1 && v[0] + h * sumA[0] < u2) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v1 + 0.00000001,max));
				}
				// 8. case
				else if (v[0] + h * sumA[0] >= u2 && v[1] + h * sumA[1] <= v1) {
					k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,u2 - 0.00000001, v1 + 0.00000001,max));
				} else {
					if (Rn.innerProduct(orientation,Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0],v[1] + h * sumA[1],max))) > 0) {
						k[l] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v[1] + h * sumA[1],max));
					} else {
						k[l] = Rn.times(null, -1,Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v[1] + h * sumA[1],max)));
					}
				}
			}
			double[] Phi1 = new double[dim];
			double[] Phi2 = new double[dim];
			for (int l = 0; l < b.length; l++) {
				Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
				Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
			}
			v = Rn.add(null, v, Rn.times(null, h, Phi2));
			if (t.getLast() + h > tspan[1]) {
				h = tspan[1] - t.getLast();
			}
			tau = Rn.euclideanNorm(Rn.add(null, Phi2, Rn.times(null, -1, Phi1)));
			vau = Rn.euclideanNorm(u.getLast()) + 1;
			if (tau <= tol * vau) {
				t.add(t.getLast() + h);
				u.add(Rn.add(null, u.getLast(), Rn.times(null, h, Phi1)));
				Rn.normalize(orientation, orientation);
				System.out.println("u " + Arrays.toString(u.getLast()));
				if (u.getLast()[0] >= u2 || u.getLast()[0] <= u1
						|| u.getLast()[1] >= v2 || u.getLast()[1] <= v1) {
					return u;
				}
				if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns,u.getLast()[0], u.getLast()[1],max)) >= 0) {
					orientation = IntegralCurves.getMaxMinCurv(ns,u.getLast()[0], u.getLast()[1],max);
				} else {
					orientation = Rn.times(null,-1,IntegralCurves.getMaxMinCurv(ns,u.getLast()[0], u.getLast()[1],max));
				}
			}
			if ((tau <= tol * vau / 2.) || (tau >= tol * vau)) {
				h = h * StrictMath.pow(tol * vau / tau, 1 / 2.);
			}
		}
		return u;
	}
	
	public static void main(String[] args){
		NURBSSurface ns = new NURBSSurface();
		double[]tspan = {0,1.527};
		double[]y0 = {0.25,0.5};
		double tol = 0.001;
		IntegralCurves.rungeKutta(ns, tspan, y0, tol, true);

		
	}
}
