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

package de.jtem.halfedgetools.algorithm.alexandrov;

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
	
	
//	public static Point2d circumCenter(Point2d p1, Point2d p2, Point2d p3) {
//		
//		double[] a = new double[] {
//				p1.x, p1.y, 1.0, 
//				p2.x, p2.y, 1.0, 
//				p3.x, p3.y, 1.0
//		};
//		
//		double[] bx = new double[] {
//		        p1.x*p1.x + p1.y*p1.y, p1.y, 1.0, 
//		        p2.x*p2.x + p2.y*p2.y, p2.y, 1.0,
//		        p3.x*p3.x + p3.y*p3.y, p3.y, 1.0
//		};
//		
//		double[] by = new double[] {
//		        p1.x*p1.x + p1.y*p1.y, p1.x, 1.0, 
//		        p2.x*p2.x + p2.y*p2.y, p2.x, 1.0,
//		        p3.x*p3.x + p3.y*p3.y, p3.x, 1.0
//		};
//		
//		double dbx = -Rn.determinant(bx);
//		double dby = Rn.determinant(by);
//		double da = 2.0*Rn.determinant(a);
//		
//		return new Point2d(- (dbx/da), - (dby/da));
//		
//	}
}
