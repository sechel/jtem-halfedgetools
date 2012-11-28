package de.jtem.halfedgetools.util;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class GeometryUtility {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double getMeanEdgeLength(HDS mesh, AdapterSet a) {
		double result = 0.0;
		int n= 0;
		for (E e : mesh.getPositiveEdges()) {
			double[] s = a.getD(Position3d.class, e.getStartVertex());
			double[] t = a.getD(Position3d.class, e.getTargetVertex());
			result += Rn.euclideanDistance(s, t);
			n++;
		}
		return result / n;
	}
	
	public static double[] circumCircle(double[] p1, double[] p2, double[] p3) {
		double[] circle = new double[4];
		double[] cc = circumCenter(p1,p2,p3);
		System.arraycopy(cc, 0, circle, 0, 3);
		circle[3] = Rn.euclideanDistance(cc, p2);
		return circle;
	}
	
	public static double[] circumCenter(double[] c1, double[] c2, double[] c3) {
		double[]
		       ec1 = Rn.subtract(null, c2, c1),
		       ec2 = Rn.subtract(null, c3, c2),
		       ec3 = Rn.subtract(null, c1, c3);
		double
			alpha = 
				Rn.euclideanNormSquared(ec2)*(-1)*Rn.innerProduct(ec1, ec3)/
				(2*Rn.euclideanNormSquared(Rn.crossProduct(null, ec1, ec2))),
			beta = 
				Rn.euclideanNormSquared(ec3)*(-1)*Rn.innerProduct(ec1, ec2)/
				(2*Rn.euclideanNormSquared(Rn.crossProduct(null, ec1, ec2))),
			gamma = 
				Rn.euclideanNormSquared(ec1)*(-1)*Rn.innerProduct(ec3, ec2)/
				(2*Rn.euclideanNormSquared(Rn.crossProduct(null, ec1, ec2)));
		double[]
		       ac1 = Rn.times(null, alpha, c1),
		       bc2 = Rn.times(null, beta, c2),
		       gc3 = Rn.times(null, gamma, c3);
		return Rn.add(null, ac1, Rn.add(null,bc2,gc3));
	}
	
	
}
