package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

import org.junit.Test;

import junit.framework.Assert;

public class LineSegmentIntersectionTest {
	
	@Test
	
	public void testSweepLineAlgorithm(){
		double[] a1 = {1,10};
		double[] a2 = {4,1};
		double[] a3 = {2,3};
		double[] a4 = {9,9};
		double[] a5 = {2,5};
		double[] a6 = {7,2};
		double[] a7 = {2,3};
		double[] a8 = {9,3};
		double[] a9 = {2,5};
		double[] a10 = {9,5};
		double[] b1 = {2,5};
		double[] b2 = {2,2};
		double[] b3 = {1,4};
		double[] b4 = {4,4};
		double[] b5 = {2,2};
		double[] b6 = {5,2};
		double[] b7 = {4,4};
		double[] b8 = {4,1};
		LineSegmentIntersection s1 = new LineSegmentIntersection();
		s1.segment = new double[2][];
		s1.segment[0] = a1;
		s1.segment[1] = a2;
		s1.curveIndex = 1;
		LineSegmentIntersection s2 = new LineSegmentIntersection();
		s2.segment = new double[2][];
		s2.segment[0] = a3;
		s2.segment[1] = a4;
		s2.curveIndex = 2;
		LineSegmentIntersection s3 = new LineSegmentIntersection();
		s3.segment = new double[2][];
		s3.segment[0] = a5;
		s3.segment[1] = a6;
		s3.curveIndex = 3;
		LineSegmentIntersection s4 = new LineSegmentIntersection();
		s4.segment = new double[2][];
		s4.segment[0] = a7;
		s4.segment[1] = a8;
		s4.curveIndex = 4;
		LineSegmentIntersection s5 = new LineSegmentIntersection();
		s5.segment = new double[2][];
		s5.segment[0] = a9;
		s5.segment[1] = a10;
		s5.curveIndex = 5;
		LineSegmentIntersection t1 = new LineSegmentIntersection();
		t1.segment = new double[2][];
		t1.segment[0] = b1;
		t1.segment[1] = b2;
		t1.curveIndex = 1;
		LineSegmentIntersection t2 = new LineSegmentIntersection();
		t2.segment = new double[2][];
		t2.segment[0] = b3;
		t2.segment[1] = b4;
		t2.curveIndex = 2;
		LineSegmentIntersection t3 = new LineSegmentIntersection();
		t3.segment = new double[2][];
		t3.segment[0] = b5;
		t3.segment[1] = b6;
		t3.curveIndex = 3;
		LineSegmentIntersection t4 = new LineSegmentIntersection();
		t4.segment = new double[2][];
		t4.segment[0] = b7;
		t4.segment[1] = b8;
		t4.curveIndex = 4;
		LinkedList<LineSegmentIntersection> seg = new LinkedList<LineSegmentIntersection>();
		seg.add(s1);
		seg.add(s2);
		seg.add(s3);
		seg.add(s4);
		seg.add(s5);
		LinkedList<LineSegmentIntersection> seg1 = new LinkedList<LineSegmentIntersection>();
		seg1.add(t1);
		seg1.add(t2);
		seg1.add(t3);
		seg1.add(t4);
		LinkedList<IntersectionPoint> iP = LineSegmentIntersection.findIntersections(seg);
		LinkedList<IntersectionPoint> iP1 = LineSegmentIntersection.findIntersections(seg1);
		Assert.assertTrue(iP.size() == 9);
		Assert.assertTrue(iP1.size() == 4);
		b1[0] = 2; b1[1] = 8.5;
		b2[0] = 3; b2[1] = 7.5;
		b3[0] = 4; b3[1] = 8.5;
		b4[0] = 3; b4[1] = 7.5;
		b5[0] = 2; b5[1] = 7.5;
		b6[0] = 4; b6[1] = 6;
		b7[0] = 4; b7[1] = 8;
		b8[0] = 2; b8[1] = 6;
		t1.segment = new double[2][];
		t1.segment[0] = b1;
		t1.segment[1] = b2;
		t1.curveIndex = 1;
		t2.segment = new double[2][];
		t2.segment[0] = b3;
		t2.segment[1] = b4;
		t2.curveIndex = 2;
		t3.segment = new double[2][];
		t3.segment[0] = b5;
		t3.segment[1] = b6;
		t3.curveIndex = 3;
		t4.segment = new double[2][];
		t4.segment[0] = b7;
		t4.segment[1] = b8;
		t4.curveIndex = 4;
		seg.clear();
		seg.add(t1);
		seg.add(t2);
		seg.add(t3);
		seg.add(t4);
		iP = LineSegmentIntersection.findIntersections(seg);
		Assert.assertTrue(iP.size() == 2);
	}

}
