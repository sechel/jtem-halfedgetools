package de.jtem.halfedgetools.nurbs;

import de.jtem.halfedgetools.nurbs.LineSegmentIntersection.PointStatus;


public class EventPoint {

	double[] point;
	PointStatus status = PointStatus.upper;
	LineSegment segment;
	
	public EventPoint(){
		
	}


	public EventPoint(double[] point, PointStatus status, LineSegment segment) {
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

	public PointStatus getStatus() {
		return status;
	}

	public void setStatus(PointStatus status) {
		this.status = status;
	}


	public LineSegment getSegment() {
		return segment;
	}


	public void setSegment(LineSegment segment) {
		this.segment = segment;
	}
	
	
	
	
}
