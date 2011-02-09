package de.jtem.halfedgetools.util;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.EVD;
import no.uib.cipr.matrix.NotConvergedException;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.bsp.KdUtility;

public class CurvatureUtility {

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getPrincipleCurvatures(
		double[] p,
		double radius,
		KdTree<V, E, F> kd,
		AdapterSet a
	) {
		EVD evd = getCurvatureTensor(p, radius, kd, a);
		double[] eigVal= evd.getRealEigenvalues();
		LinkedList<Double> minMax = new LinkedList<Double>();
		minMax.add(eigVal[0]);
		minMax.add(eigVal[1]);
		minMax.add(eigVal[2]);
		int index = getIndexOfMinMagnitude(eigVal);
		minMax.remove(index);
		if(minMax.get(1)<minMax.get(0)) {
			minMax.addFirst(minMax.removeLast());
		}
		eigVal[0]=minMax.get(0);
		eigVal[1]=minMax.get(1);
		return eigVal;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> EVD getCurvatureTensor(
		double[] p,
		double scale,
		KdTree<V, E, F> kd,
		AdapterSet a
	) {
		Collection<F> faces = KdUtility.collectFacesInRadius(kd, p, scale);
		Collection<E> edges = KdUtility.collectEdgesInRadius(kd, p, scale);
		double area=0;
		for(F f :faces){
			area += area(f, a);
		}
		if (area == 0 || edges.size() == 0) {
			try {
				EVD evd = EVD.factorize(new DenseMatrix(new double[][] {{1,0,0},{0,1,0},{0,0,1}}));
				return evd;
			} catch (NotConvergedException e1) {
				e1.printStackTrace();
			}
		}
		DenseMatrix matrix = new DenseMatrix(3,3);
		double beta = 0;
		double edgeLength = 0;
		
		for(E e : edges){
			beta = getAngle(e, a);
			edgeLength = getLength(e, a);
			
			double[] edge = getVector(e, a);
			Rn.normalize(edge, edge);
			double[][] result = new double[3][3];
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					result[i][j] = edge[i] * edge[j];
				}
			}
			double[][] T = getEdgeCurvatureTensor(e, a);
			DenseMatrix tmp = new DenseMatrix(T);
			matrix.add(beta * edgeLength, tmp);
		}
		matrix.scale(1/area);
		EVD evd = null;
		try {
			evd = EVD.factorize(matrix);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return evd;
	}
	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double area(
		F face,
		AdapterSet a
	) {
		E b = face.getBoundaryEdge();
		V v1 = b.getStartVertex();
		V v2 = b.getTargetVertex();
		V v3 = b.getNextEdge().getTargetVertex();
		double[] p1 = a.getD(Position3d.class, v1);
		double[] p2 = a.getD(Position3d.class, v2);
		double[] p3 = a.getD(Position3d.class, v3);
		double[] d1 = Rn.subtract(null, p2, p1);
		double[] d2 = Rn.subtract(null, p3, p1);
		double[] cr = Rn.crossProduct(null, d1, d2);
		return Rn.euclideanNorm(cr) / 2;
	}
	
	
	
	public static int getIndexOfMinMagnitude(double[] v){
		int r = 0;
		double val = abs(v[0]);
		for (int i = 1; i < v.length; i++){
			double tmp = abs(v[i]); 
			if (tmp < val) {
				r = i;
				val = tmp;
			}
		}
		return r;
	}
	
	
	/**
	 * The Eigenvector for the absolutely smallest Eigenvalue
	 * is sorted to the back. The two other vectors are sorted
	 * such that their Eigenvalues are ascending
	 * @param evd
	 * @return
	 */
	public static double[][] getSortedEigenVectors(EVD evd){
		double[] eigVal = evd.getRealEigenvalues();
		DenseMatrix eigVec = evd.getRightEigenvectors();
		double[][] eigVecArr = new double[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				eigVecArr[i][j]= eigVec.get(j,i);
			}
		}
		//get minimal magnitude
		int i3 = getIndexOfMinMagnitude(eigVal);
		int i1 = (i3 + 1) % 3;
		int i2 = (i3 + 2) % 3;
		double k1 = eigVal[i1];
		double k2 = eigVal[i2];
		double[][] r = new double[3][];
		r[0] = k1 < k2 ? eigVecArr[i1] : eigVecArr[i2];
		r[1] = k1 < k2 ? eigVecArr[i2] : eigVecArr[i1];
		r[2] = eigVecArr[i3];
		return r;
	}
	
	public static double[] getSortedEigenValues(EVD evd){
		double[] eigVal = evd.getRealEigenvalues();
		//get minimal magnitude
		int i3 = getIndexOfMinMagnitude(eigVal);
		int i1 = (i3 + 1) % 3;
		int i2 = (i3 + 2) % 3;
		double k1 = eigVal[i1];
		double k2 = eigVal[i2];
		double[] r = new double[3];
		r[0] = k1 < k2 ? k1 : k2;
		r[1] = k1 < k2 ? k2 : k1;
		r[2] = eigVal[i3];
		return r;
	}


	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[][] getTensor(
		double[] p,
		double scale,
		KdTree<V, E, F> kd,
		AdapterSet a
	 ){
		Collection<F> faces = KdUtility.collectFacesInRadius(kd, p, scale);
		Collection<E> edges = KdUtility.collectEdgesInRadius(kd, p, scale);
		double area=0;
		for(F f :faces){
			area += area(f, a);
		}
		DenseMatrix matrix = new DenseMatrix(3,3);
		double beta = 0;
		double edgeLength = 0;
		for(E e :edges){
			beta = getAngle(e, a);
			edgeLength = getLength(e, a);
			double[][] T = getEdgeCurvatureTensor(e, a);
			DenseMatrix tmp = new DenseMatrix(T);
			matrix.add(beta * edgeLength, tmp);
		}
		matrix.scale(1/area);
		double[][] result = {
			{matrix.getData()[0], matrix.getData()[3], matrix.getData()[6]},
			{matrix.getData()[1], matrix.getData()[4], matrix.getData()[7]},
			{matrix.getData()[2], matrix.getData()[5], matrix.getData()[8]}
		};
		return result;
	}
	
//	public static <
//		V extends Vertex<V, E, F>,
//		E extends Edge<V, E, F>,
//		F extends Face<V, E, F>
//	> double absoluteCurvatureAt(
//		double[] p,
//		double scale,
//		KdTree<V, E, F> kd,
//		AdapterSet a
//	){
//		Collection<E> edges = KdUtility.collectEdgesInRadius(kd, p, scale);
//		Collection<F> faces = incidentFaces(edges);
//		double area = 0;
//		for(F f :faces) {
//			area += area(f, a);
//		}
//		DenseMatrix matrix = new DenseMatrix(3,3);
//		double beta = 0;
//		double edgeLength = 0;
//		for(E e :edges){
//			beta = getAngle(e, a);
//			edgeLength = getLength(e, a);
//			double[][] T = getEdgeCurvatureTensor(e, a);
//			DenseMatrix tmp = new DenseMatrix(T);
//			matrix.add(beta * edgeLength, tmp);
//		}
//		matrix.scale(1/area);
//		return getMeanColumnsLength(matrix);
//	}
//
//
//	private static double getMeanColumnsLength(double[][] matrix) {
//		double[] c1 = {matrix[0][0], matrix[1][0], matrix[2][0]};
//		double[] c2 = {matrix[0][1], matrix[1][1], matrix[2][1]};
//		double[] c3 = {matrix[0][2], matrix[1][2], matrix[2][2]};
//		double c1Len = Rn.euclideanNorm(c1);
//		double c2Len = Rn.euclideanNorm(c2);
//		double c3Len = Rn.euclideanNorm(c3);
//		return (c1Len + c2Len + c3Len) / 3.0;
//	}

	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[][] getEdgeCurvatureTensor(E e, AdapterSet a) {
		double[] edge = getVector(e, a);
		Rn.normalize(edge, edge);
		double[][] result = new double[3][3];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				result[i][j] = edge[i] * edge[j];
			}
		}
		return result;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Collection<F> incidentFaces(Collection<E> edges) {
		Set<F> faces = new HashSet<F>(edges.size() / 3);
		for (E e : edges) {
			if (e.getLeftFace() != null) {
				faces.add(e.getLeftFace());
			}
		}
		return faces;
	}
	
    public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double getAngle(E e, AdapterSet a) {
		return signedAngle(e, a);
    }
    
    
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double signedAngle(E e, AdapterSet a) {
		F lf = e.getLeftFace();
		F rf = e.getRightFace();
		if (lf == null || rf == null) {
			return 0;
		}
		double[] ln = a.getD(Normal.class, lf);
		double[] rn = a.getD(Normal.class, rf);
		return curvatureSign(e, a) * Rn.euclideanAngle(ln, rn);
	}
	

	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double curvatureSign(
		E e,
		AdapterSet a
	){
		Matrix m = MatrixBuilder.euclidean().getMatrix();
		m.setColumn(0, getVector(e, a));
		m.setColumn(1, a.getD(Normal.class, e.getLeftFace()));
		m.setColumn(2, a.getD(Normal.class, e.getRightFace()));
		double det = m.getDeterminant() ;
		if(Math.abs(det) < 1E-10) {
			return 0;
		} else { 
			return signum(det);
		}
	}
    
    
    public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double getLength(
		E e,
		AdapterSet a
	){
    	double[] pos1 = a.getD(Position3d.class, e.getStartVertex());
    	double[] pos2 = a.getD(Position3d.class, e.getTargetVertex());
    	return Rn.euclideanDistance(pos1, pos2);
	}
    
    public static  <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getVector(
		E e,
		AdapterSet a
	){
    	double[] pos1 = a.getD(Position3d.class, e.getTargetVertex());
    	double[] pos2 = a.getD(Position3d.class, e.getStartVertex());
		return Rn.subtract(null, pos1, pos2);
    	
    }
	
}
