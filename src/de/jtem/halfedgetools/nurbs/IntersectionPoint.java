package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;
import java.util.LinkedList;


public class IntersectionPoint {
	
	protected double[] point = null;
	
	protected int indexOnMaxCurve = Integer.MIN_VALUE;
	protected int maxCurveIndex = Integer.MIN_VALUE;
	protected double[] indexCoordOnMaxCurve = null;
	
	protected int indexOnMinCurve = Integer.MIN_VALUE;
	protected int minCurveIndex = Integer.MIN_VALUE;
	protected double[] indexCoordOnMinCurve = null;
	
	protected int indexOnFirstBoundaryCurve = Integer.MIN_VALUE;
	protected int firstBoundaryCurveIndex = Integer.MIN_VALUE;
	protected double[] indexCoordOnFirstBoundaryCurve = null;
	
	protected int indexOnSecondBoundaryCurve = Integer.MIN_VALUE;
	protected int secondBoundaryCurveIndex = Integer.MIN_VALUE;
	protected double[] indexCoordOnSecondBoundaryCurve = null;
	
	protected double sameIndexDist;
	protected HalfedgePoint parentHP = null;
	
	protected int indexOnFirstCurve = Integer.MIN_VALUE;
	protected int indexOnSecondCurve = Integer.MIN_VALUE;
	
	protected LinkedList<LineSegmentIntersection> intersectingSegments;
	protected LinkedList<int[]> curveIndexList;
	

	public IntersectionPoint() {

	}
	
	public int getIndexOnFirstCurve() {
		return indexOnFirstCurve;
	}

	public void setIndexOnFirstCurve(int indexOnFirstCurve) {
		this.indexOnFirstCurve = indexOnFirstCurve;
	}

	public int getIndexOnSecondCurve() {
		return indexOnSecondCurve;
	}

	public void setIndexOnSecondCurve(int indexOnSecondCurve) {
		this.indexOnSecondCurve = indexOnSecondCurve;
	}

	public double[] getPoint() {
		return point;
	}

	public void setPoint(double[] point) {
		this.point = point;
	}

	public int getIndexOnMaxCurve() {
		return indexOnMaxCurve;
	}

	public void setIndexOnMaxCurve(int indexOnMaxCurve) {
		this.indexOnMaxCurve = indexOnMaxCurve;
	}

	public int getMaxCurveIndex() {
		return maxCurveIndex;
	}

	public void setMaxCurveIndex(int maxCurveIndex) {
		this.maxCurveIndex = maxCurveIndex;
	}

	public double[] getIndexCoordOnMaxCurve() {
		return indexCoordOnMaxCurve;
	}

	public void setIndexCoordOnMaxCurve(double[] indexCoordOnMaxCurve) {
		this.indexCoordOnMaxCurve = indexCoordOnMaxCurve;
	}

	public int getIndexOnMinCurve() {
		return indexOnMinCurve;
	}

	public void setIndexOnMinCurve(int indexOnMinCurve) {
		this.indexOnMinCurve = indexOnMinCurve;
	}

	public int getMinCurveIndex() {
		return minCurveIndex;
	}

	public void setMinCurveIndex(int minCurveIndex) {
		this.minCurveIndex = minCurveIndex;
	}

	public double[] getIndexCoordOnMinCurve() {
		return indexCoordOnMinCurve;
	}

	public void setIndexCoordOnMinCurve(double[] indexCoordOnMinCurve) {
		this.indexCoordOnMinCurve = indexCoordOnMinCurve;
	}

	public int getIndexOnFirstBoundaryCurve() {
		return indexOnFirstBoundaryCurve;
	}

	public void setIndexOnFirstBoundaryCurve(int indexOnFirstBoundaryCurve) {
		this.indexOnFirstBoundaryCurve = indexOnFirstBoundaryCurve;
	}

	public int getFirstBoundaryCurveIndex() {
		return firstBoundaryCurveIndex;
	}



	public void setFirstBoundaryCurveIndex(int firstBoundaryCurveIndex) {
		this.firstBoundaryCurveIndex = firstBoundaryCurveIndex;
	}



	public double[] getIndexCoordOnFirstBoundaryCurve() {
		return indexCoordOnFirstBoundaryCurve;
	}



	public void setIndexCoordOnFirstBoundaryCurve(
			double[] indexCoordOnFirstBoundaryCurve) {
		this.indexCoordOnFirstBoundaryCurve = indexCoordOnFirstBoundaryCurve;
	}



	public int getIndexOnSecondBoundaryCurve() {
		return indexOnSecondBoundaryCurve;
	}



	public void setIndexOnSecondBoundaryCurve(int indexOnSecondBoundaryCurve) {
		this.indexOnSecondBoundaryCurve = indexOnSecondBoundaryCurve;
	}



	public int getSecondBoundaryCurveIndex() {
		return secondBoundaryCurveIndex;
	}



	public void setSecondBoundaryCurveIndex(int secondBoundaryCurveIndex) {
		this.secondBoundaryCurveIndex = secondBoundaryCurveIndex;
	}



	public double[] getIndexCoordOnSecondBoundaryCurve() {
		return indexCoordOnSecondBoundaryCurve;
	}



	public void setIndexCoordOnSecondBoundaryCurve(
			double[] indexCoordOnSecondBoundaryCurve) {
		this.indexCoordOnSecondBoundaryCurve = indexCoordOnSecondBoundaryCurve;
	}



	public double getSameIndexDist() {
		return sameIndexDist;
	}



	public void setSameIndexDist(double sameIndexDist) {
		this.sameIndexDist = sameIndexDist;
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
		return "IntersectionPoint [point=" + Arrays.toString(point)
				+ ", indexOnMaxCurve=" + indexOnMaxCurve + ", maxCurveIndex="
				+ maxCurveIndex + ", indexCoordOnMaxCurve="
				+ Arrays.toString(indexCoordOnMaxCurve) + ", indexOnMinCurve="
				+ indexOnMinCurve + ", minCurveIndex=" + minCurveIndex
				+ ", indexCoordOnMinCurve="
				+ Arrays.toString(indexCoordOnMinCurve)
				+ ", indexOnFirstBoundaryCurve=" + indexOnFirstBoundaryCurve
				+ ", firstBoundaryCurveIndex=" + firstBoundaryCurveIndex
				+ ", indexCoordOnFirstBoundaryCurve="
				+ Arrays.toString(indexCoordOnFirstBoundaryCurve)
				+ ", indexOnSecondBoundaryCurve=" + indexOnSecondBoundaryCurve
				+ ", secondBoundaryCurveIndex=" + secondBoundaryCurveIndex
				+ ", indexCoordOnSecondBoundaryCurve="
				+ Arrays.toString(indexCoordOnSecondBoundaryCurve)
				+ ", sameIndexDist=" + sameIndexDist// + ", parentHP=" + parentHP
				+ ", indexOnFirstCurve=" + indexOnFirstCurve
				+ ", indexOnSecondCurve=" + indexOnSecondCurve + "]";
	}



	



	


	



	
	

}
