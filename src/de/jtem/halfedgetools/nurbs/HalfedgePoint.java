package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;
import java.util.LinkedList;

public class HalfedgePoint {
	
	

	protected IntersectionPoint point;
	protected LinkedList<IntersectionPoint> nbrs;
	protected LinkedList<IntersectionPoint> unusedNbrs;
	
	

	public HalfedgePoint(){
		
	}
	
	public HalfedgePoint(IntersectionPoint p, LinkedList<IntersectionPoint> n){
		point = p;
		nbrs = n;
	}
	

	public HalfedgePoint(IntersectionPoint point, int indexOnMaxCurve,
			int indexOnMinCurve, int maxCuveIndex, int minCurveIndex,
			LinkedList<IntersectionPoint> maxNbrs,
			LinkedList<IntersectionPoint> minNbrs) {
		super();
		this.point = point;
		this.unusedNbrs = maxNbrs;
	}

	public LinkedList<IntersectionPoint> getNbrs() {
		return nbrs;
	}

	public void setNbrs(LinkedList<IntersectionPoint> nbrs) {
		this.nbrs = nbrs;
	}




	public IntersectionPoint getPoint() {
		return point;
	}

	public void setPoint(IntersectionPoint point) {
		this.point = point;
	}


	public LinkedList<IntersectionPoint> getUnusedNbrs() {
		return unusedNbrs;
	}

	public void setUnusedNbrs(LinkedList<IntersectionPoint> unusedNbrs) {
		this.unusedNbrs = unusedNbrs;
	}




	@Override
	public String toString() {
		System.out.println("HalfedgePoint:" + Arrays.toString(point.point));
		for (IntersectionPoint n : nbrs) {
			System.out.println(Arrays.toString(n.point));
		}
		return "";
	}
	
	

	
}
