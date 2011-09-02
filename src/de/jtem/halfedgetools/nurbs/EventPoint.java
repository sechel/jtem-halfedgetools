package de.jtem.halfedgetools.nurbs;

import de.jtem.halfedgetools.nurbs.LineSegmentIntersection.PointStatus1;


public class EventPoint {

	double[] point;
	PointStatus1 status = PointStatus1.upper;
	LineSegmentIntersection segment;
	
	public EventPoint(){
		
	}


	public EventPoint(double[] point, PointStatus1 status, LineSegmentIntersection segment) {
		this.point = point;
		this.status = status;
		this.segment = segment;
	}


	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}

	public PointStatus1 getStatus() {
		return status;
	}

	public void setStatus(PointStatus1 status) {
		this.status = status;
	}


	public LineSegmentIntersection getSegment() {
		return segment;
	}


	public void setSegment(LineSegmentIntersection segment) {
		this.segment = segment;
	}
	
	
	
	
}
