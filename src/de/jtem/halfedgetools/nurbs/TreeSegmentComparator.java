package de.jtem.halfedgetools.nurbs;
 
import static java.lang.Math.signum;

import java.util.Comparator;
import java.util.LinkedList;

import de.jreality.math.Rn;


public class TreeSegmentComparator implements Comparator<LineSegment>{
	
	public EventPoint p;
	public LinkedList<EventPointSegmentList> eventPointSegmentList;
	
	
	public int compare(LineSegment s1, LineSegment s2){
		double a1;
		double a2;
		double b1;
		double b2;
		double c1;
		double c2;
		double d1;
		double d2;
		if(s2.segment[0][1] < s2.segment[1][1] || (s2.segment[0][1] == s2.segment[1][1] && s2.segment[0][0] > s2.segment[1][0])){
			b1 = s2.segment[0][0];
			b2 = s2.segment[0][1];
			a1 = s2.segment[1][0];
			a2 = s2.segment[1][1];
		}else{
			a1 = s2.segment[0][0];
			a2 = s2.segment[0][1];
			b1 = s2.segment[1][0];
			b2 = s2.segment[1][1];
		}
		if(s1.segment[0][1] < s1.segment[1][1] || (s1.segment[0][1] == s1.segment[1][1] && s1.segment[0][0] > s1.segment[1][0])){
			d1 = s1.segment[0][0];
			d2 = s1.segment[0][1];
			c1 = s1.segment[1][0];
			c2 = s1.segment[1][1];
		}else{
			c1 = s1.segment[0][0];
			c2 = s1.segment[0][1];
			d1 = s1.segment[1][0];
			d2 = s1.segment[1][1];
		}
		
//		if(isHorizontal(s1)){
//			for (EventPointSegmentList epsl : eventPointSegmentList) {
//				if(epsl != eventPointSegmentList.getLast()){
//					epsl.allSegments.remove(s1);
//				}
//			}
//		}
//		if(isHorizontal(s2)){
//			for (EventPointSegmentList epsl : eventPointSegmentList) {
//				if(epsl != eventPointSegmentList.getLast()){
//					epsl.allSegments.remove(s2);
//				}
//			}
//		}
		
		boolean segmentsIntersectInEventpoint = false;
		for (EventPointSegmentList epsl : eventPointSegmentList) {
			if(epsl.allSegments.contains(s1) && epsl.allSegments.contains(s2)){
				segmentsIntersectInEventpoint = true;
			}
		}
		
		if(segmentsIntersectInEventpoint){
//			System.out.println("intersection in EventPoint");
//			System.out.println("first " + s1.toString() + " second " + s2.toString() + " result " + (int)signum(angleOrder(a1, a2, b1, b2, c1, c2, d1, d2)));
			return (int)signum(angleOrder(a1, a2, b1, b2, c1, c2, d1, d2));
		}
		else{
			double compareS1 = 0;
			double compareS2 = 0;
			if(segmentIsInEventPointSegmentList(s1)){
				compareS1 = getXComponentFromEventPoint(s1);
			}
			else{
				compareS1 = c1 + ((d1 - c1) * (c2 - p.point[1]) / (c2 - d2));
			}
			if(segmentIsInEventPointSegmentList(s2)){
				compareS2 = getXComponentFromEventPoint(s2);
			}
			else{
				compareS2 = a1 + ((b1 - a1) * (a2 - p.point[1]) / (a2 - b2));
			}
//			System.out.println("do not intersect in same EventPoint");
//			System.out.println("first " + s1.toString() + " second " + s2.toString() + " result " + (int)signum(compareS1 - compareS2));
			return (int)signum(compareS1 - compareS2);
		}
	}
	

	
	private static double angleOrder(double a1,double a2, double b1,double b2,double c1,double c2, double d1,double d2){
		double[] n = new double[2];
		n[0] = a2 - b2;
		n[1] = b1 - a1;
		double[] v = new double[2];
		v[0] = d1 - c1;
		v[1] = d2 - c2;
		if(a1 == c1 && a2 == c2 && b1 == d1 && b2 == d2){
			return 0;
		}
		else{
			Rn.normalize(n, n);
			Rn.normalize(v, v);
			return Rn.innerProduct(n, v);
		}
	}
	
	private boolean segmentIsInEventPointSegmentList(LineSegment ls){
		for (EventPointSegmentList epsl : eventPointSegmentList) {
			if(epsl.allSegments.contains(ls)){
				return true;
			}
		}
		return false;
	}
	
	private double getXComponentFromEventPoint(LineSegment ls){
		double result = 0;
		for (EventPointSegmentList epsl : eventPointSegmentList){
			if(epsl.allSegments.contains(ls)){
				result = epsl.p.point[0];
			}
		}
		return result;
	}
	
	private boolean isHorizontal(LineSegment ls){
		if(ls.segment[0][1] == ls.segment[1][1]){
			return true;
		}
		return false;
	}
	

}