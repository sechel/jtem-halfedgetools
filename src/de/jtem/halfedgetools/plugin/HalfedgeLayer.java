package de.jtem.halfedgetools.plugin;

import static de.jreality.geometry.IndexedFaceSetUtility.removeTextureCoordinateJumps;
import static de.jreality.math.Rn.euclideanNormSquared;
import static de.jreality.scene.Appearance.INHERITED;
import static de.jreality.scene.data.Attribute.COORDINATES;
import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_STIPPLE;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jreality.shader.CommonAttributes.Z_BUFFER_ENABLED;
import static de.jtem.halfedgetools.selection.FaceSetSelection.toFaceSetSelection;
import static de.jtem.halfedgetools.selection.SelectionUtility.createSelectionGeometry;
import static java.util.Collections.unmodifiableSet;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.geometry.ThickenedSurfaceFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.ClippingPlane;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.ActionTool;
import de.jreality.util.LoggingSystem;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.jreality.ConverterHds2Ifs;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.selection.FaceSetSelection;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.TypedSelection;

public class HalfedgeLayer implements ActionListener {

	private static Logger 
		layerLogger = LoggingSystem.getLogger(HalfedgeLayer.class);
	private HalfedgeInterface 
		hif = null;
	private HalfEdgeDataStructure<?, ?, ?> 
		hds = new DefaultJRHDS();
	private IndexedFaceSet 
		geometry = new IndexedFaceSet();
	private AdapterSet 
		persistentAdapters = new AdapterSet(),
		activeVolatileAdapters = new AdapterSet(),
		volatileAdapters = new AdapterSet();
	private SceneGraphComponent 
		clippingRoot = new SceneGraphComponent("Clipping"),
		layerRoot = new SceneGraphComponent("Default Layer"), 
		displayFacesRoot = new SceneGraphComponent("Display Faces"),
		geometryRoot = new SceneGraphComponent("Geometry"),
		selectionRoot = new SceneGraphComponent("Selection"),
		visualizersRoot = new SceneGraphComponent("Visualizers"),
		boundingBoxRoot = new SceneGraphComponent("Bounding Box"),
		temporaryRoot = new SceneGraphComponent("Temporary Geometry"),
		pivotRoot = CoordinatesPivot.createPivot();
	private Appearance 
		geometryAppearance = new Appearance("Geometry Appearance");
	private Map<Integer, Edge<?, ?, ?>> 
		edgeMap = new HashMap<Integer, Edge<?, ?, ?>>();
	private Selection 
		selection = new Selection();
	private Set<VisualizerPlugin> 
		visualizers = new TreeSet<VisualizerPlugin>();

	private List<IndexedFaceSet> 
		geometryHistory = new ArrayList<IndexedFaceSet>();
	private List<AdapterSet> 
		adapterHistory = new ArrayList<AdapterSet>();
	private List<FaceSetSelection>
		selectionHistory = new ArrayList<FaceSetSelection>();
	private int 
		undoIndex = 0, 
		undoSize = 20;

	// layer properties
	private boolean 
		removeTextureJumps = false,
		implode = false,
		thickenSurface = false, 
		thickenMakeHoles = true, 
		thickenLinearHoles = true,
		thickenFaceNormals = false,
		thickenConstantWidth = false,
		thickenCurvedEdge = false;
	private int 
		thickenStepsPerEdge = 8;
	private double[][] 
		thickenProfileCurve = new double[][] { { 0, 0 }, { 0, .4 },{ .1, .5 }, { .9, .5 }, { 1.0, .4 }, { 1, 0 } };
	private double
		implodeFactor = -0.85,
		textureJumpSize = 1.0,
		thickenHoleFactor = 0.4, 
		thickenThickness = 0.05,
		thickenNormalShift = 0.5;
	
	private boolean
		clippingEnabled = false;
	private double[]
		clippingScale = new double[]{0.5,0.5,0.5};

	private boolean 
		active = true;

	private ConverterHds2Ifs 
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
		layerRoot.addChild(clippingRoot);
		layerRoot.setTransformation(new Transformation("Layer Transform"));
		layerRoot.setAppearance(new Appearance("Layer Appearance"));
		actionTool.setDescription("Selection");
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
		boundingBoxRoot.addChild(pivotRoot);

		Appearance facesAppearance = new Appearance("Faces Appearance");
		facesAppearance.setAttribute(VERTEX_DRAW, false);
		facesAppearance.setAttribute(EDGE_DRAW, false);
		facesAppearance.setAttribute(PICKABLE, false);
		displayFacesRoot.setAppearance(facesAppearance);

		geometryRoot.setAppearance(geometryAppearance);
		
		geometryHistory.add(geometry);
		adapterHistory.add(persistentAdapters);
		selectionHistory.add(new FaceSetSelection());
	}

	public HalfedgeLayer(HalfedgeInterface hif) {
		this();
		this.hif = hif;
		if (hif.getTemplateHDS() != null) {
			hds = hif.createEmpty(hif.getTemplateHDS());
		}
		if (hif.getConverterHds2Ifs() != null) {
			converterToIFS = hif.getConverterHds2Ifs();
		}
	}

	public HalfedgeLayer(Geometry geometry, HalfedgeInterface hif) {
		this(hif);
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

	public AdapterSet getVisualizerAdapters() {
		AdapterSet r = new AdapterSet();
		for (VisualizerPlugin p : visualizers) {
			r.addAll(p.getAdapters());
		}
		return r;
	}

	/**
	 * Gather the effective adapters for a convert operation. This contains all
	 * adapters of the half-edge interface, all adapters of the layer and the
	 * adapters provided by any active visualizer.
	 * 
	 * @return An {@link AdapterSet} containing the effective adapters for the
	 *         next convert.
	 */
	public AdapterSet getEffectiveAdapters() {
		AdapterSet effectiveAdapters = new AdapterSet();
		effectiveAdapters.addAll(hif.getAdapters());
		effectiveAdapters.addAll(getAdapters());
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

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNoUndo(HDS hds) {
		this.hds = hds;
		convertHDS();
		hif.fireDataChanged();
		hif.updateStates();
		hif.checkContent();
		hif.clearVolatileAdapters();
		updateSelection();
	}

	/**
	 * Update the vertex positions for a previously set mesh TODO: Decide on
	 * whether this should include visualizers. Or convert at all. Converting
	 * feels natural as colors like planar faces are displayed correctly after
	 * updateGeometry In this case this is just a convenience method
	 * 
	 * @param positionAdapter
	 *            the vertex position adapter
	 */
	public void updateGeometryNoUndo(Adapter<double[]> positionAdapter) {
		AdapterSet adapters = getEffectiveAdapters();
		double[][] posArr = new double[hds.numVertices()][];
		for (Vertex<?, ?, ?> v : hds.getVertices()) {
			double[] pos = positionAdapter.get(v, adapters);
			if (pos != null) {
				adapters.set(Position.class, v, pos);
				posArr[v.getIndex()] = pos;
			} else {
				posArr[v.getIndex()] = new double[3];
			}
		}
		// updateNoUndo();
		DataList vData = new DoubleArrayArray.Array(posArr);
		IndexedFaceSet newGeometry = new IndexedFaceSet();
		newGeometry.setVertexCountAndAttributes(geometry.getVertexAttributes());
		newGeometry.setVertexCountAndAttributes(COORDINATES, vData);
		newGeometry.setFaceCountAndAttributes(geometry.getFaceAttributes());
		newGeometry.setEdgeCountAndAttributes(geometry.getEdgeAttributes());
		IndexedFaceSetUtility.calculateAndSetNormals(newGeometry);
		geometry = newGeometry;
		createDisplayGeometry();
		updateBoundingBox();
		resetTemporaryGeometry();
		updateSelection();
		hif.updateStates();
		hif.checkContent();
		hif.fireDataChanged();
	}

	/**
	 * Update the vertex positions for a previously set mesh
	 * 
	 * @param positionAdapter
	 *            the vertex position adapter
	 */
	public void updateGeometry(Adapter<double[]> positionAdapter) {
		updateGeometryNoUndo(positionAdapter);
		appendHistoryState();
	}

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds) {
		setNoUndo(hds);
		appendHistoryState();
	}

	public void update() {
		set(get());
	}

	public void updateNoUndo() {
		setNoUndo(get());
	}

	public void setNoUndo(Geometry g) {
		if (g instanceof IndexedFaceSet) {
			geometry = (IndexedFaceSet) g;
		} else if (g instanceof IndexedLineSet) {
			IndexedLineSet ils = (IndexedLineSet) g;
			geometry = new IndexedFaceSet(g.getName());
			geometry.setVertexCountAndAttributes(ils.getVertexAttributes());
			geometry.setEdgeCountAndAttributes(ils.getEdgeAttributes());
		} else if (g instanceof PointSet) {
			PointSet ps = (PointSet) g;
			geometry = new IndexedFaceSet(g.getName());
			geometry.setVertexCountAndAttributes(ps.getVertexAttributes());
		} else {
			geometry = new IndexedFaceSet(g.getName());
		}
		convertFaceSet();
		clearSelection();
		hif.fireDataChanged();
		hif.checkContent();
		hif.updateStates();
		hif.clearVolatileAdapters();
	}

	public void set(Geometry g) {
		setNoUndo(g);
		appendHistoryState();
	}

	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS template) {
		if (template.getClass().isAssignableFrom(hds.getClass())) {
			return (HDS) hds;
		}
		hds = template;
		convertFaceSet();
		// convert selection
		Selection newSelection = new Selection();
		for (Vertex<?,?,?> v : selection.getVertices()) {
			Vertex<?,?,?> nv = hds.getVertex(v.getIndex());
			newSelection.add(nv, selection.getChannel(v));
		}
		for (Edge<?, ?, ?> e : selection.getEdges()) {
			Edge<?, ?, ?> ne = hds.getEdge(e.getIndex());
			newSelection.add(ne, selection.getChannel(e));
		}
		for (Face<?, ?, ?> f : selection.getFaces()) {
			Face<?, ?, ?> nf = hds.getFace(f.getIndex());
			newSelection.add(nf, selection.getChannel(f));
		}
		selection = newSelection;
		hif.updateStates();
		return template;
	}

	public HalfEdgeDataStructure<?, ?, ?> get() {
		return hds;
	}

	public IndexedFaceSet getGeometry() {
		return geometry;
	}

	private void convertFaceSet() {
		hds.clear();
		boolean oriented = IndexedFaceSetUtility
				.makeConsistentOrientation(geometry);
		if (!oriented) {
			System.err.println("Not orientable face set in convertFaceSet()");
			return;
		}
		converterToHDS.ifs2heds(geometry, hds, getEffectiveAdapters(), edgeMap);
		createDisplayGeometry();
		updateBoundingBox();
		resetTemporaryGeometry();
		clearVolatileAdapters();
	}

	public void setConverterToIFS(ConverterHds2Ifs converterToIFS) {
		this.converterToIFS = converterToIFS;
	}

	private void convertHDS() {
		AdapterSet ea = getEffectiveAdapters();
		initVisualizers(ea);
		ea = getEffectiveAdapters();
		geometry = converterToIFS.heds2ifs(hds, ea, edgeMap);
		createDisplayGeometry();
		updateVisualizersGeometry(ea);
		updateBoundingBox();
		resetTemporaryGeometry();
		clearVolatileAdapters();
	}

	private void clearVolatileAdapters() {
		activeVolatileAdapters.clear();
		activeVolatileAdapters.addAll(volatileAdapters);
		volatileAdapters.clear();
	}

	private void createDisplayGeometry() {
		IndexedFaceSet shownGeometry = geometry;
		if (thickenSurface && geometry != null) {
			ThickenedSurfaceFactory tsf = new ThickenedSurfaceFactory(geometry);
			tsf.setThickness(thickenThickness);
			tsf.setMakeHoles(thickenMakeHoles);
			tsf.setKeepFaceColors(false);
			tsf.setHoleFactor(thickenHoleFactor);
			tsf.setStepsPerEdge(thickenStepsPerEdge);
			tsf.setProfileCurve(thickenProfileCurve);
			tsf.setLinearHole(thickenLinearHoles);
			tsf.setCurvedEdges(thickenCurvedEdge);
			tsf.setShiftAlongNormal(thickenNormalShift);
			tsf.setConstantWidth(thickenConstantWidth);
			tsf.setThickenAlongFaceNormals(thickenFaceNormals);
			tsf.update();
			shownGeometry = tsf.getThickenedSurface();
		}
		if (removeTextureJumps && geometry != null) {
			shownGeometry = removeTextureCoordinateJumps(shownGeometry, textureJumpSize);
		}		
		if (implode && geometry != null) {
			shownGeometry = IndexedFaceSetUtility.implode(shownGeometry, implodeFactor);
		}
		geometryRoot.setGeometry(geometry);
		if (shownGeometry != geometry) {
			displayFacesRoot.setGeometry(shownGeometry);
			geometryAppearance.setAttribute(FACE_DRAW, false);
		} else {
			displayFacesRoot.setGeometry(null);
			geometryAppearance.setAttribute(FACE_DRAW, INHERITED);
		}
	}

	public Selection getSelection() {
		return new Selection(selection);
	}

	public void setSelection(TypedSelection<? extends Node<?,?,?>> sel) {
		this.selection = new Selection(sel);
		updateSelection();
		updateSelectionHistory();
		hif.fireSelectionChanged(selection);
	}
	
	public void addSelection(TypedSelection<? extends Node<?,?,?>> sel) {
		this.selection.addAll(sel);
		updateSelection();
		updateSelectionHistory();
		hif.fireSelectionChanged(selection);
	}

	public void clearSelection() {
		this.selection.clear();
		updateSelection();
		updateSelectionHistory();
		hif.fireSelectionChanged(selection);
	}
	public void clearSelection(Integer channel) {
		this.selection.clear(channel);
		updateSelection();
		updateSelectionHistory();
		hif.fireSelectionChanged(selection);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
		if (active) {
			geometryRoot.addTool(actionTool);
		} else {
			geometryRoot.removeTool(actionTool);
		}
	}

	public void setShowBoundingBox(boolean show) {
		boundingBoxRoot.setVisible(show);
	}

	protected void updateSelection() {
		for (Node<?, ?, ?> checkNode : new Selection(selection)) {
			if (!checkNode.isValid() || checkNode.getHalfEdgeDataStructure() != hds) {
				selection.remove(checkNode);
			}
		}
		layerRoot.removeChild(selectionRoot);
		AdapterSet a = hif.getAdapters();
		a.addAll(hif.getActiveVolatileAdapters());
		Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
		if (hif.getSelectionInterface() != null) {
			colorMap = hif.getSelectionInterface().getChannelColors(this);
		}
		selectionRoot = createSelectionGeometry(selection, a, colorMap);
		selectionRoot.setPickable(false);
		Appearance app = selectionRoot.getAppearance();
		hif.createSelectionAppearance(app, this, 0.1);
		layerRoot.addChild(selectionRoot);
	}

	protected void updateBoundingBox() {
		boundingBoxRoot.setGeometry(null);
		if (boundingBoxRoot.getChildNodes().contains(pivotRoot)) {
			boundingBoxRoot.removeChild(pivotRoot);
		}
		Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(layerRoot);
		if (euclideanNormSquared(bbox.getExtent()) == 0)
			return;
		BoundingBoxUtility.removeZeroExtends(bbox);
		IndexedFaceSet ifs = IndexedFaceSetUtility.representAsSceneGraph(bbox);
		ifs.setName("Bounding Box");
		boundingBoxRoot.setGeometry(ifs);
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.translate(bbox.getMinX(), bbox.getMinY(), bbox.getMaxZ());
		mb.scale(bbox.getMaxExtent() / 20);
		mb.assignTo(pivotRoot);
		boundingBoxRoot.addChild(pivotRoot);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ToolContext tc = (ToolContext) e.getSource();
		SelectionUpdater updater = new SelectionUpdater(tc);
		EventQueue.invokeLater(updater);
	}

	private class SelectionUpdater implements Runnable {

		private ToolContext toolContext = null;

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
			if (pr == null) {
				return;
			}
			int index = pr.getIndex();
			if (index < 0) {
				return;
			}
			Integer channel = TypedSelection.CHANNEL_DEFAULT;
			SelectionInterface sif = hif.getSelectionInterface();
			if (sif != null) {
				channel = sif.getActiveInputChannel(HalfedgeLayer.this);
			}
			switch (pr.getPickType()) {
				case PickResult.PICK_TYPE_POINT:
					if (index < hds.numVertices()) {
						Vertex<?,?,?> v = hds.getVertex(index);
						if (selection.contains(v)) {
							selection.remove(v);
						} else {
							selection.add(v, channel);
						}
					}
					break;
				case PickResult.PICK_TYPE_LINE:
					Edge<?,?,?> e = edgeMap.get(index);
					if (selection.contains(e)) {
						selection.remove(e);
						selection.remove(e.getOppositeEdge());
					} else {
						selection.add(e, channel);
						selection.add(e.getOppositeEdge(), channel);
					}
					break;
				case PickResult.PICK_TYPE_FACE:
					if (index < hds.numFaces()) {
						Face<?,?,?> f = hds.getFace(index);
						if (selection.contains(f)) {
							selection.remove(f);
						} else {
							selection.add(f, channel);
						}
					}
					break;
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
		hif.checkContent();
		hif.updateStates();
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
	public SceneGraphComponent getGeometryRoot() {
		return geometryRoot;
	}

	public boolean canUndo() {
		return undoIndex > 0;
	}

	public boolean canRedo() {
		return undoIndex < geometryHistory.size() - 1;
	}

	protected void updateSelectionHistory() {
		FaceSetSelection fss = toFaceSetSelection(selection);
		selectionHistory.set(undoIndex, fss);
	}
	
	private void appendHistoryState() {
		geometryHistory = new ArrayList<IndexedFaceSet>(geometryHistory.subList(0, undoIndex + 1));
		adapterHistory = new ArrayList<AdapterSet>(adapterHistory.subList(0, undoIndex + 1));
		selectionHistory = new ArrayList<FaceSetSelection>(selectionHistory.subList(0, undoIndex + 1));
		geometryHistory.add(geometry);
		adapterHistory.add(new AdapterSet(persistentAdapters));
		selectionHistory.add(toFaceSetSelection(selection));
		if (geometryHistory.size() > undoSize) {
			geometryHistory.remove(0);
			adapterHistory.remove(0);
			selectionHistory.remove(0);
		}
		undoIndex = geometryHistory.size() - 1;
	}

	public void undo() {
		if (!canUndo()) {
			return;
		}
		updateSelectionHistory();
		undoIndex--;
		geometry = geometryHistory.get(undoIndex);
		persistentAdapters = adapterHistory.get(undoIndex);
		activeVolatileAdapters.clear();
		volatileAdapters.clear();
		geometryRoot.setGeometry(geometry);
		convertFaceSet();
		convertHDS();
		FaceSetSelection fss = selectionHistory.get(undoIndex);
		selection = FaceSetSelection.toSelection(fss, hds);
		updateSelection();
	}

	public void redo() {
		if (!canRedo()) {
			return;
		}
		updateSelectionHistory();
		undoIndex++;
		geometry = geometryHistory.get(undoIndex);
		persistentAdapters = adapterHistory.get(undoIndex);
		activeVolatileAdapters.clear();
		volatileAdapters.clear();
		geometryRoot.setGeometry(geometry);
		convertFaceSet();
		convertHDS();
		FaceSetSelection fss = selectionHistory.get(undoIndex);
		selection = FaceSetSelection.toSelection(fss, hds);
		updateSelection();
	}

	public void addTemporaryGeometry(SceneGraphComponent root) {
		if (!layerRoot.isDirectAncestor(temporaryRoot)) {
			layerRoot.addChild(temporaryRoot);
		}
		SceneGraphUtility.addChildNode(temporaryRoot, root);
	}

	public void removeTemporaryGeometry(SceneGraphComponent root) {
		try {
			SceneGraphUtility.removeChildNode(temporaryRoot, root);
		} catch (Exception e) {
			layerLogger.log(Level.FINEST, e.getMessage());
		}
	}

	public void resetTemporaryGeometry() {
		SceneGraphUtility.removeChildNode(layerRoot, temporaryRoot);
		temporaryRoot = new SceneGraphComponent("Temporary Geometry");
		SceneGraphUtility.addChildNode(layerRoot, temporaryRoot);
	}

	public boolean isRemoveTextureJumps() {
		return removeTextureJumps;
	}
	public void setRemoveTextureJumps(boolean removeTextureJumps) {
		this.removeTextureJumps = removeTextureJumps;
	}
	
	public double getTextureJumpSize() {
		return textureJumpSize;
	}
	public void setTextureJumpSize(double textureJumpSize) {
		this.textureJumpSize = textureJumpSize;
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

	public double getThickenThickness() {
		return thickenThickness;
	}
	public void setThickenThickness(double thickness) {
		this.thickenThickness = thickness;
	}

	public double getImplodeFactor() {
		return implodeFactor;
	}
	public void setImplodeFactor(double implodeFactor) {
		this.implodeFactor = implodeFactor;
	}

	public boolean isThickenLinearHoles() {
		return thickenLinearHoles;
	}
	public void setThickenLinearHoles(boolean linearHoles) {
		this.thickenLinearHoles = linearHoles;
	}
	
	public boolean isThickenMakeHoles() {
		return thickenMakeHoles;
	}
	public void setThickenMakeHoles(boolean makeHoles) {
		this.thickenMakeHoles = makeHoles;
	}

	public double getThickenHoleFactor() {
		return thickenHoleFactor;
	}
	public void setThickenHoleFactor(double holeFactor) {
		this.thickenHoleFactor = holeFactor;
	}

	public int getThickenStepsPerEdge() {
		return thickenStepsPerEdge;
	}
	public void setThickenStepsPerEdge(int stepsPerEdge) {
		this.thickenStepsPerEdge = stepsPerEdge;
	}
	
	public boolean isThickenCurvedEdge() {
		return thickenCurvedEdge;
	}
	public void setThickenCurvedEdge(boolean thickenCurvedEdge) {
		this.thickenCurvedEdge = thickenCurvedEdge;
	}
	
	public double[][] getThickenProfileCurve() {
		return thickenProfileCurve;
	}
	public void setThickenProfileCurve(double[][] profileCurve) {
		this.thickenProfileCurve = profileCurve;
	}
	
	public double getThickenNormalShift() {
		return thickenNormalShift;
	}
	public void setThickenNormalShift(double thickenNormalShift) {
		this.thickenNormalShift = thickenNormalShift;
	}
	
	public boolean isThickenConstantWidth() {
		return thickenConstantWidth;
	}
	public void setThickenConstantWidth(boolean thickenConstantWidth) {
		this.thickenConstantWidth = thickenConstantWidth;
	}
	
	public boolean isThickenFaceNormals() {
		return thickenFaceNormals;
	}
	public void setThickenFaceNormals(boolean thickenFaceNormals) {
		this.thickenFaceNormals = thickenFaceNormals;
	}

	public AdapterSet getAdapters() {
		AdapterSet adapters = new AdapterSet();
		adapters.addAll(persistentAdapters);
		adapters.addAll(volatileAdapters);
		return adapters;
	}

	public AdapterSet getActiveAdapters() {
		AdapterSet adapters = new AdapterSet();
		adapters.addAll(persistentAdapters);
		adapters.addAll(activeVolatileAdapters);
		return adapters;
	}

	public AdapterSet getPersistentAdapters() {
		return persistentAdapters;
	}

	public AdapterSet getVolatileAdapters() {
		return volatileAdapters;
	}

	public AdapterSet getActiveVolatileAdapters() {
		return activeVolatileAdapters;
	}

	public boolean addAdapter(Adapter<?> a, boolean persistent) {
		if (persistent) {
			return persistentAdapters.add(a);
		} else {
			return volatileAdapters.add(a);
		}
	}

	public boolean removeAdapter(Adapter<?> a) {
		boolean pa = persistentAdapters.remove(a), va = volatileAdapters
				.remove(a);
		return pa || va;
	}
	
	/**
	 * Returns an half-edge node index for the given jReality
	 * edge index based on the latest conversion result.
	 * @param pickIndex a jReality edge pick index
	 * @return a half-edge node index corresponding to the given edge pick
	 */
	public int pickToNodeEdgeIndex(int pickIndex) {
		Edge<?,?,?> edge = edgeMap.get(pickIndex);
		if (edge != null) {
			return edge.getIndex();
		} else {
			return -1;
		}
	}

	public Visibility getVertexVisibility() {
		return getVisibility(VERTEX_DRAW);
	}
	
	public Visibility getEdgeVisibility() {
		return getVisibility(EDGE_DRAW);
	}
	
	public Visibility getFaceVisibility() {
		return getVisibility(FACE_DRAW);
	}
	
	private Visibility getVisibility(String attr) {
		Object val = geometryAppearance.getAttribute(attr);
		if(val == INHERITED) { 
			return Visibility.INHERITED;
		} else if((Boolean)val){
			return Visibility.SHOW;
		} else {
			return Visibility.HIDE;
		}
	}

	public void setVertexVisiblity(Visibility vis) {
		updateVisibility(VERTEX_DRAW, vis);
	}

	public void setEdgeVisibility(Visibility vis) {
		updateVisibility(EDGE_DRAW, vis);
	}
	
	public void setFaceVisibility(Visibility vis) {
		updateVisibility(FACE_DRAW, vis);
	}
	
	private void updateVisibility(String attr, Visibility aValue) {
		if(aValue == Visibility.INHERITED) {
			geometryAppearance.setAttribute(attr, INHERITED);
		} else if(aValue == Visibility.SHOW) {
			geometryAppearance.setAttribute(attr, true);
		} else if(aValue == Visibility.HIDE) {
			geometryAppearance.setAttribute(attr, false);
		}
	}
	
	public HalfedgeInterface getHalfedgeInterface() {
		return hif;
	}

	public void setClippingScale(double x, double y, double z) {
		clippingScale = new double[]{x,y,z};
		updateClipping();
	}

	public void setEnableClipping(boolean clip) {
		clippingEnabled = clip;
		updateClipping();
	}
	
	private void updateClipping() {
		Rectangle3D bb = BoundingBoxUtility.calculateBoundingBox(layerRoot);
		clippingRoot.removeAllChildren();
		if(clippingEnabled) {
			clippingRoot.addChild(createClippingBox(bb, clippingScale));
		}
	}

	private SceneGraphComponent createClippingBox(Rectangle3D bb, double[] cs) {
		SceneGraphComponent clipBox =  new SceneGraphComponent("Clipping Box");
		MatrixBuilder.euclidean().translate(bb.getCenter()).assignTo(clipBox);
		double[] extent = bb.getExtent();
		
		double x = extent[0]/2;
		double y = extent[1]/2;
		double z = extent[2]/2;
		
		clipBox.addChild(clippingPlane(0, cs[0]*x, false, "Right"));
		clipBox.addChild(clippingPlane(0, cs[0]*x, true, "Left"));
		clipBox.addChild(clippingPlane(1, cs[1]*y, false, "Back"));
		clipBox.addChild(clippingPlane(1, cs[1]*y, true, "Front"));
		clipBox.addChild(clippingPlane(2, cs[2]*z, false, "Top"));
		clipBox.addChild(clippingPlane(2, cs[2]*z, true, "Bottom"));
		return clipBox;
	}

	public SceneGraphComponent clippingPlane(int dir, double v, boolean flip, String name) {
		SceneGraphComponent clipPlane =  new SceneGraphComponent(name);
		double[] t = new double[3];
		t[dir]=1.1*v;
		double[] n = new double[3];
		n[dir] = 1;
		if(flip) {
			double[] plane = new double[4];
			plane[dir]=1;
			MatrixBuilder.euclidean().reflect(plane).translate(t).rotateFromTo(new double[]{0,0,1},n).assignTo(clipPlane);
		} else {
			MatrixBuilder.euclidean().translate(t).rotateFromTo(new double[]{0,0,1},n).assignTo(clipPlane);
		}
		ClippingPlane cpTop =  new ClippingPlane();
		clipPlane.setGeometry(cpTop);
		return clipPlane;
	}

	public double[] getClippingScale() {
		return clippingScale;
	}
	
	public boolean isClippingEnabled() {
		return clippingEnabled;
	}
}

