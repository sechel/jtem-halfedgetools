package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;
import java.util.LinkedList;


public class IntersectionPoint {
	
	protected double[] point = null;
	protected double sameIndexDist;
	protected HalfedgePoint parentHP = null;
	protected LinkedList<LineSegmentIntersection> intersectingSegments;

	
	public IntersectionPoint() {

	}
	

	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}

	public HalfedgePoint getParentHP() {
		return parentHP;
	}


	public void setParentHP(HalfedgePoint parentHP) {
		this.parentHP = parentHP;
	}


	public LinkedList<LineSegmentIntersection> getIntersectingSegments() {
		return intersectingSegments;
	}


	public void setIntersectingSegments(
			LinkedList<LineSegmentIntersection> intersectingSegments) {
		this.intersectingSegments = intersectingSegments;
	}


	@Override
	public String toString() {
		return "IntersectionPoint [point=" + Arrays.toString(point) + "]";
	}



	



	


	



	
	

}
