/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.functional;

import static de.jtem.halfedge.util.HalfEdgeUtils.constructFaceByVertices;
import static java.lang.Math.sqrt;
import static no.uib.cipr.matrix.Matrix.Norm.Frobenius;
import static no.uib.cipr.matrix.Vector.Norm.TwoRobust;
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
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;


public abstract class FunctionalTest <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> {

	private Double
		eps = 1E-5,
		error = 1E-4;
	private Functional<V, E, F> 
		f = null;
	private HalfEdgeDataStructure<V, E, F>
		hds = null;
	private DomainValue
		xGrad = null,
		xHess = null;
	
	
	public void setFunctional(Functional<V, E, F> f) {
		this.f = f;
	}
	
	public void setHDS(HalfEdgeDataStructure<V, E, F> hds) {
		this.hds = hds;
	}
	
	public void setXGradient(DomainValue gradX) {
		xGrad = gradX;
	}
	
	public void setXHessian(DomainValue hessX) {
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
			System.out.println("No gradient test point set in " + getClass().getSimpleName());
			return;
		}
		int n = f.getDimension(hds);
		DenseVector G = new DenseVector(n);
		MyGradient grad = new MyGradient(G);
		f.evaluate(hds, xGrad, null, grad, null);

		double normDif = 0.0;
		double fdGrad = 0.0;
		for (int i = 0; i < n; i++){
			double xi = xGrad.get(i);
			MyEnergy f1 = new MyEnergy();
			MyEnergy f2 = new MyEnergy();
			
			xGrad.set(i, xi + eps);
			f.evaluate(hds, xGrad, f1, null, null);
			xGrad.set(i, xi - eps);
			f.evaluate(hds, xGrad, f2, null, null);
			
			fdGrad = (f1.get() - f2.get()) / (2 * eps);
			
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
		if (!f.hasHessian()) {
			return;
		}
		if (hds == null) {
			Assert.fail("No HalfEdgedatastructure has been set");
		}
		if (xHess == null) {
			System.out.println("No hessian test point set in " + getClass().getSimpleName());
			return;
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
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createTetrahedron(HDS hds, AdapterSet a) {
		V v1 = hds.addNewVertex();
		V v2 = hds.addNewVertex();
		V v3 = hds.addNewVertex();
		V v4 = hds.addNewVertex();
		
		a.set(Position.class, v1, new double[] {0, 0, 0});
		a.set(Position.class, v2, new double[] {1, 0, 0});
		a.set(Position.class, v3, new double[] {0.5, 0.75, 0});
		a.set(Position.class, v4, new double[] {0.5, 0.5, 0.75});
		
		constructFaceByVertices(hds, v1, v2, v3);
		constructFaceByVertices(hds, v3, v2, v4);
		constructFaceByVertices(hds, v1, v3, v4);
		constructFaceByVertices(hds, v1, v4, v2);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createTriangulatedCube(HDS hds, AdapterSet a) {
		createCube(hds, a);
		Triangulator.triangulate(hds);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createCube(HDS hds, AdapterSet a) {
		HalfEdgeUtils.addCube(hds);
		a.set(Position.class, hds.getVertex(0), new double[] {-0.5, 0.5, 0.5});
		a.set(Position.class, hds.getVertex(1), new double[] {0.5, 0.5, 0.5});
		a.set(Position.class, hds.getVertex(2), new double[] {0.5, 0.5, -0.5});
		a.set(Position.class, hds.getVertex(3), new double[] {-0.5, 0.5, -0.5});
		a.set(Position.class, hds.getVertex(4), new double[] {-0.5, -0.5, 0.5});
		a.set(Position.class, hds.getVertex(5), new double[] {0.5, -0.5, 0.5});
		a.set(Position.class, hds.getVertex(6), new double[] {0.5, -0.5, -0.5});
		a.set(Position.class, hds.getVertex(7), new double[] {-0.5, -0.5, -0.5});
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createOctahedron(HDS hds, AdapterSet a) {
		HalfEdgeUtils.addOctahedron(hds);
		a.set(Position.class, hds.getVertex(0), new double[] {0.0, sqrt(2)/2, 0.0});
		a.set(Position.class, hds.getVertex(1), new double[] {-0.5, 0.0, 0.5});
		a.set(Position.class, hds.getVertex(2), new double[] {0.5, 0.0, 0.5});
		a.set(Position.class, hds.getVertex(3), new double[] {0.5, 0.0, -0.5});
		a.set(Position.class, hds.getVertex(4), new double[] {-0.5, 0.0, -0.5});
		a.set(Position.class, hds.getVertex(5), new double[] {0.0, -sqrt(2)/2, 0.0});
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void createIcosahedron(HDS hds, AdapterSet a) {
		hds.clear();
		hds.addNewVertices(12);
		a.set(Position.class, hds.getVertex(0), new double[] {0.850651026, 0, 0.525731027});
		a.set(Position.class, hds.getVertex(1), new double[] {0.850651026, 0, -0.525731027});
		a.set(Position.class, hds.getVertex(2), new double[] {0.525731027, 0.850651026, 0});
		a.set(Position.class, hds.getVertex(3), new double[] {0.525731027, -0.850651026, 0.0});
		a.set(Position.class, hds.getVertex(4), new double[] {0.0, -0.525731027, 0.850651026});
		a.set(Position.class, hds.getVertex(5), new double[] {0.0, 0.525731027, 0.850651026});
		a.set(Position.class, hds.getVertex(6), new double[] {-0.850651026, 0, -0.525731027});
		a.set(Position.class, hds.getVertex(7), new double[] { -0.850651026, 0, 0.525731027});
		a.set(Position.class, hds.getVertex(8), new double[] {-0.525731027, 0.850651026, 0});
		a.set(Position.class, hds.getVertex(9), new double[] { 0.0, 0.525731027, -0.850651026});
		a.set(Position.class, hds.getVertex(10), new double[] {0.0, -0.525731027, -0.850651026});
		a.set(Position.class, hds.getVertex(11), new double[] {-0.525731027, -0.850651026, 0.0});
		ConvexHull.convexHull(hds, a);
	}
	
}

