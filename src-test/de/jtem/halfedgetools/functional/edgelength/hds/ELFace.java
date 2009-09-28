package de.jtem.halfedgetools.functional.edgelength.hds;

import static java.lang.Math.PI;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

import de.jtem.halfedge.Face;


public class ELFace extends Face<ELVertex, ELEdge, ELFace> {

	public Point2d 
		xy = new Point2d();
	public Point4d
		xyzw = new Point4d();
	public boolean
		label = false;
	public double 
		rho = 0.0,
		radius = 1.0,
		grad = 0.0,
		phi = 2 * PI;
	
}
