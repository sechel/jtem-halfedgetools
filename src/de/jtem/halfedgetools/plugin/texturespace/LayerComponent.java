package de.jtem.halfedgetools.plugin.texturespace;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.java2d.SceneComponent;

public class LayerComponent extends SceneComponent {

	private Path2D
		edges = new Path2D.Float(),
		faces = new Path2D.Float(),
		edgeSelection = new Path2D.Float(),
		faceSelection = new Path2D.Float();
	private HalfedgeLayer
		layer = null;
	private SceneComponent
		vertexComponent = new SceneComponent(),
		edgeComponent = new SceneComponent(),
		faceComponent = new SceneComponent(),
		vertexSelectionComponent = new SceneComponent(),
		edgeSelectionComponent = new SceneComponent(),
		faceSelectionComponent = new SceneComponent();
	
	public LayerComponent() {
		vertexComponent.setPointOutlined(false);
		vertexComponent.setPointFilled(true);
		edgeComponent.setShape(edges);
		edgeComponent.setFilled(false);
		edgeComponent.setOutlined(true);
		faceComponent.setShape(faces);
		faceComponent.setFilled(true);
		faceComponent.setOutlined(false);
		vertexSelectionComponent.setPointFilled(true);
		vertexSelectionComponent.setPointOutlined(true);
		edgeSelectionComponent.setShape(edgeSelection);
		edgeSelectionComponent.setFilled(false);
		edgeSelectionComponent.setOutlined(true);
		faceSelectionComponent.setShape(faceSelection);
		faceSelectionComponent.setOutlined(false);
		faceSelectionComponent.setFilled(true);
		addChild(faceComponent);
		addChild(edgeComponent);
		addChild(vertexComponent);
		addChild(faceSelectionComponent);
		addChild(edgeSelectionComponent);
		addChild(vertexSelectionComponent);
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
	}
	
	public void update() {
		updateGeometry();
		updateSelection();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateGeometry() {
		vertexComponent.getPoints().clear();
		edges.reset();
		faces.reset();
		if (layer == null) return;
		HalfEdgeDataStructure<?, ?, ?> hds = layer.get();
		AdapterSet a = layer.getEffectiveAdapters();
		
		// faces
		for (Face f : hds.getFaces()) {
			Shape faceShape = getFaceShape(a, f);
			faces.append(faceShape, false);
		}
		// edges
		for (Edge e : hds.getEdges()) {
			if (e.isPositive()) continue;
			Shape edgeShape = getEdgeShape(e, a);
			edges.append(edgeShape, false);
		}
		// vertices
		for (Vertex v : hds.getVertices()) {
			double[] p = a.getD(TexturePosition2d.class, v);
			Point2D.Double p2d = new Point2D.Double(p[0], p[1]);
			vertexComponent.getPoints().add(p2d);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateSelection() {
		vertexSelectionComponent.getPoints().clear();
		edgeSelection.reset();
		faceSelection.reset();
		if (layer == null) return;
		AdapterSet a = layer.getEffectiveAdapters();
		// selection
		for (Vertex v : layer.getSelection().getVertices()) {
			double[] p = a.getD(TexturePosition2d.class, v);
			Point2D.Double p2d = new Point2D.Double(p[0], p[1]);
			vertexSelectionComponent.getPoints().add(p2d);
		}
		for (Edge e : layer.getSelection().getEdges()) {
			if (e.isPositive()) continue;
			Shape edgeShape = getEdgeShape(e, a);
			edgeSelection.append(edgeShape, false);
		}
		for (Face f : layer.getSelection().getFaces()) {
			Shape faceShape = getFaceShape(a, f);
			faceSelection.append(faceShape, false);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Shape getEdgeShape(Edge e, AdapterSet a) {
		double[] s = a.getD(TexturePosition2d.class, e.getStartVertex());
		double[] t = a.getD(TexturePosition2d.class, e.getTargetVertex());
		return new Line2D.Float((float)s[0], (float)s[1], (float)t[0], (float)t[1]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Shape getFaceShape(AdapterSet a, Face f) {
		Path2D p = new Path2D.Float();
		boolean start = true;
		for (Object ov : HalfEdgeUtils.boundaryVertices(f)) {
			Vertex<?, ?, ?> v = (Vertex<?, ?, ?>)ov;
			double[] t = a.getD(TexturePosition2d.class, v);
			if (start) {
				p.moveTo(t[0], t[1]);
				start = false;
			} else {
				p.lineTo(t[0], t[1]);
			}
		}
		p.closePath();
		return p;
	}
	
	public SceneComponent getVertexComponent() {
		return vertexComponent;
	}
	
	public SceneComponent getEdgeComponent() {
		return edgeComponent;
	}
	
	public SceneComponent getFaceComponent() {
		return faceComponent;
	}
	
	public SceneComponent getVertexSelectionComponent() {
		return vertexSelectionComponent;
	}
	
	public SceneComponent getEdgeSelectionComponent() {
		return edgeSelectionComponent;
	}
	
	public SceneComponent getFaceSelectionComponent() {
		return faceSelectionComponent;
	}
	
}
