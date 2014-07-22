package de.jtem.halfedgetools.plugin.texturespace;

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
		edges = new Path2D.Double(),
		faces = new Path2D.Double();
	private HalfedgeLayer
		layer = null;
	private SceneComponent
		vertexComponent = new SceneComponent(),
		edgeComponent = new SceneComponent(),
		faceComponent = new SceneComponent();
	
	public LayerComponent() {
		vertexComponent.setPointOutlined(false);
		vertexComponent.setPointFilled(true);
		edgeComponent.setShape(edges);
		edgeComponent.setFilled(false);
		edgeComponent.setOutlined(true);
		faceComponent.setShape(faces);
		faceComponent.setFilled(true);
		faceComponent.setOutlined(false);
		addChild(faceComponent);
		addChild(edgeComponent);
		addChild(vertexComponent);
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void update() {
		faces.reset();
		edges.reset();
		vertexComponent.getPoints().clear();
		if (layer == null) return;
		
		HalfEdgeDataStructure<?, ?, ?> hds = layer.get();
		AdapterSet a = layer.getEffectiveAdapters();
		
		// faces
		for (Face f : hds.getFaces()) {
			Path2D p = new Path2D.Double();
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
			faces.append(p, false);
		}
		
		// edges
		for (Edge e : hds.getEdges()) {
			if (e.isPositive()) continue;
			double[] s = a.getD(TexturePosition2d.class, e.getStartVertex());
			double[] t = a.getD(TexturePosition2d.class, e.getTargetVertex());
			Line2D edgeLine = new Line2D.Double(s[0], s[1], t[0], t[1]);
			edges.append(edgeLine, false);
		}
		
		// vertices
		for (Vertex v : hds.getVertices()) {
			double[] p = a.getD(TexturePosition2d.class, v);
			Point2D.Double p2d = new Point2D.Double(p[0], p[1]);
			vertexComponent.getPoints().add(p2d);
		}
		
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
	
}
