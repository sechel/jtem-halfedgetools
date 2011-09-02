package de.jtem.halfedgetools.nurbs;

import java.util.LinkedList;

public class HalfedgePoint {
	
	protected IntersectionPoint point;
	int indexOnMaxCurve;
	int indexOnMinCurve;
	int maxCurveIndex;
	int minCurveIndex;
	protected LinkedList<IntersectionPoint> nbrs;
	protected LinkedList<IntersectionPoint> maxNbrs;
	protected LinkedList<IntersectionPoint> minNbrs;
	
	

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
		this.indexOnMaxCurve = indexOnMaxCurve;
		this.indexOnMinCurve = indexOnMinCurve;
		this.maxCurveIndex = maxCuveIndex;
		this.minCurveIndex = minCurveIndex;
		this.maxNbrs = maxNbrs;
		this.minNbrs = minNbrs;
	}



	public IntersectionPoint getPoint() {
		return point;
	}

	public void setPoint(IntersectionPoint point) {
		this.point = point;
	}

	public int getIndexOnMaxCurve() {
		return indexOnMaxCurve;
	}

	public void setIndexOnMaxCurve(int indexOnMaxCurve) {
		this.indexOnMaxCurve = indexOnMaxCurve;
	}

	public int getIndexOnMinCurve() {
		return indexOnMinCurve;
	}

	public void setIndexOnMinCurve(int indexOnMinCurve) {
		this.indexOnMinCurve = indexOnMinCurve;
	}

	public int getMaxCuveIndex() {
		return maxCurveIndex;
	}

	public void setMaxCuveIndex(int maxCuveIndex) {
		this.maxCurveIndex = maxCuveIndex;
	}

	public int getMinCurveIndex() {
		return minCurveIndex;
	}

	public void setMinCurveIndex(int minCurveIndex) {
		this.minCurveIndex = minCurveIndex;
	}

	public LinkedList<IntersectionPoint> getMaxNbrs() {
		return maxNbrs;
	}

	public void setMaxNbrs(LinkedList<IntersectionPoint> maxNbrs) {
		this.maxNbrs = maxNbrs;
	}

	public LinkedList<IntersectionPoint> getMinNbrs() {
		return minNbrs;
	}

	public void setMinNbrs(LinkedList<IntersectionPoint> minNbrs) {
		this.minNbrs = minNbrs;
	}
	
	public int getMaxCurveIndex() {
		return maxCurveIndex;
	}



	public void setMaxCurveIndex(int maxCurveIndex) {
		this.maxCurveIndex = maxCurveIndex;
	}




	@Override
	public String toString() {
		return "HalfedgePoints [point=" + point + ", indexOnMaxCurve="
				+ indexOnMaxCurve + ", indexOnMinCurve=" + indexOnMinCurve
				+ ", maxCurveIndex=" + maxCurveIndex + ", minCurveIndex="
				+ minCurveIndex + ", maxNbrs=" + maxNbrs + ", minNbrs="
				+ minNbrs + "]";
	}
	
	

	
}
