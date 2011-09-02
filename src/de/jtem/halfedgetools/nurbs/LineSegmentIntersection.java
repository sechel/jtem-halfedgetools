package de.jtem.halfedgetools.nurbs;

import java.beans.EventSetDescriptor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import com.sun.corba.se.internal.Interceptors.PIORB;

import de.jreality.math.Rn;
import de.jtem.jrworkspace.junitUtils.NbPlugin;

public class LineSegmentIntersection {
	
	protected double[][] segment;
	protected int indexOnCurve = Integer.MIN_VALUE;
	protected int curveIndex = Integer.MIN_VALUE;
	protected LinkedList<double[]> ePoints;
	boolean max;
	
	public LineSegmentIntersection(){
		
	}
	
	public LineSegmentIntersection(double[][] s , int iOC,int cI,  boolean m){
		segment = s;
		indexOnCurve = iOC;
		curveIndex = cI;
		max = m;
	}
	
	
	


	
	public LinkedList<double[]> getePoints() {
		return ePoints;
	}

	public void setePoints(LinkedList<double[]> ePoints) {
		this.ePoints = ePoints;
	}

	public double[][] getSegment() {
		return segment;
	}

	public void setSegment(double[][] segment) {
		this.segment = segment;
	}

	public int getIndexOnCurve() {
		return indexOnCurve;
	}

	public void setIndexOnCurve(int indexOnCurve) {
		this.indexOnCurve = indexOnCurve;
	}

	public int getCurveIndex() {
		return curveIndex;
	}

	public void setCurveIndex(int curveIndex) {
		this.curveIndex = curveIndex;
	}

	public boolean isMax() {
		return max;
	}

	public void setMax(boolean max) {
		this.max = max;
	}
	


	/*
	 * returns true iff c lies on the lefthand side of the line from a to b
	 */
	public static boolean orientation(double[] a, double[] b, double[] c){
		return (c[0] * (a[1] - b[1]) + c[1] * (b[0] - a[0]) + a[0] * b[1] - a[1] * b[0] > 0);
	}

	/*
	 * use homogeneous coords to find orientation
	 */
	public static boolean counterClockWiseOrder(double[] a, double[] b, double[] c){
//		double ccw = (b[0] - a[0]) * (c[1] - a[1]) - (b[1] - a[1]) * (c[0] - a[0]);
		double ccw = c[0] * (a[1] -   b[1]) + c[1] * (b[0] -   a[0]) + a[0] * b[1] - a[1] * b[0];
		if(ccw >= 0){
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
	
	public static double[] intersectionPoint(LineSegmentIntersection first, LineSegmentIntersection second){
		double s1 = first.segment[0][0];
		double s2 = first.segment[0][1];
		double t1 = first.segment[1][0];
		double t2 = first.segment[1][1];
		double p1 = second.segment[0][0];
		double p2 = second.segment[0][1];
		double q1 = second.segment[1][0];
		double q2 = second.segment[1][1];
		double lambda = ((p1 - s1) * (s2 - t2) - (p2 - s2) * (s1 - t1)) / ((q2 - p2) * (s1 - t1) - (q1 - p1) * (s2 - t2));
		return Rn.add(null, second.segment[0],Rn.times(null, lambda, Rn.add(null, second.segment[1], Rn.times(null, -1, second.segment[0]))));
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
	
//	public static boolean twoSegmentIntersection(double[] p1, double[] p2, double[] p3, double[] p4){
//		if(LineSegmentIntersection.counterClockWiseOrder(p1, p3, p4) == LineSegmentIntersection.counterClockWiseOrder(p2, p3, p4)){
//			return false;
//		}
//		else if(LineSegmentIntersection.counterClockWiseOrder(p1, p2, p3) == LineSegmentIntersection.counterClockWiseOrder(p1, p2, p4)){
//			return false;
//		}else{			
//			return true;
//		}
//	}
	
	
	

	
	public static LinkedList<IntersectionPoint> bruteForceCurveCurveIntersection(LinkedList<LineSegmentIntersection> segments){
		LinkedList<IntersectionPoint> intersections = new LinkedList<IntersectionPoint>();
		int seg1Counter = -100;
		int seg2Counter = -100;
		for (LineSegmentIntersection seg1 : segments) {
			for (LineSegmentIntersection seg2 : segments) {
				if(seg1.max && !seg2.max){
					if(Math.abs(seg1Counter - seg1.indexOnCurve) != 2 && seg2Counter != seg2.indexOnCurve && Math.abs(seg2Counter - seg2.indexOnCurve) != 2){
						if(	LineSegmentIntersection.twoSegmentIntersection(seg1.segment[0], seg1.segment[1], seg2.segment[0], seg2.segment[1])
						){
							IntersectionPoint p = new IntersectionPoint();
							p.point = LineSegmentIntersection.intersectionPoint(seg1, seg2);
							p.maxCurveIndex = seg1.curveIndex;
							p.indexOnMaxCurve = seg1.indexOnCurve;
							p.minCurveIndex = seg2.curveIndex;
							p.indexOnMinCurve = seg2.indexOnCurve;
							p.indexCoordOnMaxCurve = seg1.segment[0];
							p.indexCoordOnMinCurve = seg2.segment[0];
							intersections.add(p);
						}
					}
				}
				seg2Counter = seg2.indexOnCurve;
			}
			seg1Counter = seg1.indexOnCurve;
		}
		return intersections;
	}
	
	public static LinkedList<IntersectionPoint> bruteForceBoundaryCurveIntersection(LinkedList<LineSegmentIntersection> boundarySegments, LinkedList<LineSegmentIntersection> segments){
		LinkedList<IntersectionPoint> intersections = new LinkedList<IntersectionPoint>();
		for (LineSegmentIntersection boundarySegment :boundarySegments) {
			for (LineSegmentIntersection curveSegment : segments) {
				if(LineSegmentIntersection.twoSegmentIntersection(boundarySegment.segment[0], boundarySegment.segment[1], curveSegment.segment[0], curveSegment.segment[1])){
					IntersectionPoint p = new IntersectionPoint();
					p.point = LineSegmentIntersection.intersectionPoint(boundarySegment, curveSegment);
					if(curveSegment.max){
						p.maxCurveIndex = curveSegment.curveIndex;
						p.indexOnMaxCurve = curveSegment.indexOnCurve;
						p.indexCoordOnMaxCurve = curveSegment.segment[0];
					}
					if(!curveSegment.max){
						p.minCurveIndex = curveSegment.curveIndex;
						p.indexOnMinCurve = curveSegment.indexOnCurve;
						p.indexCoordOnMinCurve = curveSegment.segment[0];
					}
					p.firstBoundaryCurveIndex = boundarySegment.curveIndex;
					p.indexOnFirstBoundaryCurve = boundarySegment.indexOnCurve;
					p.indexCoordOnFirstBoundaryCurve = boundarySegment.segment[0];
					
					p.secondBoundaryCurveIndex = boundarySegment.curveIndex;
					p.indexOnSecondBoundaryCurve = boundarySegment.indexOnCurve;
					p.indexCoordOnSecondBoundaryCurve = boundarySegment.segment[0];
					
					intersections.add(p);
				}
			}
		}
		return intersections;
	}
	/*
	 * this holds only true for trivial boundaries!!
	 */
	public static LinkedList<IntersectionPoint> bruteForceBoundaryBoundaryIntersection(LinkedList<LineSegmentIntersection> boundarySegments){
		LinkedList<IntersectionPoint> intersections = new LinkedList<IntersectionPoint>();
		for (LineSegmentIntersection b1 : boundarySegments) {
			for (LineSegmentIntersection b2 : boundarySegments) {
				if(b2.curveIndex - b1.curveIndex == 1 || (b1.curveIndex == boundarySegments.size() && b2.curveIndex == 1)){
					if(LineSegmentIntersection.twoSegmentIntersection(b1.segment[0], b1.segment[1], b2.segment[0], b2.segment[1])){
						IntersectionPoint p = new IntersectionPoint();
						p.point = LineSegmentIntersection.intersectionPoint(b1, b2);
						p.firstBoundaryCurveIndex = b1.curveIndex;
						p.indexOnFirstBoundaryCurve = b1.indexOnCurve;
						p.indexCoordOnFirstBoundaryCurve = b1.segment[0];
						p.secondBoundaryCurveIndex = b2.curveIndex;
						p.indexOnSecondBoundaryCurve = b2.indexOnCurve;
						p.indexCoordOnSecondBoundaryCurve = b2.segment[0];
						intersections.add(p);
					}
				}
			}
			
		}
		return intersections;
	}
	/*
	 * this method computes all nbrs (intersections) of a given intersection point P
	 * it uses the fact that all discrete lines consists of indexed line segments
	 * it returns for both lines the (if possible) both intersection points  P1 (indexP1 < indexP), P2 (indexP2 > indexP) that suffices the condition: 
	 * indexP - indexP1 is the minimum and indexP2 - indexP is the minimum of all intersection points on this line
	 */
	private static LinkedList<IntersectionPoint> sortSameIndex(LinkedList<IntersectionPoint> sameIndex, boolean max){
		if(max){
			for (IntersectionPoint iP : sameIndex) {
				iP.sameIndexDist = Rn.euclideanDistance(iP.point, iP.indexCoordOnMaxCurve);
			}
		}
		else{
			for (IntersectionPoint iP : sameIndex) {
				iP.sameIndexDist = Rn.euclideanDistance(iP.point, iP.indexCoordOnMinCurve);
			}
		}
		Collections.sort(sameIndex, new IntersectionPointDistanceComparator());
		return sameIndex;
	}
	

	
	public static LinkedList<HalfedgePoint> findAllNbrs(LinkedList<IntersectionPoint> intersectionPoints){
		System.out.println("IntersectionPoint list");
		for (IntersectionPoint iP : intersectionPoints) {
			System.out.println(iP.toString());
		}
		Map<IntersectionPoint, Integer> maxCurveMap = new HashMap<IntersectionPoint, Integer>();
		Map<IntersectionPoint, Integer> minCurveMap = new HashMap<IntersectionPoint, Integer>();
		LinkedList<HalfedgePoint> points = new LinkedList<HalfedgePoint>();
		// create a list of HalfedgePoints knowing all intersections on maxCurve and minCurve
		for (IntersectionPoint iP1 : intersectionPoints) {
			LinkedList<IntersectionPoint> maxLinePoints = new LinkedList<IntersectionPoint>();
			LinkedList<IntersectionPoint> minLinePoints = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP2 : intersectionPoints) {

				if(iP1.maxCurveIndex == iP2.maxCurveIndex && iP1.maxCurveIndex >= -1){
					maxLinePoints.add(iP2);
				}
				if(iP1.minCurveIndex == iP2.minCurveIndex && iP1.minCurveIndex >= -1){
					minLinePoints.add(iP2);
				}
			}
			// on maximal lines
			Collections.sort(maxLinePoints, new IntersectionPointMaxIndexComparator());
			System.out.println("maxLinePoints " + maxLinePoints.toString());
			LinkedList<LinkedList<IntersectionPoint>> maxHelp = new LinkedList<LinkedList<IntersectionPoint>>();
			int beforeMax = -1;
			for (IntersectionPoint iP : maxLinePoints) {
				if(iP.indexOnMaxCurve != beforeMax){
					maxHelp.add(new LinkedList<IntersectionPoint>());
				}
				beforeMax = iP.indexOnMaxCurve;
				maxHelp.getLast().add(iP);
			}
			System.out.println("maxhelp.size "+ maxHelp.size());
			for (LinkedList<IntersectionPoint> sameList : maxHelp) {
				if(sameList.size() > 1){
					LineSegmentIntersection.sortSameIndex(sameList, true);
				}
			}
			LinkedList<IntersectionPoint> maxMapList = new LinkedList<IntersectionPoint>();
			for (LinkedList<IntersectionPoint> list : maxHelp) {
				maxMapList.addAll(list);
			}
			int iMax = 0;
			for (IntersectionPoint iP : maxMapList) {
				maxCurveMap.put(iP, iMax);
				iMax++;
			}
			// on minimal lines
			Collections.sort(minLinePoints, new IntersectionPointMinIndexComparator());
			LinkedList<LinkedList<IntersectionPoint>> minHelp = new LinkedList<LinkedList<IntersectionPoint>>();
			int beforeMin = -1;
			for (IntersectionPoint iP : minLinePoints) {
				if(iP.indexOnMinCurve != beforeMin){
					minHelp.add(new LinkedList<IntersectionPoint>());
				}
				beforeMin = iP.indexOnMinCurve;
				minHelp.getLast().add(iP);
			}
			for (LinkedList<IntersectionPoint> sameList : minHelp) {
				if(sameList.size() > 1){
					LineSegmentIntersection.sortSameIndex(sameList, false);
				}
			}
			LinkedList<IntersectionPoint> minMapList = new LinkedList<IntersectionPoint>();
			for (LinkedList<IntersectionPoint> list : minHelp) {
				minMapList.addAll(list);
			}
			int iMin = 0;
			for (IntersectionPoint iP : minMapList) {
				minCurveMap.put(iP, iMin);
				iMin++;
			}
		}
		for (IntersectionPoint iP : intersectionPoints) {
			if(iP.indexOnMaxCurve >= -1){
				iP.indexOnMaxCurve = maxCurveMap.get(iP);
			}
			if(iP.indexOnMinCurve >= -1){
				iP.indexOnMinCurve = minCurveMap.get(iP);
			}
		}
		for (IntersectionPoint iP1 : intersectionPoints) {
			LinkedList<IntersectionPoint> maxLinePoints = new LinkedList<IntersectionPoint>();
			LinkedList<IntersectionPoint> minLinePoints = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP2 : intersectionPoints){
				if(iP1.maxCurveIndex == iP2.maxCurveIndex && Math.abs(iP1.indexOnMaxCurve - iP2.indexOnMaxCurve) == 1){
					maxLinePoints.add(iP2);
				}
				if(iP1.minCurveIndex == iP2.minCurveIndex && Math.abs(iP1.indexOnMinCurve - iP2.indexOnMinCurve) == 1){
					minLinePoints.add(iP2);
				}
			}
			HalfedgePoint p = new HalfedgePoint(iP1, iP1.indexOnMaxCurve, iP1.indexOnMinCurve, iP1.maxCurveIndex, iP1.minCurveIndex, maxLinePoints, minLinePoints);
			iP1.setParentHP(p);
			points.add(p);
		}
		return points;
	}
	
	public static LinkedList<HalfedgePoint> findAllNbrs1(LinkedList<IntersectionPoint> intersectionPoints){
		LinkedList<HalfedgePoint> points = new LinkedList<HalfedgePoint>();
		for (IntersectionPoint iP1 : intersectionPoints) {
			LinkedList<IndexedCurveList> iP1CurveList = new LinkedList<IndexedCurveList>();
			LinkedList<Integer> indexList = getIndexListFromIntersectionPoint(iP1);
			
	
			// add for each curve intersecting this intersectionPoint all IntersectionPoints contained in this curve

			for (Integer i : indexList){
				IndexedCurveList icl = new IndexedCurveList(i, new LinkedList<IntersectionPoint>());
				iP1CurveList.add(icl);
				for (IntersectionPoint iP2 : intersectionPoints) {
					int indexOnCurve = getIndexOnCurveFromCurveIndexAndIntersectionPoint(i, iP2);
					if(indexOnCurve != 0 && !icl.curveList.contains(iP2)){
						icl.curveList.add(iP2);
					}
				}
			}
			LinkedList<IntersectionPoint> nbrs = new LinkedList<IntersectionPoint>();
			
			for (IndexedCurveList icl : iP1CurveList) {
				
				
				
				// sorting each curveList w.r.t. indexOnCurve
				
				IntersectionPointIndexComparator ipic = new IntersectionPointIndexComparator();
//				System.out.println("index " +icl.index);
				ipic.curveIndex = icl.index;
//				System.out.println(icl.curveList.size());
				Collections.sort(icl.curveList, ipic);
//				System.out.println("point "+Arrays.toString(iP1.point) +" curve index " + icl.index);
				for (IntersectionPoint iP : icl.curveList) {
//					System.out.println(Arrays.toString(iP.point));
					for (LineSegmentIntersection lsi: iP.intersectingSegments) {
						if(lsi.curveIndex == icl.index){
	//						System.out.println("curveIndex"+lsi.curveIndex+" indexOnCurve "+lsi.indexOnCurve);
						}
					}
				}
				
				// add for each indexOnCurve all IntersectionPoints with same index in a list
				
				LinkedList<LinkedList<IntersectionPoint>> indexOrderList = new LinkedList<LinkedList<IntersectionPoint>>();
				int before = -1;
				for (IntersectionPoint iP : icl.curveList) {
					int indexOnCurve = getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.index, iP);
					if(indexOnCurve != before){
						indexOrderList.add(new LinkedList<IntersectionPoint>());
					}
					before = indexOnCurve;
					indexOrderList.getLast().add(iP);
				}
				
				// sort all same indexed IntersectionPoints w.r.t. euclidian distance
				
				for (LinkedList<IntersectionPoint> sameList : indexOrderList) {
					if(sameList.size() > 1){
						sortSameIndex(sameList, icl.index);
					}
				}
				
				// get back the original list in order
				
				LinkedList<IntersectionPoint> mapList = new LinkedList<IntersectionPoint>();
				for (LinkedList<IntersectionPoint> list : indexOrderList) {
					mapList.addAll(list);
				}
				
				// fill the map
				
				int i = 0;
				Map<IntersectionPoint, Integer> map = new HashMap<IntersectionPoint, Integer>();
				Map<Integer, IntersectionPoint> inverseMap = new HashMap<Integer, IntersectionPoint>();
				for (IntersectionPoint iP : mapList) {
					i++;
					map.put(iP, i);
					inverseMap.put(i, iP);
					
				}
				
				// get both (if possible) nbrs on this curve
				
				int index = map.get(iP1);
				if(index > 1){
					nbrs.add(inverseMap.get(index - 1));
				}
				if(index < mapList.size()){
					nbrs.add(inverseMap.get(index + 1));
				}
			}
			HalfedgePoint hp = new HalfedgePoint(iP1, nbrs);
			iP1.setParentHP(hp);
			points.add(hp);
		}
//		System.out.println("length " + points.size());
//		for (HalfedgePoint hP : points) {
//			System.out.println("hp " + Arrays.toString(hP.point.point));
//			System.out.println("nbrs:");
//			for (IntersectionPoint iP : hP.nbrs) {
//				System.out.println(Arrays.toString(iP.point));
//			}
//		}
		return points;
	}
	
	private static int getIndexOnCurveFromCurveIndexAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
		int result = 0;
		for (LineSegmentIntersection seg : iP.intersectingSegments) {
			if(seg.curveIndex == curveIndex && result < seg.indexOnCurve){
				result = seg.indexOnCurve;
			}
		}
		return result;
	}
	
	private static double[] getFirstSegmentCoordsFromCurveIndexAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
		for (LineSegmentIntersection seg : iP.intersectingSegments) {
			if(seg.curveIndex == curveIndex){
				return seg.segment[0];
			}
		}
		return null;
	}
	
	private static LinkedList<IntersectionPoint> sortSameIndex(LinkedList<IntersectionPoint> sameIndex, int curveIndex){
		for (IntersectionPoint iP : sameIndex) {
			double[] firstCoordFromIndexedSegment = getFirstSegmentCoordsFromCurveIndexAndIntersectionPoint(curveIndex, iP);
			iP.sameIndexDist = Rn.euclideanDistance(iP.point, firstCoordFromIndexedSegment);
		}
		Collections.sort(sameIndex, new IntersectionPointDistanceComparator());
		return sameIndex;
	}
	

	
	private static LinkedList<Integer> getIndexListFromIntersectionPoint(IntersectionPoint iP){
		 LinkedList<Integer> indexList = new LinkedList<Integer>();
		 for (LineSegmentIntersection seg : iP.intersectingSegments) {
			if(!indexList.contains(seg.curveIndex)){
				indexList.add(seg.curveIndex);
			}
		}
		return indexList;
	}
	
	
	/*
	 * returns a list of oriented nbrs  !!and the last element equals first!!
	 */
	public static LinkedList<HalfedgePoint> orientedNbrs (LinkedList<HalfedgePoint> halfPoints){
		for (HalfedgePoint hP : halfPoints) {
			LinkedList<IntersectionPoint> orientedList = new LinkedList<IntersectionPoint>();
			hP.minNbrs.addAll(hP.maxNbrs);
			LinkedList<IntersectionPoint> allNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.minNbrs) {
				if(iP.point != null){
					allNbrs.add(iP);
				}
			}
			orientedList.add(allNbrs.getFirst());
			int bound = allNbrs.size();
			IntersectionPoint lastOrientedPoint = orientedList.getLast();
			boolean sameNbr = false;
			while(orientedList.size() < bound){
				double angle = Double.MAX_VALUE;
				boolean leftOrientation = false;
				IntersectionPoint next = new IntersectionPoint();
				IntersectionPoint before = allNbrs.getFirst();
				if(orientedList.getLast() == lastOrientedPoint && orientedList.size() > 1){
					sameNbr = true;
				}
				boolean firstPoint = true;
				for (IntersectionPoint nonOrientedNbr : allNbrs) {
					if(orientedList.getLast() == before && before == nonOrientedNbr && firstPoint == false && !sameNbr){
						next = nonOrientedNbr;
						angle = 0;
						leftOrientation = true;
						sameNbr = true;
					}
					firstPoint = false;
					before = nonOrientedNbr;
					double[] first = orientedList.getLast().point;
					double[] middle = hP.point.point;
					double[] last = nonOrientedNbr.point;
					double[] a = Rn.normalize(null, Rn.subtract(null, first, middle));
					double[] b = Rn.normalize(null, Rn.subtract(null, last, middle));
					if(first != last && allNbrs.size() > 1 && LineSegmentIntersection.orientation(first, middle, last)){
						leftOrientation = true;
						if(angle > Math.acos(Rn.innerProduct(a, b))){
							angle = Math.acos(Rn.innerProduct(a, b));
							next = nonOrientedNbr;
						}
					}
				}
				if(!leftOrientation){
					angle = Double.MIN_VALUE;

					for (IntersectionPoint nonOrientedNbr : allNbrs) {
						double[] first = orientedList.getLast().point;
						double[] middle = hP.point.point;
						double[] last = nonOrientedNbr.point;;
						double[] a = Rn.normalize(null, Rn.subtract(null, first, middle));
						double[] b = Rn.normalize(null, Rn.subtract(null, last, middle));
						if(first != last && allNbrs.size() > 1){
							if(angle < Math.acos(Rn.innerProduct(a, b))){
								angle = Math.acos(Rn.innerProduct(a, b));
								next = nonOrientedNbr;
							}
						}
					}
				}
				lastOrientedPoint = orientedList.getLast();
				orientedList.add(next);
			}
			System.out.println("Test Knoten");
			System.out.println(Arrays.toString(hP.point.point));
			System.out.println("nachbarn");
			for (IntersectionPoint iP : orientedList) {
				System.out.println(Arrays.toString(iP.point));
			}
			System.out.println();
			IntersectionPoint before = null;
			LinkedList<IntersectionPoint> ori = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : orientedList) {
				if(before != iP){
					ori.add(iP);
				}
				before = iP;
			}
			if(ori.getLast() == ori.getFirst() && ori.size() > 1){
				ori.pollLast();
			}
			ori.add(ori.getFirst());
			hP.minNbrs = ori;
		}
		
		return halfPoints;
	}
	
	public static LinkedList<HalfedgePoint> orientedNbrs1 (LinkedList<HalfedgePoint> halfPoints){
		for (HalfedgePoint hP : halfPoints) {
			LinkedList<IntersectionPoint> orientedList = new LinkedList<IntersectionPoint>();
			LinkedList<IntersectionPoint> allNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.nbrs) {
				if(iP.point != null){
					allNbrs.add(iP);
				}
			}
			orientedList.add(allNbrs.getFirst());
			int bound = allNbrs.size();
			IntersectionPoint lastOrientedPoint = orientedList.getLast();
			boolean sameNbr = false;
			while(orientedList.size() < bound){
				double angle = Double.MAX_VALUE;
				boolean leftOrientation = false;
				IntersectionPoint next = new IntersectionPoint();
				IntersectionPoint before = allNbrs.getFirst();
				if(orientedList.getLast() == lastOrientedPoint && orientedList.size() > 1){
					sameNbr = true;
				}
				boolean firstPoint = true;
				for (IntersectionPoint nonOrientedNbr : allNbrs) {
					if(orientedList.getLast() == before && before == nonOrientedNbr && firstPoint == false && !sameNbr){
						next = nonOrientedNbr;
						angle = 0;
						leftOrientation = true;
						sameNbr = true;
					}
					firstPoint = false;
					before = nonOrientedNbr;
					double[] first = orientedList.getLast().point;
					double[] middle = hP.point.point;
					double[] last = nonOrientedNbr.point;
					double[] a = Rn.normalize(null, Rn.subtract(null, first, middle));
					double[] b = Rn.normalize(null, Rn.subtract(null, last, middle));
					if(first != last && allNbrs.size() > 1 && LineSegmentIntersection.orientation(first, middle, last)){
						leftOrientation = true;
						if(angle > Math.acos(Rn.innerProduct(a, b))){
							angle = Math.acos(Rn.innerProduct(a, b));
							next = nonOrientedNbr;
						}
					}
				}
				if(!leftOrientation){
					angle = Double.MIN_VALUE;

					for (IntersectionPoint nonOrientedNbr : allNbrs) {
						double[] first = orientedList.getLast().point;
						double[] middle = hP.point.point;
						double[] last = nonOrientedNbr.point;;
						double[] a = Rn.normalize(null, Rn.subtract(null, first, middle));
						double[] b = Rn.normalize(null, Rn.subtract(null, last, middle));
						if(first != last && allNbrs.size() > 1){
							if(angle < Math.acos(Rn.innerProduct(a, b))){
								angle = Math.acos(Rn.innerProduct(a, b));
								next = nonOrientedNbr;
							}
						}
					}
				}
				lastOrientedPoint = orientedList.getLast();
				orientedList.add(next);
			}
//			System.out.println("Test Knoten");
//			System.out.println(Arrays.toString(hP.point.point));
//			System.out.println("nachbarn");
			for (IntersectionPoint iP : orientedList) {
//				System.out.println(Arrays.toString(iP.point));
			}
//			System.out.println();
			IntersectionPoint before = null;
			LinkedList<IntersectionPoint> ori = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : orientedList) {
				if(before != iP){
					ori.add(iP);
				}
				before = iP;
			}
			if(ori.getLast() == ori.getFirst() && ori.size() > 1){
				ori.pollLast();
			}
			ori.add(ori.getFirst());
			hP.nbrs = ori;
		}
		
		return halfPoints;
	}
	/*
	 * returns the next vertex w.r.t. a face in order
	 */
	
	public static IntersectionPoint getNextNbr(IntersectionPoint before, HalfedgePoint point){
		System.out.println("getNextNbr");
		boolean isEqual = false;
		for (IntersectionPoint iP : point.minNbrs) {
			if(iP.point == before.point && !isEqual){
				isEqual = true;
			}
			else if(isEqual){
				return iP;
			}
		}
		return null;
	}
	
	public static IntersectionPoint getNextNbr1(IntersectionPoint before, HalfedgePoint point){
		boolean isEqual = false;
		for (IntersectionPoint iP : point.nbrs) {
			if(iP.point == before.point && !isEqual){
				isEqual = true;
			}
			else if(isEqual){
				return iP;
			}
		}
		return null;
	}
	
	/*
	 * 
	 */
	public static LinkedList<IntersectionPoint>  allAjacentNbrs(HalfedgePoint point , LinkedList<HalfedgePoint> halfedgePoints){
		LinkedList<IntersectionPoint> allAjacentNbrs = new LinkedList<IntersectionPoint>();
		LinkedList<HalfedgePoint> allNbrs = new LinkedList<HalfedgePoint>();
		// starts with the first element of the NbrList
		IntersectionPoint firstIP = point.maxNbrs.getFirst();
//		IntersectionPoint firstIP = point.nbrs.getFirst();
		HalfedgePoint first = firstIP.getParentHP();
		allNbrs.add(point);
		allAjacentNbrs.add(point.point);
		allNbrs.add(first);
		allAjacentNbrs.add(first.point);
		IntersectionPoint before = point.point;
		HalfedgePoint bP = point;
		
		for (HalfedgePoint halfedgePoint : halfedgePoints) {
			if(bP == halfedgePoint){
	//			System.out.println("before removing "+halfedgePoint.maxNbrs.toString());
				LinkedList<IntersectionPoint> removeStartDirection = new LinkedList<IntersectionPoint>();
					for (IntersectionPoint interP : halfedgePoint.maxNbrs) {
						if(interP.parentHP != allAjacentNbrs.getLast().parentHP){
							removeStartDirection.add(interP);
						}
					}
				halfedgePoint.setMaxNbrs(removeStartDirection);
			}
		}
		while(point != allNbrs.getLast()){
			IntersectionPoint next = getNextNbr(before, allNbrs.getLast());
			before = allAjacentNbrs.getLast();
			bP = before.getParentHP();
			for (HalfedgePoint halfedgePoint : halfedgePoints) {
				if(bP == halfedgePoint){
					LinkedList<IntersectionPoint> removeStartDirection = new LinkedList<IntersectionPoint>();
					for (IntersectionPoint interP : halfedgePoint.maxNbrs) {
						if(interP.parentHP != next.parentHP){
							removeStartDirection.add(interP);
						}
					}
						halfedgePoint.setMaxNbrs(removeStartDirection);
				}
			}
			allAjacentNbrs.add(next);
			HalfedgePoint hP = next.getParentHP();
			allNbrs.add(hP);
		}
		allAjacentNbrs.pollLast();
		for (IntersectionPoint iP : allAjacentNbrs) {
			System.out.println(Arrays.toString(iP.point));
		}
		return allAjacentNbrs;
	}
	
	public static LinkedList<IntersectionPoint>  allAjacentNbrs1(HalfedgePoint point , LinkedList<HalfedgePoint> halfedgePoints){
		LinkedList<IntersectionPoint> allAjacentNbrs = new LinkedList<IntersectionPoint>();
		LinkedList<HalfedgePoint> allNbrs = new LinkedList<HalfedgePoint>();
		// starts with the first element of the NbrList
		IntersectionPoint firstIP = point.maxNbrs.getFirst();
//		IntersectionPoint firstIP = point.nbrs.getFirst();
		HalfedgePoint first = firstIP.getParentHP();
		allNbrs.add(point);
		allAjacentNbrs.add(point.point);
		allNbrs.add(first);
		allAjacentNbrs.add(first.point);
		IntersectionPoint before = point.point;
		HalfedgePoint bP = point;
		
		for (HalfedgePoint halfedgePoint : halfedgePoints) {
			if(bP == halfedgePoint){
	//			System.out.println("before removing "+halfedgePoint.maxNbrs.toString());
				LinkedList<IntersectionPoint> removeStartDirection = new LinkedList<IntersectionPoint>();
					for (IntersectionPoint interP : halfedgePoint.maxNbrs) {
						if(interP.parentHP != allAjacentNbrs.getLast().parentHP){
							removeStartDirection.add(interP);
						}
					}
				halfedgePoint.setMaxNbrs(removeStartDirection);
			}
		}
		while(point != allNbrs.getLast()){
			IntersectionPoint next = getNextNbr1(before, allNbrs.getLast());
			before = allAjacentNbrs.getLast();
			bP = before.getParentHP();
			for (HalfedgePoint halfedgePoint : halfedgePoints) {
				if(bP == halfedgePoint){
					LinkedList<IntersectionPoint> removeStartDirection = new LinkedList<IntersectionPoint>();
					for (IntersectionPoint interP : halfedgePoint.maxNbrs) {
						if(interP.parentHP != next.parentHP){
							removeStartDirection.add(interP);
						}
					}
						halfedgePoint.setMaxNbrs(removeStartDirection);
				}
			}
			allAjacentNbrs.add(next);
			HalfedgePoint hP = next.getParentHP();
			allNbrs.add(hP);
		}
		allAjacentNbrs.pollLast();
		for (IntersectionPoint iP : allAjacentNbrs) {
//			System.out.println(Arrays.toString(iP.point));
		}
		return allAjacentNbrs;
	}
	
	public static FaceSet  createFaceSet(LinkedList<HalfedgePoint> orientedNbrs){
		FaceSet fS = new FaceSet();
		double[][] verts = new double[orientedNbrs.size()][2];
		LinkedList<int[]> faceNbrs = new LinkedList<int[]>();
		int c = 0;

		for (HalfedgePoint hP: orientedNbrs) {
			LinkedList<IntersectionPoint> maxNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.minNbrs) {
				maxNbrs.add(iP);
			}
			maxNbrs.pollLast();
			hP.setMaxNbrs(maxNbrs);
			verts[c] = hP.point.point;
			c++;
		}
		fS.setVerts(verts);
		for (HalfedgePoint hP : orientedNbrs) {
			if(!hP.maxNbrs.isEmpty()){
				System.out.println("out NBRS in ceateFaceSet " + hP.maxNbrs.toString());
				for (IntersectionPoint  outNbrs : hP.maxNbrs) {
					HalfedgePoint start = outNbrs.getParentHP();
					System.out.println("vom startpunkt weg"+ Arrays.toString(start.point.point));
					LinkedList<IntersectionPoint> facePoints = allAjacentNbrs(hP, orientedNbrs);
					LinkedList<Integer> ind = new LinkedList<Integer>();
					for (IntersectionPoint fP : facePoints) {
						for (int i = 0; i < verts.length; i++) {
							if(fP.point == verts[i]){
								ind.add(i);
							}
						}
					}
					int[] index = new int[ind.size()];
					int count = 0;
					for (Integer i : ind) {
						index[count] = i;
						count++;
					}
					faceNbrs.add(index);
				}
			}
		}
	
		int[][] faceIndex = new int[faceNbrs.size()][];
		int counter = 0;
		for (int[] fs : faceNbrs) {
				faceIndex[counter] = fs;
				counter++;
		}
		fS.setFaces(faceIndex);
		return fS;
	}
	
	public static FaceSet  createFaceSet1(LinkedList<HalfedgePoint> orientedNbrs){
		FaceSet fS = new FaceSet();
		double[][] verts = new double[orientedNbrs.size()][2];
		LinkedList<int[]> faceNbrs = new LinkedList<int[]>();
		int c = 0;

		for (HalfedgePoint hP: orientedNbrs) {
			LinkedList<IntersectionPoint> maxNbrs = new LinkedList<IntersectionPoint>();
			for (IntersectionPoint iP : hP.nbrs) {
				maxNbrs.add(iP);
			}
			maxNbrs.pollLast();
			hP.setMaxNbrs(maxNbrs);
			verts[c] = hP.point.point;
			c++;
		}
		fS.setVerts(verts);
		for (HalfedgePoint hP : orientedNbrs) {
			if(!hP.maxNbrs.isEmpty()){
//				System.out.println("out NBRS in ceateFaceSet " + hP.maxNbrs.toString());
				for (IntersectionPoint  outNbrs : hP.maxNbrs) {
					HalfedgePoint start = outNbrs.getParentHP();
//					System.out.println("vom startpunkt weg"+ Arrays.toString(start.point.point));
					LinkedList<IntersectionPoint> facePoints = allAjacentNbrs1(hP, orientedNbrs);
					LinkedList<Integer> ind = new LinkedList<Integer>();
					for (IntersectionPoint fP : facePoints) {
						for (int i = 0; i < verts.length; i++) {
							if(fP.point == verts[i]){
								ind.add(i);
							}
						}
					}
					int[] index = new int[ind.size()];
					int count = 0;
					for (Integer i : ind) {
						index[count] = i;
						count++;
					}
					faceNbrs.add(index);
				}
			}
		}
	
		int[][] faceIndex = new int[faceNbrs.size()][];
		int counter = 0;
		for (int[] fs : faceNbrs) {
				faceIndex[counter] = fs;
				counter++;
		}
		fS.setFaces(faceIndex);
		return fS;
	}
	



	@Override
	public String toString() {
		return //"LineSegmentIntersection [segment=" + Arrays.toString(segment[0]) + " " + Arrays.toString(segment[1])
				//+ ", index=" + indexOnCurve +
				" curveIndex = " +curveIndex+ " indexOnCurve = " + indexOnCurve;
	}
	
	public static enum PointStatus {
		upper,
		containsInterior,
		lower
	}
	
	/*
	 *  plane sweep algorithm from "Computational Geometry"
	 */
	
	public static enum PointStatus1 {
		upper,
		containsInterior,
		lower
	}
	
	public static LinkedList<IntersectionPoint> findIntersections(LinkedList<LineSegmentIntersection> segments){
		LinkedList<IntersectionPoint> interPoints = new LinkedList<IntersectionPoint>();
		LinkedList<double[]> currentIntersections = new LinkedList<double[]>();
		for (LineSegmentIntersection s : segments) {
//			System.out.println("s.segment[0][1] = " + s.segment[0][1] + " s.segment[1][1] = " + s.segment[1][1]);
			if(s.segment[0][1] < s.segment[1][1] || (s.segment[0][1] == s.segment[1][1] && s.segment[0][0] > s.segment[1][0])){
				double[] temp = s.segment[0];
				s.segment[0] = s.segment[1];
				s.segment[1] = temp;
			}
		}
		PriorityQueue<EventPoint> eventPoints = new PriorityQueue<EventPoint>(3 * segments.size(), new EventPointYComparator());
		for (LineSegmentIntersection s : segments) {
			LinkedList<LineSegmentIntersection> seg = new LinkedList<LineSegmentIntersection>();
			seg.add(s);
			EventPoint first = new EventPoint(s.segment[0], PointStatus1.upper, s);
			EventPoint second = new EventPoint(s.segment[1], PointStatus1.lower, s);
			eventPoints.add(first);
			eventPoints.add(second);
		}
		TreeSegmentComparator tsc = new TreeSegmentComparator();
		TreeSet<LineSegmentIntersection> T = new TreeSet<LineSegmentIntersection>(tsc);
		LinkedList<EventPointSegmentList> eventPointSegmentList = new LinkedList<EventPointSegmentList>();
		tsc.eventPointSegmentList = eventPointSegmentList;
		LinkedList<LineSegmentIntersection> Up = new LinkedList<LineSegmentIntersection>();
		LinkedList<LineSegmentIntersection> Cp = new LinkedList<LineSegmentIntersection>();
		LinkedList<LineSegmentIntersection> Lp = new LinkedList<LineSegmentIntersection>();
		EventPoint testPoint = new EventPoint();
		while(!eventPoints.isEmpty()){
			EventPoint p = eventPoints.poll();
//			System.out.println("p "+ Arrays.toString(p.point) + " segment " + p.segment.curveIndex);
			tsc.p = p;
			EventPoint next = eventPoints.peek();
			if(next == null || p.point[0] != next.point[0] || p.point[1] != next.point[1]){
				if(p.status == PointStatus1.upper){
					Up.add(p.segment);
					testPoint = p;
				}
				else if(p.status == PointStatus1.containsInterior){
					Cp.add(p.segment);
					testPoint = p;
				}
				else{
					Lp.add(p.segment);
				}
				handleEventPoint(p,testPoint, T, eventPoints, Up, Cp, Lp, interPoints, currentIntersections, eventPointSegmentList);
				Up.clear();
				Cp.clear();
				Lp.clear();
			}else{
				if(p.status == PointStatus1.upper){
					Up.add(p.segment);
					testPoint = p;
				}
				else if(p.status == PointStatus1.containsInterior){
					Cp.add(p.segment);
					testPoint = p;
				}
				else{
					Lp.add(p.segment);
				}
			}
		}
		System.out.println("Intersections: ");
		for (IntersectionPoint ip : interPoints) {
			System.out.println(ip.toString());
		}
		return interPoints;
	}
	
	public static void handleEventPoint(EventPoint p, EventPoint testPoint, TreeSet<LineSegmentIntersection> T, PriorityQueue<EventPoint> eventPoints, LinkedList<LineSegmentIntersection> Up, LinkedList<LineSegmentIntersection> Cp, LinkedList<LineSegmentIntersection> Lp, LinkedList<IntersectionPoint> interPoints, LinkedList<double[]> currentIntersections, LinkedList<EventPointSegmentList> eventPointSegmentList){
		LinkedList<LineSegmentIntersection> segments = new LinkedList<LineSegmentIntersection>();
//		if(Cp.size() + Up.size()  > 0){  vielleicht auch so !!!!!!!!
		if(Cp.size() + Up.size()  > 0){
			segments.addAll(Cp);
			segments.addAll(Up);
			EventPointSegmentList pSegments = new EventPointSegmentList();
			pSegments.p = p;
			pSegments.allSegments = new LinkedList<LineSegmentIntersection>();
			pSegments.allSegments.addAll(segments);
			eventPointSegmentList.add(pSegments);
	
			while(eventPointSegmentList.peekFirst().p.point[1] != p.point[1]){
				eventPointSegmentList.pollFirst();
			}
		}
		LinkedList<LineSegmentIntersection> allSegments = new LinkedList<LineSegmentIntersection>();
		allSegments.addAll(Lp);
		allSegments.addAll(Cp);
		allSegments.addAll(Up);
		int firstCurveIndex = allSegments.peekFirst().curveIndex;
		boolean moreThanOneCurve = false;
		for (LineSegmentIntersection aS : allSegments) {
			if(firstCurveIndex != aS.curveIndex){
				moreThanOneCurve = true;
			}
		}
		if(moreThanOneCurve){
			IntersectionPoint iP = new IntersectionPoint();
			iP.point = p.point;
			iP.intersectingSegments = allSegments;
			interPoints.add(iP);
		}
		T.removeAll(Cp);
		T.removeAll(Lp);
		T.addAll(Cp);
		T.addAll(Up);
		if(Up.size() + Cp.size() == 0){
			if(T.lower(p.segment) != null && T.higher(p.segment) != null){
				LineSegmentIntersection sl = T.lower(p.segment);
				LineSegmentIntersection sr = T.higher(p.segment);
				findNewEvent(sl, sr, p, currentIntersections, eventPoints);
			}
		}
		else{
			LineSegmentIntersection leftmost = testPoint.segment;
			
			while(T.lower(leftmost) != null && (Up.contains(T.lower(leftmost)) || Cp.contains(T.lower(leftmost)))){
				leftmost = T.lower(leftmost);
			}
			if(T.lower(leftmost) != null){
				LineSegmentIntersection sl = T.lower(leftmost);
				findNewEvent(sl, leftmost, p, currentIntersections, eventPoints);
			}
			LineSegmentIntersection rightmost = testPoint.segment;
			while(T.higher(rightmost) != null && (Up.contains(T.higher(rightmost)) || Cp.contains(T.higher(rightmost)))){
				rightmost = T.higher(rightmost);
			}
			if(T.higher(rightmost) != null){
				LineSegmentIntersection sr = T.higher(rightmost);
				findNewEvent(rightmost, sr, p, currentIntersections, eventPoints);
			}
		}
	}
	
	public static void findNewEvent(LineSegmentIntersection sl, LineSegmentIntersection sr, EventPoint p, LinkedList<double[]> currentIntersections, PriorityQueue<EventPoint> eventPoints){
		boolean intersection = twoSegmentIntersection(sl.segment[0], sl.segment[1], sr.segment[0], sr.segment[1]);
		double[] intersectionPoint = intersectionPoint(sl, sr);
		if(intersection){
		}
		double[] reversedIntersectionPoint = intersectionPoint(sr, sl);
		if((intersection && intersectionPoint[1] < p.point[1]) || (intersection && intersectionPoint[1] == p.point[1] && intersectionPoint[0] > p.point[0])){
			
			boolean isSelected = false;
			for (double[] ci  : currentIntersections) {
				if((intersectionPoint[0] == ci[0] && intersectionPoint[1] == ci[1]) || (reversedIntersectionPoint[0] == ci[0] && reversedIntersectionPoint[1] == ci[1])){
					isSelected = true;
				}
			}
			if(!isSelected){
				if(!Rn.equals(intersectionPoint, sl.segment[1]) && !Rn.equals(reversedIntersectionPoint, sl.segment[1])){
					EventPoint left = new EventPoint(intersectionPoint, PointStatus1.containsInterior, sl);
					eventPoints.add(left);
					currentIntersections.add(intersectionPoint);
					currentIntersections.add(reversedIntersectionPoint);
				}
				if(!Rn.equals(intersectionPoint, sr.segment[1]) && !Rn.equals(reversedIntersectionPoint, sr.segment[1])){
					EventPoint right = new EventPoint(intersectionPoint, PointStatus1.containsInterior, sr);
					eventPoints.add(right);
					currentIntersections.add(intersectionPoint);
					currentIntersections.add(reversedIntersectionPoint);
				}
				
				
			}
		}
	}
	
	
	public static void main(String[] args){
//		double[] a1 = {1,10};
//		double[] a2 = {4,1};
//		double[] a3 = {2,3};
//		double[] a4 = {9,9};
//		double[] a5 = {2,5};
//		double[] a6 = {7,2};
//		double[] a7 = {2,3};
//		double[] a8 = {9,3};
//		double[] a9 = {2,5};
//		double[] a10 = {9,5};
//		double[] b1 = {2,5};
//		double[] b2 = {2,2};
//		double[] b3 = {1,4};
//		double[] b4 = {4,4};
//		double[] b5 = {2,2};
//		double[] b6 = {5,2};
//		double[] b7 = {4,4};
//		double[] b8 = {4,1};
//		LineSegmentIntersection s1 = new LineSegmentIntersection();
//		s1.segment = new double[2][];
//		s1.segment[0] = a1;
//		s1.segment[1] = a2;
//		s1.curveIndex = 1;
//		LineSegmentIntersection s2 = new LineSegmentIntersection();
//		s2.segment = new double[2][];
//		s2.segment[0] = a3;
//		s2.segment[1] = a4;
//		s2.curveIndex = 2;
//		LineSegmentIntersection s3 = new LineSegmentIntersection();
//		s3.segment = new double[2][];
//		s3.segment[0] = a5;
//		s3.segment[1] = a6;
//		s3.curveIndex = 3;
//		LineSegmentIntersection s4 = new LineSegmentIntersection();
//		s4.segment = new double[2][];
//		s4.segment[0] = a7;
//		s4.segment[1] = a8;
//		s4.curveIndex = 4;
//		LineSegmentIntersection s5 = new LineSegmentIntersection();
//		s5.segment = new double[2][];
//		s5.segment[0] = a9;
//		s5.segment[1] = a10;
//		s5.curveIndex = 5;
//		LineSegmentIntersection t1 = new LineSegmentIntersection();
//		t1.segment = new double[2][];
//		t1.segment[0] = b1;
//		t1.segment[1] = b2;
//		t1.curveIndex = 1;
//		LineSegmentIntersection t2 = new LineSegmentIntersection();
//		t2.segment = new double[2][];
//		t2.segment[0] = b3;
//		t2.segment[1] = b4;
//		t2.curveIndex = 2;
//		LineSegmentIntersection t3 = new LineSegmentIntersection();
//		t3.segment = new double[2][];
//		t3.segment[0] = b5;
//		t3.segment[1] = b6;
//		t3.curveIndex = 3;
//		LineSegmentIntersection t4 = new LineSegmentIntersection();
//		t4.segment = new double[2][];
//		t4.segment[0] = b7;
//		t4.segment[1] = b8;
//		t4.curveIndex = 4;
//		LinkedList<LineSegmentIntersection> seg = new LinkedList<LineSegmentIntersection>();
//		seg.add(s1);
//		seg.add(s2);
//		seg.add(s3);
//		seg.add(s4);
//		seg.add(s5);
//		LinkedList<LineSegmentIntersection> seg1 = new LinkedList<LineSegmentIntersection>();
//		seg1.add(t1);
//		seg1.add(t2);
//		seg1.add(t3);
//		seg1.add(t4);
////		findIntersections(seg);
////		findIntersections(seg1);
//		b1[0] = 2; b1[1] = 8.5;
//		b2[0] = 3; b2[1] = 7.5;
//		b3[0] = 4; b3[1] = 8.5;
//		b4[0] = 3; b4[1] = 7.5;
//		b5[0] = 2; b5[1] = 8;
//		b6[0] = 3; b6[1] = 7;
//		b7[0] = 4; b7[1] = 8;
//		b8[0] = 2; b8[1] = 6;
//		t1.segment = new double[2][];
//		t1.segment[0] = b1;
//		t1.segment[1] = b2;
//		t1.curveIndex = 1;
//		t2.segment = new double[2][];
//		t2.segment[0] = b3;
//		t2.segment[1] = b4;
//		t2.curveIndex = 2;
//		t3.segment = new double[2][];
//		t3.segment[0] = b5;
//		t3.segment[1] = b6;
//		t3.curveIndex = 3;
//		t4.segment = new double[2][];
//		t4.segment[0] = b7;
//		t4.segment[1] = b8;
//		t4.curveIndex = 4;
//		seg.clear();
//		seg.add(t1);
//		seg.add(t2);
//		seg.add(t3);
//		seg.add(t4);
//		findIntersections(seg);
//		System.out.println(Arrays.toString(intersectionPoint(t3, t4)));
//		System.out.println(twoSegmentIntersection(b5, b6, b7, b8));
		double[] r1 = {0,3};
		double[] r2 = {2,3};
		double[] r3 = {4,3};
		double[] r4 = {6,3};
		double[] r5 = {8,3};
		double[] r6 = {1,1};
		double[] r7 = {3,1};
		double[] r8 = {5,1};
		double[] r9 = {7,1};
		double[] r10 = {9,1};
		double[] g1 = {2,5};
		double[] g2 = {2,3};
		double[] g3 = {2,0};
		double[] g4 = {7,4};
		double[] g5 = {7,2};
		double[] g6 = {7,1};
		LineSegmentIntersection s1 = new LineSegmentIntersection();
		s1.segment = new double[2][];
		s1.segment[0] = r1;
		s1.segment[1] = r2;
		s1.curveIndex = 1;
		s1.indexOnCurve = 1;
		LineSegmentIntersection s2 = new LineSegmentIntersection();
		s2.segment = new double[2][];
		s2.segment[0] = r2;
		s2.segment[1] = r3;
		s2.curveIndex = 1;
		s2.indexOnCurve = 2;
		LineSegmentIntersection s3 = new LineSegmentIntersection();
		s3.segment = new double[2][];
		s3.segment[0] = r3;
		s3.segment[1] = r4;
		s3.curveIndex = 1;
		s3.indexOnCurve = 3;
		LineSegmentIntersection s4 = new LineSegmentIntersection();
		s4.segment = new double[2][];
		s4.segment[0] = r4;
		s4.segment[1] = r5;
		s4.curveIndex = 1;
		s4.indexOnCurve = 4;
		LineSegmentIntersection s5 = new LineSegmentIntersection();
		s5.segment = new double[2][];
		s5.segment[0] = r6;
		s5.segment[1] = r7;
		s5.curveIndex = 1;
		s5.indexOnCurve = 5;
		LineSegmentIntersection s6 = new LineSegmentIntersection();
		s6.segment = new double[2][];
		s6.segment[0] = r7;
		s6.segment[1] = r8;
		s6.curveIndex = 1;
		s6.indexOnCurve = 6;
		LineSegmentIntersection s7 = new LineSegmentIntersection();
		s7.segment = new double[2][];
		s7.segment[0] = r8;
		s7.segment[1] = r9;
		s7.curveIndex = 1;
		s7.indexOnCurve = 7;
		LineSegmentIntersection s8 = new LineSegmentIntersection();
		s8.segment = new double[2][];
		s8.segment[0] = r9;
		s8.segment[1] = r10;
		s8.curveIndex = 1;
		s8.indexOnCurve = 8;
		LineSegmentIntersection s9 = new LineSegmentIntersection();
		s9.segment = new double[2][];
		s9.segment[0] = g1;
		s9.segment[1] = g2;
		s9.curveIndex = 2;
		s9.indexOnCurve = 1;
		LineSegmentIntersection s10 = new LineSegmentIntersection();
		s10.segment = new double[2][];
		s10.segment[0] = g2;
		s10.segment[1] = g3;
		s10.curveIndex = 2;
		s10.indexOnCurve = 2;
		LineSegmentIntersection s11 = new LineSegmentIntersection();
		s11.segment = new double[2][];
		s11.segment[0] = g4;
		s11.segment[1] = g5;
		s11.curveIndex = 2;
		s11.indexOnCurve = 3;
		LineSegmentIntersection s12 = new LineSegmentIntersection();
		s12.segment = new double[2][];
		s12.segment[0] = g5;
		s12.segment[1] = g6;
		s12.curveIndex = 2;
		s12.indexOnCurve = 4;
		LinkedList<LineSegmentIntersection> segments = new LinkedList<LineSegmentIntersection>();
		segments.clear();
		segments.add(s1);
		segments.add(s2);
		segments.add(s3);
		segments.add(s4);
		segments.add(s5);
		segments.add(s6);
		segments.add(s7);
		segments.add(s8);
		segments.add(s9);
		segments.add(s10);
		segments.add(s11);
		segments.add(s12);
		findIntersections(segments);
		
	}
}
