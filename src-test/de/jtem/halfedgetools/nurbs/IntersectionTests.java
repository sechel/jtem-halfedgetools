package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.thoughtworks.xstream.XStream;

import junit.framework.Assert;

public class IntersectionTests {
	
	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testIntersection02() {
//		XStream x = new XStream();
//		Object intersectionsObj = x.fromXML(IntersectionTests.class.getResourceAsStream("testSegments.xml"));
//		List<LineSegment> segments = (List<LineSegment>) intersectionsObj;
//		LinkedList<IntersectionPoint> iP = LineSegmentIntersection.findIntersections(segments);
//		Assert.assertEquals("number of intersections", 16, iP.size());
//	}
	
	
	@Test
	public void testSweepLineAlgorithm01(){
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
		LineSegment s1 = new LineSegment();
		s1.segment = new double[2][];
		s1.segment[0] = a1;
		s1.segment[1] = a2;
		s1.curveIndex = 1;
		LineSegment s2 = new LineSegment();
		s2.segment = new double[2][];
		s2.segment[0] = a3;
		s2.segment[1] = a4;
		s2.curveIndex = 2;
		LineSegment s3 = new LineSegment();
		s3.segment = new double[2][];
		s3.segment[0] = a5;
		s3.segment[1] = a6;
		s3.curveIndex = 3;
		LineSegment s4 = new LineSegment();
		s4.segment = new double[2][];
		s4.segment[0] = a7;
		s4.segment[1] = a8;
		s4.curveIndex = 4;
		LineSegment s5 = new LineSegment();
		s5.segment = new double[2][];
		s5.segment[0] = a9;
		s5.segment[1] = a10;
		s5.curveIndex = 5;
		LineSegment t1 = new LineSegment();
		t1.segment = new double[2][];
		t1.segment[0] = b1;
		t1.segment[1] = b2;
		t1.curveIndex = 1;
		LineSegment t2 = new LineSegment();
		t2.segment = new double[2][];
		t2.segment[0] = b3;
		t2.segment[1] = b4;
		t2.curveIndex = 2;
		LineSegment t3 = new LineSegment();
		t3.segment = new double[2][];
		t3.segment[0] = b5;
		t3.segment[1] = b6;
		t3.curveIndex = 3;
		LineSegment t4 = new LineSegment();
		t4.segment = new double[2][];
		t4.segment[0] = b7;
		t4.segment[1] = b8;
		t4.curveIndex = 4;
		LinkedList<LineSegment> seg = new LinkedList<LineSegment>();
		seg.add(s1);
		seg.add(s2);
		seg.add(s3);
		seg.add(s4);
		seg.add(s5);
		LinkedList<LineSegment> seg1 = new LinkedList<LineSegment>();
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
	

	@Test
	public void testSweepLineAlgorithm02(){
		double[] b1 = {0.001,0.001};
		double[] b2 = {0.999,0.001};
		double[] b3 = {0.001,0.999};
		double[] b4 = {0.999,0.999};
//		double[] b1 = {0,0};
//		double[] b2 = {1,0};
//		double[] b3 = {0,1};
//		double[] b4 = {1,1};
		LineSegment seg1_1 = new LineSegment();
		seg1_1.segment = new double[2][];
		seg1_1.segment[0] = b1;
		seg1_1.segment[1] = b2;
		seg1_1.curveIndex = 1;
		seg1_1.indexOnCurve = 1;
		LineSegment seg2_1 = new LineSegment();
		seg2_1.segment = new double[2][];
		seg2_1.segment[0] = b2;
		seg2_1.segment[1] = b4;
		seg2_1.curveIndex = 2;
		seg2_1.indexOnCurve = 1;
		LineSegment seg3_1 = new LineSegment();
		seg3_1.segment = new double[2][];
		seg3_1.segment[0] = b4;
		seg3_1.segment[1] = b3;
		seg3_1.curveIndex = 3;
		seg3_1.indexOnCurve = 1;
		LineSegment seg4_1 = new LineSegment();
		seg4_1.segment = new double[2][];
		seg4_1.segment[0] = b3;
		seg4_1.segment[1] = b1;
		seg4_1.curveIndex = 4;
		seg4_1.indexOnCurve = 1;
		double[] t1 = {1.0, 0.3333333333333333};
		double[] t2 = {0.5, 0.3333333333333333};
		double[] t3 = {0, 0.3333333333333333};
		double[] t4 = {0.5, 1};
		double[] t5 = {0.5, 0.5};
		double[] t6 = {0.5, 0};
		LineSegment seg5_1 = new LineSegment();
		seg5_1.segment = new double[2][];
		seg5_1.segment[0] = t1;
		seg5_1.segment[1] = t2;
		seg5_1.curveIndex = 5;
		seg5_1.indexOnCurve = 1;
		LineSegment seg5_2 = new LineSegment();
		seg5_2.segment = new double[2][];
		seg5_2.segment[0] = t2;
		seg5_2.segment[1] = t3;
		seg5_2.curveIndex = 5;
		seg5_2.indexOnCurve = 2;
		LineSegment seg6_1 = new LineSegment();
		seg6_1.segment = new double[2][];
		seg6_1.segment[0] = t4;
		seg6_1.segment[1] = t5;
		seg6_1.curveIndex = 6;
		seg6_1.indexOnCurve = 1;
		LineSegment seg6_2 = new LineSegment();
		seg6_2.segment = new double[2][];
		seg6_2.segment[0] = t5;
		seg6_2.segment[1] = t6;
		seg6_2.curveIndex = 6;
		seg6_2.indexOnCurve = 2;
		LinkedList<LineSegment> segmentsT = new LinkedList<LineSegment>();
		segmentsT.add(seg1_1);
		segmentsT.add(seg2_1);
		segmentsT.add(seg3_1);
		segmentsT.add(seg4_1);
		segmentsT.add(seg5_1);
		segmentsT.add(seg5_2);
		segmentsT.add(seg6_1);
		segmentsT.add(seg6_2);
		LinkedList<IntersectionPoint> iP = LineSegmentIntersection.findIntersections(segmentsT);
		Assert.assertTrue(iP.size() == 9);
	}

}
