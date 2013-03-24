package de.jtem.halfedgetools.symmetry.util;

import static java.lang.Double.isNaN;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point4d;
import javax.vecmath.Vector3d;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

public class VecmathTools {

	public static final Point4d
		zero4d = new Point4d(0,0,0,1);
	
	public static void dehomogenize(Point4d p){
		p.x /= p.w;
		p.y /= p.w;
		p.z /= p.w;
		p.w = 1.0;
	}
	
	public static void normalize(Point4d p){
		p.scale(1 / p.distance(zero4d));
	}
	
	
	public static double length(Point4d p){
		dehomogenize(p);
		return p.distance(zero4d);
	}
	
	
	public static double distance(Point4d p1, Point4d p2) {
		dehomogenize(p1); dehomogenize(p2);
		return Math.sqrt((p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y) + (p1.z - p2.z)*(p1.z - p2.z));
	}
	
	
	public static void projectInverseStereographic(Point2d p2d, Point4d p4d, double scale) {
			double x = p2d.x / scale;
			double y = p2d.y / scale;
			double nx = 2 * x;
			double ny = x*x + y*y - 1;
			double nz = 2 * y;
			double nw = ny + 2;
			p4d.set(nx, ny, nz, nw);
	}
	
	
	
	public static void sphereMirror(Point4d p){
		VecmathTools.dehomogenize(p);
		double lengthSqr = (p.x*p.x + p.y*p.y + p.z*p.z);
		p.w = lengthSqr;
		VecmathTools.dehomogenize(p);
	}
	
	
	public static boolean isNAN(Point4d p){
		return isNaN(p.x) || isNaN(p.y) || isNaN(p.z) || isNaN(p.w);
	}

	public static Vector cross(Vector p1, Vector p2){
		Vector result = new DenseVector(3);
		double x = p1.get(1) * p2.get(2) - p1.get(2) * p2.get(1);
		double y = p1.get(2) * p2.get(0) - p1.get(0) * p2.get(2);
		double z = p1.get(0) * p2.get(1) - p1.get(1) * p2.get(0);
		result.set(0, x);
		result.set(1, y);
		result.set(2, z);
		return result;
	}

	public static Point4d cross(Point4d p1, Point4d p2){
		dehomogenize(p1);
		dehomogenize(p2);
		Point4d result = new Point4d();
		result.x = p1.y * p2.z - p1.z * p2.y;
		result.y = p1.z * p2.x - p1.x * p2.z;
		result.z = p1.x * p2.y - p1.y * p2.x;
		result.w = 1.0;
		return result;
	}
	
	public static double dot(Vector p1, Vector p2){
		return p1.get(0) * p2.get(0) + p1.get(1) * p2.get(1) + p1.get(2) * p2.get(2);
	}
	
	public static Point3d p4top3(Point4d p) {
		Point4d t = new Point4d(p);
		dehomogenize(t);
		return new Point3d(t.x, t.y, t.z);
	}
	
	public static Vector3d p4tov3(Point4d p) {
		Point4d t = new Point4d(p);
		dehomogenize(t);
		return new Vector3d(t.x, t.y, t.z);
	}
	
	public static double distFromPointToLine(Point4d x0, Point4d x1, Point4d x2) {
		
		Point3d
			v = p4top3(x0),
			l = p4top3(x1),
			r = p4top3(x2);
		
		// calculate |v|, that is, the distance from the coordinate
		// v to the line (l, r)
		//                         |(r - l) x (l - v) |
		// this is given by |v| = -----------------------
		//                              |(r - l)|
		// where a,b are points of the line and c the point to measure
		// TODO can get rid of sqrt if we want, but it is no big deal
		Vector3d rl = new Vector3d();
		rl.sub(r, l);
		
		Vector3d lv = new Vector3d();
		lv.sub(l, v);
		
		Vector3d temp = new Vector3d();
		temp.cross(rl, lv);
		
		double dist = temp.length()/rl.length();
		return dist;
	}
}
