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

package de.jtem.halfedgetools.plugin;

import static de.jtem.halfedge.util.HalfEdgeUtils.boundaryVertices;
import static de.jtem.halfedge.util.HalfEdgeUtils.facesIncidentWithVertex;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.plugin.AnnotationAdapter.EdgeAnnotation;
import de.jtem.halfedgetools.plugin.AnnotationAdapter.FaceAnnotation;
import de.jtem.halfedgetools.plugin.AnnotationAdapter.VertexAnnotation;
import de.jtem.java2d.Annotation;
import de.jtem.java2dx.Line2DDouble;
import de.jtem.java2dx.Point2DDouble;
import de.jtem.java2dx.Point2DList;
import de.jtem.java2dx.Polygon2D;
import de.jtem.java2dx.modelling.DraggableAnnotation;
import de.jtem.java2dx.modelling.DraggableLine2D;
import de.jtem.java2dx.modelling.DraggablePoint2D;
import de.jtem.java2dx.modelling.DraggablePolygon2D;
import de.jtem.java2dx.modelling.SimpleModeller2D;

public class DebugFactory {


	/**
	 * Debugging of a small neighborhood of root
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param root
	 * @param neighborhood
	 */ 
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void makeNeighborhood(
		V root, 
		int neighborhood, 		
		boolean showVertices, 
		boolean showEdges, 
		boolean showFaces, 
		SimpleModeller2D moddeller,
		AnnotationAdapter<?>... a
	) {
		moddeller.getViewer().getRoot().removeAllChildren();
//		MyHDS N = new MyHDS();
		Set<F> innerFaces = new HashSet<F>();
		Set<F> faces = new HashSet<F>(facesIncidentWithVertex(root));
		while (--neighborhood > 0) {
			for (F f : new LinkedList<F>(faces)) {
				if (innerFaces.contains(f)) {
					continue;
				}
				innerFaces.add(f);
				for (V v : boundaryVertices(f)) {
					faces.addAll(facesIncidentWithVertex(v));
				}
			}
		}
		
//		for (F f : faces) {
//			//TODO Make this happen
//			MyFace newF = N.addNewFace();
//		}
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void makeVertexCloseUp(
		HDS hds,
		V v,
		SimpleModeller2D moddeller,
		AnnotationAdapter<?>... a
	) {
		moddeller.getViewer().getRoot().removeAllChildren();
		Point2DDouble p0 = showVertex(v, new double[2], moddeller, a);
		
		List<E> eList = HalfEdgeUtils.incomingEdges(v);
		double delta = 2*Math.PI / eList.size();
		double alpha = Math.PI / 8; 
		double[] pos = {sin(alpha), cos(alpha)}; 
		for (E e : eList) {

			Point2DDouble p = showVertex(e.getStartVertex(), pos, moddeller, a);
			showEdge(e, p, p0, moddeller, a);
			showEdge(e.getOppositeEdge(), p0, p, moddeller, a);

			alpha += delta;
			pos[0] = sin(alpha);
			pos[1] = cos(alpha);
			
			Point2DList pList = new Point2DList();
			pList.add(p0);
			pList.add(p);
			pList.add(new Point2DDouble(pos[0], pos[1]));
			if (e.getLeftFace() != null) {
				showFace(e.getLeftFace(), pList, moddeller, a);
			}
		}
		
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void makeEdgeCloseUp(
		HDS hds,
		E e,
		SimpleModeller2D moddeller,
		AnnotationAdapter<?>... a
	) {
		moddeller.getViewer().getRoot().removeAllChildren();
		
		Point2DDouble s = showVertex(e.getStartVertex(), new double[] {-1, 0.2}, moddeller, a);
		Point2DDouble t = showVertex(e.getTargetVertex(), new double[] {1, -0.2}, moddeller, a);
		Map<V, Point2DDouble> pointMap = new HashMap<V, Point2DDouble>();
		pointMap.put(e.getStartVertex(), s);
		pointMap.put(e.getTargetVertex(), t);
		
		showEdge(e, s, t, moddeller, a);
		showEdge(e.getOppositeEdge(), t, s, moddeller, a);
		
		Point2DDouble p1 = new Point2DDouble(1.5, 1.5);
		Point2DDouble p2 = new Point2DDouble(-1.5, 1.5);
		Point2DDouble p3 = new Point2DDouble(-1.5, -1.5);
		Point2DDouble p4 = new Point2DDouble(1.5, -1.5);
		V v1 = e.getNextEdge().getTargetVertex();
		V v2 = e.getPreviousEdge().getStartVertex();
		V v3 = e.getOppositeEdge().getNextEdge().getTargetVertex();
		V v4 = e.getOppositeEdge().getPreviousEdge().getStartVertex();
		pointMap.put(v1, p1);
		pointMap.put(v2, p2);
		pointMap.put(v3, p3);
		pointMap.put(v4, p4);
		showEdge(e.getNextEdge(), t, p1, moddeller, a);
		showEdge(e.getPreviousEdge(), p2, s, moddeller, a);
		showEdge(e.getOppositeEdge().getNextEdge(), s, p3, moddeller, a);
		showEdge(e.getOppositeEdge().getPreviousEdge(), p4, t, moddeller, a);
		showVertex(v1, new double[] {p1.x, p1.y}, moddeller, a);
		showVertex(v2, new double[] {p2.x, p2.y}, moddeller, a);
		showVertex(v3, new double[] {p3.x, p3.y}, moddeller, a);
		showVertex(v4, new double[] {p4.x, p4.y}, moddeller, a);
		
		Point2DList pList1 = new Point2DList();
		Point2DList pList2 = new Point2DList();
		pList1.add(s);
		pList1.add(t);
		pList1.add(p1);
		pList1.add(p2);
		pList2.add(p3);
		pList2.add(s);
		pList2.add(t);
		pList2.add(p4);
		if (e.getLeftFace() != null) {
			showFace(e.getLeftFace(), pList1, moddeller, a);
		}
		if (e.getRightFace() != null) {
			showFace(e.getRightFace(), pList2, moddeller, a);
		}
	}
	
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void makeFaceCloseUp(
		HDS hds,
		F f,
		SimpleModeller2D moddeller,
		AnnotationAdapter<?>... a
	) {
		moddeller.getViewer().getRoot().removeAllChildren();
		Map<V, Point2DDouble> pointMap = new HashMap<V, Point2DDouble>();
		List<E> eList = HalfEdgeUtils.boundaryEdges(f);
		double delta = 2*Math.PI / eList.size();
		double alpha = Math.PI / 8;
		double[] pos = {sin(alpha), cos(alpha)};
		Point2DList pList = new Point2DList();
		for (E e : eList) {
			Point2DDouble p1 = showVertex(e.getStartVertex(), pos, moddeller);
			alpha += delta;
			pos[0] = sin(alpha);
			pos[1] = cos(alpha);
			Point2DDouble p2 = showVertex(e.getTargetVertex(), pos, moddeller);
			pointMap.put(e.getStartVertex(), p1);
			pointMap.put(e.getTargetVertex(), p2);
			showEdge(e, p1, p2, moddeller, a);
			showEdge(e.getOppositeEdge(), p2, p1, moddeller, a);
			pList.add(p2);
		} 
		
		showFace(f, pList, moddeller, a);
	}
	
	
	
	
	/**
	 * Sphere or disk minus one face debugging
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param <HDS>
	 * @param hds
	 * @param embedding
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void makeTutte(
		HDS hds, 
		F bounds, 
		boolean showVertices, 
		boolean showEdges, 
		boolean showFaces, 
		SimpleModeller2D moddeller,
		AnnotationAdapter<?>... a
	) {
		moddeller.getViewer().getRoot().removeAllChildren();
		List<E> outerCycle = HalfEdgeUtils.boundaryEdges(bounds);
		List<V> vCycle = HalfEdgeUtils.boundaryVertices(bounds);
		int n = hds.numVertices();
		Vector rhsX = new DenseVector(n);
		Vector rhsY = new DenseVector(n);
		DenseMatrix stressMatrix = new DenseMatrix(n,n);
		double delta = 2 * PI / outerCycle.size();
		double angle = 0.0;
		for(E e : outerCycle) {
			int i = e.getTargetVertex().getIndex();
			stressMatrix.set(i, i, 1);
			rhsX.set(i, sin(angle));
			rhsY.set(i, cos(angle));
			angle += delta;
		}
		for(V v : hds.getVertices()) {
			if(!(vCycle.contains(v))) {
				int i = v.getIndex();
				List<V> neighs = HalfEdgeUtils.neighboringVertices(v);
				stressMatrix.set(i,i,-neighs.size());
				for(V neigh : neighs) {
					int j = neigh.getIndex();
					stressMatrix.set(i,j,1);
				}
			}
		}
		Vector coordsX = new DenseVector(n);
		Vector coordsY = new DenseVector(n);
		stressMatrix.solve(rhsX, coordsX);
		stressMatrix.solve(rhsY, coordsY);
		double[][] result = new double[n][2];
		for (int i = 0; i < n; i++) {
			result[i][0] = coordsX.get(i);
			result[i][1] = coordsY.get(i);
		}
		display(hds, result, showVertices, showEdges, showFaces, moddeller);
	}
	
	
	
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void display(
		HDS hds, 
		double[][] coords, 		
		boolean showVertices, 
		boolean showEdges, 
		boolean showFaces, 
		SimpleModeller2D moddeller
	) {
		Map<V, Point2DDouble> pointMap = new HashMap<V, Point2DDouble>();
		for (V v : hds.getVertices()) {
			Point2DDouble p = null;
			if (showVertices) {
				p = showVertex(v, coords[v.getIndex()], moddeller);
			} else {
				p = new Point2DDouble(coords[v.getIndex()][0], coords[v.getIndex()][1]);
			}
			pointMap.put(v, p);
		}
	
		if (showEdges) {
			for (E e : hds.getEdges()) {
				Point2DDouble s = pointMap.get(e.getStartVertex());
				Point2DDouble t = pointMap.get(e.getTargetVertex());
				showEdge(e, s, t, moddeller);
			}
		}
		
		if (showFaces) {
			for (F f : hds.getFaces()) {
				Point2DList pList = new Point2DList();
				for (E e : HalfEdgeUtils.boundaryEdges(f)) {
					pList.add(pointMap.get(e.getTargetVertex()));
				}
				showFace(f, pList, moddeller);
			}
		}
	}

	
	
	@SuppressWarnings("unchecked")
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Point2DDouble showVertex(
		V v, double[] coord, 
		SimpleModeller2D moddeller, 
		AnnotationAdapter<?>... a
	) {
		Point2DDouble p = new Point2DDouble(coord[0], coord[1]);
		DraggablePoint2D dragTool = new DraggablePoint2D(p);
		dragTool.getViewScene().setPointPaint(Color.green);
		dragTool.getViewScene().setPointShape(new Ellipse2D.Double(-3,-3,6,6));
		moddeller.getViewer().getRoot().addChild(dragTool.getViewScene());
		moddeller.getModeller().addTool(dragTool, null);
		String annText = "";
		for (AnnotationAdapter<?> aa : a) {
			if (!(aa instanceof VertexAnnotation)) {
				continue;
			}
			annText += " " + ((VertexAnnotation<V>)aa).getText(v);
		}
		Annotation ann = new Annotation(annText, coord[0], coord[1], Annotation.WEST);
		DraggableAnnotation dragAnn = new DraggableAnnotation(ann);
		dragTool.getViewScene().addChild(dragAnn.getViewScene());
		moddeller.getModeller().addTool(dragAnn, null);
		return p;
	}
	
	@SuppressWarnings("unchecked")
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Line2DDouble showEdge(
		E e, 
		Point2DDouble start,
		Point2DDouble end,
		SimpleModeller2D moddeller, 
		AnnotationAdapter<?>... a
	) {
		Line2DDouble line = new Line2DDouble(start, end);
		DraggableLine2D dragLine = new DraggableLine2D(line);
		dragLine.setCirclesInsteadOfArrows(false);
		dragLine.setArrowHeadCount(1);
		dragLine.setArrowHeadAspectRatio(0.3);
		dragLine.setArrowHeadRadius(5.5);
		dragLine.getViewScene().setStroke(new BasicStroke(1));
		dragLine.getViewScene().setAnnotated(true);
		moddeller.getModeller().addTool(dragLine, null);
		moddeller.addScene(dragLine.getViewScene());
		String annText = "";
		for (AnnotationAdapter<?> aa : a) {
			if (!(aa instanceof EdgeAnnotation)) {
				continue;
			}
			annText += " " + ((EdgeAnnotation<E>)aa).getText(e);
		}
		Annotation ann = new Annotation(annText, 0.4*start.x + 0.6*end.x, 0.4*start.y + 0.6*end.y, Annotation.WEST);
		DraggableAnnotation dragAnn = new DraggableAnnotation(ann);
		dragAnn.getViewScene().setPaint(Color.BLACK);
		dragLine.getViewScene().getAnnotations().add(ann);
		moddeller.getModeller().addTool(dragAnn, null);
		return line;
	}
	
	
	@SuppressWarnings("unchecked")
	private static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Polygon2D showFace(
		F f, 
		List<Point2DDouble> pList,
		SimpleModeller2D moddeller,
		AnnotationAdapter<?>... a
	) {
		Point2DDouble bary = new Point2DDouble(0, 0);
		for (Point2DDouble p : pList) {
			bary.x += p.x;
			bary.y += p.y;
		}
		bary.x /= pList.size();
		bary.y /= pList.size();
		Point2DList p2DList = new Point2DList(pList);
		Polygon2D poly = new Polygon2D(p2DList, true);
		DraggablePolygon2D dragPolygon = new DraggablePolygon2D(poly, 0, 0, 0, 1.0, false);
		moddeller.getModeller().addTool(dragPolygon, null);
		moddeller.addScene(dragPolygon.getViewScene());
		dragPolygon.getViewScene().setStroke(new BasicStroke(0));
		dragPolygon.getViewScene().setPaint(new Color(128,0,80,30));
		dragPolygon.getViewScene().setOutlinePaint(Color.red);
		dragPolygon.getViewScene().setPointPaint(Color.yellow);
		
		String annText = "";
		for (AnnotationAdapter<?> aa : a) {
			if (!(aa instanceof FaceAnnotation)) {
				continue;
			}
			annText += " " + ((FaceAnnotation<F>)aa).getText(f);
		}
		Annotation ann = new Annotation(annText, bary.x, bary.y, Annotation.WEST);
		DraggableAnnotation dragAnn = new DraggableAnnotation(ann);
		dragAnn.getViewScene().setPaint(Color.BLACK);
		dragPolygon.getViewScene().getAnnotations().add(ann);
		moddeller.getModeller().addTool(dragAnn, null);
		return poly;
	}
	
	
}
