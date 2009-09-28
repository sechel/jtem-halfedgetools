package de.jtem.halfedgetools.algorithm.circlepattern;

import static java.lang.Math.PI;

import javax.vecmath.Point2d;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.circlepattern.CPLayoutAdapters.Rho;
import de.jtem.halfedgetools.algorithm.circlepattern.CPLayoutAdapters.Theta;
import de.jtem.halfedgetools.algorithm.circlepattern.CPLayoutAlgorithm.Rotation;
import de.jtem.mfc.field.Complex;
import de.jtem.mfc.geometry.ComplexProjective1;
import de.jtem.mfc.group.Moebius;

public class CPEuclideanRotation 
<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Rotation<V, E, F> {

	
	public Point2d rotate(Point2d p, Point2d center, Double phi, Double logScale) {
		Moebius rot = new Moebius();
		Complex c1 = new Complex(center.x, center.y);
		Complex c2 = new Complex(1,0);
		ComplexProjective1 c = new ComplexProjective1(c1, c2);
		
		rot.assignEuclideanLogScaleRotation(c, logScale, phi);
		Complex pc = new Complex(p.x, p.y);
		pc = rot.applyTo(pc);
		return new Point2d(pc.re, pc.im);
	}

	
	public double getPhi(E edge, Rho<F> rho, Theta<E> theta) {
		double th = theta.getTheta(edge);
		double thStar = PI - th;
		if (edge.getLeftFace() == null || edge.getRightFace() == null)
			return thStar;
		double leftRho = rho.getRho(edge.getLeftFace());
		double rightRho = rho.getRho(edge.getRightFace());
		double p = p(thStar, rightRho - leftRho);
		return 0.5*(p + thStar);
	}

	
	public Double getRadius(Double rho) {
		return Math.exp(rho);
	}
	
    public Double p(Double thStar, Double diffRho) {
        Double exp = Math.exp(diffRho);
        Double tanhDiffRhoHalf = (exp - 1.0) / (exp + 1.0);
        return  2.0 * Math.atan(Math.tan(0.5 * thStar) * tanhDiffRhoHalf);
    }
	
	
}
