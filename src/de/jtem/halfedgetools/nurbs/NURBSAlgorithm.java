package de.jtem.halfedgetools.nurbs;

import de.jreality.math.Rn;

public class NURBSAlgorithm {
	
	private static long binomialCoefficient(int n, int k) {
		if (n - k == 1 || k == 1)
			return n;

		long[][] b = new long[n + 1][n - k + 1];
		b[0][0] = 1;
		for (int i = 1; i < b.length; i++) {
			for (int j = 0; j < b[i].length; j++) {
				if (i == j || j == 0)
					b[i][j] = 1;
				else if (j == 1 || i - j == 1)
					b[i][j] = i;
				else
					b[i][j] = b[i - 1][j - 1] + b[i - 1][j];
			}
		}
		return b[n][n - k];
	}

	/**
	 * Algorithm A2.1 from the NURBS Book
	 * uses that U is a clamped knot vector uses binary search
	 * @param n = U.length - 2
	 * @param p = degree
	 * @param u = parameter
	 * @param U = knot vector
	 * @return 
	 */
	public static int FindSpan(int n, int p, double u, double[] U) {
		if (u == U[n + 1])
			return n;
		int low = p;
		int high = n + 1;
		int mid = (low + high) / 2;
		while (u < U[mid] || u >= U[mid + 1]) {
			if (u < U[mid]) {
				high = mid;
			} else {
				low = mid;
			}
			mid = (low + high) / 2;
		}
		return mid;
	}
	
	public static double[][][] affineCoords(double[][][] P){
		double affine[][][] = new double [P.length][P[0].length][3];
		for (int i = 0; i < P.length; i++) {
			for (int j = 0; j < P[0].length; j++) {
				for (int k = 0; k < 3; k++) {
					affine[i][j][k] = P[i][j][k]/P[i][j][3];
				}
				
			}
		}
		return affine;
	}

	/**
	 * Algorithm A2.2 from the NURBS Book
	 * 
	 * @param i =  knot span computed by FindSpan
	 * @param u = parameter
	 * @param p = degree
	 * @param U = knot vector
	 * @param N = empty array of length p + 1
	 *            p = degree assuming u is in the ith span ==> nonzero basis
	 *            functions are N[i-p][p],...,N[i][p]
	 */
	public static void BasisFuns(int i, double u, int p, double[] U, double[] N) {
		N[0] = 1.0;
		double[] left = new double[p + 1];
		double[] right = new double[p + 1];
		for (int j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i + j] - u;
			double saved = 0.0;
			for (int r = 0; r < j; r++) {
				double temp = N[r] / (right[r + 1] + left[j - r]);
				N[r] = saved + right[r + 1] * temp;
				saved = left[j - r] * temp;
			}
			N[j] = saved;
		}
	}


	/**
	 * Algorithm A2.3 from the NURBS Book
	 * 
	 * @param i = knot span given by FindSpan
	 * @param u = the point in the domain
	 * @param p = the degree
	 * @param n = the highest derivative
	 * @param U = knot vector
	 * @param ders  is an empty array[n+1][p+1]
	 * 
	 */
	
	public static void DersBasisFuns(int i, double u, int p, int n, double[] U,double[][] ders) {
		
		double[][] ndu = new double[p + 1][p + 1];
		double[] left = new double[p + 1];
		double[] right = new double[p + 1];
		ndu[0][0] = 1.0;
		for (int j = 1; j <= p; j++) {
			left[j] = u - U[i + 1 - j];
			right[j] = U[i + j] - u;
			double saved = 0.0;
			for (int r = 0; r < j; r++) {
				ndu[j][r] = right[r + 1] + left[j - r];
				double temp = ndu[r][j - 1] / ndu[j][r];
				ndu[r][j] = saved + right[r + 1] * temp;
				saved = left[j - r] * temp;
			}
			ndu[j][j] = saved;
		}
		for (int j = 0; j <= p; j++) { 
			ders[0][j] = ndu[j][p];
		}
		for (int r = 0; r <= p; r++) {
			int s1 = 0;
			int s2 = 1;
			double[][] a = new double[2][p + 1];
			a[0][0] = 1.0;
			for (int k = 1; k <= n; k++) {
				double d = 0.0;
				int rk = r - k;
				int pk = p - k;
				if (r >= k) {
					a[s2][0] = a[s1][0] / ndu[pk + 1][rk];
					d = a[s2][0] * ndu[rk][pk];
				}
				int j1, j2;
				if (rk >= -1)
					j1 = 1;
				else
					j1 = -rk;
				if (r - 1 <= pk)
					j2 = k - 1;
				else
					j2 = p - r;
				for (int j = j1; j <= j2; j++) {
					a[s2][j] = (a[s1][j] - a[s1][j - 1]) / ndu[pk + 1][rk + j];
					d += a[s2][j] * ndu[rk + j][pk];
				}
				if (r <= pk) {
					a[s2][k] = -a[s1][k - 1] / ndu[pk + 1][r];
					d += a[s2][k] * ndu[r][pk];
				}
				ders[k][r] = d;
				int j = s1;
				s1 = s2;
				s2 = j;
			}
		}
		int r = p;
		for (int k = 1; k <= n; k++) {
			for (int j = 0; j <= p; j++) {
				ders[k][j] *= r;
			}
			r *= (p - k);
		}
	}

	/**
	 * Algorithm A2.5 from the NURBS book.
	 * 
	 * Computes all n derivatives of one basis function N_i,p (derivatives from
	 * the right)
	 * 
	 * 
	 */

	public static void dersOneBasisFuns(int p,int m, double[] U, int i, double u, int n, double[] ders) {

		double[][] N = new double[p+1][p+1];
		double saved = 0.0;
		double Uleft, Uright, temp;
		double[] nD = new double[n+2];
		if (u < U[i] || u >= U[i + p + 1]) {
			for (int k = 0; k <= n; k++) {
				ders[k] = 0.0;
			}
			return;
		}
		// initialize zeroth-degree functions
		for (int j = 0; j <= p; j++) {
			if (u >= U[i + j] && u < U[i + j + 1]) {
				N[j][0] = 1.0;
			} else {
				N[j][0] = 0.0;
				
			}
		}
		// compute full triangular table
		for (int k = 1; k <= p; k++) {
			if (N[0][k - 1] == 0)
				saved = 0.0;
			else
				saved = ((u - U[i]) * N[0][k - 1]) / (U[i + k] - U[i]);
			for (int j = 0; j < p - k + 1; j++) {
				Uleft = U[i + j + 1];
				Uright = U[i + j + k + 1];
				if (N[j + 1][k - 1] == 0.0) {
					N[j][k] = saved;
					saved = 0.0;
				} else {
					temp = N[j + 1][k - 1] / (Uright - Uleft);
					N[j][k] = saved + (Uright - u) * temp;
					saved = (u - Uleft) * temp;
				}
			}
		}
		ders[0] = N[0][p];
		// compute the derivatives
		for (int k = 1; k <= n; k++) {
			for (int j = 0; j <= k; j++) {
				nD[j] = N[j][p - k];
			}
			// l = jj in the book
			for (int l = 0; l <=k; l++) {
				if (nD[0] == 0.0) {
					saved = 0.0;
				} else
					saved = nD[0] / (U[i + p - k + l] - U[i]);
				for (int j = 0; j < k - l + 1; j++) {
					Uleft = U[i + j + 1];
					Uright = U[i + j + p + l + 1];
					if (nD[j + 1] == 0.0) {
						nD[j] = (p - k + l) * saved;
						saved = 0.0;
					} else {
						temp = nD[j + 1] / (Uright - Uleft);
						nD[j] = (p - k + l) * (saved - temp);
						saved = temp;
					}
				}
			}
			ders[k] = nD[0]; /* kth derivative */
		}
	}

	/**
	 * Algorithm A3.2 from the NURBS Book
	 * 
	 * @param n
	 * @param p
	 * @param U
	 * @param P
	 * @param u
	 * @param d 
	 * @param CK
	 */

	public static void CurveDerivatives(int n, int p, double[] U, double[] P, double u, int d, double[] CK) {
		int du = Math.min(d, p);
		for (int k = p + 1; k <= d; k++) {
			CK[k] = 0;
		}
		int span = FindSpan(n, p, u, U);
		double nders[][] = new double[du + 1][p + 1];
		DersBasisFuns(span, u, p, du, U, nders);
		for (int k = 0; k <= du; k++) {
			CK[k] = 0;
			for (int j = 0; j <= p; j++) {
				CK[k] = CK[k] + nders[k][j] * P[span - p + j];
			}
		}
	}
	
	/**
	 * Algorithm A3.6 from the NURBS Book
	 * @param n
	 * @param p = degree
	 * @param U = knotvector
	 * @param m
	 * @param q = degree
	 * @param V = knotvector
	 * @param P = controlmesh
	 * @param u = component of the point in the domain
	 * @param v = component of the point in the domain
	 * @param d is the highest derivative in both directions i.e. k + l <= d
	 * @param SKL array stores the partial derivatives
	 */
	
	public static void SurfaceDerivatives(int n, int p, double[] U,int m, int q, double [] V,double[][][]P,double u, double v, int d, double[][][]SKL){

		int du = Math.min(d, p);

		int dv = Math.min(d, q);

		int uspan = FindSpan(n,p,u,U);
		double [][] Nu = new double[du + 1][p + 1];
		DersBasisFuns(uspan, u, p, du, U, Nu);
		int vspan = FindSpan(m,q,v,V);
		double [][] Nv = new double[dv + 1][q + 1];
		DersBasisFuns(vspan, v, q, dv, V, Nv);
		for(int k=0; k<=du; k++){
			double[] []temp = new double[q + 1][4];
			for(int s=0; s<=q; s++){
				for(int r = 0; r<= p; r++){
					Rn.add(temp[s], temp[s],Rn.times(null, Nu[k][r], P[uspan - p + r][vspan - q + s]));
				}
			}
			int dd = Math.min(d-k,dv);
			for(int l=0; l<=dd; l++){
				for(int s=0; s<=q; s++){
				 Rn.add(SKL[k][l], SKL[k][l], Rn.times(null, Nv[l][s], temp[s]));
				}
			}
		}
	}
	


	/**
	 * Algorithm A4.3 from the NURBS Book
	 * 
	 * @param p
	 * @param U
	 * @param q
	 * @param V
	 * @param Pw
	 * @param u
	 * @param v
	 * @param S
	 */
	public static void SurfacePoint(int p, double[] U, int q, double[] V, double[][][] Pw, double u, double v, double[] S) {
		int n = Pw.length - 1;
		int m = Pw[0].length - 1;
		int uspan = FindSpan(n, p, u, U);
		int vspan = FindSpan(m, q, v, V);
		double[] Nu = new double[p + 1];
		double[] Nv = new double[q + 1];
		BasisFuns(uspan, u, p, U, Nu);
		BasisFuns(vspan, v, q, V, Nv);
		double[][] temp = new double[q + 1][4];
		for (int l = 0; l <= q; l++) {
			for (int k = 0; k <= p; k++) {
				Rn.add(temp[l], temp[l],Rn.times(null, Nu[k], Pw[uspan - p + k][vspan - q + l]));
			}
		}
		for (int l = 0; l <= q; l++) {
			Rn.add(S, S, Rn.times(null, Nv[l], temp[l]));
		}
	}

	/**
	 * Algorithm A4.4 from the NURBS Book
	 * 
	 * @param Aders
	 * @param wders
	 * @param d
	 * @param SKL
	 */
	public static void RatSurfaceDerivs(double[][][] Aders, double[][] wders, int d, double[][][] SKL) {
		int a = wders.length-1;
		int b = wders[0].length-1;
		for (int k = 0; k <= a; k++) {
			for (int l = 0; l <= b; l++) {
				double [] v = Aders[k][l];
				for (int j = 1; j <= l; j++) {
					v = Rn.add(v, v, Rn.times(null, - binomialCoefficient(l, j) * wders[0][j], SKL[k][l - j]));
				}
				for (int i = 1; i <= k; i++) {
					v = Rn.add(v, v, Rn.times(null, - binomialCoefficient(k, i) * wders[i][0], SKL[k - i][l]));
					double []v2 = new double[3];
					for (int j = 1; j <= l; j++) {
						v2 = Rn.add(v2, v2, Rn.times(null, binomialCoefficient(l, j) * wders[i][j], SKL[k - i][l - j]));
					}
					v = Rn.add(v, v, Rn.times(null, - binomialCoefficient(k, i) ,v2));
				}
				SKL[k][l] = Rn.times(null, 1/wders[0][0], v);
				}
		}
	}
}
