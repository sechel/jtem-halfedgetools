package de.jtem.halfedgetools.nurbs;
 
import static java.lang.Math.signum;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Set;

import de.jreality.math.Rn;


public class TreeSegmentComparator implements Comparator<LineSegmentIntersection>{
	
	public EventPoint p;
	public LinkedList<EventPointSegmentList> eventPointSegmentList;
	
	
	public int compare(LineSegmentIntersection s1, LineSegmentIntersection s2){
		double a1 = s2.segment[0][0];
		double a2 = s2.segment[0][1];
		double b1 = s2.segment[1][0];
		double b2 = s2.segment[1][1];
		double c1 = s1.segment[0][0];
		double c2 = s1.segment[0][1];
		double d1 = s1.segment[1][0];
		double d2 = s1.segment[1][1];

		boolean segmentsIntersectInEventpoint = false;
		for (EventPointSegmentList epsl : eventPointSegmentList) {
			if(epsl.allSegments.contains(s1) && epsl.allSegments.contains(s2)){
				segmentsIntersectInEventpoint = true;
			}
		}
		if(segmentsIntersectInEventpoint){
			double[] n = new double[2];
			n[0] = a2 - b2;
			n[1] = b1 - a1;
			double[] v = new double[2];
			v[0] = d1 - c1;
			v[1] = d2 - c2;
			return (int)signum(Rn.innerProduct(n, v));
		}
		else{
			double compareS1;
			double compareS2;
			if(a2 == b2){
				compareS1 = p.point[0];
			}else{
				compareS1 = a1 + ((b1 - a1) * (a2 - p.point[1]) / (a2 - b2));
			}
			if(c2 == d2){
				compareS2 = p.point[0];
			}else{
				compareS2 = c1 + ((d1 - c1) * (c2 - p.point[1]) / (c2 - d2));
			}
			return (int)signum(compareS2 - compareS1);
		}
	}

}