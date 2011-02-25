package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;  
import java.util.LinkedList;

import de.jreality.math.Rn;

public class IntegralCurves {

	public static double[] getMaxMinCurv(NURBSSurface ns, double u, double v,
			boolean max) {
		if (max) {
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[1];
		} else {	
			return NURBSCurvatureUtility.curvatureAndDirections(ns, u, v).getCurvatureDirectionsDomain()[0];
		}
	}

	public static IntObjects rungeKutta(NURBSSurface ns, double[] y0,double tol, boolean secondOrientation, boolean max, double eps) {
		double[][] A = { { 0, 0, 0, 0 }, { 0.5, 0, 0, 0 }, { 0, 0.75, 0, 0 },{ 2 / 9., 1 / 3., 4 / 9., 0 } };
		double[] c1 = { 2 / 9., 1 / 3., 4 / 9., 0 };
		double[] c2 = { 7 / 24., 0.25, 1 / 3., 1 / 8. };
		double[] b = { 0, 0.5, 0.75, 1 };
		LinkedList<double[]> u = new LinkedList<double[]>();
		int dim = y0.length;
		double h = 1 / 1000.;
		double tau;
		double vau;
		u.add(y0);
		double[] Udomain = { ns.U[0], ns.U[ns.U.length - 1] };
		double[] Vdomain = { ns.V[0], ns.V[ns.V.length - 1] };
		double u1 = Udomain[0];
		double u2 = Udomain[1];
		double v1 = Vdomain[0];
		double v2 = Vdomain[1];
		double[] orientation = new double[2];
		if (!secondOrientation) {
			orientation = IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max);
		} else {
			orientation = Rn.times(null, -1,
					IntegralCurves.getMaxMinCurv(ns, y0[0], y0[1], max));
		}
		boolean nearBy = false;
		boolean first = true;
		double dist;
		double[] ori = orientation;

		while (!nearBy ) {
			double[] v = new double[dim];
			double[] sumA = new double[dim];
			for (int i = 0; i < dim; i++) {
				v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
			}
			double[][] k = new double[b.length][2];

			if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)) > 0) {
				k[0] = Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max));
			} else {
				k[0] = Rn.times(null, -1, Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0], v[1], max)));
			}
			for (int l = 1; l < b.length; l++) {
				sumA = Rn.times(null, A[l][0], k[0]);
				for (int m = 1; m < l - 1; m++) {
					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
				}
				if ((v[0] + h * sumA[0]) >= u2 || (v[0] + h * sumA[0]) <= u1|| (v[1] + h * sumA[1]) >= v2|| (v[1] + h * sumA[1]) <= v1) {
					System.out.println("out of domain");
					IntObjects intObj = new IntObjects(u, ori, nearBy, max);
					return intObj;
				}
				if (Rn.innerProduct(orientation,Rn.normalize(null,IntegralCurves.getMaxMinCurv(ns, v[0] + h* sumA[0], v[1] + h * sumA[1], max))) > 0) {
					k[l] = Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns,v[0] + h * sumA[0], v[1] + h * sumA[1], max));
				} else {
					k[l] = Rn.times(null, -1, Rn.normalize(null, IntegralCurves.getMaxMinCurv(ns, v[0] + h * sumA[0], v[1] + h* sumA[1], max)));
				}
			}
			double[] Phi1 = new double[dim];
			double[] Phi2 = new double[dim];
			for (int l = 0; l < b.length; l++) {
				Phi1 = Rn.add(null, Phi1, Rn.times(null, c1[l], k[l]));
				Phi2 = Rn.add(null, Phi2, Rn.times(null, c2[l], k[l]));
			}
				v = Rn.add(null, v, Rn.times(null, h, Phi2));
				tau = Rn.euclideanNorm(Rn.add(null, Phi2,Rn.times(null, -1, Phi1)));
				vau = Rn.euclideanNorm(u.getLast()) + 1;
				if (tau <= tol * vau) {
					u.add(Rn.add(null, u.getLast(), Rn.times(null, h, Phi1)));
//					System.out.println(Arrays.toString(u.getLast()));
					if (u.getLast()[0] >= u2 || u.getLast()[0] <= u1
							|| u.getLast()[1] >= v2 || u.getLast()[1] <= v1) {
						u.pollLast();
						System.out.println("out of domain");
						IntObjects intObj = new IntObjects(u, ori, nearBy, max);
						return intObj;
					}
					if (Rn.innerProduct(orientation,IntegralCurves.getMaxMinCurv(ns, u.getLast()[0],u.getLast()[1], max)) > 0) {
						orientation = IntegralCurves.getMaxMinCurv(ns,u.getLast()[0], u.getLast()[1], max);
					} else {
						orientation = Rn.times(null, -1, IntegralCurves.getMaxMinCurv(ns, u.getLast()[0],u.getLast()[1], max));
					}
				}
				if ((tau > tol * vau)) {
					h = h * StrictMath.pow(tol * vau / tau, 1 / 2.);
					System.out.println(Math.pow(tol * vau / tau, 1 / 2.));
				}
				dist = Rn.euclideanDistance(u.getLast(), y0);
				if (!(dist < eps) && first) {
					first = false;
				}
				if (dist < eps && !first) {
					nearBy = true;
				}
			}
		
		IntObjects intObj = new IntObjects(u, ori, nearBy, max);
		return intObj;
	}
	
	public static LinkedList<double[]> umbilicPoints(NURBSSurface ns, int n){
		LinkedList<double[]> umbilics = new LinkedList<double[]>();
		double[] Udomain = { ns.U[0], ns.U[ns.U.length - 1] };
		double[] Vdomain = { ns.V[0], ns.V[ns.V.length - 1] };
		double u1 = Udomain[0];
		double u2 = Udomain[1];
		double v1 = Vdomain[0];
		double v2 = Vdomain[1];
		double hu = (u2 - u1)/(n + 1);
		double hv = (v2 - v1)/(n + 1);
		double[][] umb = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				double[][] shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv * (j + 1)).getWeingartenOperator();
				umb[i][j] =Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
				//umb[i][j] = Math.abs(NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv * (j + 1)).getMaxCurvature() - NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv * (j + 1)).getMinCurvature());
				
			}
		}
		for (int i = 1; i < n-1; i++) {
			for (int j = 1; j < n-1; j++) {
				if(umb[i][j] < umb[i-1][j-1] && umb[i][j] < umb[i-1][j] && umb[i][j] < umb[i-1][j+1] && umb[i][j] < umb[i][j-1] &&
				   umb[i][j] < umb[i][j+1] && umb[i][j] < umb[i+1][j-1] && umb[i][j] < umb[i+1][j] && umb[i][j] < umb[i+1][j+1]){
					double[] uPoint = {u1 + hu * (i+1),v1 + hv * (j+1),umb[i][j]};
					if(Math.abs(NURBSCurvatureUtility.curvatureAndDirections(ns, uPoint[0], uPoint[1]).GaussCurvature) > 1E-6 && umb[i][j] < 1E-6){
						umbilics.add(uPoint);
						System.out.println("umbilic: "+umb[i][j]);
					}
				}
			}
		}
		return umbilics;
	}
	
	private static Refinement refineUmbilics(Refinement umb){
		double [][][]points = umb.point;
		double hu = umb.hu;
		double hv = umb.hv;
		int deepnes = umb.depth;
		double eps = umb.eps;
		int i =umb.indexI;
		int j = umb.indexJ;
		double[][][] newPoints = new double[2][2][];
		
		return null;
	}
	
}
