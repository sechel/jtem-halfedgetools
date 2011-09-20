package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

public class HalfedgePoint {
	
	protected IntersectionPoint point;
	protected LinkedList<IntersectionPoint> nbrs;
	protected LinkedList<IntersectionPoint> usedNbrs;
	
	

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
		this.usedNbrs = maxNbrs;
	}



	public IntersectionPoint getPoint() {
		return point;
	}

	public void setPoint(IntersectionPoint point) {
		this.point = point;
	}


	public LinkedList<IntersectionPoint> getMaxNbrs() {
		return usedNbrs;
	}

	public void setMaxNbrs(LinkedList<IntersectionPoint> maxNbrs) {
		this.usedNbrs = maxNbrs;
	}




	@Override
	public String toString() {
		return "HalfedgePoints [point=" + point +  "]";
	}
	
	

	
}
