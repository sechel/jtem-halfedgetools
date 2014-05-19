package de.jtem.halfedgetools.functional;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

public class FunctionalUtils {

	private static final double
		eps = 1E-5;
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void calculateFDGradient(HDS hds, Functional<V, E, F> app, int dim, DomainValue x, Gradient G) {
		MyEnergy E = new MyEnergy();
		for (int i = 0; i < dim; i++){
			double xi = x.get(i);
			x.set(i, xi + eps);
			app.evaluate(hds, x, E, null, null);
			double f1 = E.get();
			x.set(i, xi - eps);
			app.evaluate(hds, x, E, null, null);
			double f2 = E.get();
			double fdGrad = (f1 - f2) / (2 * eps);
			G.set(i, fdGrad);
			x.set(i, xi);
		}
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void calculateFDHessian(HDS hds, Functional<V, E, F> app, int dim, DomainValue x, Hessian H) {
		MyEnergy E = new MyEnergy();
		app.evaluate(hds, x, E, null, null);
		double y = E.get();
		for (int i = 0; i < dim; i++){
			for (int j = 0; j < dim; j++){
				double fdHessian = 0.0;
				double xi = x.get(i);
				double xj = x.get(j);
				if (i == j) {
					x.set(i, xi + eps);
					app.evaluate(hds, x, E, null, null);
					double iPlus = E.get();
					x.set(i, xi + 2*eps);
					app.evaluate(hds, x, E, null, null);
					double i2Plus = E.get();
					x.set(i, xi - eps);
					app.evaluate(hds, x, E, null, null);
					double iMinus = E.get();
					x.set(i, xi - 2*eps);
					app.evaluate(hds, x, E, null, null);
					double i2Minus = E.get();
					fdHessian = (-i2Plus/eps + 16*iPlus/eps - 30*y/eps + 16*iMinus/eps - i2Minus/eps) / (12 * eps);
				} else {
					x.set(i, xi + eps);
					x.set(j, xj + eps);
					app.evaluate(hds, x, E, null, null);
					double iPlusjPlus = E.get();
					x.set(i, xi + eps);
					x.set(j, xj - eps);
					app.evaluate(hds, x, E, null, null);
					double iPlusjMinus = E.get();
					x.set(i, xi - eps);
					x.set(j, xj + eps);
					app.evaluate(hds, x, E, null, null);
					double iMinusjPlus = E.get();
					x.set(i, xi - eps);
					x.set(j, xj - eps);
					app.evaluate(hds, x, E, null, null);
					double iMinusjMinus = E.get();
					fdHessian = (iPlusjPlus/eps - iPlusjMinus/eps - iMinusjPlus/eps + iMinusjMinus/eps) / (4 * eps);
				}
				x.set(i, xi);
				x.set(j, xj);
				H.set(i, j, fdHessian);
			}
		}
	}
	
	public static 
		<
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>
		> 
	double[] getPosition(V v, DomainValue x, double[] pos) {
		if(pos == null) {
			pos = new double[3];
		}
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
		return pos;
	}
	
	public static double[] getHomogPosition(DomainValue x, Vertex<?,?,?> v) {
		double[] pos = new double[4];
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
		pos[3] = 1.0;
		return pos;
	}

	public  static void addVectorToGradient(Gradient G, int startIndex, double[] d) {
		for (int i = 0; i < d.length; i++) {
			G.add(startIndex+i, d[i]);
		}
	}
	public  static void subtractVectorFromGradient(Gradient G, int startIndex, double[] d) {
		for (int i = 0; i < d.length; i++) {
			G.add(startIndex+i, -d[i]);
		}
	}
	
	public  static void addRowToHessian(Hessian H, int rowIndex, double[] d) {
		for (int i = 0; i < d.length; i++) {
			H.add(rowIndex,i, d[i]);
		}
	}
	public  static void subtractRowFromHessian(Hessian H, int rowIndex, double[] d) {
		for (int i = 0; i < d.length; i++) {
			H.add(rowIndex,i, -d[i]);
		}
	}
	public  static void addColumnToHessian(Hessian H, int colIndex, double[] d) {
		for (int i = 0; i < d.length; i++) {
			H.add(i, colIndex, d[i]);
		}
	}
	public  static void subtractColumnFromHessian(Hessian H, int colIndex, double[] d) {
		for (int i = 0; i < d.length; i++) {
			H.add(i, colIndex, -d[i]);
		}
	}	
	
	public  static void addToDiagonal(Hessian H, double[] d) {
		for (int i = 0; i < d.length; i++) {
			H.add(i, i, d[i]);
		}
	}
	
	public static double angle(double[] a, double[] b) {
		return Math.atan2(Rn.euclideanNorm(Rn.crossProduct(null, a, b)),Rn.innerProduct(a, b));
	}
	
	// Calculate the angle at vj spanned by vi-vj and vk-vj
	public static double angle(double[] vi, double[] vj, double[] vk) {
		return angle(Rn.subtract(null, vi, vj), Rn.subtract(null, vk, vj) );
	}
	
	public static void angleGradient(
		//input
			double[] vi, double[] vj, double[] vk, 
		//output 
			double[] di, double[] dj, double[] dk) {
		
		double[] 
		       a = new double[3], 
		       b = new double[3];

		Rn.subtract(a, vi, vj);
		Rn.subtract(b, vk, vj);

		angleGradient(a,b,di);
		angleGradient(b,a,dk);
		Rn.times(dj, -1.0, Rn.add(null, di, dk) );
	}
	
	public static void angleGradient(
		//input
			double[] a, double[] b,
		//output
			double[] ta)
	{
		double al = Rn.euclideanNorm(a);
		Rn.projectOntoComplement(ta, b, a);
		Rn.normalize(ta, ta);
		Rn.times(ta, - 1.0 / al, ta);
	}
	
	//M = v*w^t
	public static void outerProduct(double[] v, double[] w, double[][] M) 
	{
		for (int i = 0; i < M.length; i++) {
			for (int j = 0; j < M[0].length; j++) {
				M[i][j] = v[i] * w[j];
			}
		}
	}
	
	
	// M = v * v^t;
	public static void outerProduct(double[] v, double[][] M) {
		outerProduct(v,v,M);
	}
	

	public static double[] getVectorFromGradient(Gradient g, int i) {
		return new double[]{g.get(i),g.get(i+1),g.get(i+2)};
	}

	public static void setVectorToGradient(Gradient g, int startIndex, double[] vg) {
		for (int i = 0; i < vg.length; i++) {
			g.set(startIndex+i, vg[i]);
		}		
	}

	public static void setVectorToDomainValue(DomainValue x, int startIndex, double[] vg) {
		for (int i = 0; i < vg.length; i++) {
			x.set(startIndex+i, vg[i]);
		}
		
	}

}
