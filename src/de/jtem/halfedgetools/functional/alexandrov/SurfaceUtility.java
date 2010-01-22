/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2009, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.functional.alexandrov;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasAngle;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXY;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;
import de.jtem.halfedgetools.functional.alexandrov.decorations.IsBoundary;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.halfedgetools.util.surfaceutilities.SurfaceException;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;


/**
 * Basic operations on HalfEdge surfaces
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class SurfaceUtility {


	/**
	 * Constructs a border in a already linked graph. The Border is the 
	 * outline of the graphs geometry.
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param graph
	 * @throws SurfaceException
	 */
	public static 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & IsBoundary,
		F extends Face<V, E, F>
	> void constructBoundary(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		for (E e : graph.getEdges())
			e.setBoundary(false);
		Comparator<V> comp = new VertexXPosComparator<V, E, F>();
		V rightMost = Collections.max(graph.getVertices(), comp);
		List<E> cocycle =HalfEdgeUtilsExtra.getEdgeStar(rightMost);
		Comparator<E> comp2 = new SinComparator<V, E, F>(rightMost);
		E boundaryEdge = Collections.max(cocycle, comp2);
		F boundaryFace = boundaryEdge.getLeftFace();
		List<E> border = HalfEdgeUtilsExtra.getBoundary(boundaryFace);
		for (E e : border){
			e.setBoundary(true);
			e.getOppositeEdge().setBoundary(true);
		}
		graph.removeFace(boundaryFace);
	}
	
	
	/**
	 * Links the boundary in correctly linked graph. 
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param graph
	 * @throws SurfaceException
	 */
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void linkBoundary(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		for (F f : graph.getFaces()){
			if (HalfEdgeUtils.isInteriorFace(f))
				continue;
			for (E b : HalfEdgeUtilsExtra.getBoundary(f)){
				if (HalfEdgeUtils.isInteriorEdge(b))
					continue;
				E boundaryEdge = b.getOppositeEdge();
				if (HalfEdgeUtilsExtra.isBoundaryEdge(b.getPreviousEdge())){
					boundaryEdge.linkNextEdge(b.getPreviousEdge().getOppositeEdge());
					continue;
				}
				E nextBoundaryEdge = b;
				do {
					nextBoundaryEdge = nextBoundaryEdge.getPreviousEdge().getOppositeEdge();
				} while (HalfEdgeUtils.isInteriorEdge(nextBoundaryEdge));
				boundaryEdge.linkNextEdge(nextBoundaryEdge);
			}
		}
	}
	
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & IsBoundary & IsFlippable & HasAngle&HasLength,
		F extends Face<V, E, F>
	> void calculateAngles(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		
		for(V v : graph.getVertices()) {
			double ang = 0.0;
			for(E e : HalfEdgeUtilsExtra.getEdgeStar(v)) {
				ang += getAngle(e);
				e.setAngle(ang);
			}
		}
	}
	
	public static <V extends Vertex<V, E, F>, E extends Edge<V, E, F> & IsFlippable&HasLength, F extends Face<V, E, F>> Double getAngle(
			E edge) throws TriangulationException {
		Double a = edge.getLength();
		Double b = edge.getNextEdge().getLength();
		Double c = edge.getPreviousEdge().getLength();
		if ((a*a + b*b - c*c) / (2*a*b) > 1)
			throw new TriangulationException("Triangle inequation doesn't hold for " + edge);
		Double result = Math.abs(StrictMath.acos((a*a + b*b - c*c) / (2*a*b)));
//		System.err.println("angle between " + edge + " and " + edge.getNextEdge() + " is " + result);
		return result;
	}
	
	
	private static class SinComparator
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & IsBoundary,
		F extends Face<V, E, F>
	> implements Comparator<E>{

		private Point2d
			center = null;
		
		public SinComparator(V center){
			this.center = center.getXY();
		}
		
		public int compare(E e1, E e2) {
			Point2d v1 = e1.getOppositeEdge().getTargetVertex().getXY();
			Point2d v2 = e2.getOppositeEdge().getTargetVertex().getXY();
			double sin1 = (center.y - v1.y) / (center.x - v1.x);
			double sin2 = (center.y - v2.y) / (center.x - v2.x);
			if (sin1 < sin2)
				return -1;
			if (sin1 > sin2)
				return 1;
			return 0;
		}
		
	}
	
	public static class EdgeLengthComparator
	<
		E extends Edge<?,?,?> & HasLength
	> implements Comparator<E>{

		public int compare(E e1, E e2) {
	
			Double l = e1.getLength() - e2.getLength();
			if(l < 0)
				return -1;
			if(l > 0 )
				return 1;
			return 0;
			
		}
		
	}
	
	public static class EdgeAngleComparator 
	<
//		E extends Edge<?,?,?> & HasAngle
		E extends HasAngle
	> implements Comparator<E>{

		public int compare(E e1, E e2) {
			Double check = e1.getAngle() - e2.getAngle();
			if (check < 0)
				return -1;
			if (check > 0)
				return 1;
			return 0;
		}
		
	}
	
	public static class VertexIndexComparator 
	< 
		V extends Vertex<?,?,?>
	>
	implements Comparator<V>{

		public int compare(V v1, V v2) {
			int check = v1.getIndex() - v2.getIndex();
			if (check < 0)
				return -1;
			if (check > 0)
				return 1;
			return 0;
		}
		
	}
	

	private static class VertexXPosComparator 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F> & IsBoundary,
		F extends Face<V, E, F>
	> implements Comparator<V>{

		public int compare(V v1, V v2) {
			double check = v1.getXY().x - v2.getXY().x;
			if (check < 0)
				return -1;
			if (check > 0)
				return 1;
			return 0;
		}
		
	}
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void linkAllEdges(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		for (E e : graph.getEdges()){
			E actEdge = e;
			int counter = 0;
			do {
				E nextEdge = findNextEdge(graph, actEdge);
				actEdge.linkNextEdge(nextEdge);
				nextEdge.linkPreviousEdge(actEdge);
				actEdge = nextEdge;
				counter++;
			} while (actEdge != e && counter <= graph.numEdges());
			if (counter == graph.numEdges() + 1)
				throw new SurfaceException("No valid geometry in link all edges!");
		}
	}
	
	
//	public static 
//	<
//		V extends Vertex<V, E, F>,
//		E extends Edge<V, E, F>,
//		F extends Face<V, E, F>
//	> void fillHoles(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
//		for (E e : graph.getEdges()){
//			if (e.getLeftFace() != null)
//				continue;
//			F face = graph.addNewFace();
//			E actEdge = e;
//			int counter = 0;
//			do {
//				if (actEdge == null)
//					throw new SurfaceException("No valid surface while generating faces!");
//				actEdge.setLeftFace(face);
//				actEdge = actEdge.getNextEdge();
//				counter++;
//			} while (e != actEdge && counter < graph.numEdges());
//		}
//		if (!ConsistencyCheck.isValidSurface(graph))
//			throw new SurfaceException("No valid surface could be constructed!");
//	}
	
	public static 
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<F>   fillHoles(HalfEdgeDataStructure<V, E, F> graph) throws SurfaceException{
		List<F> faces = new LinkedList<F>();
		for (E e : graph.getEdges()){
			if (e.getLeftFace() != null)
				continue;
			F face = graph.addNewFace();
			faces.add(face);
			E actEdge = e;
			int counter = 0;
			do {
				if (actEdge == null)
					throw new SurfaceException("No valid surface while generating faces!");
				actEdge.setLeftFace(face);
				actEdge = actEdge.getNextEdge();
				counter++;
			} while (e != actEdge && counter < graph.numEdges());
		}

		if(!HalfEdgeUtils.isValidSurface(graph, true))
			throw new SurfaceException("No valid surface could be constructed!");
	
		return faces;
	}
	
	
	public static 
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> E findNextEdge (HalfEdgeDataStructure<V, E, F> graph, E edge){
		V vertex = edge.getTargetVertex();
		List<E> edges = HalfEdgeUtilsExtra.findEdgesWithTarget(vertex);
		edges.remove(edge);
		AngleComparator<V, E, F> comp = new AngleComparator<V, E, F>(edge);
		Collections.sort(edges, comp);
		if (edges.isEmpty())
			return edge;
		else
			return edges.get(0).getOppositeEdge();
	}
	
	
	
	private static class AngleComparator 	
	<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  implements Comparator<E>{

		private E 
			referenceEdge = null;
		
		public AngleComparator(E referenceEdge){
			this.referenceEdge = referenceEdge;
		}
		
		public int compare(E e1, E e2) {
			Point2d c = referenceEdge.getTargetVertex().getXY();
			Point2d p0 = referenceEdge.getOppositeEdge().getTargetVertex().getXY();
			double zeroAng = 2 * Math.PI - (Math.atan2(p0.y - c.y, p0.x - c.x) + Math.PI);
			
			Point2d p1 = e1.getOppositeEdge().getTargetVertex().getXY();
			Point2d p2 = e2.getOppositeEdge().getTargetVertex().getXY();
			double ang1 = (Math.atan2(p1.y - c.y, p1.x - c.x) + Math.PI + zeroAng) % (2 * Math.PI);
			double ang2 = (Math.atan2(p2.y - c.y, p2.x - c.x) + Math.PI + zeroAng) % (2 * Math.PI);
			if (ang1 < ang2)
				return -1;
			if (ang1 > ang2)
				return 1;
			return 0;
		}
		
	}
	
	
	public static 	
		<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void rescaleSurface(HalfEdgeDataStructure<V, E, F> surface, double scale){
		double[][] bBox = getBBox(surface);
		double xExtend = Math.abs(bBox[0][0] - bBox[1][0]);
		double yExtend = Math.abs(bBox[0][1] - bBox[1][1]);
		double zExtend = Math.abs(bBox[0][2] - bBox[1][2]);
		double size = Math.max(xExtend, yExtend);
		size = Math.max(size, zExtend);
		double factor = scale/size;
		for (V v : surface.getVertices()){
			VecmathTools.dehomogenize(v.getXYZW());
			v.getXYZW().scale(factor);
			v.getXYZW().w = 1.0;
		}
		
	}
	
	
	public static 	
		<
		V extends Vertex<V, E, F> & HasXY,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void rescaleGraph(HalfEdgeDataStructure<V, E, F> surface, double scale){
		double xMax = -Double.MAX_VALUE;
		double xMin = Double.MAX_VALUE;
		double yMax = -Double.MAX_VALUE;
		double yMin = Double.MAX_VALUE;
		for (V v : surface.getVertices()){
			Point2d p = v.getXY();
			if (p.x > xMax) xMax = p.x;
			if (p.y > yMax) yMax = p.y;
			if (p.x < xMin) xMin = p.x;
			if (p.y < yMin) yMin = p.y;
		}
		double xExtend = xMax - xMin;
		double yExtend = yMax - yMin;
		double size = Math.max(xExtend, yExtend);
		double factor = scale/size;
		for (V v : surface.getVertices()){
			v.getXY().scale(factor);
		}
		
	}

	
	public static 	
		<
		V extends Vertex<V, E, F> & HasXYZW,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[][] getBBox(HalfEdgeDataStructure<V, E, F> surface) {
    	double result[][] =  new double[2][3];
    	Point4d p0 = surface.getVertex(0).getXYZW();
    	VecmathTools.dehomogenize(p0);
    	result[0] = new double[]{p0.x, p0.y, p0.z};
    	result[1] = result[0].clone();
    	for (int i=1; i< surface.numVertices(); i++) {
    		Point4d p = surface.getVertex(i).getXYZW();
    		VecmathTools.dehomogenize(p);
       		if ( result[0][0] > p.x ) result[0][0] = p.x;
       		if ( result[1][0] < p.x ) result[1][0]=  p.x;
       		if ( result[0][1] > p.y ) result[0][1] = p.y;
       		if ( result[1][1] < p.y ) result[1][1]=  p.y;
       		if ( result[0][2] > p.z ) result[0][2] = p.z;
       		if ( result[1][2] < p.z ) result[1][2]=  p.z;
    	}
    	return result;
    }
	
}
