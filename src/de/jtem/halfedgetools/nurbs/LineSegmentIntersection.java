package de.jtem.halfedgetools.nurbs;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import compgeom.RLineSegment2D;
import compgeom.RPoint2D;
import compgeom.Rational;
import compgeom.algorithms.BentleyOttmann;

import de.jreality.math.Rn;

public class LineSegmentIntersection {
	

	public static LinkedList<IntersectionPoint> BentleyOttmannAlgoritm(List<LineSegment> segments){
		Set<RLineSegment2D> RSegments = new HashSet<RLineSegment2D>();
		Map<LineSegment, RLineSegment2D> map = new HashMap<LineSegment,RLineSegment2D>();
		Map<RLineSegment2D, LineSegment> inverseMap = new HashMap<RLineSegment2D, LineSegment>();
		
		System.out.println("START TO CONVERT TO RATIONAL");
		for (LineSegment ls : segments) {
			
			BigRational BigP1X = new BigRational(ls.segment[0][0]);
			BigRational BigP1Y = new BigRational(ls.segment[0][1]);
			BigRational BigP2X = new BigRational(ls.segment[1][0]);
			BigRational BigP2Y = new BigRational(ls.segment[1][1]);
			
			Rational p1X = new Rational(BigP1X.getNumerator(), BigP1X.getDenominator());
			Rational p1Y = new Rational(BigP1Y.getNumerator(), BigP1Y.getDenominator());
			Rational p2X = new Rational(BigP2X.getNumerator(), BigP2X.getDenominator());
			Rational p2Y = new Rational(BigP2Y.getNumerator(), BigP2Y.getDenominator());
			
			RPoint2D p1 = new RPoint2D(p1X, p1Y);
			RPoint2D p2 = new RPoint2D(p2X, p2Y);
			
			RLineSegment2D rSeg = new RLineSegment2D(p1, p2, ls.curveIndex, ls.indexOnCurve);
			if(p1.isLeftOf(p2) || (!p1.isLeftOf(p2) && !p1.isRightOf(p2) && p1.isBelow(p2))){
			}
			else{
				rSeg = new RLineSegment2D(p2, p1,  ls.curveIndex, ls.indexOnCurve);
			}
			inverseMap.put(rSeg, ls);
			map.put(ls, rSeg);
			RSegments.add(rSeg);
		}
		System.out.println("START TO COMPUTE INTERSECTIONS");
		LinkedList<IntersectionPoint> intersectionPoints = new LinkedList<IntersectionPoint>();
		Map<RPoint2D, Set<RLineSegment2D>> intersections = BentleyOttmann.intersectionsMap(RSegments);
		for(RPoint2D point : intersections.keySet()){
			LinkedList<LineSegment> segList = new LinkedList<LineSegment>();
			for (RLineSegment2D lS2D : intersections.get(point)) {
				segList.add(inverseMap.get(lS2D));
			}
			IntersectionPoint ip = new IntersectionPoint();
			double x = point.x.doubleValue();
			double y = point.y.doubleValue();
			ip.point = new double[2];
			ip.point[0] = x;
			ip.point[1] = y;
			ip.intersectingSegments = segList;
			
			intersectionPoints.add(ip);
		}
		System.out.println("BentleyOttmannAlgoritm IntersectionSize: " + intersections.size());
		return intersectionPoints;
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
//	public static boolean interchangedEndpoints(double[] a, double[] b, double[] c, double[]d, int i){
//		if(a[i] <= c[i] && d[i] <= b[i]){
//			return true;
//		}
//		return false;
//	}
	
	
	
	public static double[] intersectionPoint(LineSegment first, LineSegment second, EventPoint p){
		double s1 = first.segment[0][0];
		double s2 = first.segment[0][1];
		double t1 = first.segment[1][0];
		double t2 = first.segment[1][1];
		double p1 = second.segment[0][0];
		double p2 = second.segment[0][1];
		double q1 = second.segment[1][0];
		double q2 = second.segment[1][1];
		double[] result;
		if(isClosedToHorizontal(second)){
//			System.out.println("second "+ Arrays.toString(second.segment[0]) + " " + Arrays.toString(second.segment[1]));
//			System.out.println("horizontal second");
			result = new double[2];
			result[0] = s1 + ((t1 - s1) * (s2 - p.point[1]) / (s2 - t2));
			if(result[0] < p.point[0]){
				result[0] = p.point[0];
			}
			result[1] = p.point[1];
			return result;
		}
		else if(isClosedToHorizontal(first)){
//			System.out.println("first "+ Arrays.toString(first.segment[0]) + " " + Arrays.toString(first.segment[1]));
//			System.out.println("horizontal first");
			result = new double[2];
			result[0] = p1 + ((q1 - p1) * (p2 - p.point[1]) / (p2 - q2));
			if(result[0] < p.point[0]){
				result[0] = p.point[0];
			}
			result[1] = p.point[1];
			return result;
		}
		else{
			double lambda = ((p1 - s1) * (s2 - t2) - (p2 - s2) * (s1 - t1)) / ((q2 - p2) * (s1 - t1) - (q1 - p1) * (s2 - t2));
			result = Rn.add(null, second.segment[0],Rn.times(null, lambda, Rn.add(null, second.segment[1], Rn.times(null, -1, second.segment[0]))));
			//new
			double currEps = Math.min(conditionalEps(first), conditionalEps(second)) ;
			if(p.point[1] < result[1] && (result[1] - p.point[1]) < currEps){
				System.out.println("hier passierts");
				result[1] = p.point[1] - result[1];
			}
			//
			return result;
			
		}
	}
		
	
	

	
	public static boolean isHorizontal(LineSegment ls){
		if(ls.segment[0][1] != ls.segment[1][1]){
			return false;
		}
		return true;
	}
	
	

	
	public static boolean twoSegmentIntersection( LineSegment seg1, LineSegment seg2){
		double[] p1 = seg1.segment[0];
		double[] p2 = seg1.segment[1]; 
		double[] p3 = seg2.segment[0]; 
		double[] p4 = seg2.segment[1];
		double lengthSeg1 = Rn.euclideanDistance(p1, p2);
		double lengthSeg2 = Rn.euclideanDistance(p3, p4);
		double[] p2MinusP1 = Rn.add(null, p2, Rn.times(null, -1, p1));
		double[] q2 = Rn.add(null, p2, Rn.times(null, lengthSeg1 / 100, p2MinusP1));	
//		System.out.println("p2 "+Arrays.toString(p2)+"q2 "+Arrays.toString(q2));
		double[] q1 = Rn.add(null, p1, Rn.times(null, lengthSeg1 / -100, p2MinusP1));
//		System.out.println("p1 "+Arrays.toString(p1)+"q1 "+Arrays.toString(q1));
		double[] p4MinusP3 = Rn.add(null, p4, Rn.times(null, -1, p3));
		double[] q4 = Rn.add(null, p4, Rn.times(null, lengthSeg2 / 100, p4MinusP3));	
//		System.out.println("p4 "+Arrays.toString(p4)+"q4 "+Arrays.toString(q4));
		double[] q3 = Rn.add(null, p3, Rn.times(null, lengthSeg2 / -100, p4MinusP3));
//		System.out.println("p3 "+Arrays.toString(p3)+"q3 "+Arrays.toString(q3));
		
		if(LineSegmentIntersection.counterClockWiseOrder(q1, q3, q4) == LineSegmentIntersection.counterClockWiseOrder(q2, q3, q4)){
			return false;
		}
		else if(LineSegmentIntersection.counterClockWiseOrder(q1, q2, q3) == LineSegmentIntersection.counterClockWiseOrder(q1, q2, q4)){
			return false;
		}
		else{
			return true;
		}	
	}

	public static LinkedList<HalfedgePoint> findAllNbrs(LinkedList<IntersectionPoint> intersectionPoints){
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
			
			// only debugging
//			System.out.println("all curves intersecting this point unordered");
//			System.out.println(iP1.toString());
//			for (IndexedCurveList icl : iP1CurveList) {
//				System.out.println("index: " + icl.getIndex());
//				for (IntersectionPoint ip : icl.curveList) {
//					System.out.println(Arrays.toString(ip.point));
//				}
//			}
			//
	
			LinkedList<IntersectionPoint> nbrs = new LinkedList<IntersectionPoint>();
			
//			System.out.println("all curves intersecting this point ordered");
			
			for (IndexedCurveList icl : iP1CurveList) {
				
				// sorting each curveList w.r.t. indexOnCurve
				
				IntersectionPointIndexComparator ipic = new IntersectionPointIndexComparator();
				ipic.curveIndex = icl.index;
				Collections.sort(icl.curveList, ipic);
				
				//only debugging
//				System.out.println("index: " + icl.getIndex());
//				for (IntersectionPoint ip : icl.curveList){
//					System.out.println(Arrays.toString(ip.point));
//				}
				//
				
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
				
				//only debugging
//				System.out.println("index: " + icl.getIndex());
//					for (LinkedList<IntersectionPoint> linkedList : indexOrderList) {
//						for (IntersectionPoint iP : linkedList) {
//							System.out.println("indexOnCurve: " + getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.getIndex(), iP));
//							System.out.println(Arrays.toString(iP.point));
//						}
//					}
				//
				
				// sort all same indexed IntersectionPoints w.r.t. euclidian distance
				
				for (LinkedList<IntersectionPoint> sameList : indexOrderList) {
					if(sameList.size() > 1){
						sortSameIndex(sameList, icl.index, getIndexOnCurveFromCurveIndexAndIntersectionPoint(icl.index, sameList.getFirst()));
					}
				}
				
				// get back the original list in order
				
				LinkedList<IntersectionPoint> mapList = new LinkedList<IntersectionPoint>();
				for (LinkedList<IntersectionPoint> list : indexOrderList) {
					mapList.addAll(list);
				}
				
				//only debugging
//				System.out.println("after distancesorting");
//				System.out.println("index: " + icl.getIndex());
//				for (IntersectionPoint ip : mapList) {
//					System.out.println(Arrays.toString(ip.point));
//				}
				//
				
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
			System.out.println(hp.toString());
			iP1.setParentHP(hp);
			points.add(hp);
		}
		return points;
	}
	
	private static int getIndexOnCurveFromCurveIndexAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
		int result = 0;
		for (LineSegment seg : iP.intersectingSegments) {
			if(seg.curveIndex == curveIndex && result < seg.indexOnCurve){
				result = seg.indexOnCurve;
			}
		}
		return result;
	}
	
	private static double[] getFirstSegmentCoordsFromCurveIndex_IndexOnCurveAndIntersectionPoint(int curveIndex,int indexOnCurve, IntersectionPoint iP){
		for (LineSegment seg : iP.intersectingSegments) {
			if(seg.curveIndex == curveIndex && seg.indexOnCurve == indexOnCurve){
				return seg.segment[0];			
			}
		}
		return null;
	}
	
	private static LinkedList<IntersectionPoint> sortSameIndex(LinkedList<IntersectionPoint> sameIndexList, int curveIndex, int indexOnCurve){
		for (IntersectionPoint iP : sameIndexList) {
			double[] firstCoordFromIndexedSegment = getFirstSegmentCoordsFromCurveIndex_IndexOnCurveAndIntersectionPoint(curveIndex,indexOnCurve, iP);
			iP.sameIndexDist = Rn.euclideanDistance(iP.point, firstCoordFromIndexedSegment);
//			System.out.println("firstCoord "+Arrays.toString(firstCoordFromIndexedSegment)+" iP "+Arrays.toString(iP.point)+" iP.sameIndexDist " + iP.sameIndexDist);
		}
		Collections.sort(sameIndexList, new IntersectionPointDistanceComparator());
		return sameIndexList;
	}

//	private static double[] getFirstSegmentCoordsFromCurveIndex_IndexOnCurveAndIntersectionPoint(int curveIndex, IntersectionPoint iP){
//		for (LineSegment seg : iP.intersectingSegments) {
//			if(seg.curveIndex == curveIndex){
//				return seg.segment[0];			
//			}
//		}
//		return null;
//	}
//	
//	private static LinkedList<IntersectionPoint> sortSameIndex(LinkedList<IntersectionPoint> sameIndexList, int curveIndex){
//		for (IntersectionPoint iP : sameIndexList) {
//			double[] firstCoordFromIndexedSegment = getFirstSegmentCoordsFromCurveIndex_IndexOnCurveAndIntersectionPoint(curveIndex, iP);
//			iP.sameIndexDist = Rn.euclideanDistance(iP.point, firstCoordFromIndexedSegment);
//			System.out.println("firstCoord "+Arrays.toString(firstCoordFromIndexedSegment)+" iP "+Arrays.toString(iP.point)+" iP.sameIndexDist " + iP.sameIndexDist);
//		}
//		Collections.sort(sameIndexList, new IntersectionPointDistanceComparator());
//		return sameIndexList;
//	}

	
	private static LinkedList<Integer> getIndexListFromIntersectionPoint(IntersectionPoint iP){
		 LinkedList<Integer> indexList = new LinkedList<Integer>();
		 for (LineSegment seg : iP.intersectingSegments) {
			if(!indexList.contains(seg.curveIndex)){
				indexList.add(seg.curveIndex);
			}
		}
		return indexList;
	}
	
	

	public static LinkedList<HalfedgePoint> orientedNbrs (LinkedList<HalfedgePoint> halfPoints){
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
		IntersectionPoint firstIP = point.usedNbrs.getFirst();
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
					for (IntersectionPoint interP : halfedgePoint.usedNbrs) {
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
					for (IntersectionPoint interP : halfedgePoint.usedNbrs) {
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
		return allAjacentNbrs;
	}
	
	
	
	public static FaceSet  createFaceSet(LinkedList<HalfedgePoint> orientedNbrs){
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
			if(!hP.usedNbrs.isEmpty()){
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
	
		int[][] faceIndex = new int[faceNbrs.size()][];
		int counter = 0;
		for (int[] fs : faceNbrs) {
				faceIndex[counter] = fs;
				counter++;
		}
		fS.setFaces(faceIndex);
		return fS;
	}
	

	

	
	public static enum PointStatus {
		upper,
		containsInterior,
		lower
	}
	
	/*
	 *  plane sweep algorithm from "Computational Geometry"
	 */
	
	private static double conditionalEps(LineSegment ls){
		return Rn.euclideanDistance(ls.segment[0], ls.segment[1]) / 100;
	}
	
	public static LinkedList<IntersectionPoint> findIntersections(List<LineSegment> segments){
		LinkedList<IntersectionPoint> interPoints = new LinkedList<IntersectionPoint>();
		LinkedList<double[]> currentIntersections = new LinkedList<double[]>();
		
		PriorityQueue<EventPoint> eventPoints = new PriorityQueue<EventPoint>(3 * segments.size(), new EventPointYComparator());
		
//		for (LineSegment s : segments) {
//			if(s.segment[0][1] < s.segment[1][1] || (s.segment[0][1] == s.segment[1][1] && s.segment[0][0] > s.segment[1][0])){
//				double[] temp = s.segment[0];
//				s.segment[0] = s.segment[1];
//				s.segment[1] = temp;
//			}
//			EventPoint first = new EventPoint(s.segment[0], PointStatus.upper, s);
//			EventPoint second = new EventPoint(s.segment[1], PointStatus.lower, s);
//			eventPoints.add(first);
//			eventPoints.add(second);
//		}
		
		for (LineSegment s : segments) {
//			System.out.println(s.toString() + "endpoints" + Arrays.toString(s.segment[0]) + Arrays.toString(s.segment[1]));
			
			// new preProcessing
			if(isClosedToHorizontal(s)){
				double y = (s.segment[0][1] + s.segment[1][1]) / 2;
				s.segment[0][1] = y;
				s.segment[1][1] = y;
			}
			//
			EventPoint first = new EventPoint();
			EventPoint second = new EventPoint();
			if(s.segment[0][1] < s.segment[1][1] || (s.segment[0][1] == s.segment[1][1] && s.segment[0][0] > s.segment[1][0])){
				first = new EventPoint(s.segment[1], PointStatus.upper, s);
				second = new EventPoint(s.segment[0], PointStatus.lower, s);
			}
			else{
				first = new EventPoint(s.segment[0], PointStatus.upper, s);
				second = new EventPoint(s.segment[1], PointStatus.lower, s);
			}
			eventPoints.add(first);
			eventPoints.add(second);
		}
		TreeSegmentComparator tsc = new TreeSegmentComparator();
		TreeSet<LineSegment> T = new TreeSet<LineSegment>(tsc);
		LinkedList<EventPointSegmentList> eventPointSegmentList = new LinkedList<EventPointSegmentList>();
		tsc.eventPointSegmentList = eventPointSegmentList;
		LinkedList<LineSegment> Up = new LinkedList<LineSegment>();
		LinkedList<LineSegment> Cp = new LinkedList<LineSegment>();
		LinkedList<LineSegment> Lp = new LinkedList<LineSegment>();
		EventPoint testPoint = new EventPoint();
		while(!eventPoints.isEmpty()){
			EventPoint p = eventPoints.poll();
//			System.out.println("EventPoint: " + Arrays.toString(p.point) + " curveIndex = " + p.segment.curveIndex + " indexOnCurve = " + p.segment.indexOnCurve);
			tsc.p = p;
			EventPoint next = eventPoints.peek();
			if(next == null || p.point[0] != next.point[0] || p.point[1] != next.point[1]){
				if(p.status == PointStatus.upper){
					Up.add(p.segment);
					testPoint = p;
				}
				else if(p.status == PointStatus.containsInterior){
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
				if(p.status == PointStatus.upper){
					Up.add(p.segment);
					testPoint = p;
				}
				else if(p.status == PointStatus.containsInterior){
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
	
	public static void handleEventPoint(EventPoint p, EventPoint testPoint, TreeSet<LineSegment> T, PriorityQueue<EventPoint> eventPoints, LinkedList<LineSegment> Up, LinkedList<LineSegment> Cp, LinkedList<LineSegment> Lp, LinkedList<IntersectionPoint> interPoints, LinkedList<double[]> currentIntersections, LinkedList<EventPointSegmentList> eventPointSegmentList){
		
		
		LinkedList<LineSegment> segments = new LinkedList<LineSegment>();
		
		//search for all EventPoints on the sweepline
		if(Cp.size() + Up.size() > 0){
			segments.addAll(Cp);
			segments.addAll(Up);
//			segments.addAll(Lp);
			EventPointSegmentList pSegments = new EventPointSegmentList();
			pSegments.p = p;
			pSegments.allSegments = new LinkedList<LineSegment>();
			pSegments.allSegments.addAll(segments);
			eventPointSegmentList.add(pSegments);
			
			while(eventPointSegmentList.peekFirst().p.point[1] != p.point[1]){
				eventPointSegmentList.pollFirst();
			}
//			System.out.println("EventPointSegmentList:");
//			for (EventPointSegmentList epsl : eventPointSegmentList) {
//				System.out.println("EventPoint " + Arrays.toString(epsl.p.point));
//				for (LineSegment ls : epsl.allSegments) {
//					System.out.println(ls.toString());
//				}
//			}
			
		}
		
		
		LinkedList<LineSegment> allSegments = new LinkedList<LineSegment>();
		allSegments.addAll(Lp);
		allSegments.addAll(Cp);
		allSegments.addAll(Up);
		int firstCurveIndex = allSegments.peekFirst().curveIndex;
		boolean moreThanOneCurve = false;
		for (LineSegment aS : allSegments) {
			if(firstCurveIndex != aS.curveIndex){
				moreThanOneCurve = true;
			}
		}
		if(moreThanOneCurve){
			IntersectionPoint iP = new IntersectionPoint();
			iP.point = p.point;
			iP.intersectingSegments = allSegments;
//			System.out.println("Inersection detected:");
//			System.out.println(Arrays.toString(iP.point));
			interPoints.add(iP);
		}
//		System.out.println("Up");
//		for (LineSegment up : Up){
//			System.out.println(up.toString());
//		}
//		System.out.println("Cp");
//		for (LineSegment cp : Cp){
//			System.out.println(cp.toString());
//		}
//		System.out.println("Lp");
//		for (LineSegment lp : Lp){
//			System.out.println(lp.toString());
//		}
//		System.out.println("REMOVING");
		T.removeAll(Cp);
		T.removeAll(Lp);
//		System.out.println("tree: " + T.toString());
//		System.out.println("ADDING");
		T.addAll(Cp);
		T.addAll(Up);
	
//		System.out.println();
//		System.out.println("EventPoint " + Arrays.toString(p.point));
//		System.out.println("tree: " + T.toString());
		if(Up.size() + Cp.size() == 0){
			if(T.lower(p.segment) != null && T.higher(p.segment) != null){
				LineSegment sl = T.lower(p.segment);
				LineSegment sr = T.higher(p.segment);
				findNewEvent(sl, sr, p, currentIntersections, eventPoints);
			}
		}
		else{
			LineSegment leftmost = testPoint.segment;
			
			while(T.lower(leftmost) != null && (Up.contains(T.lower(leftmost)) || Cp.contains(T.lower(leftmost)))){
				leftmost = T.lower(leftmost);
			}
			if(T.lower(leftmost) != null){
				LineSegment sl = T.lower(leftmost);
				findNewEvent(sl, leftmost, p, currentIntersections, eventPoints);
			}
			LineSegment rightmost = testPoint.segment;
			while(T.higher(rightmost) != null && (Up.contains(T.higher(rightmost)) || Cp.contains(T.higher(rightmost)))){
				rightmost = T.higher(rightmost);
			}
			if(T.higher(rightmost) != null){
				LineSegment sr = T.higher(rightmost);
				findNewEvent(rightmost, sr, p, currentIntersections, eventPoints);
			}
		}
	}
	
	public static void findNewEvent(LineSegment sl, LineSegment sr, EventPoint p, LinkedList<double[]> currentIntersections, PriorityQueue<EventPoint> eventPoints){
		boolean intersection = twoSegmentIntersection(sl, sr);
//		System.out.println("for intersection checked segments:");
//		System.out.println("sr " + sr.toString() + " sl " + sl.toString());
//		boolean reversedIntersection = twoSegmentIntersection(sr, sl);
		EventPoint next = eventPoints.peek();
		double[] intersectionPoint = intersectionPoint(sl, sr, p);
		double[] reversedIntersectionPoint = intersectionPoint(sr, sl, p);
		// new
		double currEps = Math.min(conditionalEps(sl), conditionalEps(sr));
//		if((intersectionPoint[1] - p.point[1]) < currEps){
//			intersectionPoint[1] = p.point[1] - intersectionPoint[1];
//		}
		//
//		System.out.println("left seg:  curveIndex"+ sl.curveIndex + " indexOnCurve " + sl.indexOnCurve);
//		System.out.println("right seg:  curveIndex"+ sr.curveIndex + " indexOnCurve " + sr.indexOnCurve);
//		System.out.println(" segments intersect: " + intersection );
//		System.out.println(" segments intersect reversed: " + reversedIntersection );
		if(intersection){
//			System.out.println();
//			System.out.println("Intersection located");
//			intersectionPoint(sl, sr);
//			System.out.println("sl horizontal "+isHorizontal(sl));
//			System.out.println("sr horizontal "+isHorizontal(sr));
//			System.out.println("sl "+ sl.toString() + "endpoints " + Arrays.toString(sl.segment[0]) + " " + Arrays.toString(sl.segment[1]));
//			System.out.println("sr "+ sr.toString() + "endpoints " + Arrays.toString(sr.segment[0]) + " " + Arrays.toString(sr.segment[1]));
//			System.out.println("EventPoint " + Arrays.toString(p.point));
//			System.out.println(" intersectionPoint" + Arrays.toString(intersectionPoint));
//			System.out.println(" reversedIntersectionPoint" + Arrays.toString(reversedIntersectionPoint));
//			System.out.println();
		}
		if(intersection && (intersectionPoint[1] > p.point[1] || reversedIntersectionPoint[1] > p.point[1])){
//			System.out.println();
//			System.out.println("GROESSER");
//			System.out.println();
		}
		if((intersection && intersectionPoint[1] < p.point[1]) || (intersection && intersectionPoint[1] == p.point[1] && intersectionPoint[0] >= p.point[0])
				||(intersection && reversedIntersectionPoint[1] < p.point[1]) || (intersection && reversedIntersectionPoint[1] == p.point[1] && reversedIntersectionPoint[0] >= p.point[0])){
//			System.out.println("drinn mit Intersectionpoint: " + Arrays.toString(intersectionPoint));
			boolean isSelected = false;
			for (double[] ci  : currentIntersections) {
				if((intersectionPoint[0] == ci[0] && intersectionPoint[1] == ci[1]) || (reversedIntersectionPoint[0] == ci[0] && reversedIntersectionPoint[1] == ci[1])){
					isSelected = true;
				}
			}
			if(!isSelected){

				if(!Rn.equals(intersectionPoint, sl.segment[1]) && !Rn.equals(reversedIntersectionPoint, sl.segment[1]) && !Rn.equals(intersectionPoint, sl.segment[0]) && !Rn.equals(reversedIntersectionPoint, sl.segment[0])){
//				if(!Rn.equals(intersectionPoint, sl.segment[1]) && !Rn.equals(reversedIntersectionPoint, sl.segment[1])){
//					System.out.println("new IntersectionPoint left "+sl.curveIndex+"|"+sl.indexOnCurve +" "+ Arrays.toString(intersectionPoint));
					EventPoint left = new EventPoint(intersectionPoint, PointStatus.containsInterior, sl);
					eventPoints.add(left);
					currentIntersections.add(intersectionPoint);
					currentIntersections.add(reversedIntersectionPoint);
				}
//				if(!Rn.equals(intersectionPoint, sr.segment[1]) && !Rn.equals(reversedIntersectionPoint, sr.segment[1]) && !Rn.equals(intersectionPoint, sr.segment[0]) && !Rn.equals(reversedIntersectionPoint, sr.segment[0])){
				if(!Rn.equals(intersectionPoint, sr.segment[1]) && !Rn.equals(reversedIntersectionPoint, sr.segment[1])){
//					System.out.println("new IntersectionPoint right " +sr.curveIndex+"|"+sr.indexOnCurve +" "+ Arrays.toString(intersectionPoint));
					EventPoint right = new EventPoint(intersectionPoint, PointStatus.containsInterior, sr);
					eventPoints.add(right);
					currentIntersections.add(intersectionPoint);
					currentIntersections.add(reversedIntersectionPoint);
				}
			}
		}
	}
	
	private static boolean isClosedToHorizontal(LineSegment ls){
		double eps = Rn.euclideanDistance(ls.segment[0], ls.segment[1]) / 100000;
		if(Math.abs(ls.segment[0][1] - ls.segment[1][1]) < eps){
			return true;
		}
		return false;
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
//		LineSegment s1 = new LineSegment();
//		s1.segment = new double[2][];
//		s1.segment[0] = a1;
//		s1.segment[1] = a2;
//		s1.curveIndex = 1;
//		s1.indexOnCurve = 1;
//		LineSegment s2 = new LineSegment();
//		s2.segment = new double[2][];
//		s2.segment[0] = a3;
//		s2.segment[1] = a4;
//		s2.curveIndex = 2;
//		s2.indexOnCurve = 1;
//		LineSegment s3 = new LineSegment();
//		s3.segment = new double[2][];
//		s3.segment[0] = a5;
//		s3.segment[1] = a6;
//		s3.curveIndex = 3;
//		s3.indexOnCurve = 1;
//		LineSegment s4 = new LineSegment();
//		s4.segment = new double[2][];
//		s4.segment[0] = a7;
//		s4.segment[1] = a8;
//		s4.curveIndex = 4;
//		s4.indexOnCurve = 1;
//		LineSegment s5 = new LineSegment();
//		s5.segment = new double[2][];
//		s5.segment[0] = a9;
//		s5.segment[1] = a10;
//		s5.curveIndex = 5;
//		LineSegment t1 = new LineSegment();
//		t1.segment = new double[2][];
//		t1.segment[0] = b1;
//		t1.segment[1] = b2;
//		t1.curveIndex = 1;
//		t1.indexOnCurve = 1;
//		LineSegment t2 = new LineSegment();
//		t2.segment = new double[2][];
//		t2.segment[0] = b3;
//		t2.segment[1] = b4;
//		t2.curveIndex = 2;
//		t2.indexOnCurve = 1;
//		LineSegment t3 = new LineSegment();
//		t3.segment = new double[2][];
//		t3.segment[0] = b5;
//		t3.segment[1] = b6;
//		t3.curveIndex = 3;
//		t3.indexOnCurve = 1;
//		LineSegment t4 = new LineSegment();
//		t4.segment = new double[2][];
//		t4.segment[0] = b7;
//		t4.segment[1] = b8;
//		t4.curveIndex = 4;
//		t4.indexOnCurve = 1;
//		LinkedList<LineSegment> seg = new LinkedList<LineSegment>();
//		seg.add(s1);
//		seg.add(s2);
//		seg.add(s3);
//		seg.add(s4);
//		seg.add(s5);
//		LinkedList<LineSegment> seg1 = new LinkedList<LineSegment>();
//		seg1.add(t1);
//		seg1.add(t2);
//		seg1.add(t3);
//		seg1.add(t4);
//		LinkedList<IntersectionPoint> iP = LineSegmentIntersection.findIntersections(seg);
//		LinkedList<IntersectionPoint> iP1 = LineSegmentIntersection.findIntersections(seg1);
//		Assert.assertTrue(iP.size() == 9);
//		Assert.assertTrue(iP1.size() == 4);
//		b1[0] = 2; b1[1] = 8.5;
//		b2[0] = 3; b2[1] = 7.5;
//		b3[0] = 4; b3[1] = 8.5;
//		b4[0] = 3; b4[1] = 7.5;
//		b5[0] = 2; b5[1] = 7.5;
//		b6[0] = 4; b6[1] = 6;
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
		
		

//		double[] b1 = {0.001,0.001};
//		double[] b2 = {0.999,0.001};
//		double[] b3 = {0.001,0.999};
//		double[] b4 = {0.999,0.999};
////		double[] b1 = {0,0};
////		double[] b2 = {1,0};
////		double[] b3 = {0,1};
////		double[] b4 = {1,1};
//		LineSegment seg1_1 = new LineSegment();
//		seg1_1.segment = new double[2][];
//		seg1_1.segment[0] = b1;
//		seg1_1.segment[1] = b2;
//		seg1_1.curveIndex = 1;
//		seg1_1.indexOnCurve = 1;
//		LineSegment seg2_1 = new LineSegment();
//		seg2_1.segment = new double[2][];
//		seg2_1.segment[0] = b2;
//		seg2_1.segment[1] = b4;
//		seg2_1.curveIndex = 2;
//		seg2_1.indexOnCurve = 1;
//		LineSegment seg3_1 = new LineSegment();
//		seg3_1.segment = new double[2][];
//		seg3_1.segment[0] = b4;
//		seg3_1.segment[1] = b3;
//		seg3_1.curveIndex = 3;
//		seg3_1.indexOnCurve = 1;
//		LineSegment seg4_1 = new LineSegment();
//		seg4_1.segment = new double[2][];
//		seg4_1.segment[0] = b3;
//		seg4_1.segment[1] = b1;
//		seg4_1.curveIndex = 4;
//		seg4_1.indexOnCurve = 1;
//		double[] s1 = {1.0, 0.3333333333333333};
//		double[] s2 = {0.9333333333333332, 0.3333333333333333};
//		double[] s3 = {0.8333333333333333, 0.3333333333333333};
//		double[] s4 = {0.7333333333333333, 0.3333333333333333};
//		double[] s5 = {0.6333333333333333, 0.3333333333333333};
//		
//		double[] s6 = {0.5333333333333333, 0.3333333333333333};
//		double[] s7 = {0.43333333333333335, 0.3333333333333333};
//		double[] s8 = {0.3333333333333333, 0.3333333333333333};
//		double[] s9 = {0.2333333333333333, 0.3333333333333333};
//		double[] s10 = {0.1333333333333333, 0.3333333333333333};
//		
//		double[] s11 = {0.0333333333333333, 0.3333333333333333};
//		double[] s12 = {0.0, 0.3333333333333333};
//		
//		LineSegment seg1 = new LineSegment();
//		seg1.segment = new double[2][];
//		seg1.segment[0] = s1;
//		seg1.segment[1] = s2;
//		seg1.curveIndex = 5;
//		seg1.indexOnCurve = 1;
//		LineSegment seg2 = new LineSegment();
//		seg2.segment = new double[2][];
//		seg2.segment[0] = s2;
//		seg2.segment[1] = s3;
//		seg2.curveIndex = 5;
//		seg2.indexOnCurve = 2;
//		LineSegment seg3 = new LineSegment();
//		seg3.segment = new double[2][];
//		seg3.segment[0] = s3;
//		seg3.segment[1] = s4;
//		seg3.curveIndex = 5;
//		seg3.indexOnCurve = 3;
//		LineSegment seg4 = new LineSegment();
//		seg4.segment = new double[2][];
//		seg4.segment[0] = s4;
//		seg4.segment[1] = s5;
//		seg4.curveIndex = 5;
//		seg4.indexOnCurve = 4;
//		LineSegment seg5 = new LineSegment();
//		seg5.segment = new double[2][];
//		seg5.segment[0] = s5;
//		seg5.segment[1] = s6;
//		seg5.curveIndex = 5;
//		seg5.indexOnCurve = 5;
//		LineSegment seg6 = new LineSegment();
//		seg6.segment = new double[2][];
//		seg6.segment[0] = s6;
//		seg6.segment[1] = s7;
//		seg6.curveIndex = 5;
//		seg6.indexOnCurve = 6;
//		LineSegment seg7 = new LineSegment();
//		seg7.segment = new double[2][];
//		seg7.segment[0] = s1;
//		seg7.segment[1] = s2;
//		seg7.curveIndex = 5;
//		seg7.indexOnCurve = 7;
//		LineSegment seg8 = new LineSegment();
//		seg8.segment = new double[2][];
//		seg8.segment[0] = s8;
//		seg8.segment[1] = s9;
//		seg8.curveIndex = 5;
//		seg8.indexOnCurve = 8;
//		LineSegment seg9 = new LineSegment();
//		seg9.segment = new double[2][];
//		seg9.segment[0] = s9;
//		seg9.segment[1] = s10;
//		seg9.curveIndex = 5;
//		seg9.indexOnCurve = 9;
//		LineSegment seg10 = new LineSegment();
//		seg10.segment = new double[2][];
//		seg10.segment[0] = s10;
//		seg10.segment[1] = s11;
//		seg10.curveIndex = 5;
//		seg10.indexOnCurve = 10;
//		LineSegment seg11 = new LineSegment();
//		seg11.segment = new double[2][];
//		seg11.segment[0] = s11;
//		seg11.segment[1] = s12;
//		seg11.curveIndex = 5;
//		seg11.indexOnCurve = 11;
//		//index 6
//		double[] s13 = {0.3333333333333333, 1.0};
//		double[] s14 = {0.3333333333333333, 0.9333333333333332};
//		double[] s15 = {0.3333333333333333, 0.8333333333333333};
//		
//		double[] s16 = {0.3333333333333333, 0.7333333333333333};
//		double[] s17 = {0.3333333333333333, 0.6333333333333333};
//		double[] s18 = {0.3333333333333333, 0.5333333333333333};
//		double[] s19 = {0.3333333333333333, 0.43333333333333335};
//		double[] s20 = {0.3333333333333333, 0.3333333333333333};
//		
//		double[] s21 = {0.3333333333333333, 0.2333333333333333};
//		double[] s22 = {0.3333333333333333, 0.1333333333333333};
//		double[] s23 = {0.3333333333333333, 0.0333333333333333};
//		double[] s24 = {0.3333333333333333, 0.0};
//		
//		LineSegment seg12 = new LineSegment();
//		seg12.segment = new double[2][];
//		seg12.segment[0] = s13;
//		seg12.segment[1] = s14;
//		seg12.curveIndex = 6;
//		seg12.indexOnCurve = 1;
//		LineSegment seg13 = new LineSegment();
//		seg13.segment = new double[2][];
//		seg13.segment[0] = s14;
//		seg13.segment[1] = s15;
//		seg13.curveIndex = 6;
//		seg13.indexOnCurve = 2;
//		LineSegment seg14 = new LineSegment();
//		seg14.segment = new double[2][];
//		seg14.segment[0] = s15;
//		seg14.segment[1] = s16;
//		seg14.curveIndex = 6;
//		seg14.indexOnCurve = 3;
//		LineSegment seg15 = new LineSegment();
//		seg15.segment = new double[2][];
//		seg15.segment[0] = s16;
//		seg15.segment[1] = s17;
//		seg15.curveIndex = 6;
//		seg15.indexOnCurve = 4;
//		LineSegment seg16 = new LineSegment();
//		seg16.segment = new double[2][];
//		seg16.segment[0] = s17;
//		seg16.segment[1] = s18;
//		seg16.curveIndex = 6;
//		seg16.indexOnCurve = 5;
//		LineSegment seg17 = new LineSegment();
//		seg17.segment = new double[2][];
//		seg17.segment[0] = s18;
//		seg17.segment[1] = s19;
//		seg17.curveIndex = 6;
//		seg17.indexOnCurve = 6;
//		LineSegment seg18 = new LineSegment();
//		seg18.segment = new double[2][];
//		seg18.segment[0] = s19;
//		seg18.segment[1] = s20;
//		seg18.curveIndex = 6;
//		seg18.indexOnCurve = 7;
//		LineSegment seg19 = new LineSegment();
//		seg19.segment = new double[2][];
//		seg19.segment[0] = s20;
//		seg19.segment[1] = s21;
//		seg19.curveIndex = 6;
//		seg19.indexOnCurve = 8;
//		LineSegment seg20 = new LineSegment();
//		seg20.segment = new double[2][];
//		seg20.segment[0] = s21;
//		seg20.segment[1] = s22;
//		seg20.curveIndex = 6;
//		seg20.indexOnCurve = 9;
//		LineSegment seg21 = new LineSegment();
//		seg21.segment = new double[2][];
//		seg21.segment[0] = s22;
//		seg21.segment[1] = s23;
//		seg21.curveIndex = 6;
//		seg21.indexOnCurve = 10;
//		LineSegment seg22 = new LineSegment();
//		seg22.segment = new double[2][];
//		seg22.segment[0] = s23;
//		seg22.segment[1] = s24;
//		seg22.curveIndex = 6;
//		seg22.indexOnCurve = 11;
//		
//
//		LinkedList<LineSegment> segments = new LinkedList<LineSegment>();
//		segments.clear();
//		segments.add(seg1_1);
//		segments.add(seg2_1);
//		segments.add(seg3_1);
//		segments.add(seg4_1);
//		segments.add(seg1);
//		segments.add(seg2);
//		segments.add(seg3);
//		segments.add(seg4);
//		segments.add(seg5);
//		segments.add(seg6);
//		segments.add(seg7);
//		segments.add(seg8);
//		segments.add(seg9);
//		segments.add(seg10);
//		segments.add(seg11);
//		segments.add(seg12);
//		segments.add(seg13);
//		segments.add(seg14);
//		segments.add(seg15);
//		segments.add(seg16);
//		segments.add(seg17);
//		segments.add(seg18);
//		segments.add(seg19);
//		segments.add(seg20);
//		segments.add(seg21);
//		segments.add(seg22);
////		findIntersections(segments);
//		
//		double[] t1 = {1.0, 0.3333333333333333};
//		double[] t2 = {0.5, 0.3333333333333333};
//		double[] t3 = {0, 0.3333333333333333};
//		double[] t4 = {0.5, 1};
//		double[] t5 = {0.5, 0.5};
//		double[] t6 = {0.5, 0};
//		LineSegment seg5_1 = new LineSegment();
//		seg5_1.segment = new double[2][];
//		seg5_1.segment[0] = t1;
//		seg5_1.segment[1] = t2;
//		seg5_1.curveIndex = 5;
//		seg5_1.indexOnCurve = 1;
//		LineSegment seg5_2 = new LineSegment();
//		seg5_2.segment = new double[2][];
//		seg5_2.segment[0] = t2;
//		seg5_2.segment[1] = t3;
//		seg5_2.curveIndex = 5;
//		seg5_2.indexOnCurve = 2;
//		LineSegment seg6_1 = new LineSegment();
//		seg6_1.segment = new double[2][];
//		seg6_1.segment[0] = t4;
//		seg6_1.segment[1] = t5;
//		seg6_1.curveIndex = 6;
//		seg6_1.indexOnCurve = 1;
//		LineSegment seg6_2 = new LineSegment();
//		seg6_2.segment = new double[2][];
//		seg6_2.segment[0] = t5;
//		seg6_2.segment[1] = t6;
//		seg6_2.curveIndex = 6;
//		seg6_2.indexOnCurve = 2;
//		LinkedList<LineSegment> segmentsT = new LinkedList<LineSegment>();
//		segmentsT.add(seg1_1);
//		segmentsT.add(seg2_1);
//		segmentsT.add(seg3_1);
//		segmentsT.add(seg4_1);
//		segmentsT.add(seg5_1);
//		segmentsT.add(seg5_2);
//		segmentsT.add(seg6_1);
//		segmentsT.add(seg6_2);
//		findIntersections(segmentsT);
////		LinkedList<LineSegmentIntersection> test = new LinkedList<LineSegmentIntersection>();
		
		
		/*
		 * this is the example from NURBS-Surface c1 if you select the highest  right vertex 
		 */
		double[] p1 = {2,4};
		double[] p2 = {6,4};
		double[] p3 = {2,1};
		double[] p4 = {6,1};
		double[] p5 = {3,5};
		double[] p6 = {5,5};
		double[] p7 = {4,3};
		double[] p8 = {1,2};
		double[] p9 = {7,2};
		LineSegment seg1_1 = new LineSegment();
		seg1_1.segment = new double[2][];
		seg1_1.segment[0] = p3;
		seg1_1.segment[1] = p4;
		seg1_1.curveIndex = 1;
		seg1_1.indexOnCurve = 1;
		LineSegment seg2_1 = new LineSegment();
		seg2_1.segment = new double[2][];
		seg2_1.segment[0] = p2;
		seg2_1.segment[1] = p4;
		seg2_1.curveIndex = 2;
		seg2_1.indexOnCurve = 1;
		LineSegment seg3_1 = new LineSegment();
		seg3_1.segment = new double[2][];
		seg3_1.segment[0] = p2;
		seg3_1.segment[1] = p1;
		seg3_1.curveIndex = 3;
		seg3_1.indexOnCurve = 1;
		LineSegment seg4_1 = new LineSegment();
		seg4_1.segment = new double[2][];
		seg4_1.segment[0] = p1;
		seg4_1.segment[1] = p3;
		seg4_1.curveIndex = 4;
		seg4_1.indexOnCurve = 1;
		LineSegment seg5_2 = new LineSegment();
		seg5_2.segment = new double[2][];
		seg5_2.segment[0] = p5;
		seg5_2.segment[1] = p7;
		seg5_2.curveIndex = 5;
		seg5_2.indexOnCurve = 2;
		LineSegment seg5_3 = new LineSegment();
		seg5_3.segment = new double[2][];
		seg5_3.segment[0] = p7;
		seg5_3.segment[1] = p9;
		seg5_3.curveIndex = 5;
		seg5_3.indexOnCurve = 3;
		LineSegment seg6_2 = new LineSegment();
		seg6_2.segment = new double[2][];
		seg6_2.segment[0] = p6;
		seg6_2.segment[1] = p7;
		seg6_2.curveIndex = 6;
		seg6_2.indexOnCurve = 2;
		LineSegment seg6_3 = new LineSegment();
		seg6_3.segment = new double[2][];
		seg6_3.segment[0] = p8;
		seg6_3.segment[1] = p7;
		seg6_3.curveIndex = 6;
		seg6_3.indexOnCurve = 3;
		LinkedList<LineSegment> seg = new LinkedList<LineSegment>();
		seg.add(seg1_1);
		seg.add(seg2_1);
		seg.add(seg3_1);
		seg.add(seg4_1);
		seg.add(seg5_2);
		seg.add(seg5_3);
		seg.add(seg6_2);
		seg.add(seg6_3);
//		findIntersections(seg);
		BentleyOttmannAlgoritm(seg);
		
		
		
		
	}
}
