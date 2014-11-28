package de.jtem.halfedgetools.plugin;

import static java.awt.event.MouseEvent.BUTTON3;
import static javax.swing.SwingUtilities.isLeftMouseButton;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

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
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.TypedSelection;
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
	private Selection 
		startSelection = new Selection();
	private SceneGraphComponent	
		marqueeRoot = new SceneGraphComponent("Marquee");
	private Appearance
		marqueeAppearance = new Appearance("Marquee Appearance");
	
	public MarqueeSelectionPlugin() {
		marqueeRoot.setGeometry(Primitives.regularPolygon(4));
		marqueeRoot.setAppearance(marqueeAppearance);
	}
	
	
	private Set<Node<?,?,?>> getMarqueeNodes(boolean v, boolean e, boolean f) {
		Set<Node<?,?,?>> result = new HashSet<>();
		Dimension size = view.getViewer().getViewingComponentSize();
		int w = Math.abs(active.x - start.x);
		int h = Math.abs(active.y - start.y);
		int xMin = Math.min(active.x, start.x) - size.width / 2;  
		int yMin = Math.min(active.y, start.y) - size.height / 2;
		int xMax = xMin + w;
		int yMax = yMin + h;
		SceneGraphComponent root = view.getViewer().getSceneRoot();
		SceneGraphComponent layerRoot = hif.getActiveLayer().getLayerRoot();
		List<SceneGraphPath> paths = SceneGraphUtility.getPathsBetween(root, layerRoot);
		if (paths.isEmpty()) return result;
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
		List<Node<?,?,?>> nodes = new LinkedList<>();
		if (v) nodes.addAll(hds.getVertices());
		if (e) nodes.addAll(hds.getEdges());
		if (f) nodes.addAll(hds.getFaces());
		for (Node<?,?,?> n : nodes) {
			double[] pos = a.get(BaryCenter4d.class, n, double[].class);
			Pn.dehomogenize(homPos, pos);
			T.transformVector(homPos);
			P.transformVector(homPos);
			Pn.dehomogenize(homPos, homPos);
			double xPos = homPos[0] * size.width / 2;
			double yPos = -homPos[1] * size.height / 2;
			if (xPos > xMin && xPos < xMax &&
				yPos > yMin && yPos < yMax) {
				result.add(n);
			}
		}
		return result;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		contentTools = c.getPlugin(ContentTools.class);
		view.getViewer().getViewingComponent().addMouseListener(this);
		view.getViewer().getViewingComponent().addMouseMotionListener(this);
	}
	
	private void cancelMarqee() {
		marqueeEnabled = false;
		hif.setSelection(startSelection);
		contentTools.setRotationEnabled(rotateWasEnabled);
		contentTools.setDragEnabled(dragWasEnabled);
	}
	

	private void updateMarqueeSelection(MouseEvent ev) {
		HalfedgeLayer layer = hif.getActiveLayer();
		Integer channel = TypedSelection.CHANNEL_DEFAULT;
		if (hif.getSelectionInterface() != null) {
			channel = hif.getSelectionInterface().getActiveInputChannel(layer);
		}
		boolean v = ev.isShiftDown() && !ev.isAltDown() && !ev.isControlDown();
		boolean e = ev.isShiftDown() && ev.isAltDown() && !ev.isControlDown();
		boolean f = ev.isShiftDown() && !ev.isAltDown() && ev.isControlDown();
		
		Set<Node<?,?,?>> marqeeNodes = getMarqueeNodes(v, e, f);
		Selection marqueeSelection = new Selection(marqeeNodes);
		Selection newSelection = new Selection(startSelection);
		newSelection.addAll(marqueeSelection, channel);
		hif.setSelection(newSelection);
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
		updateMarqueeSelection(e);
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
		if (!(e.isControlDown() || e.isAltDown() || e.isShiftDown()) || !isLeftMouseButton(e)) {
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
		contentTools.setRotationEnabled(true);
		contentTools.setDragEnabled(true);
		marqueeEnabled = false;
		updateMarqueeSelection(e);
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
