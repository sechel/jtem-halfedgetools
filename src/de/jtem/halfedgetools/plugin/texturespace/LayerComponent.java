package de.jtem.halfedgetools.plugin.texturespace;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.TextureBaryCenter2d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.java2d.Annotation;
import de.jtem.java2d.SceneComponent;

public class LayerComponent extends SceneComponent {

	private double[]
		defaultCoord = {0, 0};
	private Path2D
		edges = new Path2D.Float(),
		faces = new Path2D.Float();
	private HalfedgeLayer
		layer = null;
	private SceneComponent
		vertexComponent = new SceneComponent(),
		edgeComponent = new SceneComponent(),
		faceComponent = new SceneComponent(),
		vertexSelectionComponent = new SceneComponent(),
		edgeSelectionComponent = new SceneComponent(),
		faceSelectionComponent = new SceneComponent(),
		addonComponentBack = new SceneComponent(),
		addonComponentFront = new SceneComponent();
	
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
		edgeSelectionComponent.setFilled(false);
		edgeSelectionComponent.setOutlined(true);
		faceSelectionComponent.setOutlined(false);
		faceSelectionComponent.setFilled(true);
		addChild(addonComponentBack);
		addChild(faceComponent);
		addChild(edgeComponent);
		addChild(vertexComponent);
		addChild(addonComponentFront);
		addChild(faceSelectionComponent);
		addChild(edgeSelectionComponent);
		addChild(vertexSelectionComponent);
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
	}
	
	public synchronized void update() {
		updateGeometry();
		updateSelection();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void updateGeometry() {
		vertexComponent.getPoints().clear();
		vertexComponent.getAnnotations().clear();
		edges.reset();
		edgeComponent.getAnnotations().clear();
		faces.reset();
		faceComponent.getAnnotations().clear();
		if (layer == null) return;
		HalfEdgeDataStructure<?, ?, ?> hds = layer.get();
		AdapterSet a = layer.getEffectiveAdapters();
		
		// faces
		for (Face f : new LinkedList<Face>(hds.getFaces())) {
			if (!f.isValid()) continue;
			Shape faceShape = getFaceShape(a, f);
			faces.append(faceShape, false);
			double[] p = a.getDefault(TextureBaryCenter2d.class, f, defaultCoord);
			Annotation indexAnnotation = new Annotation("" + f.getIndex(), p[0], p[1], Annotation.CENTER);
			faceComponent.getAnnotations().add(indexAnnotation);
		}
		// edges
		for (Edge e : new LinkedList<Edge>(hds.getEdges())) {
			if (e.isPositive() || !e.isValid()) continue;
			Shape edgeShape = getEdgeShape(e, a);
			edges.append(edgeShape, false);
			double[] p = a.getDefault(TextureBaryCenter2d.class, e, defaultCoord);
			String annotationText = e.getOppositeEdge().getIndex() + "/" + e.getIndex();
			Annotation indexAnnotation = new Annotation(annotationText, p[0], p[1], Annotation.CENTER);
			edgeComponent.getAnnotations().add(indexAnnotation);
		}
		// vertices
		for (Vertex v : new LinkedList<Vertex>(hds.getVertices())) {
			if (!v.isValid()) continue;
			double[] p = a.getDefault(TextureBaryCenter2d.class, v, defaultCoord);
			Point2D.Double p2d = new Point2D.Double(p[0], p[1]);
			vertexComponent.getPoints().add(p2d);
			Annotation indexAnnotation = new Annotation("" + v.getIndex(), p[0], p[1], Annotation.SOUTH);
			vertexComponent.getAnnotations().add(indexAnnotation);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void updateSelection() {
		if (layer == null) return;
		SelectionInterface sif = layer.getHalfedgeInterface().getSelectionInterface();
		Map<Integer, Color> channelColors = sif.getChannelColors(layer);
		vertexSelectionComponent.removeAllChildren();
		edgeSelectionComponent.removeAllChildren();
		faceSelectionComponent.removeAllChildren();
		AdapterSet a = layer.getEffectiveAdapters();
		Selection s = new Selection(layer.getSelection());
		Map<Integer, SceneComponent> vMap = new HashMap<Integer, SceneComponent>();
		Map<Integer, SceneComponent> eMap = new HashMap<Integer, SceneComponent>();
		Map<Integer, SceneComponent> fMap = new HashMap<Integer, SceneComponent>();
		// selection
		for (Vertex v : s.getVertices()) {
			if (!v.isValid()) continue;
			double[] p = a.getDefault(TexturePosition2d.class, v, defaultCoord);
			Point2D.Double p2d = new Point2D.Double(p[0], p[1]);
			Integer channel = s.getChannel(v);
			SceneComponent vComp = vMap.get(channel);
			if (!vMap.containsKey(channel)) {
				vComp = new SceneComponent();
				vComp.setName("Vertex Channel " + channel);
				vComp.setPointPaint(channelColors.get(channel));
				vertexSelectionComponent.addChild(vComp);
				vMap.put(channel, vComp);
			}
			vComp.getPoints().add(p2d);
		}
		Set<Edge> doneEdges = new HashSet<Edge>();
		for (Edge e : s.getEdges()) {
			if (!e.isValid() || doneEdges.contains(e)) {
				continue;
			}
			Shape edgeShape = getEdgeShape(e, a);
			Integer channel = s.getChannel(e);
			SceneComponent eComp = eMap.get(channel);
			if (!eMap.containsKey(channel)) {
				eComp = new SceneComponent();
				eComp.setName("Edge Channel " + channel);
				eComp.setOutlinePaint(channelColors.get(channel));
				eComp.setShape(new Path2D.Float());
				eMap.put(channel, eComp);
				edgeSelectionComponent.addChild(eComp);
			}
			Path2D path = (Path2D)eComp.getShape();
			path.append(edgeShape, false);
			doneEdges.add(e);
			doneEdges.add(e.getOppositeEdge());
		}
		for (Face f : s.getFaces()) {
			if (!f.isValid()) return;
			Shape faceShape = getFaceShape(a, f);
			Integer channel = s.getChannel(f);
			SceneComponent fComp = fMap.get(channel);
			if (!fMap.containsKey(channel)) {
				fComp = new SceneComponent();
				fComp.setName("Face Channel " + channel);
				Color fc = channelColors.get(channel);
				int selAlpha = (int)(0.6 * 255);
				Color faceSelColor = new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), selAlpha);
				fComp.setPaint(faceSelColor);
				fComp.setShape(new Path2D.Float());
				fMap.put(channel, fComp);
				faceSelectionComponent.addChild(fComp);
			}
			Path2D path = (Path2D)fComp.getShape();
			path.append(faceShape, false);
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Shape getEdgeShape(Edge e, AdapterSet a) {
		double[] s = a.getDefault(TexturePosition2d.class, e.getStartVertex(), defaultCoord);
		double[] t = a.getDefault(TexturePosition2d.class, e.getTargetVertex(), defaultCoord);
		return new Line2D.Float((float)s[0], (float)s[1], (float)t[0], (float)t[1]);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Shape getFaceShape(AdapterSet a, Face f) {
		Path2D p = new Path2D.Float();
		boolean start = true;
		for (Object ov : HalfEdgeUtils.boundaryVertices(f)) {
			Vertex<?, ?, ?> v = (Vertex<?, ?, ?>)ov;
			double[] t = a.getDefault(TexturePosition2d.class, v, defaultCoord);
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
	
	public SceneComponent getAddonComponentBack() {
		return addonComponentBack;
	}
	
	public SceneComponent getAddonComponentFront() {
		return addonComponentFront;
	}
	
}
