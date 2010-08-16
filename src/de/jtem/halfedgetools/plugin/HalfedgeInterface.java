package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.util.SceneGraphUtility.getPathsBetween;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Content.ContentChangedEvent;
import de.jreality.plugin.basic.Content.ContentChangedListener;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.Transformation;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.ActionTool;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Calculator;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.io.HalfedgeIO;
import de.jtem.halfedgetools.jreality.adapter.JRNormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRTexCoordAdapter;
import de.jtem.halfedgetools.jreality.calculator.JRFaceAreaCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRFaceNormalCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRSubdivisionCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRVertexPositionCalculator;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;


public class HalfedgeInterface extends ShrinkPanelPlugin implements ListSelectionListener, ActionListener, PopupMenuListener {

	private Scene
		scene = null;
	private Content
		content = null;
	private VisualizersManager
		visualizersManager = null;
	private ViewMenuBar
		menuBar = null;
	private SceneGraphComponent
		root = new SceneGraphComponent("Halfedge Root");
	private JTable
		layersTable = new JTable();
	private JScrollPane
		layersScroller = new JScrollPane(layersTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	private HalfedgeContentListener
		contentChangedListener = new HalfedgeContentListener();
	private Action
		newLayerAction = new NewLayerAction(),
		importAction = new ImportAction(),
		exportAction = new ExportAction(),
		undoAction = new UndoAction(),
		redoAction = new RedoAction();
	private JButton
		importButton = new JButton(importAction),
		exportButton = new JButton(exportAction),
		undoButton = new JButton(undoAction),
		redoButton = new JButton(redoAction);
	private JPopupMenu
		visualizersPopup = new JPopupMenu("Visualizers");
	private JToolBar
		layerToolbar = new JToolBar();
	
	private JLabel
		hdsLabel = new JLabel("No HDS cached");
	private JFileChooser 
		chooser = new JFileChooser();
	
	private AdapterSet
		adapters = new AdapterSet();
	private CalculatorSet
		calculators = new CalculatorSet();
	
	private List<HalfedgeListener>
		listeners = new LinkedList<HalfedgeListener>();
	
	private ActionTool
		layerActivationTool = new ActionTool("PrimaryAction");
	private boolean
		disableListeners = false;
	private List<SelectionListener>	
		selectionListeners = new LinkedList<SelectionListener>();

	
	
	private List<HalfedgeLayer>
		layers = new ArrayList<HalfedgeLayer>();
	private HalfedgeLayer
		activeLayer = new HalfedgeLayer(this);
	
	
	public HalfedgeInterface() {
		makeLayout();
		adapters.add(new JRNormalAdapter());
		adapters.add(new JRPositionAdapter());
		adapters.add(new JRTexCoordAdapter());
		adapters.add(new NormalAdapter());
		calculators.add(new JRVertexPositionCalculator());
		calculators.add(new JRFaceAreaCalculator());
		calculators.add(new JRFaceNormalCalculator());
		calculators.add(new JRSubdivisionCalculator());
		root.addTool(layerActivationTool);
		layerActivationTool.addActionListener(this);
		visualizersPopup.addPopupMenuListener(this);
		
		addLayer(activeLayer);
		activateLayer(activeLayer);
	}
	
	
	public boolean addSelectionListener(SelectionListener l) {
		return selectionListeners.add(l);
	}
	public boolean removeSelectionListener(SelectionListener l) {
		return selectionListeners.remove(l);
	}
	protected void fireSelectionChanged(HalfedgeSelection sel) {
		for (SelectionListener l : selectionListeners) {
			l.selectionChanged(sel, this);
		}
	}
	
	
//	private class LayerVisibilityListener implements CellEditorListener {
//
//		@Override
//		public void editingCanceled(ChangeEvent e) {
//		}
//
//		@Override
//		public void editingStopped(ChangeEvent e) {
//			
//		}
//		
//	}
	
	
	private class LayerModel extends DefaultTableModel {
		
		private static final long 
			serialVersionUID = 1L;

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public int getRowCount() {
			return layers.size();
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 && layers.size() >= row) return null;
			HalfedgeLayer layer = layers.get(row);
			switch (column) {
			case 0:
				return layer.isVisible();
			case 1:
				return layer.getName();
			default:
				return "-";
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
			case 0:
			case 1: 
				return true;
			default: 
				return false;
			}
		}
		
		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (row < 0 && layers.size() >= row) return;
			HalfedgeLayer layer = layers.get(row);
			switch (column) {
			case 0:
				layer.setVisible((Boolean)aValue);
				break;
			case 1: 
				layer.setName((String)aValue);
				break;
			default:
				return;
			}
		}
		
	}
	
	
	
	public class AuxSceneGraphComponent extends SceneGraphComponent {
		
		public AuxSceneGraphComponent() {
			super("Halfedge Aux");
		}
		
		public void startWriting() {
			startWriter();
		}
		
		public void finishWriting() {
			finishWriter();
		}
		
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		
	}
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

	}
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		visualizersManager.update();
		visualizersPopup.removeAll();
		visualizersPopup.setLayout(new GridLayout());
		visualizersPopup.add(visualizersManager.getPanel());
		visualizersPopup.setSize(300, 400);
		visualizersPopup.setMinimumSize(new Dimension(300, 400));
		visualizersPopup.setPreferredSize(new Dimension(300, 400));
	}
	
	private void makeLayout() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
				
		JPanel layersPanel = new JPanel();
		layersPanel.setLayout(new GridLayout());
		layersPanel.add(layersScroller);
		layersScroller.setMinimumSize(new Dimension(30, 150));
		layersTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
//		layersTable.getDefaultEditor(Boolean.class).addCellEditorListener(new LayerVisibilityListener());
		layersTable.setRowHeight(22);
		layersTable.getSelectionModel().addListSelectionListener(this);
		layersTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		layersTable.setComponentPopupMenu(visualizersPopup);
		
		shrinkPanel.add(hdsLabel, c);
		c.weighty = 1.0;
		shrinkPanel.add(layersPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(layerToolbar, c);
		layerToolbar.add(newLayerAction);
		layerToolbar.add(new JSeparator());
		layerToolbar.add(undoAction);
		layerToolbar.add(redoAction);
		layerToolbar.add(new JSeparator());
		layerToolbar.add(importAction);
		layerToolbar.add(exportAction);
		layerToolbar.setFloatable(false);
		
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Wavefront OBJ (*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Halfedge XML (*.heml)";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".heml");
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});
		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj") ||
				f.getName().toLowerCase().endsWith(".heml");
			}

			@Override
			public String getDescription() {
				return "Halfedge Geomtry (.heml|*.obj)";
			}
			
			@Override
			public String toString() {
				return getDescription();
			}
		});
		
		undoButton.addActionListener(this);
		undoButton.setEnabled(false);
		undoButton.setToolTipText("Undo");
		redoButton.addActionListener(this);
		redoButton.setEnabled(false);
		redoButton.setToolTipText("Redo");
		exportButton.addActionListener(this);
		exportButton.setToolTipText("Save Halfedge Geometry");
		importButton.addActionListener(this);
		importButton.setToolTipText("Load Halfedge Geometry");
	}
	
	
	private class NewLayerAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public NewLayerAction() {
			putValue(NAME, "New Layer");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', CTRL_DOWN_MASK));
			putValue(SHORT_DESCRIPTION, "New Layer");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			HalfedgeLayer layer = new HalfedgeLayer();
			layer.setName("New Layer");
			addLayer(layer);
			updateStates();
		}
		
	}
	
	
	private class UndoAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public UndoAction() {
			putValue(NAME, "Undo");
			putValue(SMALL_ICON, ImageHook.getIcon("book_previous.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Z', CTRL_DOWN_MASK));
			putValue(SHORT_DESCRIPTION, "Undo");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			getActiveLayer().undo();
			updateStates();
		}
		
	}
	
	private class RedoAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public RedoAction() {
			putValue(NAME, "Redo");
			putValue(SMALL_ICON, ImageHook.getIcon("book_next.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Y', CTRL_DOWN_MASK));
			putValue(SHORT_DESCRIPTION, "Redo");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			getActiveLayer().redo();
			updateStates();
		}
		
	}
	
	
	private class ExportAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public ExportAction() {
			putValue(NAME, "Export");
			putValue(SMALL_ICON, ImageHook.getIcon("disk.png"));
			putValue(SHORT_DESCRIPTION, "Export");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			HalfEdgeDataStructure<?, ?, ?> hds = get();
			chooser.setDialogTitle("Export Layer Geometry");
			int result = chooser.showSaveDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File file = chooser.getSelectedFile();
			try {
				if(file.getName().toLowerCase().endsWith(".heml")) {
					HalfedgeIO.writeHDS(hds, file.getAbsolutePath());
				} else if(file.getName().toLowerCase().endsWith(".obj")) {
					HalfedgeIO.writeOBJ(hds, adapters, file.getAbsolutePath());
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.getLocalizedMessage());
			}
		}
		
	}
	
	
	private class ImportAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public ImportAction() {
			putValue(NAME, "Import");
			putValue(SMALL_ICON, ImageHook.getIcon("folder.png"));
			putValue(SHORT_DESCRIPTION, "Import");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Import Into Layer");
			int result = chooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION) return;
			File file = chooser.getSelectedFile();
			try {
				if (file.getName().toLowerCase().endsWith(".obj")) {
					ReaderOBJ reader = new ReaderOBJ();
					SceneGraphComponent c = reader.read(file);
					Geometry g = SceneGraphUtility.getFirstGeometry(c);
					if (g instanceof IndexedFaceSet) {
						IndexedFaceSet ifs = (IndexedFaceSet)g;
						IndexedFaceSetUtility.calculateAndSetNormals(ifs);
						getActiveLayer().set(ifs);
					}
				} else
				if (file.getName().toLowerCase().endsWith(".heml")) {
					HalfEdgeDataStructure<?, ?, ?> hds = HalfedgeIO.readHDS(file.getAbsolutePath());
					set(hds, getAdapters());
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
			}
			updateStates();
			checkContent();
		}
		
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("ActionTool".equals(e.getActionCommand())) {
			ToolContext tc = (ToolContext)e.getSource();
			SceneGraphNode pickNode = tc.getCurrentPick().getPickPath().getLastElement();
			for (HalfedgeLayer layer : layers) {
				if (pickNode == layer.getGeometry()) {
					activateLayer(layer);
					return;
				}
			}
		}
		updateStates();
	}

	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (disableListeners) return;
		int row = layersTable.getSelectedRow();
		if (row < 0) return;
		if (layersTable.getRowSorter() != null) {
			row = layersTable.getRowSorter().convertRowIndexToModel(row);
		}
		activateLayer(layers.get(row));
	}
	
	
	protected void updateStates() {
		HalfEdgeDataStructure<?, ?, ?> hds = activeLayer.get();
		String text = hds.getClass().getSimpleName() + ": ";
		text += "V" + hds.numVertices() + " ";
		text += "E" + hds.numEdges() + " ";
		text += "F" + hds.numFaces() + " ";
		hdsLabel.setText(text);
		hdsLabel.repaint();
		undoAction.setEnabled(activeLayer.canUndo());
		redoAction.setEnabled(activeLayer.canRedo());
		undoButton.updateUI();
		redoButton.updateUI();
		
		HalfedgeLayer layer = getActiveLayer();
		int index = layers.indexOf(layer);
		disableListeners = true;
		layersTable.revalidate();
		layersTable.getSelectionModel().setSelectionInterval(index, index);
		disableListeners = false;
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(final HDS hds, final AdapterSet a) {
		activeLayer.set(hds, a);
		updateStates();
		checkContent();
	}

	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds) {
		activeLayer.set(hds);
		updateStates();
		checkContent();
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS hds, AdapterSet a) {
		return activeLayer.get(hds, a);
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS hds) {
		return activeLayer.get(hds);
	}
	
	
	public HalfEdgeDataStructure<?, ?, ?> get() {
		return activeLayer.get();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS createEmpty(HDS template) {
		try {
			return (HDS)template.getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	
	public void update() {
		activeLayer.set(activeLayer.get());
		updateStates();
		checkContent();
	}
	
	public void updateNoUndo() {
		activeLayer.setNoUndo(activeLayer.get(), new AdapterSet());
		updateStates();
		checkContent();
	}
	
	
	/**
	 * Returns a collection of cached and normal adapters
	 * @param a
	 */
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(adapters);
		return result;
	}
	
	public boolean addAdapter(Adapter<?> a) {
		return adapters.add(a);
	}
	
	public boolean removeAdapter(Adapter<?> a) {
		return adapters.remove(a);
	}
	
	
	public CalculatorSet getCalculators() {
		return calculators;
	}
	
	public boolean addCalculator(Calculator c) {
		return calculators.add(c);
	}
	
	public boolean removeCalculator(Calculator c) {
		return calculators.remove(c);
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "importExportLocation", chooser.getCurrentDirectory());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		File chooserDir = new File(System.getProperty("user.dir"));
		chooserDir = c.getProperty(getClass(), "importExportLocation", chooserDir);
		if (chooserDir.exists()) {
			chooser.setCurrentDirectory(chooserDir);
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		content = JRViewerUtility.getContentPlugin(c);
		content.addContentChangedListener(contentChangedListener);
		scene = c.getPlugin(Scene.class);
		visualizersManager = c.getPlugin(VisualizersManager.class);
		adapters.add(new SelectionAdapter(this));
		menuBar = c.getPlugin(ViewMenuBar.class);
		
		menuBar.addMenuItem(getClass(), -101, undoAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -100, redoAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -51, exportAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -50, importAction, "Halfedge");
		
		layersTable.setModel(new LayerModel());
		layersTable.getColumnModel().getColumn(0).setMaxWidth(30);
		layersTable.getSelectionModel().addListSelectionListener(this);
		layersTable.setSelectionMode(SINGLE_SELECTION);
		
		updateStates();
	}
	
	
	public List<Action> getHalfedgeActions() {
		List<Action> actions = new LinkedList<Action>();
		actions.add(undoAction);
		actions.add(redoAction);
		actions.add(exportAction);
		actions.add(importAction);
		return actions;
	}
	
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		content.removeContentChangedListener(contentChangedListener);
	}
	
	
	@Override
	public void mainUIChanged(String uiClass) {
		SwingUtilities.updateComponentTreeUI(chooser);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Halfedge JReality Interface";
		info.vendorName = "Stefan Sechelmann";
		return info;
	}
	
	
	protected void createSelectionAppearance(Appearance app, HalfedgeLayer layer) {
		if (scene == null) return;
		SceneGraphComponent sceneRoot = scene.getSceneRoot();
		List<SceneGraphPath> pathList = SceneGraphUtility.getPathsBetween(sceneRoot, layer.getLayerRoot());
		if (pathList.isEmpty()) return;
		EffectiveAppearance ea = EffectiveAppearance.create(pathList.get(0));
		DefaultGeometryShader dgs1 = ShaderUtility.createDefaultGeometryShader(ea);
		DefaultPointShader dps1 = (DefaultPointShader) dgs1.getPointShader();
		DefaultLineShader dls1 = (DefaultLineShader) dgs1.getLineShader();
		app.setAttribute(POINT_SHADER + "." + RADII_WORLD_COORDINATES, dps1.getRadiiWorldCoordinates());
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, dps1.getPointRadius() * 1.1);
		app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls1.getTubeRadius() * 1.1);
		app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls1.getRadiiWorldCoordinates());
	}
	
	
	public void addLayer(HalfedgeLayer layer) {
		layers.add(0, layer);
		root.addChild(layer.getLayerRoot());
	}
	
	public void removeLayer(HalfedgeLayer layer) {
		layers.remove(layer);
		root.removeChild(layer.getLayerRoot());
	}
	
	public void activateLayer(HalfedgeLayer layer) {
		activeLayer = layer;
		for (HalfedgeLayer l : layers) {
			l.setActive(l == layer);
		}
		fireSelectionChanged(getSelection());
		updateStates();		
	}
	
	public HalfedgeLayer getActiveLayer() {
		return activeLayer;
	}
	
	public void checkContent() {
		if (scene == null) return;
		List<SceneGraphPath> paths = getPathsBetween(scene.getSceneRoot(), root);
		if (paths.isEmpty()) {
			content.setContent(root);
		}
	}
	
	
	public class HalfedgeContentListener implements ContentChangedListener {
		
		@Override
		public void contentChanged(ContentChangedEvent cce) {
			if (cce.node == root || cce.node == null) return; // update boomerang
			
			final List<HalfedgeLayer> newLayers = new LinkedList<HalfedgeLayer>();
			cce.node.accept(new SceneGraphVisitor() {
				
				private SceneGraphPath
					path = new SceneGraphPath();
				
				@Override
				public void visit(SceneGraphComponent c) {
					if (!c.isVisible()) return;
					path.push(c);
					if (c.getGeometry() instanceof IndexedFaceSet) {
						Transformation layerTransform = new Transformation(path.getMatrix(null));
						IndexedFaceSet ifs = (IndexedFaceSet)c.getGeometry();
						HalfedgeLayer layer = new HalfedgeLayer(ifs, HalfedgeInterface.this);
						layer.setName(ifs.getName());
						layer.setLayerTransformation(layerTransform);
						newLayers.add(layer);
					}
					c.childrenAccept(this);
					path.pop();
				}
			});
			if (newLayers.isEmpty()) return;
			Collections.reverse(newLayers);
			for (HalfedgeLayer layer : newLayers) {
				addLayer(layer);
			}
			activateLayer(newLayers.get(0));
			checkContent();
			updateStates();
		}

	}
	
	
	public void addHalfedgeListener(HalfedgeListener l) {
		listeners.add(l);
	}
	
	public void removeHalfedgeListener(HalfedgeListener l) {
		listeners.remove(l);
	}
	
	
	protected < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void fireHalfedgeConverting(HDS hds) {
		for (HalfedgeListener l : listeners) {
			l.halfedgeConverting(hds, getAdapters(), this);
		}
	}
	
	protected < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void fireHalfedgeChanged(HDS hds) {
		for (HalfedgeListener l : listeners) {
			l.halfedgeChanged(hds, getAdapters(), this);
		}
	}
	
	
	public HalfedgeSelection getSelection() {
		return activeLayer.getSelection();
	}
	public void setSelection(HalfedgeSelection s) {
		activeLayer.setSelection(s);
	}
	public void clearSelection() {
		activeLayer.clearSelection();
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected) {
		getSelection().setSelected(n, selected);
		activeLayer.updateSelection();
	}
	
	public boolean isSelected(Node<?,?,?> n) {
		return getSelection().isSelected(n);
	}
	
}
