package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

public class EventPointSegmentList {
	
	protected EventPoint p;
	protected LinkedList<LineSegmentIntersection> allSegments;
	
	public EventPointSegmentList(){
		
	}
	
	public EventPointSegmentList(EventPoint p, LinkedList<LineSegmentIntersection> allSegments) {
		this.p = p;
		this.allSegments = allSegments;
	}

	public EventPoint getP() {
		return p;
	}

	public void setP(EventPoint p) {
		this.p = p;
	}

	public LinkedList<LineSegmentIntersection> getAllSegments() {
		return allSegments;
	}

	public void setAllSegments(LinkedList<LineSegmentIntersection> allSegments) {
		this.allSegments = allSegments;
	}
	
	
	
	
}
