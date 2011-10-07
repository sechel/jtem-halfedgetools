package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

public class EventPointSegmentList {
	
	protected EventPoint p;
	protected LinkedList<LineSegment> allSegments;
	
	public EventPointSegmentList(){
		
	}
	
	public EventPointSegmentList(EventPoint p, LinkedList<LineSegment> allSegments) {
		this.p = p;
		this.allSegments = allSegments;
	}

	public EventPoint getP() {
		return p;
	}

	public void setP(EventPoint p) {
		this.p = p;
	}

	public LinkedList<LineSegment> getAllSegments() {
		return allSegments;
	}

	public void setAllSegments(LinkedList<LineSegment> allSegments) {
		this.allSegments = allSegments;
	}
	
	
	
	
}
