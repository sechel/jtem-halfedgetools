package de.jtem.halfedge.functional;

import static de.jtem.halfedge.util.HalfEdgeUtils.constructFaceByVertices;
import static java.lang.Math.sqrt;
import static no.uib.cipr.matrix.Matrix.Norm.Frobenius;
import static no.uib.cipr.matrix.Vector.Norm.TwoRobust;

import javax.vecmath.Point3d;

import junit.framework.Assert;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;

import org.junit.Before;
import org.junit.Test;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Functional;


public abstract class FunctionalTest <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	X extends DomainValue
> {

	private Double
		eps = 1E-5,
		error = 1E-4;
	private Functional<V, E, F, X> 
		f = null;
	private HalfEdgeDataStructure<V, E, F>
		hds = null;
	private X
		xGrad = null,
		xHess = null;
	
	
	public void setFuctional(Functional<V, E, F, X> f) {
		this.f = f;
	}
	
	public void setHDS(HalfEdgeDataStructure<V, E, F> hds) {
		this.hds = hds;
	}
	
	public void setXGradient(X gradX) {
		xGrad = gradX;
	}
	
	public void setXHessian(X hessX) {
		xHess = hessX;
	}
	
	public void setEps(Double eps) {
		this.eps = eps;
	}
	
	public void setError(Double error) {
		this.error = error;
	}
	
	@Before
	public abstract void init();
	

	/**
	 * http://www.otago.ac.nz/sas/ormp/chap5/sect28.htm
	 * @throws Exception
	 */
	@Test
	public void testGradient() throws Exception {
		if (f == null) {
			Assert.fail("Functional has not been set");
		}
		if (hds == null) {
			Assert.fail("No HalfEdgedatastructure has been set");
		}
		if (xGrad == null) {
			Assert.fail("No gradient domain value has been set");
		}
		int n = f.getDimension(hds);
		DenseVector G = new DenseVector(n);
		MyGradient grad = new MyGradient(G);
		f.evaluate(hds, xGrad, null, grad, null);

		double normDif = 0.0;
		for (int i = 0; i < n; i++){
			double xi = xGrad.get(i);
			MyEnergy f1 = new MyEnergy();
			MyEnergy f2 = new MyEnergy();
			
			xGrad.set(i, xi + eps);
			f.evaluate(hds, xGrad, f1, null, null);
			xGrad.set(i, xi - eps);
			f.evaluate(hds, xGrad, f2, null, null);
			
			double fdGrad = (f1.get() - f2.get()) / (2 * eps);
			
			normDif += (fdGrad - G.get(i)) * (fdGrad - G.get(i));
			xGrad.set(i, xi);
		}
		normDif = sqrt(normDif);
		
		double normHC = G.norm(TwoRobust);
		double ratio = normDif / normHC;
		Assert.assertEquals(0.0, ratio, error);
	}
	
	
	/**
	 * http://www.otago.ac.nz/sas/ormp/chap5/sect28.htm
	 * @throws Exception
	 */
	@Test
	public void testHessian() throws Exception {
		if (f == null) {
			Assert.fail("Functional has not been set");
		}
		if (hds == null) {
			Assert.fail("No HalfEdgedatastructure has been set");
		}
		if (xHess == null) {
			Assert.fail("No hessian domain value has been set");
		}
		int n = f.getDimension(hds);
		int[][] nz = f.getNonZeroPattern(hds);
		Matrix H = new CompRowMatrix(n, n, nz);
		MyHessian hessian = new MyHessian(H);
		MyEnergy y = new MyEnergy();
		f.evaluate(hds, xHess, y, null, hessian);
		
		double normDif = 0.0;
		for (int i = 0; i < n; i++){
			for (int j = 0; j < n; j++){
				double fdHessian = 0.0;
				double xi = xHess.get(i);
				double xj = xHess.get(j);
				if (i == j) {
					MyEnergy iPlus = new MyEnergy(); 
					MyEnergy iMinus = new MyEnergy();
					MyEnergy i2Plus = new MyEnergy(); 
					MyEnergy i2Minus = new MyEnergy();			
					
					xHess.set(i, xi + eps);
					f.evaluate(hds, xHess, iPlus, null, null);
					xHess.set(i, xi + 2*eps);
					f.evaluate(hds, xHess, i2Plus, null, null);	
					xHess.set(i, xi - eps);
					f.evaluate(hds, xHess, iMinus, null, null);
					xHess.set(i, xi - 2*eps);
					f.evaluate(hds, xHess, i2Minus, null, null);
					
					fdHessian = (-i2Plus.E/eps + 16*iPlus.E/eps - 30*y.E/eps + 16*iMinus.E/eps - i2Minus.E/eps) / (12 * eps);
				} else {
					MyEnergy iPlusjPlus = new MyEnergy();
					MyEnergy iPlusjMinus = new MyEnergy();
					MyEnergy iMinusjPlus = new MyEnergy();
					MyEnergy iMinusjMinus = new MyEnergy();
					
					xHess.set(i, xi + eps);
					xHess.set(j, xj + eps);
					f.evaluate(hds, xHess, iPlusjPlus, null, null);
					xHess.set(i, xi + eps);
					xHess.set(j, xj - eps);
					f.evaluate(hds, xHess, iPlusjMinus, null, null);
					xHess.set(i, xi - eps);
					xHess.set(j, xj + eps);
					f.evaluate(hds, xHess, iMinusjPlus, null, null);
					xHess.set(i, xi - eps);
					xHess.set(j, xj - eps);
					f.evaluate(hds, xHess, iMinusjMinus, null, null);

					fdHessian = (iPlusjPlus.E/eps - iPlusjMinus.E/eps - iMinusjPlus.E/eps + iMinusjMinus.E/eps) / (4 * eps);
				}
				normDif += (fdHessian - H.get(i,j)) * (fdHessian - H.get(i,j));
				xHess.set(i, xi);
				xHess.set(j, xj);
			}
		}
		normDif = sqrt(normDif);
		
		double normHC = H.norm(Frobenius);
		double ratio = normDif / normHC;
		Assert.assertEquals(0.0, ratio, error);
	}
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasPosition,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createTetrahedron(HDS hds) {
		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
		V v4 = hds.addNewVertex();
		
		v1.setPosition(new Point3d(0, 0, 0));
		v2.setPosition(new Point3d(1, 0, 0));
		v3.setPosition(new Point3d(0.5, 0.75, 0));
		v4.setPosition(new Point3d(0.5, 0.5, 0.75));
		
		constructFaceByVertices(hds, v1, v2, v3);
		constructFaceByVertices(hds, v3, v2, v4);
		constructFaceByVertices(hds, v1, v3, v4);
		constructFaceByVertices(hds, v1, v4, v2);
	}
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasPosition,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCube(HDS hds) {
		HalfEdgeUtils.addCube(hds);
		Triangulator<V, E, F> trian = new Triangulator<V, E, F>();
		trian.triangulate(hds);
		hds.getVertex(0).setPosition(new Point3d(-0.5, -0.5, -0.5));
		hds.getVertex(1).setPosition(new Point3d(0.5, -0.5, -0.5));
		hds.getVertex(2).setPosition(new Point3d(-0.5, 0.5, -0.5));
		hds.getVertex(3).setPosition(new Point3d(0.5, 0.5, -0.5));
		hds.getVertex(4).setPosition(new Point3d(-0.5, -0.5, 0.5));
		hds.getVertex(5).setPosition(new Point3d(0.5, -0.5, 0.5));
		hds.getVertex(6).setPosition(new Point3d(-0.5, 0.5, 0.5));
		hds.getVertex(7).setPosition(new Point3d(0.5, 0.5, 0.5));
	}
	
	
	
	
}

