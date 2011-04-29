package de.jtem.halfedgetools.nurbs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;

import de.jreality.math.Rn;



public class LineSegmentIntersection {
	
	protected double[][] segment;
	protected int index;
	
	public LineSegmentIntersection(){
		
	}
	
	public LineSegmentIntersection(double[][] s , int i){
		segment = s;
		index = i;
	}
	
	public double[][] getSegment() {
		return segment;
	}

	public void setSegment(double[][] segment) {
		this.segment = segment;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	/*
	 * use homogeneous coords to find orientation
	 */
	public static boolean counterClockWiseOrder(double[] a, double[] b, double[] c){
//		double ccw = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
		double ccw = c[0] * (a[1] -   b[1]) + c[1] * (b[0] -   a[0]) + a[0] * b[1] - a[1] * b[0];
		if(ccw > 0){
			return true;
		}
		return false;
	}
	
	/*
	 * this constillation us used
	 * 
	 *   a     c
	 *    \   /
	 *     
	 *    /   \
	 *   d     b 
	 */
	public static boolean interchangedEndpoints(double[] a, double[] b, double[] c, double[]d, int i){
		if(a[i] <= c[i] && d[i] <= b[i]){
			return true;
		}
		return false;
	}
	
	
	/*
	 * 
	 */
	public static boolean twoSegmentIntersection(double[] p1, double[] p2, double[] p3, double[] p4){
		if(Rn.equals(p1, p3) || Rn.equals(p1, p4) || Rn.equals(p2, p3) || Rn.equals(p2, p4)){
			return true;
		}
		if(LineSegmentIntersection.counterClockWiseOrder(p1, p3, p4) == LineSegmentIntersection.counterClockWiseOrder(p2, p3, p4)){
			return false;
		}
		else if(LineSegmentIntersection.counterClockWiseOrder(p1, p2, p3) == LineSegmentIntersection.counterClockWiseOrder(p1, p2, p4)){
			return false;
		}			
		if(interchangedEndpoints(p1, p2, p3, p4, 0) && interchangedEndpoints(p2, p1, p3, p4, 1)){
			return true;
		}
		else if(interchangedEndpoints(p1, p2, p4, p3, 0) && interchangedEndpoints(p2, p1, p4, p3, 1)){
			return true;
		}
		else if(interchangedEndpoints(p2, p1, p3, p4, 0) && interchangedEndpoints(p1, p2, p3, p4, 1)){
			return true;
		}
		else if(interchangedEndpoints(p2, p1, p4, p3, 0) && interchangedEndpoints(p1, p2, p4, p3, 1)){
			return true;
		}
		else if(interchangedEndpoints(p3, p4, p1, p2, 0) && interchangedEndpoints(p4, p3, p1, p2, 1)){
			return true;
		}
		else if(interchangedEndpoints(p3, p4, p2, p1, 0) && interchangedEndpoints(p4, p3, p2, p1, 1)){
			return true;
		}
		else if(interchangedEndpoints(p4, p3, p1, p2, 0) && interchangedEndpoints(p3, p4, p1, p2, 1)){
			return true;
		}
		else if(interchangedEndpoints(p4, p3, p2, p1, 0) && interchangedEndpoints(p3, p4, p2, p1, 1)){
			return true;
		}
		else{
			return false;
		}	
	}
	

	public static LinkedList<LineSegmentIntersection>bruteForceIntersection(LinkedList<LineSegmentIntersection> segments){
		LinkedList<LineSegmentIntersection> intersections = new LinkedList<LineSegmentIntersection>();
		LinkedList<Integer> indexList = new LinkedList<Integer>();
		for (LineSegmentIntersection seg1 : segments) {
			for (LineSegmentIntersection seg2 : segments) {
				
					if(Math.abs(seg1.index - seg2.index) > 1 &&
						LineSegmentIntersection.twoSegmentIntersection(seg1.segment[0], seg1.segment[1], seg2.segment[0], seg2.segment[1])){
						boolean isInList = false;
						for (Integer id : indexList) {
							if(id == seg2.index){
								isInList = true;
							}
						}
						if(!isInList){
							indexList.add(seg1.index);
							intersections.add(seg1);
						}
					}
			}
		}
		LinkedList<LineSegmentIntersection> returnIntersections = new LinkedList<LineSegmentIntersection>();
		for (LineSegmentIntersection inter1 : intersections) {
			int counter = 0;
			for (LineSegmentIntersection  interReturn: returnIntersections) {
				if(Rn.equals(inter1.segment[0],interReturn.segment[0]) || Rn.equals(inter1.segment[0],interReturn.segment[1]) 
						|| Rn.equals(inter1.segment[1],interReturn.segment[0]) || Rn.equals(inter1.segment[1],interReturn.segment[1]) ){
					counter++;
				}
			
			}
			if(counter < 2){
				returnIntersections.add(inter1);
			}
		}
		return returnIntersections;
	}
	
	


	@Override
	public String toString() {
		return "LineSegmentIntersection [segment=" + Arrays.toString(segment[0]) + " " + Arrays.toString(segment[1])
				+ ", index=" + index + "]";
	}

	public static Set<double[]> planeSweepAlgorithm(){
		return null;
	}
	
	public static void main(String[] args){
		double[] a = {0.041202886238466685, 0.041202886238466685}; 
		double[] b = {0.04049577945728014, 0.04049577945728014};
		double[] c = {0.49445833297905134, 0.4944583329790515}; 
		double[] d = {0.49516543976023786, 0.4951654397602381}; 
		System.out.println(LineSegmentIntersection.twoSegmentIntersection(a, b, c, d));
	}

}
