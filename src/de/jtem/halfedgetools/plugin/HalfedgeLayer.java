package de.jtem.halfedgetools.plugin;

import static de.jreality.scene.Appearance.INHERITED;
import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_STIPPLE;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
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
import de.jreality.geometry.ThickenedSurfaceFactory;
import de.jreality.math.Matrix;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.ActionTool;
import de.jreality.util.Rectangle3D;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
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
	private AdapterSet
		persistentAdapters = new AdapterSet(),
		volatileAdapters = new AdapterSet();
	private SceneGraphComponent
		layerRoot = new SceneGraphComponent("Default Layer"),
		displayFacesRoot = new SceneGraphComponent("Display Faces"),
		geometryRoot = new SceneGraphComponent("Geometry"),
		selectionRoot = new SceneGraphComponent("Selection"),
		visualizersRoot = new SceneGraphComponent("Visualizers"),
		boundingBoxRoot = new SceneGraphComponent("Bounding Box"),
		temporaryRoot = new SceneGraphComponent("Temporary Geometry");
	private Appearance
		geometryAppearance = new Appearance("Geometry Appearance");
	private Map<Integer, Edge<?,?,?>>
		edgeMap = new HashMap<Integer, Edge<?,?,?>>();
	private HalfedgeSelection
		selection = new HalfedgeSelection();
	private Set<VisualizerPlugin>
		visualizers = new TreeSet<VisualizerPlugin>();
	
	private List<IndexedFaceSet> 
		undoHistory = new ArrayList<IndexedFaceSet>();
	private int
		undoIndex = -1,
		undoSize = 10;
	
	//layer properties
	private boolean
		thickenSurface = false,
		makeHoles = true,
		implode = false;
	private int 
		stepsPerEdge = 8; 
	private double[][]
	    profileCurve = new double[][]{{0,0}, {0,.4}, {.1,.5},{.9, .5},{1.0, .4}, {1,0}};
	private double
		holeFactor = 0.4,
		thickness = 0.05,
		implodeFactor = -0.85;
	
	private boolean 
		active = true;
	
	private ConverterHeds2JR 
		converterToIFS = new ConverterHeds2JR();
	private ConverterJR2Heds
		converterToHDS = new ConverterJR2Heds();
	
	private ActionTool
		actionTool = new ActionTool("PrimaryAction");

	private HalfedgeLayer() {
		layerRoot.addChild(geometryRoot);
		layerRoot.addChild(displayFacesRoot);
		layerRoot.addChild(boundingBoxRoot);
		layerRoot.addChild(selectionRoot);
		layerRoot.addChild(temporaryRoot);
		layerRoot.setTransformation(new Transformation("Layer Transform"));
		layerRoot.setAppearance(new Appearance("Layer Appearance"));
		geometryRoot.addTool(actionTool);
		selectionRoot.setPickable(false);
		actionTool.addActionListener(this);
		
		Appearance bBoxApp = new Appearance("Bounding Box Appearance");
		bBoxApp.setAttribute(FACE_DRAW, false);
		bBoxApp.setAttribute(VERTEX_DRAW, false);
		bBoxApp.setAttribute(EDGE_DRAW, true);
		bBoxApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		bBoxApp.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 0.5);
		bBoxApp.setAttribute(LINE_SHADER + "." + LINE_STIPPLE, true);
		bBoxApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		bBoxApp.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, 1.5f);
		bBoxApp.setAttribute(LINE_SHADER + "." + Z_BUFFER_ENABLED, true);
		bBoxApp.setAttribute(PICKABLE, false);
		boundingBoxRoot.setAppearance(bBoxApp);
		
		Appearance facesAppearance = new Appearance("Faces Appearance");
		facesAppearance.setAttribute(VERTEX_DRAW, false);
		facesAppearance.setAttribute(EDGE_DRAW, false);
		facesAppearance.setAttribute(PICKABLE, false);
		displayFacesRoot.setAppearance(facesAppearance);
		
		geometryRoot.setAppearance(geometryAppearance);
	}
	
	
	public HalfedgeLayer(HalfedgeInterface hif) {
		this();
		this.hif = hif;
	}
	
	public HalfedgeLayer(Geometry geometry, HalfedgeInterface hif) {
		this();
		this.hif = hif;
		set(geometry);
	}
	
	
	public Transformation getTransformation() {
		return layerRoot.getTransformation();
	}
	public void setTransformation(Transformation T) {
		layerRoot.setTransformation(T);
	}
	public Appearance getAppearance() {
		return layerRoot.getAppearance();
	}
	public void setAppearance(Appearance layerAppearance) {
		layerRoot.setAppearance(layerAppearance);
	}
	
	
	private AdapterSet getVisualizerAdapters() {
		AdapterSet r = new AdapterSet();
		for (VisualizerPlugin p : visualizers) {
			r.addAll(p.getAdapters());
		}
		return r;
	}
	
	private AdapterSet getEffectiveAdapters() {
		AdapterSet effectiveAdapters = new AdapterSet();
		effectiveAdapters.addAll(hif.getGlobalAdapters());
		effectiveAdapters.addAll(getAllAdapters());
		effectiveAdapters.addAll(getVisualizerAdapters());
		return effectiveAdapters;
	}
	
	private void initVisualizers(AdapterSet a) {
		for (VisualizerPlugin vp : visualizers) {
			vp.initVisualization(hds, a, hif);
		}
	}
	
	
	private void updateVisualizersGeometry(AdapterSet a) {
		layerRoot.removeChild(visualizersRoot);
		visualizersRoot = new SceneGraphComponent();
		visualizersRoot.setName("Visualizers");
		for (VisualizerPlugin vp : visualizers) {
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
	> void setNoUndo(HDS hds) {
		this.hds = hds;
		convertHDS();
		clearSelection();
		
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds) {
		setNoUndo(hds);
		updateUndoList();
		volatileAdapters.clear();
	}
	
	public void update() {
		set(get());
	}
	
	
	public void updateNoUndo() {
		setNoUndo(get());
	}
	
	
	public void setNoUndo(Geometry g) {
		if (g instanceof IndexedFaceSet) {
			geometry = (IndexedFaceSet)g;
		} else 
		if (g instanceof IndexedLineSet){
			IndexedLineSet ils = (IndexedLineSet)g;
			geometry = new IndexedFaceSet(g.getName());
			geometry.setVertexAttributes(ils.getVertexAttributes());
			geometry.setEdgeAttributes(ils.getEdgeAttributes());
		} else 
		if (g instanceof PointSet) {
			PointSet ps = (PointSet)g;
			geometry = new IndexedFaceSet(g.getName());
			geometry.setVertexAttributes(ps.getVertexAttributes());
		} else {
			geometry = new IndexedFaceSet(g.getName());
		}
		convertFaceSet();
		clearSelection();
	}
	
	public void set(Geometry g) {
		setNoUndo(g);
		updateUndoList();
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS template) {
		if (template.getClass().isAssignableFrom(hds.getClass())) {
			return (HDS)hds;
		}
		hds = template;
		convertFaceSet();
		// convert selection
		HalfedgeSelection newSelection = new HalfedgeSelection();
		for (Vertex<?,?,?> v : selection.getVertices()) {
			Vertex<?,?,?> nv = hds.getVertex(v.getIndex());
			newSelection.setSelected(nv, true);
		}
		for (Edge<?,?,?> e : selection.getEdges()) {
			Edge<?,?,?> ne = hds.getEdge(e.getIndex());
			newSelection.setSelected(ne, true);
		}
		for (Face<?,?,?> f : selection.getFaces()) {
			Face<?,?,?> nf = hds.getFace(f.getIndex());
			newSelection.setSelected(nf, true);
		}
		selection = newSelection;
		return template;
	}
	
	public HalfEdgeDataStructure<?,?,?> get() {
		return hds;
	}
	
	
	public IndexedFaceSet getGeometry() {
		return geometry;
	}
	
	
	private void convertFaceSet() {
		hds.clear();
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(geometry);
		if (!oriented) {
			System.err.println("Not orientable face set in convertFaceSet()");
			return;
		}
		converterToHDS.ifs2heds(geometry, hds, getEffectiveAdapters(), edgeMap);
		createDisplayGeometry();
		updateBoundingBox();
		resetTemporaryGeometry();
	}
	
	
	private void convertHDS() {
		clearSelection();
		AdapterSet ea = getEffectiveAdapters();
		initVisualizers(ea);
		geometry = converterToIFS.heds2ifs(hds, ea, edgeMap);
		createDisplayGeometry();
		updateVisualizersGeometry(ea);
		updateBoundingBox();
		resetTemporaryGeometry();
	}
	
	
	private void createDisplayGeometry() {
		IndexedFaceSet shownGeometry = geometry;
		if (thickenSurface && geometry != null) {
			ThickenedSurfaceFactory tsf = new ThickenedSurfaceFactory(geometry);
			tsf.setThickness(thickness);
			tsf.setMakeHoles(makeHoles);
			tsf.setKeepFaceColors(true);
			tsf.setHoleFactor(holeFactor);
			tsf.setStepsPerEdge(stepsPerEdge);
			tsf.setProfileCurve(profileCurve);
			tsf.update();
			shownGeometry = tsf.getThickenedSurface();
		}
		if (implode && geometry != null) {
			shownGeometry = IndexedFaceSetUtility.implode(shownGeometry, implodeFactor);
		}
		geometryRoot.setGeometry(geometry);
		if (shownGeometry != geometry) {
			displayFacesRoot.setGeometry(shownGeometry);
			geometryAppearance.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, 1.0);
		} else {
			displayFacesRoot.setGeometry(null);
			geometryAppearance.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, INHERITED);
		}
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
		convertFaceSet();
		convertHDS();
	}
	
	public void redo() {
		if (!canRedo()) return;
		undoIndex++;
		geometry = undoHistory.get(undoIndex);
		geometryRoot.setGeometry(geometry);
		convertFaceSet();
		convertHDS();
	}
	
	public void addTemporaryGeometry(SceneGraphComponent root) {
		temporaryRoot.addChild(root);
		updateBoundingBox();
	}
	
	public void resetTemporaryGeometry() {
		layerRoot.removeChild(temporaryRoot);
		temporaryRoot = new SceneGraphComponent("Temporary Geometry");
		layerRoot.addChild(temporaryRoot);
		updateBoundingBox();
	}


	public boolean isThickenSurface() {
		return thickenSurface;
	}


	public void setThickenSurface(boolean thickenSurface) {
		this.thickenSurface = thickenSurface;
	}


	
	public boolean isImplode() {
		return implode;
	}


	public void setImplode(boolean implode) {
		this.implode = implode;
	}


	public double getThickness() {
		return thickness;
	}


	public void setThickness(double thickness) {
		this.thickness = thickness;
	}


	public double getImplodeFactor() {
		return implodeFactor;
	}


	public void setImplodeFactor(double implodeFactor) {
		this.implodeFactor = implodeFactor;
	}
	
	public boolean isMakeHoles() {
		return makeHoles;
	}
	
	public void setMakeHoles(boolean makeHoles) {
		this.makeHoles = makeHoles;
	}
	
	public double getHoleFactor() {
		return holeFactor;
	}
	public void setHoleFactor(double holeFactor) {
		this.holeFactor = holeFactor;
	}
	
	public int getStepsPerEdge() {
		return stepsPerEdge;
	}
	public void setStepsPerEdge(int stepsPerEdge) {
		this.stepsPerEdge = stepsPerEdge;
	}
	
	public AdapterSet getAllAdapters() {
		AdapterSet adapters = new AdapterSet();
		adapters.addAll(persistentAdapters);
		adapters.addAll(volatileAdapters);
		return adapters;
	}
	
	public AdapterSet getPersistentAdapters() {
		return persistentAdapters;
	}
	
	public AdapterSet getVolatileAdapters() {
		return volatileAdapters;
	}
	
	public double[][] getProfileCurve() {
		return profileCurve;
	}
	public void setProfileCurve(double[][] profileCurve) {
		this.profileCurve = profileCurve;
	}


	public boolean addAdapter(Adapter<?> a, boolean persistent) {
		if(persistent) {
			return persistentAdapters.add(a);
		} else {
			return volatileAdapters.add(a);
		}
	}

	public boolean removeAdapter(Adapter<?> a) {
		boolean 
			pa = persistentAdapters.remove(a), 
			va = volatileAdapters.remove(a);
		return pa || va;
	}
	
}
