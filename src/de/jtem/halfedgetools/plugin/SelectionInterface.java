package de.jtem.halfedgetools.plugin;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import de.jreality.plugin.JRViewerUtility;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.FaceDragEvent;
import de.jreality.tools.FaceDragListener;
import de.jreality.tools.LineDragEvent;
import de.jreality.tools.LineDragListener;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SelectionInterface extends Plugin implements PointDragListener, LineDragListener, FaceDragListener, HalfedgeListener {

	private HalfedgeInterface
		hif = null;
	private DragEventTool
		tool = new DragEventTool();
	
	public SelectionInterface() {
		tool.addPointDragListener(this);
		tool.addLineDragListener(this);
		tool.addFaceDragListener(this);
	}
	
	@Override
	public void halfedgeChanged(HalfedgeInterface hif) {
		hif.getSelection().clear();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
		JRViewerUtility.getContentPlugin(c).addContentTool(tool);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		hif.removeHalfedgeListener(this);
		JRViewerUtility.getContentPlugin(c).removeContentTool(tool);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	@Override
	public void pointDragEnd(PointDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.getCache();
		if (hds == null) return;
		if (hds.numVertices() <= e.getIndex()) return;
		if (e.getIndex() < 0) return;
		Vertex<?,?,?> v = hds.getVertex(e.getIndex());
		boolean selected = hif.getSelection().isSelected(v);
		hif.getSelection().setSelected(v, !selected);
		hif.updateStates();
	}
	
	@Override
	public void pointDragStart(PointDragEvent e) {
	}
	@Override
	public void pointDragged(PointDragEvent e) {
	}

	@Override
	public void lineDragEnd(LineDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.getCache();
		if (hds == null) return;
		Map<? extends Edge<?,?,?>, Integer> edgeMap = hif.getEdgeMap();
		TreeSet<Integer> indexSet = new TreeSet<Integer>();
		indexSet.addAll(edgeMap.values());
		if (!indexSet.contains(e.getIndex())) {
			System.err.println("Edge index not found");
			return;
		}
		List<Edge<?,?,?>> eList = new LinkedList<Edge<?,?,?>>();
		for (Edge<?,?,?> edge : edgeMap.keySet()) {
			if (edgeMap.get(edge).equals(e.getIndex())) {
				eList.add(edge);
			}
		}
		for (Edge<?,?,?> edge : eList) {
			boolean selected = hif.getSelection().isSelected(edge);
			hif.getSelection().setSelected(edge, !selected);
		}
		hif.updateStates();
	}
	@Override
	public void lineDragStart(LineDragEvent e) {
	}
	@Override
	public void lineDragged(LineDragEvent e) {
	}
	
	@Override
	public void faceDragEnd(FaceDragEvent e) {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.getCache();
		if (hds == null) return;
		if (hds.numFaces() <= e.getIndex()) return;
		if (e.getIndex() < 0) return;
		Face<?,?,?> f = hds.getFace(e.getIndex());
		boolean selected = hif.getSelection().isSelected(f);
		hif.getSelection().setSelected(f, !selected);
		hif.updateStates();
	}
	@Override
	public void faceDragStart(FaceDragEvent e) {
	}
	@Override
	public void faceDragged(FaceDragEvent e) {
	}
	
}
