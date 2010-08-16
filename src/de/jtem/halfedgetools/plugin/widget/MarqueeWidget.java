package de.jtem.halfedgetools.plugin.widget;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import de.jreality.math.Matrix;
import de.jreality.math.Pn;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.WidgetInterface;
import de.jtem.halfedgetools.plugin.WidgetPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class MarqueeWidget extends WidgetPlugin implements MouseMotionListener, MouseListener {
	
	private ContentTools
		contentTools = null;
	private View
		view = null;
	private HalfedgeInterface
		hif = null;
	private WidgetInterface
		gui = null;
	private boolean
		isDragging = false;
	private Point
		start = new Point(),
		active = new Point();
	
	
	private void updateVertexSelection() {
		Dimension size = view.getViewer().getViewingComponentSize();
		int w = Math.abs(active.x - start.x);
		int h = Math.abs(active.y - start.y);
		int xMin = Math.min(active.x, start.x) - size.width / 2;
		int yMin = Math.min(active.y, start.y) - size.height / 2;
		int xMax = xMin + w;
		int yMax = yMin + h;
		SceneGraphComponent root = view.getViewer().getSceneRoot();
		SceneGraphComponent layerRoot = hif.getActiveLayer().getLayerRoot();
		SceneGraphPath hifPath = SceneGraphUtility.getPathsBetween(root, layerRoot).get(0);
		SceneGraphPath camPath = view.getViewer().getCameraPath();
		Matrix P = new Matrix(CameraUtility.getCameraToNDC(view.getViewer()));
		Matrix C = new Matrix(camPath.getMatrix(null));
		Matrix T = new Matrix(hifPath.getMatrix(null));
		C.invert();
		T.multiplyOnLeft(C);

		HalfedgeSelection sel = new HalfedgeSelection();
		AdapterSet a = hif.getAdapters();
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		double[] homPos = {0,0,0,1};
		for (Vertex<?,?,?> v : hds.getVertices()) {
			double[] pos = a.get(Position.class, v, double[].class);
			if (pos.length > 3) {
				homPos[0] = pos[0] / pos[3];
				homPos[1] = pos[1] / pos[3];
				homPos[2] = pos[2] / pos[3];
			} else {
				homPos[0] = pos[0];
				homPos[1] = pos[1];
				homPos[2] = pos[2];
			}
			homPos[3] = 1.0;
			T.transformVector(homPos);
			P.transformVector(homPos);
			Pn.dehomogenize(homPos, homPos);
			double xPos = homPos[0] * size.width / 2;
			double yPos = -homPos[1] * size.height / 2;
			if (xPos > xMin && xPos < xMax &&
				yPos > yMin && yPos < yMax) {
				sel.setSelected(v, true);
			}
		}
		hif.setSelection(sel);
	}
	
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		contentTools = c.getPlugin(ContentTools.class);
		gui = c.getPlugin(WidgetInterface.class);
		gui.getPanel().addMouseListener(this);
		gui.getPanel().addMouseMotionListener(this);
	}
	
	
	@Override
	public void paint(Graphics2D g, JPanel canvas) {
		if (!isDragging) return;
		Stroke sOld = g.getStroke();
		int w = Math.abs(active.x - start.x);
		int h = Math.abs(active.y - start.y);
		int x = Math.min(active.x, start.x);
		int y = Math.min(active.y, start.y);

		g.setColor(new Color(255, 0, 0, 50));
		g.fillRect(x, y, w, h);
		
		float[] dash = {0f, 1f, 3f, 4f};
		BasicStroke s = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash, 0);
		g.setStroke(s);
		g.setColor(Color.RED);
		g.drawRect(x, y, w == 0 ? 1 : w, h == 0 ? 1 : h);
		
		g.setStroke(sOld);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (!e.isControlDown()) {
			isDragging = false;
			repaint();
			return;
		}
		isDragging = true;
		active = e.getPoint();
		repaint();
		updateVertexSelection();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
	}
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (!e.isControlDown()) return;
		start = e.getPoint();
		active = start;
		contentTools.setRotationEnabled(false);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		contentTools.setRotationEnabled(true);
		if (e.isControlDown()) {
			updateVertexSelection();
		}
		isDragging = false;
		repaint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

}
