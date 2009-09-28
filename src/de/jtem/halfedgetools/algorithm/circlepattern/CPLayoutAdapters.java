package de.jtem.halfedgetools.algorithm.circlepattern;

import javax.vecmath.Point2d;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;

public class CPLayoutAdapters {

	public static interface Rho <F extends Face<?, ?, F>> {
		public double getRho(F f);
	}
	
	public static interface Theta <E extends Edge<?, E, ?>> {
		public double getTheta(E e);
	}
	
	public static interface Radius <F extends Face<?, ?, F>> {
		public double getRadius(F v);
		public void setRadius(F v, double r);
	}

	public static interface XYVertex <V extends Vertex<V, ?, ?>> {
		public Point2d getXY(V v, Point2d xy);
		public void setXY(V v, Point2d xy);
	}

	public static interface XYFace <F extends Face<?, ?, F>> {
		public Point2d getXY(F v, Point2d xy);
		public void setXY(F f, Point2d xy);
	}
	
}
