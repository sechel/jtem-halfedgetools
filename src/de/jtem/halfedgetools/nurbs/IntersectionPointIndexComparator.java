package de.jtem.halfedgetools.nurbs;

import java.util.Comparator;

public class IntersectionPointIndexComparator implements Comparator<IntersectionPoint> {
	
	public int curveIndex;
	
	@Override	
	public int compare(IntersectionPoint ip1, IntersectionPoint ip2) {
		
		return (int)Math.signum(getIndexOnCurveFromCurveIndexAndIntersectionPoint(curveIndex, ip1)- getIndexOnCurveFromCurveIndexAndIntersectionPoint(curveIndex, ip2));
	}
	
	private static int getIndexOnCurveFromCurveIndexAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
		int result = 0;
		for (LineSegment seg : iP.intersectingSegments) {
			if(seg.curveIndex == curveIndex && result < seg.indexOnCurve){
				result = seg.indexOnCurve;
			}
		}
		return result;
	}
}

