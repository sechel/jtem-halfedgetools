package de.jtem.halfedgetools.plugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.geometry.Primitives;
import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position4d;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class MarqueeSelectionPlugin extends Plugin implements MouseMotionListener, MouseListener {
	
	private ContentTools
		contentTools = null;
	private View
		view = null;
	private HalfedgeInterface
		hif = null;
	private boolean
		activated = true,
		rotateWasEnabled = false,
		dragWasEnabled = false,
		marqueeEnabled = false;
	private Point
		start = new Point(),
		active = new Point();
	private boolean 
		selectInside = false;
	private HalfedgeSelection 
		startSelection = new HalfedgeSelection();
	private SceneGraphComponent	
		marqueeRoot = new SceneGraphComponent("Marquee");
	private Appearance
		marqueeAppearance = new Appearance("Marquee Appearance");
	
	public MarqueeSelectionPlugin() {
		marqueeRoot.setGeometry(Primitives.regularPolygon(4));
		marqueeRoot.setAppearance(marqueeAppearance);
	}
	
	
	private Set<Vertex<?,?,?>> getMarqueeVertices() {
		Set<Vertex<?,?,?>> vSet = new HashSet<Vertex<?,?,?>>();
		Dimension size = view.getViewer().getViewingComponentSize();
		int sign = active.x - start.x;
		selectInside = (sign >=0);
		int w = Math.abs(active.x - start.x);
		int h = Math.abs(active.y - start.y);
		int xMin = Math.min(active.x, start.x) - size.width / 2;  
		int yMin = Math.min(active.y, start.y) - size.height / 2;
		int xMax = xMin + w;
		int yMax = yMin + h;
		SceneGraphComponent root = view.getViewer().getSceneRoot();
		SceneGraphComponent layerRoot = hif.getActiveLayer().getLayerRoot();
		List<SceneGraphPath> paths = SceneGraphUtility.getPathsBetween(root, layerRoot);
		if (paths.isEmpty()) return vSet;
		SceneGraphPath hifPath = paths.get(0);
		SceneGraphPath camPath = view.getViewer().getCameraPath();
		Matrix P = new Matrix(CameraUtility.getCameraToNDC(view.getViewer()));
		Matrix C = new Matrix(camPath.getMatrix(null));
		Matrix T = new Matrix(hifPath.getMatrix(null));
		C.invert();
		T.multiplyOnLeft(C);
		AdapterSet a = hif.getAdapters();
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		double[] homPos = {0,0,0,1};
		for (Vertex<?,?,?> v : hds.getVertices()) {
			double[] pos = a.get(Position4d.class, v, double[].class);
			Pn.dehomogenize(homPos, pos);
			T.transformVector(homPos);
			P.transformVector(homPos);
			Pn.dehomogenize(homPos, homPos);
			double xPos = homPos[0] * size.width / 2;
			double yPos = -homPos[1] * size.height / 2;
			if (xPos > xMin && xPos < xMax &&
				yPos > yMin && yPos < yMax) {
				vSet.add(v);
			}
		}
		return vSet;
	}
	
	private 
	 	Set<Face<?,?,?>> getMarqueeFaces(Set<Vertex<?,?,?>> verts) {
		Set<Face<?,?,?>> result = new HashSet<Face<?,?,?>>();
		if (selectInside) {//inside
			for (Vertex<?,?,?> v : verts){
				for(Face<?,?,?> f : getFaceStar(v) ){
					boolean containsAll = true;
					for(Vertex<?,?,?> vf : boundaryVertices(f)){
						containsAll &= verts.contains(vf);
					}
					if(containsAll) {
						result.add(f);
					}
				}
			}
		} else {//touching
			for (Vertex<?,?,?> v : verts){
				for(Face<?,?,?> f : getFaceStar(v) ){		
					result.add(f);
				}
			}
		}
		return result;
	}
	
	private
	 	Set<Edge<?,?,?>> getMarqueeEdges(Set<Vertex<?,?,?>> verts) {
		Set<Edge<?,?,?>> result = new HashSet<Edge<?,?,?>>();
		if (selectInside){//inside
			for (Vertex<?,?,?> v : verts){ 
				for(Edge<?,?,?> e : incomingEdges(v) ){
					if(verts.contains(e.getStartVertex())) {
						result.add(e);
						result.add(e.getOppositeEdge());
					}
				}
			}
			return result;
	
		} else {//touching
			for (Vertex<?,?,?> v : verts){
				for(Edge<?,?,?> e : incomingEdges(v) ){
					result.add(e);
					result.add(e.getOppositeEdge());
				}
			}
			return result;
		}
	}
	

	
	private static List<Edge<?,?,?>> incomingEdges(Vertex<?,?,?> vertex){
		Edge<?,?,?> e0 = vertex.getIncomingEdge();
		if (e0 == null) {
			return Collections.emptyList();
		}
		LinkedList<Edge<?,?,?>> result = new LinkedList<Edge<?,?,?>>();
		Edge<?,?,?> e = e0;
		do {
			if (vertex != e.getTargetVertex()) {
				throw new RuntimeException("Edge " + e + " does not have vertex " + vertex + " as target vertex, " +
				"although it is the opposite of the next edge of an edge which does.");
			}
			result.add(e);
			e = e.getNextEdge();
			if (e == null) {
				throw new RuntimeException("Some edge has null as next edge.");
			}
			e = e.getOppositeEdge();
			if (e == null) {
				throw new RuntimeException("Some edge has null as opposite edge.");
			}
		} while (e != e0);
		return result;
	}
	
	 private static 	
	    	List<Face<?,?,?>> getFaceStar(Vertex<?,?,?> vertex) {
	        List<Face<?,?,?>> faceStar = new ArrayList<Face<?,?,?>>();
	        for (Edge<?,?,?> e : getEdgeStar(vertex)){
	        	if (e.getLeftFace() != null)
	        		faceStar.add(e.getLeftFace());
	        }
	        return faceStar;
	    }
	 
	 public static 
	 	List<Vertex<?,?,?>> boundaryVertices(Face<?,?,?> face) {
		Collection<Edge<?,?,?>> b = boundaryEdges(face);
		LinkedList<Vertex<?,?,?>> vList = new LinkedList<Vertex<?,?,?>>();
		for (Edge<?,?,?> e : b) {
			vList.add(e.getTargetVertex());
		}
		return vList;
	}
	 
	 private static  List<Edge<?,?,?>> boundaryEdges(Face<?,?,?> face) {
			final Edge<?,?,?> e0 = face.getBoundaryEdge();
			if (e0 == null) {
				return Collections.emptyList();
			}
			LinkedList<Edge<?,?,?>> result = new LinkedList<Edge<?,?,?>>();
			Edge<?,?,?> e = e0;
			do {
				if (face != e.getLeftFace()) {
					throw new RuntimeException("Edge " + e + " does not have face " + face + " as left face, " +
							"although it is the next edge of an edge which does.");
				}
				result.add(e);
				e = e.getNextEdge();
				if (e == null) {
					throw new RuntimeException("Some edge has null as next edge.");
				}
			} while (e != e0);
			return result;
		}
	
	 private static 
	 	 List<Edge<?,?,?>> getEdgeStar(Vertex<?,?,?> vertex){
		 List<Edge<?,?,?>> edgeStar = new LinkedList<Edge<?,?,?>>();
		 if (vertex.getIncomingEdge() == null || !vertex.getIncomingEdge().isValid())
			 return Collections.emptyList();
		 Edge<?,?,?> actEdge = vertex.getIncomingEdge();
		 do {
			if (actEdge == null)
			return Collections.emptyList();
	     	edgeStar.add(actEdge);
	     	if (actEdge.getNextEdge() == null)
	     		return Collections.emptyList();
	     	actEdge = actEdge.getNextEdge().getOppositeEdge();
		} while (actEdge != vertex.getIncomingEdge());
		return edgeStar;
	 }
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		contentTools = c.getPlugin(ContentTools.class);
		view.getViewer().getViewingComponent().addMouseListener(this);
		view.getViewer().getViewingComponent().addMouseMotionListener(this);
//		Scene scene = c.getPlugin(Scene.class);
//		scene.getSceneRoot().addChild(marqueeRoot);
//		gui = c.getPlugin(WidgetInterface.class);
//		gui.getPanel().addMouseListener(this);
//		gui.getPanel().addMouseMotionListener(this);
	}
	
	private void updateMarquee() {
//		Component comp = view.getViewer().getViewingComponent();
//		SceneGraphPath camPath = view.getViewer().getCameraPath();
//		Camera cam = camPath.getLastComponent().getCamera();
//		Matrix P = new Matrix(CameraUtility.getCameraToNDC(view.getViewer()));
//		Matrix C = new Matrix(camPath.getInverseMatrix(null));
//		Matrix T = new Matrix();
//		T.multiplyOnLeft(C);
//		T.multiplyOnLeft(P);
//		T.invert();
//		double w = Math.abs(active.x - start.x) / (double)comp.getWidth();
//		double h = Math.abs(active.y - start.y) / (double)comp.getHeight();
//		double x = Math.min(active.x, start.x) / (double)comp.getWidth() - 0.5; 
//		double y = Math.min(active.y, start.y) / (double)comp.getHeight() - 0.5;
//		MatrixBuilder mb = MatrixBuilder.euclidean();
//		mb.translate(x, y, 0);
//		mb.scale(w, h, 1.0);
//		mb.translate(0,0,cam.getNear());
//		T.multiplyOnLeft(mb.getMatrix());
//		T.assignTo(marqueeRoot);
	}
	
	
//	@Override
//	public void paint(Graphics2D g, JPanel canvas) {
//		if (!marqueeEnabled) return;
//		Stroke sOld = g.getStroke();
//		int w = Math.abs(active.x - start.x);
//		int h = Math.abs(active.y - start.y);
//		int x = Math.min(active.x, start.x);
//		int y = Math.min(active.y, start.y);
//
//		g.setColor(new Color(255, 0, 0, 50));
//		g.fillRect(x, y, w, h);
//		
//		float[] dash = {0f, 1f, 3f, 4f};
//		BasicStroke s = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
//		g.setStroke(s);
//		g.setColor(Color.RED);
//		g.drawRect(x, y, w == 0 ? 1 : w, h == 0 ? 1 : h);
//		
//		g.setStroke(sOld);
//	}

	
	private void cancelMarqee() {
		marqueeEnabled = false;
		hif.setSelection(startSelection);
		updateMarquee();
		contentTools.setRotationEnabled(rotateWasEnabled);
		contentTools.setDragEnabled(dragWasEnabled);
	}
	
	
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if (!activated) return;
		if (!marqueeEnabled) return;
		if (!(e.isControlDown()||e.isAltDown()||e.isShiftDown())) {
			cancelMarqee();
			return;
		}
		active = e.getPoint();
		updateMarquee();
		
		HalfedgeSelection sel = new HalfedgeSelection();
		Set<Vertex<?,?,?>> marqeeVertices = getMarqueeVertices();
		if (marqeeVertices.isEmpty()) {
			hif.setSelection(startSelection);
			return;
		}
		
		if(e.isShiftDown()){
			for (Vertex<?,?,?> v : marqeeVertices) {
				sel.setSelected(v, true, hif.getSelectionColor());
			}
		}
		if(e.isAltDown()){
			for(Edge<?,?,?> edge : getMarqueeEdges(marqeeVertices)){
				sel.setSelected(edge, true, hif.getSelectionColor());
			}
		}
		
		if(e.isControlDown()){
			for (Face<?,?,?> face : getMarqueeFaces(marqeeVertices)){
				sel.setSelected(face, true, hif.getSelectionColor());
			}
		}	
		sel.addAll(startSelection.getNodes());
		hif.setSelection(sel);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!activated) return;
		if (!(e.isControlDown()||e.isAltDown()||e.isShiftDown())) {
			return;
		}
		marqueeEnabled = true;
		start = e.getPoint();
		active = start;
		rotateWasEnabled = contentTools.isRotationEnabled();
		dragWasEnabled = contentTools.isDragEnabled();
		contentTools.setRotationEnabled(false);
		contentTools.setDragEnabled(false);
		startSelection = hif.getSelection();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (!marqueeEnabled) return;
		if (!(e.isControlDown()||e.isAltDown()||e.isShiftDown())) {
			cancelMarqee();
			return;
		}
		Set<Vertex<?,?,?>> marqeeVertices = getMarqueeVertices();
		HalfedgeSelection sel = new HalfedgeSelection();
		contentTools.setRotationEnabled(true);
		contentTools.setDragEnabled(true);
		if (e.isShiftDown()) {
			for (Vertex<?,?,?> v : marqeeVertices) {
				sel.setSelected(v, true, hif.getSelectionColor());
			}
		}
		if (e.isAltDown()){
			for(Edge<?,?,?> edge : getMarqueeEdges(marqeeVertices)){
				sel.setSelected(edge, true, hif.getSelectionColor());
			}
		}
		if (e.isControlDown()){
			for (Face<?,?,?> face : getMarqueeFaces(marqeeVertices)){
				sel.setSelected(face, true, hif.getSelectionColor());
			}
		}
		sel.addAll(startSelection.getNodes());
		hif.setSelection(sel);
		marqueeEnabled = false;
		updateMarquee();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
	
	public void setActivated(boolean activated) {
		this.activated = activated;
		if (!activated) {
			cancelMarqee();
		}
	}
	
	public boolean isActivated() {
		return activated;
	}

}
