package de.jtem.halfedgetools.plugin;

import static de.jreality.math.Pn.EUCLIDEAN;
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
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingUtilities.getWindowAncestor;

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
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.MatrixBuilder;
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
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.AngleDefectAdapter;
import de.jtem.halfedgetools.adapter.generic.BaryCenter3dAdapter;
import de.jtem.halfedgetools.adapter.generic.BaryCenter4dAdapter;
import de.jtem.halfedgetools.adapter.generic.BaryCenterAdapter;
import de.jtem.halfedgetools.adapter.generic.EdgeLengthAdapter;
import de.jtem.halfedgetools.adapter.generic.EdgeVectorAdapter;
import de.jtem.halfedgetools.adapter.generic.FaceAreaAdapter;
import de.jtem.halfedgetools.adapter.generic.GaussCurvatureAdapter;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.adapter.generic.Position3dAdapter;
import de.jtem.halfedgetools.adapter.generic.Position4dAdapter;
import de.jtem.halfedgetools.adapter.generic.SelectionAdapter;
import de.jtem.halfedgetools.adapter.generic.TexturePosition2dAdapter;
import de.jtem.halfedgetools.adapter.generic.TexturePosition3dAdapter;
import de.jtem.halfedgetools.adapter.generic.TexturePosition4dAdapter;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.io.HalfedgeIO;
import de.jtem.halfedgetools.jreality.adapter.JRNormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRTexturePositionAdapter;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.plugin.widget.LayerPropertyWidget;
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
	private Transformation
		rootTransform = new Transformation("Normalization");
	private JTable
		layersTable = new JTable();
	private JScrollPane
		layersScroller = new JScrollPane(layersTable, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	private HalfedgeContentListener
		contentChangedListener = new HalfedgeContentListener();
	private Action
		newLayerAction = new NewLayerAction(),
		deleteLayerAction = new DeleteLayerAction(),
		mergeLayersAction = new MergeLayersAction(),
		importAction = new ImportAction(),
		exportAction = new ExportAction(),
		undoAction = new UndoAction(),
		redoAction = new RedoAction();
	private JButton
		importButton = new JButton(importAction),
		exportButton = new JButton(exportAction),
		undoButton = new JButton(undoAction),
		redoButton = new JButton(redoAction);
	private LayerPropertyWidget
		layerPropertyPanel = new LayerPropertyWidget();
	private JToggleButton
		visualizersToggle = new JToggleButton(ImageHook.getIcon("page_white_paint_arrow.png")),
		layerOptionsToggle = new JToggleButton(ImageHook.getIcon("page_white_gear_arrow.png"));
	private JPopupMenu
		layerOptionsPopup = new JPopupMenu("Layer Options"),
		visualizersPopup = new JPopupMenu("Visualizers");
	private JToolBar
		layerToolbar = new JToolBar();
	
	private JLabel
		hdsLabel = new JLabel("No HDS cached");
	private JFileChooser 
		chooser = new JFileChooser();
	
	private AdapterSet
		persistentAdapters = new AdapterSet(),
		activeVolatileAdapters = new AdapterSet(),
		volatileAdapters = new AdapterSet();
	
	private List<HalfedgeListener>
		listeners = new LinkedList<HalfedgeListener>();
	
	private ActionTool
		layerActivationTool = new ActionTool("PrimaryAction");
	private boolean
		showBoundingBox = false,
		disableListeners = false;
	private List<SelectionListener>	
		selectionListeners = new LinkedList<SelectionListener>();
	
	private List<HalfedgeLayer>
		layers = new ArrayList<HalfedgeLayer>();
	private HalfedgeLayer
		activeLayer = new HalfedgeLayer(this);
	private HalfEdgeDataStructure<?, ?, ?>
		templateHDS = null;
	
	private GeometryPreviewerPanel 
		previewPanel = new GeometryPreviewerPanel();
	
	public HalfedgeInterface() {
		makeLayout();
		// add generic and default adapters
		persistentAdapters.add(new NormalAdapter());
		persistentAdapters.add(new BaryCenterAdapter());
		persistentAdapters.add(new BaryCenter3dAdapter());
		persistentAdapters.add(new BaryCenter4dAdapter());
		persistentAdapters.add(new FaceAreaAdapter());
		persistentAdapters.add(new Position3dAdapter());
		persistentAdapters.add(new Position4dAdapter());
		persistentAdapters.add(new TexturePosition2dAdapter());
		persistentAdapters.add(new TexturePosition3dAdapter());
		persistentAdapters.add(new TexturePosition4dAdapter());
		persistentAdapters.add(new EdgeVectorAdapter());
		persistentAdapters.add(new EdgeLengthAdapter());
		persistentAdapters.add(new UndirectedEdgeIndex());
		persistentAdapters.add(new GaussCurvatureAdapter());
		persistentAdapters.add(new AngleDefectAdapter());
		
		persistentAdapters.add(new JRNormalAdapter());
		persistentAdapters.add(new JRPositionAdapter());
		persistentAdapters.add(new JRTexturePositionAdapter());
		layerActivationTool.setDescription("Layer Activation");
		root.addTool(layerActivationTool);
		root.setTransformation(rootTransform);
		layerActivationTool.addActionListener(this);
		visualizersPopup.addPopupMenuListener(this);
		
		addLayer(activeLayer);
		activateLayer(activeLayer);
		
		chooser.setAccessory(previewPanel);
		chooser.addPropertyChangeListener(previewPanel);
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
	
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
		if (visualizersPopup == e.getSource()) {
			visualizersToggle.setSelected(false);
		}
		if (layerOptionsPopup == e.getSource()) {
			layerOptionsToggle.setSelected(false);
		}
	}
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

	}
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		if (visualizersPopup == e.getSource()) {
			visualizersManager.update();
			visualizersPopup.setBorderPainted(true);
			visualizersPopup.removeAll();
			visualizersPopup.setLayout(new GridLayout());
			visualizersPopup.add(visualizersManager.getPanel());
			visualizersPopup.setMinimumSize(new Dimension(250, 400));
			visualizersPopup.setPreferredSize(new Dimension(250, 400));
		}
		if (layerOptionsPopup == e.getSource()) {
			layerPropertyPanel.setLayer(activeLayer);
			layerOptionsPopup.setBorderPainted(true);
			layerOptionsPopup.removeAll();
			layerOptionsPopup.setLayout(new GridLayout());
			layerOptionsPopup.add(layerPropertyPanel);
			layerOptionsPopup.setMinimumSize(new Dimension(250, 400));
			layerOptionsPopup.setPreferredSize(new Dimension(250, 400));
		}
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
		layerToolbar.add(deleteLayerAction);
		layerToolbar.add(mergeLayersAction);
		layerToolbar.add(new JToolBar.Separator());
		layerToolbar.add(undoAction);
		layerToolbar.add(redoAction);
		layerToolbar.add(new JToolBar.Separator());
		layerToolbar.add(importAction);
		layerToolbar.add(exportAction);
		layerToolbar.add(new JToolBar.Separator());
		layerToolbar.add(layerOptionsToggle);
		layerToolbar.add(visualizersToggle);
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
				return "Geometry Data (.heml|*.obj)";
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
		visualizersToggle.addActionListener(this);
		visualizersToggle.setToolTipText("Visualizers");
		visualizersPopup.addPopupMenuListener(this);
		layerOptionsToggle.addActionListener(this);
		layerOptionsToggle.setToolTipText("Layer Options");
		layerOptionsPopup.addPopupMenuListener(this);
	}
	
	
	private class NewLayerAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public NewLayerAction() {
			putValue(NAME, "New Layer");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white_add.png"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('N', CTRL_DOWN_MASK));
			putValue(SHORT_DESCRIPTION, "New Layer");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			HalfedgeLayer layer = new HalfedgeLayer(HalfedgeInterface.this);
			layer.setName("New Layer");
			addLayer(layer);
			activateLayer(layer);
			updateStates();
		}
		
	}
	
	private class DeleteLayerAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public DeleteLayerAction() {
			putValue(NAME, "Delete Layer");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white_delete.png"));
			putValue(SHORT_DESCRIPTION, "Delete Layer");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			HalfedgeLayer layer = getActiveLayer();
			Window w = getWindowAncestor(shrinkPanel);
			int result = JOptionPane.showConfirmDialog(w, "Delete Layer " + layer + "?");
			if (result != JOptionPane.OK_OPTION) return;
			removeLayer(layer);
			updateStates();
			checkContent();
		}
		
	}
	
	
	private class MergeLayersAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;

		public MergeLayersAction() {
			putValue(NAME, "Merge Layers");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white_link.png"));
			putValue(SHORT_DESCRIPTION, "Merge Layers");
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = getWindowAncestor(shrinkPanel);
			HalfedgeLayer layer = getActiveLayer();
			List<HalfedgeLayer> otherLayers = new LinkedList<HalfedgeLayer>(layers);
			otherLayers.remove(layer);
			HalfedgeLayer[] layersArr = otherLayers.toArray(new HalfedgeLayer[otherLayers.size()]);
			HalfedgeLayer mergeLayer = (HalfedgeLayer)JOptionPane.showInputDialog(
				w, 
				"Merge Layer " + layer.getName(), 
				"Merge Layers", 
				PLAIN_MESSAGE, 
				(Icon)getValue(SMALL_ICON), 
				layersArr, 
				layer
			);
			if (mergeLayer == null) return;
			mergeLayers(layer, mergeLayer);
			updateStates();
			checkContent();
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
			clearVolatileAdapters();
			updateStates();
			fireDataChanged();
			checkContent();
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
			clearVolatileAdapters();
			updateStates();
			fireDataChanged();
			checkContent();
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
			String name = file.getName().toLowerCase();
			if (!name.endsWith(".obj") && !name.endsWith(".heml")) {
				file = new File(file.getAbsoluteFile() + ".obj");
			}
			if (file.exists()) {
				int result2 = JOptionPane.showConfirmDialog(
					w, 
					"File " + file.getName() + " exists. Overwrite?", 
					"Overwrite?", 
					YES_NO_OPTION
				);
				if (result2 != JOptionPane.YES_OPTION) return;
			}
			try {
				if(file.getName().toLowerCase().endsWith(".heml")) {
					HalfedgeIO.writeHDS(hds, file.getAbsolutePath());
				} else if(file.getName().toLowerCase().endsWith(".obj")) {
					HalfedgeIO.writeOBJ(hds, getAdapters(), file.getAbsolutePath());
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
				ex.printStackTrace();
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
					if (g == null) return;
					if (g instanceof IndexedFaceSet) {
						IndexedFaceSet ifs = (IndexedFaceSet)g;
						IndexedFaceSetUtility.calculateAndSetNormals(ifs);
					}
					set(g);
				} else
				if (file.getName().toLowerCase().endsWith(".heml")) {
					HalfEdgeDataStructure<?, ?, ?> hds = HalfedgeIO.readHDS(file.getAbsolutePath());
					set(hds);
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.toString(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
			}
			updateStates();
			checkContent();
			encompassAll();
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
		if (visualizersToggle == e.getSource() && visualizersToggle.isSelected()) {
			int posx = visualizersToggle.getSize().width / 2;
			int posy = visualizersToggle.getSize().height / 2;
			visualizersPopup.show(visualizersToggle, posx, posy);
		}
		if (layerOptionsToggle == e.getSource() && layerOptionsToggle.isSelected()) {
			int posx = layerOptionsToggle.getSize().width / 2;
			int posy = layerOptionsToggle.getSize().height / 2;
			layerOptionsPopup.show(layerOptionsToggle, posx, posy);
		}
		updateStates();
	}

	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (disableListeners || e.getValueIsAdjusting()) return;
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
		deleteLayerAction.setEnabled(layers.size() > 1);
		undoAction.setEnabled(activeLayer.canUndo());
		redoAction.setEnabled(activeLayer.canRedo());
		undoButton.validate();
		redoButton.validate();
		
		HalfedgeLayer layer = getActiveLayer();
		int index = layers.indexOf(layer);
		disableListeners = true;
		layersTable.revalidate();
		layersTable.getSelectionModel().setSelectionInterval(index, index);
		disableListeners = false;
		getAdapters().revalidateAdapters();
		for (HalfedgeLayer l : layers) {
			l.updateBoundingBox();
			l.setShowBoundingBox(l.isActive() & isShowBoundingBox());
		}
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(final HDS hds) {
		activeLayer.set(hds);
		updateStates();
		clearVolatileAdapters();
		fireDataChanged();
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS hds) {
		HDS r = activeLayer.get(hds);
		updateStates();
		return r;
	}
	
	
	public void set(Geometry g) {
		activeLayer.set(g);
		updateStates();
		clearVolatileAdapters();
		fireDataChanged();
	}
	
	
	public HalfEdgeDataStructure<?, ?, ?> get() {
		HalfEdgeDataStructure<?, ?, ?> r = activeLayer.get();
		updateStates();
		return r;
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
		activeLayer.update();
		updateStates();
		checkContent();
		fireDataChanged();
	}
	
	public void updateNoUndo() {
		activeLayer.updateNoUndo();
		updateStates();
		checkContent();
		fireDataChanged();
	}
	
	public void updateGeometry(Adapter<double[]> positionAdapter) {
		activeLayer.updateGeometry(positionAdapter);
		updateStates();
		checkContent();
		fireDataChanged();
	}
	
	public void updateGeometryNoUndo(Adapter<double[]> positionAdapter) {
		activeLayer.updateGeometryNoUndo(positionAdapter);
		updateStates();
		checkContent();
		fireDataChanged();
	}
	
	
	/**
	 * Returns the persistent adapters of the interface and of the active
	 * layer. In addition the this the adapters are returned that have been volatile
	 * during the last conversion.
	 * last conversion
	 * @param a
	 */
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(persistentAdapters);
		result.addAll(volatileAdapters);
		result.addAll(activeLayer.getCurrentAdapters());
		return result;
	}
	
	
	/**
	 * Returns the persistent adapters of the interface and 
	 * of the active layer
	 * @return
	 */
	public AdapterSet getPersistantAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(persistentAdapters);
		result.addAll(activeLayer.getPersistentAdapters());
		return result;
	}
	
	/**
	 * Returns the adapters that will be volatile during
	 * the next conversion
	 * @return
	 */
	public AdapterSet getVolatileAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(volatileAdapters);
		result.addAll(activeLayer.getVolatileAdapters());
		return result;
	}
	
	/**
	 * Returns the adapters that have been volatile during the last 
	 * conversion
	 * @return
	 */
	public AdapterSet getActiveVolatileAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(activeVolatileAdapters);
		result.addAll(activeLayer.getActiveVolatileAdapters());
		return result;
	}
	
	/**
	 * Returns the adapters of the interface and of the active layer 
	 * that will be used during the next conversion
	 * @return
	 */
	public AdapterSet getActiveAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(persistentAdapters);
		result.addAll(activeVolatileAdapters);
		result.addAll(activeLayer.getActiveAdapters());
		return result;
	}
	
	public boolean addAdapter(Adapter<?> a, boolean persistent) {
		boolean result = false;
		if(persistent) {
			result = persistentAdapters.add(a);
		} else {
			result = volatileAdapters.add(a);
		}
		fireAdaptersChanged();
		return result;
	}
	
	public boolean addLayerAdapter(Adapter<?> a, boolean persistent) {
		boolean result = activeLayer.addAdapter(a, persistent);
		fireAdaptersChanged();
		return result;
	}
	
	public boolean removeAdapter(Adapter<?> a) {
		boolean 
			pa = persistentAdapters.remove(a),
			va = volatileAdapters.remove(a),
			la = activeLayer.removeAdapter(a);
		fireAdaptersChanged();
		return pa || va || la;
	}
	
	
	private void clearVolatileAdapters() {
		activeVolatileAdapters.clear();
		activeVolatileAdapters.addAll(volatileAdapters);
		volatileAdapters.clear();
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		String chooserDir = chooser.getCurrentDirectory().getAbsolutePath();
		c.storeProperty(getClass(), "importExportLocation", chooserDir);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		String chooserDir = System.getProperty("user.dir");
		chooserDir = c.getProperty(getClass(), "importExportLocation", chooserDir);
		File chooserDirFile = new File(chooserDir);
		if (chooserDirFile.exists()) {
			chooser.setCurrentDirectory(chooserDirFile);
		}
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		content = JRViewerUtility.getContentPlugin(c);
		content.addContentChangedListener(contentChangedListener);
		scene = c.getPlugin(Scene.class);
		visualizersManager = c.getPlugin(VisualizersManager.class);
		persistentAdapters.add(new SelectionAdapter(this));
		menuBar = c.getPlugin(ViewMenuBar.class);
		
		menuBar.addMenuItem(getClass(), -101, undoAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -100, redoAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -51, exportAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -50, importAction, "Halfedge");
		menuBar.addMenuSeparator(getClass(), -1, "Halfedge");
		
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
		SwingUtilities.updateComponentTreeUI(layerPropertyPanel);
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
		info.icon = ImageHook.getIcon("asterisk_orange.png");
		return info;
	}
	
	
	public void createSelectionAppearance(Appearance app, HalfedgeLayer layer, double offset) {
		if (scene == null) return;
		SceneGraphComponent sceneRoot = scene.getSceneRoot();
		List<SceneGraphPath> pathList = SceneGraphUtility.getPathsBetween(sceneRoot, layer.getLayerRoot());
		if (pathList.isEmpty()) return;
		EffectiveAppearance ea = EffectiveAppearance.create(pathList.get(0));
		DefaultGeometryShader dgs1 = ShaderUtility.createDefaultGeometryShader(ea);
		DefaultPointShader dps1 = (DefaultPointShader) dgs1.getPointShader();
		DefaultLineShader dls1 = (DefaultLineShader) dgs1.getLineShader();
		app.setAttribute(POINT_SHADER + "." + RADII_WORLD_COORDINATES, dps1.getRadiiWorldCoordinates());
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, dps1.getPointRadius() * (1 + offset));
		app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls1.getTubeRadius() * (1 + offset));
		app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls1.getRadiiWorldCoordinates());
	}
	
	
	public HalfedgeLayer createLayer(String name) {
		HalfedgeLayer newLayer = new HalfedgeLayer(this);
		newLayer.setName(name);
		layers.add(0, newLayer);
		root.addChild(newLayer.getLayerRoot());
		updateStates();
		fireLayerAdded(newLayer);
		return newLayer;
	}
	
	
	public void addLayer(HalfedgeLayer layer) {
		if (layers.contains(layer)) return;
		layers.add(0, layer);
		activateLayer(layer);
		root.addChild(layer.getLayerRoot());
		updateStates();
		fireLayerAdded(layer);
	}
	
	public void removeLayer(HalfedgeLayer layer) {
		if (!layers.contains(layer)) return;
		int index = Math.max(layers.indexOf(layer) - 1, 0);
		layers.remove(layer);
		root.removeChild(layer.getLayerRoot());
		if (layer == activeLayer) {
			HalfedgeLayer newActLayer = layers.get(index);
			activateLayer(newActLayer);
		}
		updateStates();
		fireLayerRemoved(layer);
	}
	
	public void encompassAll() {
		SceneGraphPath avatarPath = scene.getAvatarPath();
		SceneGraphPath scenePath = scene.getContentPath();
		SceneGraphPath cameraPath = scene.getCameraPath();
		CameraUtility.encompass(avatarPath, scenePath, cameraPath, 1.75, EUCLIDEAN);
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mergeLayers(HalfedgeLayer layer, HalfedgeLayer mergeLayer) {
		HalfEdgeDataStructure<?,?,?> hds1 = layer.get();
		HalfEdgeDataStructure<?,?,?> hds2 = createEmpty(hds1);
		hds2 = mergeLayer.get(hds2);
		int vOffset = hds1.numVertices();
		int eOffset = hds1.numEdges();
		int fOffset = hds1.numFaces();
		HalfEdgeUtils.copy(hds2, hds1);
		
		for (Vertex v : hds2.getVertices()) {
			Vertex vv = hds1.getVertex(v.getIndex() + vOffset);
			vv.copyData(v);
		}
		for (Edge e : hds2.getEdges()) {
			Edge ee = hds1.getEdge(e.getIndex() + eOffset);
			ee.copyData(e);
		}
		for (Face f : hds2.getFaces()) {
			Face ff = hds1.getFace(f.getIndex() + fOffset);
			ff.copyData(f);
		}
		
		removeLayer(mergeLayer);
		layer.set(hds1);
		activateLayer(layer);
	}
	
	public void activateLayer(HalfedgeLayer layer) {
		HalfedgeLayer old = activeLayer;
		activeLayer = layer;
		for (HalfedgeLayer l : layers) {
			l.setActive(l == layer);
		}
		fireSelectionChanged(getSelection());
		fireActiveLayerChanged(old, activeLayer);
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
		MatrixBuilder mb = MatrixBuilder.euclidean();
		rootTransform.setMatrix(mb.getArray());
		Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(root);
		double maxExtend = bbox.getMaxExtent();		
		mb.scale(10 / maxExtend);
		rootTransform.setMatrix(mb.getArray());
		for (HalfedgeLayer l : layers) {
			l.updateBoundingBox();
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
					if (c.getGeometry() != null) {
						Geometry g = c.getGeometry();
						Transformation layerTransform = new Transformation(path.getMatrix(null));
						HalfedgeLayer layer = new HalfedgeLayer(g, HalfedgeInterface.this);
						layer.setName(g.getName());
						layer.setTransformation(layerTransform);
						newLayers.add(layer);
					}
					c.childrenAccept(this);
					path.pop();
				}
			});
			if (newLayers.isEmpty()) return;
			Collections.reverse(newLayers);
			List<HalfedgeLayer> oldLayers = new LinkedList<HalfedgeLayer>(layers);
			for (HalfedgeLayer l : newLayers) {
				addLayer(l);
			}
			for (HalfedgeLayer l : oldLayers) {
				removeLayer(l);
			}
			activateLayer(newLayers.get(0));
			checkContent();
			updateStates();
			encompassAll();
		}

	}
	
	
	public void addHalfedgeListener(HalfedgeListener l) {
		listeners.add(l);
	}
	
	public void removeHalfedgeListener(HalfedgeListener l) {
		listeners.remove(l);
	}
	
	
	protected void fireActiveLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		for (HalfedgeListener l : listeners) {
			l.activeLayerChanged(old, active);
		}
	}
	
	protected void fireDataChanged() {
		for (HalfedgeListener l : listeners) {
			l.dataChanged(getActiveLayer());
		}
	}
	
	protected void fireAdaptersChanged() {
		for (HalfedgeListener l : listeners) {
			l.adaptersChanged(getActiveLayer());
		}
	}
	
	protected void fireLayerAdded(HalfedgeLayer layer) {
		for (HalfedgeListener l : listeners) {
			l.layerCreated(layer);
		}
	}
	protected void fireLayerRemoved(HalfedgeLayer layer) {
		for (HalfedgeListener l : listeners) {
			l.layerRemoved(layer);
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
	
	public boolean isShowBoundingBox() {
		return showBoundingBox;
	}
	
	protected void setShowBoundingBox(boolean showBoundingBox) {
		this.showBoundingBox = showBoundingBox;
		updateStates();
	}
	
	public SceneGraphComponent getHalfedgeRoot() {
		return root;
	}
	
	public void setTemplateHDS(HalfEdgeDataStructure<?, ?, ?> templateHDS) {
		this.templateHDS = templateHDS;
	}
	public HalfEdgeDataStructure<?, ?, ?> getTemplateHDS() {
		return templateHDS;
	}

}
