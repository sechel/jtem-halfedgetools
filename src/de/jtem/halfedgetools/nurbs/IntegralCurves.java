package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;  
import java.util.LinkedList;

import de.jreality.math.Rn;

public class IntegralCurves {

	public static double[] getMaxMinCurv(NURBSSurface ns, double u, double v,boolean max) {
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
				if ((tau >= tol * vau)) {
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
	
	public static double[] parallelTransport(double[] start){
		return null;
	}
	
	public static LinkedList<double[]> geodesicExponential(NURBSSurface ns, double[] y0, double eps, double tol){
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
		boolean nearBy = false;

		while (!nearBy ) {
			double[] v = new double[dim];
			double[] sumA = new double[dim];
			for (int i = 0; i < dim; i++) {
				v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
			}
//			System.out.println(v[0] + " , " + v[1] + " , "+v[2] + " , " + v[3]);
			double[][] k = new double[b.length][4];
			ChristoffelInfo c0 = NURBSChristoffelUtility.christoffel(ns, v[0], v[1]);
			k[0][0] = v[2];
			k[0][1] = v[3];
			k[0][2] = -(c0.G111 * v[2] * v[2] + 2 * c0.G121 * v[2] * v[3] + c0.G221 * v[3] * v[3]);
			k[0][3] = -(c0.G112 * v[2] * v[2] + 2 * c0.G122 * v[2] * v[3] + c0.G222 * v[3] * v[3]);
			for (int l = 1; l < b.length; l++) {
				sumA = Rn.times(null, A[l][0], k[0]);
				for (int m = 1; m < l - 1; m++) {
					sumA = Rn.add(null, sumA, Rn.times(null, A[l][m], k[m]));
				}
				if ((v[0] + h * sumA[0]) >= u2 || (v[0] + h * sumA[0]) <= u1 || (v[1] + h * sumA[1]) >= v2 || (v[1] + h * sumA[1]) <= v1) {
					System.out.println("1. out of domain");
					return u;
				}
				ChristoffelInfo cl = NURBSChristoffelUtility.christoffel(ns, v[0] + h * sumA[0], v[1] + h * sumA[1]);
				k[l][0] = v[2] + h * sumA[2];
				k[l][1] = v[3] + h * sumA[3];
				k[l][2] = -(cl.G111 * (v[2] + h * sumA[2]) * (v[2] + h * sumA[2]) + 2 * cl.G121 * (v[2] + h * sumA[2]) * (v[3] + h * sumA[3]) + cl.G221 * (v[3] + h * sumA[3]) * (v[3] + h * sumA[3]));
				k[l][3] = -(cl.G112 * (v[2] + h * sumA[2]) * (v[2] + h * sumA[2]) + 2 * cl.G122 * (v[2] + h * sumA[2]) * (v[3] + h * sumA[3]) + cl.G222 * (v[3] + h * sumA[3]) * (v[3] + h * sumA[3]));
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
						System.out.println("2. out of domain");
						return u;
					}
				}
				if ((tau >= tol * vau)) {
					h = h * StrictMath.pow(tol * vau / tau, 1 / 2.);
//					System.out.println(Math.pow(tol * vau / tau, 1 / 2.));
				}
			}
		return u;
	}
	
	public static LinkedList<double[]> geodesicExponentialTest(NURBSSurface ns, double[] y0, double eps, double tol){
		
		LinkedList<double[]> u = new LinkedList<double[]>();
		int dim = y0.length;
		double h = 1 / 20.;
		u.add(y0);
		double[] Udomain = { ns.U[0], ns.U[ns.U.length - 1] };
		double[] Vdomain = { ns.V[0], ns.V[ns.V.length - 1] };
		double u1 = Udomain[0];
		double u2 = Udomain[1];
		double v1 = Vdomain[0];
		double v2 = Vdomain[1];
		boolean nearBy = false;

		while (!nearBy ) {
			double[] v = new double[dim];
			double[] vNext = new double[dim];
			
			for (int i = 0; i < dim; i++) {
				v[i] = u.getLast()[i]; // initialisiere das AWP v = y0
			}
			ChristoffelInfo c = NURBSChristoffelUtility.christoffel(ns, v[0], v[1]);
			vNext[0] = v[0] + h * v[2];
			vNext[1] = v[1] + h * v[3];
			vNext[2] = v[2] - h * (c.G111 * v[2] * v[2] + 2 * c.G121 * v[2] * v[3] + c.G221 * v[3] * v[3]);
			vNext[3] = v[3] - h * (c.G112 * v[2] * v[2] + 2 * c.G122 * v[2] * v[3] + c.G222 * v[3] * v[3]);
			u.add(vNext);
			if (u.getLast()[0] >= u2 || u.getLast()[0] <= u1
					|| u.getLast()[1] >= v2 || u.getLast()[1] <= v1) {
				u.pollLast();
				System.out.println("2. out of domain");
				return u;
			}
			System.out.println(v[0] + " , " + v[1] + " , "+v[2] + " , " + v[3]);
			
			}
		return u;
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
			}
		}
		for (int i = 1; i < n-1; i++) {
			for (int j = 1; j < n-1; j++) {
				if(umb[i][j] < umb[i-1][j-1] && umb[i][j] < umb[i-1][j] && umb[i][j] < umb[i-1][j+1] && umb[i][j] < umb[i][j-1] &&
				   umb[i][j] < umb[i][j+1] && umb[i][j] < umb[i+1][j-1] && umb[i][j] < umb[i+1][j] && umb[i][j] < umb[i+1][j+1]){
					double[] uPoint = {u1 + hu * (i+1),v1 + hv * (j+1),umb[i][j]};
					if(Math.abs(NURBSCurvatureUtility.curvatureAndDirections(ns, uPoint[0], uPoint[1]).GaussCurvature) > 1E-6 && umb[i][j] < 1E-6){
						umbilics.add(uPoint);
						System.out.println("umbilic: "+Arrays.toString(uPoint));
					}
				}
			}
		}
		return umbilics;
	}
	public static LinkedList<double[]> umbilicPoints1(NURBSSurface ns, int n){
		int depth = 4;
		double eps = 1E-7;
		
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
		double[][] shapeOp = new double[2][2];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				shapeOp = NURBSCurvatureUtility.curvatureAndDirections( ns ,u1 + hu * (i + 1) , v1 + hv* (j + 1)).getWeingartenOperator();
				umb[i][j] =Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
			}
		}
		for (int i = 1; i < n-1; i++) {
			for (int j = 1; j < n-1; j++) {
				if(umb[i][j] < umb[i-1][j-1] && umb[i][j] < umb[i-1][j] && umb[i][j] < umb[i-1][j+1] && umb[i][j] < umb[i][j-1] &&
				   umb[i][j] < umb[i][j+1] && umb[i][j] < umb[i+1][j-1] && umb[i][j] < umb[i+1][j] && umb[i][j] < umb[i+1][j+1]){
					double[] uPoint = {u1 + hu * (i+1),v1 + hv * (j+1),umb[i][j]};
					//umbilics.add(uPoint);
					System.out.println("old coords "+(u1 + hu * (i +1))+" "+(v1 + hv * (j+1))+" "+umb[i][j]);
					System.out.println();
					double [][]rfPoints = new double[3][3];
					for (int k = 0; k < rfPoints.length; k++) {
						for (int l = 0; l < rfPoints.length; l++) {
							rfPoints[k][l] = umb[i-1+k][j-1+l];
							System.out.println("old coords "+(u1 + hu * (i+k))+" "+(v1 + hv * (j+l))+" "+rfPoints[k][l]);
						}
					}
					Refinement rf = new Refinement(umbilics, ns, u1, v1, rfPoints, hu, hv, depth, i, j, 1);
//					System.out.println("die liste "+IntegralCurves.refineUmbilics(rf).umbilcs);
					umbilics.addAll(IntegralCurves.refineUmbilics(rf).umbilcs);
				}
			}
		}
		LinkedList<double[]> finalUmbilics = new LinkedList<double[]>();
		for (int i = 0; i < umbilics.size(); i++) {
			if(Math.abs(NURBSCurvatureUtility.curvatureAndDirections(ns, umbilics.get(i)[0],umbilics.get(i)[1]).GaussCurvature) > 1E-6 && umbilics.get(i)[2]<eps){
				finalUmbilics.add(umbilics.get(i));
//				System.out.println(+umbilics.get(i)[2]);
			}
		}
		System.out.println("listenlaenge "+finalUmbilics.size());
		for (int i = 0; i < finalUmbilics.size(); i++) {
			System.out.println("umb "+Arrays.toString(finalUmbilics.get(i)));
			
		}
		return finalUmbilics;
	}
	
	private static Refinement refineUmbilics(Refinement umb){
		
		LinkedList<double[]>umbilics = umb.umbilcs;
		NURBSSurface ns = umb.ns;
		double [][]points = umb.point;
		double hu = umb.hu;
		double hv = umb.hv;
		int depth = umb.depth;
		int counter = umb.counter;
		int i =umb.indexI;
		int j = umb.indexJ;
		double u1 = umb.u1;
		double v1 = umb.v1;
		LinkedList<double[]> u = new LinkedList<double[]>();
//		System.out.println("Tiefe: "+ counter);
//		System.out.println("input points");
//		for (int k = 0; k < points.length; k++) {
//			System.out.println(points[k][0]+" "+points[k][1]+" "+points[k][2]);
//		}
	
		if(counter == depth) {
			double [][]shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv* (j + 1)).getWeingartenOperator();
			double min = Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
			double[] possibleUmbilic = {u1 + hu * (i + 1) , v1 + hv* (j + 1),min};
			umbilics.add(possibleUmbilic);
			return umb;
//		}
		}
		//1. step: create the refinement points
		else{
			counter++;
//			System.out.println("new refinement");
			double[][] newPoints = new double[5][5];
			for (int k = 0; k < 5; k++) {
				for (int l = 0; l < 5; l++) {
//					if(k%2 == 0 && l%2 == 0){
//						newPoints[k][l] = points[k/2][l/2]; //pick up all old points
//					}else{
//						//compute the new points
						double [][]shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * i + k * hu/2 , v1 + hv * j + l * hv/2).getWeingartenOperator();
						newPoints[k][l] = Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
//					}
//					System.out.println("k "+k+" l "+l+" :"+newPoints[k][l]);
				}
			}
//			System.out.println();
			//2. step: search in all 9 squares 
			int c = 0;
			for (int k = 0; k < 3; k++) {
				for (int l = 0; l < 3; l++) {
				
					if(newPoints[k+1][l+1] < newPoints[k][l] && newPoints[k+1][l+1] < newPoints[k][l+1] && newPoints[k+1][l+1] < newPoints[k][l+2]&&
							newPoints[k+1][l+1] < newPoints[k+1][l] && newPoints[k+1][l+1] < newPoints[k+1][l+2]&& 
							newPoints[k+1][l+1] < newPoints[k+2][l] &&newPoints[k+1][l+1] < newPoints[k+2][l+1] && newPoints[k+1][l+1] < newPoints[k+2][l+2]){
						
						//create the points for the new recursion
						double[][]rfPoints = new double[3][3];
						for (int m = 0; m < 3; m++) {
							for (int n = 0; n < 3; n++) {
								rfPoints[m][n] = newPoints[k+m][l+n];
								//System.out.println("rf "+rfPoints[m][n]);
							}
						}
						//give the umbilic point coords(in our domain) and the value H^2 - K
						double[] possibleUmbilic = {u1 + hu * i + (k+1) * hu/2 , v1 + hv * j + (l+1) * hv/2,rfPoints[1][1]};
						System.out.println("k: "+(k+1)+" l "+(l+1));
						System.out.println(counter);
						System.out.println("possible "+ Arrays.toString(possibleUmbilic));
						System.out.println(rfPoints[0][0]+" "+rfPoints[0][1]+" "+rfPoints[0][2]);
						System.out.println(rfPoints[1][0]+" "+rfPoints[1][1]+" "+rfPoints[1][2]);
						System.out.println(rfPoints[2][0]+" "+rfPoints[2][1]+" "+rfPoints[2][2]);
						System.out.println();
						Refinement rf = new Refinement(umbilics, ns, u1, v1, rfPoints, hu/2, hv/2, depth, 2*i+k, 2*j+l, counter);
						IntegralCurves.refineUmbilics(rf);	
					}else{
						c++;
						if(c == 9){
							double [][]shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv* (j + 1)).getWeingartenOperator();
							double min = Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
							double[] possibleUmbilic = {u1 + hu * (i + 1) , v1 + hv* (j + 1),min};
							umbilics.add(possibleUmbilic);
							return umb;
						}
					}
				}
			}
			
			return umb;
		}
		
		
	}
	
private static Refinement refineUmbilics1(Refinement umb){
		
		LinkedList<double[]>umbilics = umb.umbilcs;
		NURBSSurface ns = umb.ns;
		double [][]points = umb.point;
		double hu = umb.hu;
		double hv = umb.hv;
		int depth = umb.depth;
		int counter = umb.counter;
		int i =umb.indexI;
		int j = umb.indexJ;
		double u1 = umb.u1;
		double v1 = umb.v1;
		LinkedList<double[]> u = new LinkedList<double[]>();
//		System.out.println("Tiefe: "+ counter);
//		System.out.println("input points");
//		for (int k = 0; k < points.length; k++) {
//			System.out.println(points[k][0]+" "+points[k][1]+" "+points[k][2]);
//		}
	
		if(counter == depth) {
			double [][]shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv* (j + 1)).getWeingartenOperator();
			double min = Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
			double[] possibleUmbilic = {u1 + hu * (i + 1) , v1 + hv* (j + 1),min};
			umbilics.add(possibleUmbilic);
			return umb;
//		}
		}
		//1. step: create the refinement points
		else{
			counter++;
//			System.out.println("new refinement");
			double[][] newPoints = new double[7][7];
			for (int k = 0; k < 7; k++) {
				for (int l = 0; l < 7; l++) {
//					if(k%2 == 0 && l%2 == 0){
//						newPoints[k][l] = points[k/2][l/2]; //pick up all old points
//					}else{
//						//compute the new points
						double [][]shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * i + (k-1) * hu/2 , v1 + hv * j + (l-1) * hv/2).getWeingartenOperator();
						newPoints[k][l] = Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
//					}
//					System.out.println("k "+k+" l "+l+" :"+newPoints[k][l]);
				}
			}
//			System.out.println();
			//2. step: search in all 9 squares 
			int c = 0;
			for (int k = 0; k < 5; k++) {
				for (int l = 0; l < 5; l++) {
				
					if(newPoints[k+1][l+1] < newPoints[k][l] && newPoints[k+1][l+1] < newPoints[k][l+1] && newPoints[k+1][l+1] < newPoints[k][l+2]&&
							newPoints[k+1][l+1] < newPoints[k+1][l] && newPoints[k+1][l+1] < newPoints[k+1][l+2]&& 
							newPoints[k+1][l+1] < newPoints[k+2][l] &&newPoints[k+1][l+1] < newPoints[k+2][l+1] && newPoints[k+1][l+1] < newPoints[k+2][l+2]){
						
						//create the points for the new recursion
						double[][]rfPoints = new double[3][3];
						for (int m = 0; m < 3; m++) {
							for (int n = 0; n < 3; n++) {
								rfPoints[m][n] = newPoints[k+m][l+n];
								//System.out.println("rf "+rfPoints[m][n]);
							}
						}
						//give the umbilic point coords(in our domain) and the value H^2 - K
						double[] possibleUmbilic = {u1 + hu * i + (k+1) * hu/2 , v1 + hv * j + (l+1) * hv/2,rfPoints[1][1]};
						System.out.println("k: "+(k+1)+" l "+(l+1));
						System.out.println(counter);
						System.out.println("possible "+ Arrays.toString(possibleUmbilic));
						System.out.println(rfPoints[0][0]+" "+rfPoints[0][1]+" "+rfPoints[0][2]);
						System.out.println(rfPoints[1][0]+" "+rfPoints[1][1]+" "+rfPoints[1][2]);
						System.out.println(rfPoints[2][0]+" "+rfPoints[2][1]+" "+rfPoints[2][2]);
						System.out.println();
						Refinement rf = new Refinement(umbilics, ns, u1, v1, rfPoints, hu/2, hv/2, depth, 2*i+k, 2*j+l, counter);
						IntegralCurves.refineUmbilics(rf);	
					}else{
						c++;
						if(c == 25){
							double [][]shapeOp = NURBSCurvatureUtility.curvatureAndDirections(ns ,u1 + hu * (i + 1) , v1 + hv* (j + 1)).getWeingartenOperator();
							double min = Math.abs((shapeOp[0][0] * shapeOp[0][0] + 2 * shapeOp[0][0]*shapeOp[1][1] + shapeOp[1][1] * shapeOp[1][1])/4 - shapeOp[0][0]*shapeOp[1][1] + shapeOp[0][1] * shapeOp[1][0]);
							double[] possibleUmbilic = {u1 + hu * (i + 1) , v1 + hv* (j + 1),min};
							umbilics.add(possibleUmbilic);
							return umb;
						}
					}
				}
			}
			
			return umb;
		}
		
		
	}


	
}
