package de.jtem.halfedgetools.nurbs;

import java.util.Comparator;

public class IntersectionPointIndexComparator implements Comparator<IntersectionPoint> {
	
	public int curveIndex;
	
	@Override
	public int compare(IntersectionPoint ip1, IntersectionPoint ip2) {
		for (LineSegment seg1 : ip1.intersectingSegments) {
			for (LineSegment seg2 : ip2.intersectingSegments) {
				if(seg1.curveIndex == curveIndex && seg2.curveIndex == curveIndex){
					return (int)Math.signum(seg1.indexOnCurve - seg2.indexOnCurve);
				}
			}
		}
		return 0;
	}
}

