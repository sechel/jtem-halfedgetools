package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_STIPPLE;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.Z_BUFFER_ENABLED;
import static java.util.Collections.unmodifiableSet;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Matrix;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.tools.ActionTool;
import de.jreality.util.Rectangle3D;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;

public class HalfedgeLayer implements ActionListener {

	private HalfedgeInterface
		hif = null;
	private HalfEdgeDataStructure<?, ?, ?>
		hds = new DefaultJRHDS();
	private IndexedFaceSet
		geometry = new IndexedFaceSet();
	private SceneGraphComponent
		layerRoot = new SceneGraphComponent("Default Layer"),
		geometryRoot = new SceneGraphComponent("Geometry"),
		selectionRoot = new SceneGraphComponent("Selection"),
		visualizersRoot = new SceneGraphComponent("Visualizers"),
		boundingBoxRoot = new SceneGraphComponent("Bounding Box");
	private Map<Integer, Edge<?,?,?>>
		edgeMap = new HashMap<Integer, Edge<?,?,?>>();
	private HalfedgeSelection
		selection = new HalfedgeSelection();
	private Set<VisualizerPlugin>
		visualizers = new TreeSet<VisualizerPlugin>();
	
	private List<IndexedFaceSet> 
		undoHistory = new ArrayList<IndexedFaceSet>();
	private int
		undoIndex = 0,
		undoSize = 10;
	
	private boolean 
		active = true;
	
	private ConverterHeds2JR 
		converterToIFS = new ConverterHeds2JR();
	private ConverterJR2Heds
		converterToHDS = new ConverterJR2Heds();
	
	private ActionTool
		actionTool = new ActionTool("PrimaryAction");

	public HalfedgeLayer() {
		layerRoot.addChild(geometryRoot);
		layerRoot.addChild(boundingBoxRoot);
		layerRoot.addChild(selectionRoot);
		layerRoot.setTransformation(new Transformation("Layer Transform"));
		geometryRoot.addTool(actionTool);
		selectionRoot.setPickable(false);
		actionTool.addActionListener(this);
		
		Appearance bBoxApp = new Appearance("Bounding Box Appearance");
		bBoxApp.setAttribute(CommonAttributes.FACE_DRAW, false);
		bBoxApp.setAttribute(CommonAttributes.VERTEX_DRAW, false);
		bBoxApp.setAttribute(CommonAttributes.EDGE_DRAW, true);
		bBoxApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		bBoxApp.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 2);
		bBoxApp.setAttribute(LINE_SHADER + "." + LINE_STIPPLE, true);
		bBoxApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.WHITE);
		bBoxApp.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, 1.5f);
		bBoxApp.setAttribute(LINE_SHADER + "." + Z_BUFFER_ENABLED, true);
		boundingBoxRoot.setAppearance(bBoxApp);
	}
	
	
	public HalfedgeLayer(HalfedgeInterface hif) {
		this();
		this.hif = hif;
		undoHistory.add(undoIndex, geometry);
	}
	
	public HalfedgeLayer(IndexedFaceSet geometry, HalfedgeInterface hif) {
		this();
		this.hif = hif;
		this.geometry = geometry;
		geometryRoot.setGeometry(geometry);
		undoHistory.add(undoIndex, geometry);
		convertFaceSet(getEffectiveAdapters());
	}
	
	
	public Transformation getLayerTransformation() {
		return layerRoot.getTransformation();
	}
	public void setLayerTransformation(Transformation T) {
		layerRoot.setTransformation(T);
	}
	
	
	private AdapterSet getVisualizerAdapters() {
		AdapterSet r = new AdapterSet(hif.getAdapters());
		for (VisualizerPlugin p : visualizers) {
			r.addAll(p.getAdapters());
		}
		return r;
	}
	
	private AdapterSet getEffectiveAdapters(AdapterSet a) {
		AdapterSet effectiveAdapters = new AdapterSet();
		effectiveAdapters.addAll(a);
		effectiveAdapters.addAll(hif.getAdapters());
		effectiveAdapters.addAll(getVisualizerAdapters());
		return effectiveAdapters;
	}
	
	private AdapterSet getEffectiveAdapters() {
		return getEffectiveAdapters(new AdapterSet());
	}
	
	
	private void updateVisualizersGeometry(AdapterSet a) {
		layerRoot.removeChild(visualizersRoot);
		visualizersRoot = new SceneGraphComponent();
		visualizersRoot.setName("Visualizers");
		for (VisualizerPlugin vp : visualizers) {
			vp.initVisualization(hds, a, hif);
			if (vp.getComponent() != null) {
				visualizersRoot.addChild(vp.getComponent());
			}
		}
		layerRoot.addChild(visualizersRoot);
	}
	
	protected void setBoundingBox(Rectangle3D bbox, Matrix T) {
		BoundingBoxUtility.removeZeroExtends(bbox);
		IndexedFaceSet bBoxGeometry = IndexedFaceSetUtility.representAsSceneGraph(bbox);
		boundingBoxRoot.setGeometry(bBoxGeometry);
		boundingBoxRoot.setTransformation(new Transformation(T.getArray()));
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNoUndo(HDS hds, AdapterSet a) {
		this.hds = hds;
		convertHDS(getEffectiveAdapters(a));
		clearSelection();
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds, AdapterSet a) {
		setNoUndo(hds, a);
		updateUndoList();
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds) {
		set(hds, new AdapterSet());
	}
	
	public void setNoUndo(IndexedFaceSet ifs, AdapterSet a) {
		geometry = ifs;
		geometryRoot.setGeometry(geometry);
		convertFaceSet(getEffectiveAdapters(a));
		clearSelection();
	}
	
	public void set(IndexedFaceSet ifs, AdapterSet a) {
		setNoUndo(ifs, a);
		updateUndoList();
	}
	
	public void set(IndexedFaceSet ifs) {
		set(ifs, new AdapterSet());
	}
	
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS template, AdapterSet a) {
		if (template.getClass().isAssignableFrom(hds.getClass())) {
			return (HDS)hds;
		}
		hds = template;
		convertFaceSet(getEffectiveAdapters(a));
		return template;
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS template) {
		return get(template, new AdapterSet());
	}
		
	
	public HalfEdgeDataStructure<?, ?, ?> get() {
		return hds;
	}
	
	
	public IndexedFaceSet getGeometry() {
		return geometry;
	}
	
	
	private void convertFaceSet(AdapterSet a) {
		hds.clear();
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(geometry);
		if (!oriented) {
			System.err.println("Not orientable face set in convertFaceSet()");
			return;
		}
		converterToHDS.ifs2heds(geometry, hds, a, edgeMap);
		updateBoundingBox();
	}
	
	
	private void convertHDS(AdapterSet a) {
		clearSelection();
		AdapterSet ea = getEffectiveAdapters(a);
		geometry = converterToIFS.heds2ifs(hds, ea, edgeMap);
		geometryRoot.setGeometry(geometry);
		updateVisualizersGeometry(ea);
		updateBoundingBox();
	}
	

	public Map<Integer, ? extends Edge<?, ?, ?>> getEdgeMap() {
		return edgeMap;
	}
	
	public HalfedgeSelection getSelection() {
		return selection;
	}
	public void setSelection(HalfedgeSelection sel) {
		this.selection = sel;
		updateSelection();
		hif.fireSelectionChanged(selection);
	}
	
	public void clearSelection() {
		this.selection.clear();
		updateSelection();
		hif.fireSelectionChanged(selection);
	}
	
	
	public boolean isActive() {
		return active;
	}
	public void setActive(final boolean active) {
		this.active = active;
		boundingBoxRoot.setVisible(active);
		if (active) {
			geometryRoot.addTool(actionTool);
		} else {
			geometryRoot.removeTool(actionTool);
		}
	}
	
	
	protected void updateSelection() {
		layerRoot.removeChild(selectionRoot);
		AdapterSet a = hif.getAdapters();
		selectionRoot = selection.createSelectionGeometry(a);
		selectionRoot.setPickable(false);
		Appearance app = selectionRoot.getAppearance();
		hif.createSelectionAppearance(app, this);
		layerRoot.addChild(selectionRoot);
	}
	
	
	protected void updateBoundingBox() {
		Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(geometryRoot);
		BoundingBoxUtility.removeZeroExtends(bbox);
		IndexedFaceSet ifs = IndexedFaceSetUtility.representAsSceneGraph(bbox);
		ifs.setName("Bounding Box");
		boundingBoxRoot.setGeometry(ifs);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		ToolContext tc = (ToolContext)e.getSource();
		SelectionUpdater updater = new SelectionUpdater(tc);
		SwingUtilities.invokeLater(updater);
	}
	
	
	private class SelectionUpdater implements Runnable {
		
		private ToolContext
			toolContext = null;
		
		public SelectionUpdater(ToolContext toolContext) {
			this.toolContext = toolContext;
		}

		@Override
		public void run() {
			PickResult pr = null;
			try {
				pr = toolContext.getCurrentPick();
			} catch (Exception e) {
				return;
			}
			if (pr == null) return;
			int index = pr.getIndex();
			if (index < 0) return;
			
			switch (pr.getPickType()) {
			case PickResult.PICK_TYPE_POINT:
				if (hds.numVertices() <= index) return;
				Vertex<?,?,?> v = hds.getVertex(index);
				boolean selected = selection.isSelected(v);
				selection.setSelected(v, !selected);
				break;
			case PickResult.PICK_TYPE_LINE:
				Edge<?,?,?> e = edgeMap.get(index);
				if (e == null) {
					System.err.println("Edge index not found");
					return;
				}
				selected = selection.isSelected(e);
				selection.setSelected(e, !selected);
				selected = selection.isSelected(e.getOppositeEdge());
				selection.setSelected(e.getOppositeEdge(), !selected);
				break;
			case PickResult.PICK_TYPE_FACE:
				if (hds.numFaces() <= index) return;
				Face<?,?,?> f = hds.getFace(index);
				selected = selection.isSelected(f);
				selection.setSelected(f, !selected);
				break;
			default:
				return;
			}
			hif.fireSelectionChanged(selection);
			updateSelection();
		}
	}
	
	public void addVisualizer(VisualizerPlugin v) {
		visualizers.add(v);
	}
	public void removeVisualizer(VisualizerPlugin v) {
		visualizers.remove(v);
	}
	public Set<VisualizerPlugin> getVisualizers() {
		return unmodifiableSet(visualizers);
	}
	
	public boolean isVisible() {
		return layerRoot.isVisible();
	}

	public void setVisible(boolean visible) {
		layerRoot.setVisible(visible);
	}
	
	public String getName() {
		return getLayerRoot().getName();
	}
	public void setName(String name) {
		layerRoot.setName(name);
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public SceneGraphComponent getLayerRoot() {
		return layerRoot;
	}
	
	public boolean canUndo() {
		return undoIndex > 0;
	}
	
	public boolean canRedo() {
		return undoIndex < undoHistory.size() - 1;
	}
	
	private void updateUndoList() {
		undoHistory = undoHistory.subList(0, undoIndex + 1);
		undoHistory.add(geometry);
		if(undoHistory.size() > undoSize) {
			undoHistory.remove(0);
		}
		undoIndex = undoHistory.size() - 1;		
	}
	
	public void undo() {
		if (!canUndo()) return;
		undoIndex--;
		geometry = undoHistory.get(undoIndex);
		geometryRoot.setGeometry(geometry);
		convertFaceSet(getEffectiveAdapters());
		convertHDS(getEffectiveAdapters());
	}
	
	public void redo() {
		if (!canRedo()) return;
		undoIndex++;
		geometry = undoHistory.get(undoIndex);
		geometryRoot.setGeometry(geometry);
		convertFaceSet(getEffectiveAdapters());
		convertHDS(getEffectiveAdapters());
	}
	
}
