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
import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingUtilities.getWindowAncestor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
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
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

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
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobListener;
import de.jreality.plugin.job.JobQueuePlugin;
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
import de.jtem.halfedgetools.adapter.generic.GaussCurvatureAdapter;
import de.jtem.halfedgetools.adapter.generic.SelectionAdapter;
import de.jtem.halfedgetools.adapter.generic.UndirectedEdgeIndex;
import de.jtem.halfedgetools.io.HalfedgeIO;
import de.jtem.halfedgetools.jreality.ConverterHds2Ifs;
import de.jtem.halfedgetools.jreality.adapter.JRNormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRTexturePositionAdapter;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.plugin.widget.LayerPropertyWidget;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.SelectionListener;
import de.jtem.halfedgetools.selection.TypedSelection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class HalfedgeInterface extends ShrinkPanelPlugin implements
		ListSelectionListener, ActionListener, PopupMenuListener {

	private Controller 
		controller = null;
	private Scene 
		scene = null;
	private Content 
		content = null;
	private VisualizersManager 
		visualizersManager = null;
	private ViewMenuBar 
		menuBar = null;
	private JobQueuePlugin 
		jobQueue = null;
	private SelectionInterface
		selectionInterface = null;

	private SceneGraphComponent 
		root = new SceneGraphComponent("Halfedge Root");
	private Transformation 
		rootTransform = new Transformation("Normalization");
	private JTable 
		layersTable = new JTable();
	private JScrollPane 
		layersScroller = new JScrollPane(layersTable,VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
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
		listeners = Collections.synchronizedList(new LinkedList<HalfedgeListener>());

	private ActionTool 
		layerActivationTool = new ActionTool("PrimaryAction");
	private boolean 
		showBoundingBox = false, 
		disableListeners = false;
	private List<SelectionListener> 
		selectionListeners = Collections.synchronizedList(new LinkedList<SelectionListener>());

	private List<HalfedgeLayer> 
		layers = new ArrayList<HalfedgeLayer>();
	private HalfedgeLayer 
		activeLayer = new HalfedgeLayer(this);
	private HalfEdgeDataStructure<?, ?, ?> 
		templateHDS = null;

	private GeometryPreviewerPanel 
		previewPanel = new GeometryPreviewerPanel();

	private ConverterHds2Ifs 
		converterHds2Ifs = null;
	
	public HalfedgeInterface() {
		makeLayout();
		// add generic and default adapters
		persistentAdapters.addAll(AdapterSet.createGenericAdapters());

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

	protected void fireSelectionChanged(final Selection sel) {
		synchronized (selectionListeners) {
			for (final SelectionListener l : selectionListeners) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						l.selectionChanged(sel, HalfedgeInterface.this);
					}
				};
				EventQueue.invokeLater(r);
			}
		}
	}

	private class LayerModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		private final String[] header = new String[]{"","Name","V","E","F"}; 
		
		@Override
		public String getColumnName(int column) {
			return header[column];
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0:
				return Boolean.class;
			case 2:
			case 3:
			case 4:
				return Visibility.class;
			case 1:
				return String.class;
			default:
				return String.class;
			}
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return layers.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 && layers.size() >= row)
				return null;
			HalfedgeLayer layer = layers.get(row);
			switch (column) {
			case 0:
				return layer.isVisible();
			case 1:
				return layer.getName();
			case 2:
				return layer.getVertexVisibility();
			case 3:
				return layer.getEdgeVisibility();
			case 4:
				return layer.getFaceVisibility();
			default:
				return "-";
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
				return true;
			default:
				return false;
			}
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			if (row < 0 && layers.size() >= row)
				return;
			HalfedgeLayer layer = layers.get(row);
			switch (column) {
			case 0:
				layer.setVisible((Boolean) aValue);
				break;
			case 1:
				layer.setName((String) aValue);
				break;
			case 2:
				layer.setVertexVisiblity((Visibility)aValue);
				break;
			case 3:
				layer.setEdgeVisibility((Visibility)aValue);
				break;
			case 4:
				layer.setFaceVisibility((Visibility)aValue);
				break;
			default:
				return;
			}
		}

	}

	private class VisibilityRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		public VisibilityRenderer() {
			super();
			setHorizontalAlignment(CENTER);
		}
		
		@Override
		protected void setValue(Object value) {
			if(value == Visibility.INHERITED) {
				setIcon(ImageHook.getIcon("bullet_arrow_up.png"));
				setToolTipText("Inherited");
			} else if(value == Visibility.SHOW) {
				setIcon(ImageHook.getIcon("bullet_green.png"));
				setToolTipText("Show");
			} else {
				setIcon(ImageHook.getIcon("bullet_red.png"));
				setToolTipText("Hide");
			}
		}
	}
	
	private class VisibilityEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		private static final long serialVersionUID = 1L;
		private final JButton
			greenButton = new JButton(ImageHook.getIcon("bullet_green.png")),
			redButton = new JButton(ImageHook.getIcon("bullet_red.png")),
			inheritButton = new JButton(ImageHook.getIcon("bullet_arrow_up.png"));
		private Visibility currentValue = null;
		
		public VisibilityEditor() {
			super();
			greenButton.addActionListener(this);
			redButton.addActionListener(this);
			inheritButton.addActionListener(this);
		}
		
		@Override
		public Object getCellEditorValue() {
			return currentValue;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			currentValue = (Visibility)value;
			if(currentValue == Visibility.SHOW) {
				return greenButton;	
			} else if(currentValue == Visibility.HIDE) {
				return redButton;
			} else { //if(currentValue == Visibility.INHERITED){
				return inheritButton;
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if(greenButton == source) {
				currentValue = Visibility.HIDE;
			} else if(redButton == source) {
				currentValue = Visibility.INHERITED;
			} else if(inheritButton == source) {
				currentValue = Visibility.SHOW;
			}
			fireEditingStopped();
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
			visualizersPopup.setMinimumSize(new Dimension(300, 400));
			visualizersPopup.setPreferredSize(new Dimension(300, 400));
		}
		if (layerOptionsPopup == e.getSource()) {
			layerPropertyPanel.setLayer(activeLayer);
			layerOptionsPopup.setBorderPainted(true);
			layerOptionsPopup.removeAll();
			layerOptionsPopup.setLayout(new GridLayout());
			layerOptionsPopup.add(layerPropertyPanel);
			layerOptionsPopup.setMinimumSize(new Dimension(300, 430));
			layerOptionsPopup.setPreferredSize(new Dimension(300, 430));
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
		layersTable.setRowHeight(22);
		layersTable.getSelectionModel().addListSelectionListener(this);
		layersTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		layersTable.setDefaultEditor(Visibility.class, new VisibilityEditor());
		layersTable.setDefaultRenderer(Visibility.class, new VisibilityRenderer());
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
		//add File Filters
		chooser.setFileFilter(new FileNameExtensionFilter("Wavefront OBJ (*.obj)", "obj"));
		chooser.setFileFilter(new FileNameExtensionFilter("Halfedge XML (*.heml)", "heml"));

		chooser.setFileFilter(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".obj")
						|| f.getName().toLowerCase().endsWith(".heml");
			}

			@Override
			public String getDescription() {
				return "Geometry Data (*.heml|*.obj)";
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

		private static final long serialVersionUID = 1L;

		public NewLayerAction() {
			putValue(NAME, "New Layer");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white_add.png"));
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke('N', CTRL_DOWN_MASK));
			putValue(SHORT_DESCRIPTION, "New Layer");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			HalfedgeLayer layer = new HalfedgeLayer(HalfedgeInterface.this);
			int i = 1;
			Set<String> layerNames = new HashSet<String>();
			for(HalfedgeLayer l : layers) {
				layerNames.add(l.getName());
			}
			while(layerNames.contains("New layer " + i)) {
				++i;
			}
			layer.setName("New layer " + i);
			addLayer(layer);
			activateLayer(layer);
			updateStates();
		}

	}

	private class DeleteLayerAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public DeleteLayerAction() {
			putValue(NAME, "Delete Layer");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white_delete.png"));
			putValue(SHORT_DESCRIPTION, "Delete Layer");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			HalfedgeLayer layer = getActiveLayer();
			Window w = getWindowAncestor(shrinkPanel);
			int result = JOptionPane.showConfirmDialog(w, "Delete Layer "
					+ layer + "?");
			if (result != JOptionPane.OK_OPTION)
				return;
			removeLayer(layer);
			updateStates();
			checkContent();
		}

	}

	private class MergeLayersAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public MergeLayersAction() {
			putValue(NAME, "Merge Layers");
			putValue(SMALL_ICON, ImageHook.getIcon("page_white_link.png"));
			putValue(SHORT_DESCRIPTION, "Merge Layers");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = getWindowAncestor(shrinkPanel);
			final HalfedgeLayer layer = getActiveLayer();
			List<HalfedgeLayer> otherLayers = new LinkedList<HalfedgeLayer>(layers);
			otherLayers.remove(layer);
			HalfedgeLayer[] layersArr = otherLayers.toArray(new HalfedgeLayer[otherLayers.size()]);
			final JList layerList = new JList(layersArr);
			JScrollPane scroller = new JScrollPane(layerList);
			scroller.setPreferredSize(new Dimension(200, 300));
			
			int r = JOptionPane.showConfirmDialog(w, scroller, "Merge Layers", OK_CANCEL_OPTION, PLAIN_MESSAGE, (Icon)getValue(SMALL_ICON));
			if (r != JOptionPane.OK_OPTION) {
				return;
			}
			
			Job mergeJob = new AbstractJob() {
				@Override
				public String getJobName() {
					return "Merge Layers";
				}
				@Override
				protected void executeJob() throws Exception {
					fireJobProgress(0.0);
					Object[] selectedLayers = layerList.getSelectedValues();
					double count = 0;
					for (Object l : selectedLayers) {
						HalfedgeLayer hl = (HalfedgeLayer)l;
						mergeLayers(layer, hl);
						fireJobProgress(count++ / selectedLayers.length);
					}
					updateStates();
				}
			};
			jobQueue.queueJob(mergeJob);
		}

	}

	private class UndoAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public UndoAction() {
			putValue(NAME, "Undo");
			putValue(SMALL_ICON, ImageHook.getIcon("book_previous.png"));
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke('Z', CTRL_DOWN_MASK));
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

		private static final long serialVersionUID = 1L;

		public RedoAction() {
			putValue(NAME, "Redo");
			putValue(SMALL_ICON, ImageHook.getIcon("book_next.png"));
			putValue(ACCELERATOR_KEY,
					KeyStroke.getKeyStroke('Y', CTRL_DOWN_MASK));
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

	private class ExportAction extends AbstractAction implements Job {

		private static final long serialVersionUID = 1L;
		private File selectedFile = null;

		public ExportAction() {
			putValue(NAME, "Export");
			putValue(SMALL_ICON, ImageHook.getIcon("disk.png"));
			putValue(SHORT_DESCRIPTION, "Export");
		}

		private ExportAction(File selectedFile) {
			this.selectedFile = selectedFile;
		}

		@Override
		public void execute() throws Exception {
			final Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			HalfEdgeDataStructure<?, ?, ?> hds = get();
			try {
				if (selectedFile.getName().toLowerCase().endsWith(".heml")) {
					HalfedgeIO.writeHDS(hds, selectedFile.getAbsolutePath());
				} else if (selectedFile.getName().toLowerCase()
						.endsWith(".obj")) {
					HalfedgeIO.writeOBJ(hds, getAdapters(),
							selectedFile.getAbsolutePath());
				}
			} catch (final Exception ex) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(w, ex.getMessage(), ex
								.getClass().getSimpleName(), ERROR_MESSAGE);
					}
				};
				EventQueue.invokeLater(r);
			}
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Export Layer Geometry");
			chooser.setPreferredSize(new Dimension(800, 700));
			int result = chooser.showSaveDialog(w);
			if (result != JFileChooser.APPROVE_OPTION)
				return;
			File file = chooser.getSelectedFile();

			String name = file.getName().toLowerCase();
			if (!name.endsWith(".obj") && !name.endsWith(".heml")) {
				file = new File(file.getAbsoluteFile() + ".obj");
			}
			if (file.exists()) {
				int result2 = JOptionPane.showConfirmDialog(w,
						"File " + file.getName() + " exists. Overwrite?",
						"Overwrite?", YES_NO_OPTION);
				if (result2 != JOptionPane.YES_OPTION)
					return;
			}

			ExportAction exportJob = new ExportAction(file);
			jobQueue.queueJob(exportJob);
		}

		@Override
		public String getJobName() {
			return "Export Geometry";
		}

		@Override
		public void addJobListener(JobListener arg0) {
		}

		@Override
		public void removeJobListener(JobListener arg0) {
		}

		@Override
		public void removeAllJobListeners() {
		}

	}

	private class ImportAction extends AbstractAction implements Job {

		private static final long serialVersionUID = 1L;
		private File selectedFile = null;

		public ImportAction() {
			putValue(NAME, "Import");
			putValue(SMALL_ICON, ImageHook.getIcon("folder.png"));
			putValue(SHORT_DESCRIPTION, "Import");
		}

		public ImportAction(File selectedFile) {
			this.selectedFile = selectedFile;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			final Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			chooser.setDialogTitle("Import Into Layer");
			int result = chooser.showOpenDialog(w);
			if (result != JFileChooser.APPROVE_OPTION)
				return;
			File file = chooser.getSelectedFile();
			ImportAction importJob = new ImportAction(file);
			jobQueue.queueJob(importJob);
		}

		@Override
		public void execute() throws Exception {
			final Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
			try {
				if (selectedFile.getName().toLowerCase().endsWith(".obj")) {
					ReaderOBJ reader = new ReaderOBJ();
					SceneGraphComponent c = reader.read(selectedFile);
					Geometry g = SceneGraphUtility.getFirstGeometry(c);
					if (g == null)
						return;
					if (g instanceof IndexedFaceSet) {
						IndexedFaceSet ifs = (IndexedFaceSet) g;
						IndexedFaceSetUtility.calculateAndSetNormals(ifs);
					}
					set(g);
				} else if (selectedFile.getName().toLowerCase()
						.endsWith(".heml")) {
					String filePath = selectedFile.getAbsolutePath();
					HalfEdgeDataStructure<?, ?, ?> hds = HalfedgeIO
							.readHDS(filePath);
					set(hds);
				}
			} catch (final Exception ex) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(w, ex.toString(), ex
								.getClass().getSimpleName(), ERROR_MESSAGE);
					}
				};
				EventQueue.invokeLater(r);
			}
			updateStates();
			checkContent();
			encompassContent();
		}

		@Override
		public String getJobName() {
			return "Import Geometry";
		}

		@Override
		public void addJobListener(JobListener arg0) {
		}

		@Override
		public void removeJobListener(JobListener arg0) {
		}

		@Override
		public void removeAllJobListeners() {
		}

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("ActionTool".equals(e.getActionCommand())) {
			ToolContext tc = (ToolContext) e.getSource();
			SceneGraphNode pickNode = tc.getCurrentPick().getPickPath()
					.getLastElement();
			for (HalfedgeLayer layer : layers) {
				if (pickNode == layer.getGeometry()) {
					activateLayer(layer);
					return;
				}
			}
		}
		if (visualizersToggle == e.getSource()
				&& visualizersToggle.isSelected()) {
			int posx = visualizersToggle.getSize().width / 2;
			int posy = visualizersToggle.getSize().height / 2;
			visualizersPopup.show(visualizersToggle, posx, posy);
		}
		if (layerOptionsToggle == e.getSource()
				&& layerOptionsToggle.isSelected()) {
			int posx = layerOptionsToggle.getSize().width / 2;
			int posy = layerOptionsToggle.getSize().height / 2;
			layerOptionsPopup.show(layerOptionsToggle, posx, posy);
		}
		updateStates();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (disableListeners || e.getValueIsAdjusting())
			return;
		int row = layersTable.getSelectedRow();
		if (row < 0)
			return;
		if (layersTable.getRowSorter() != null) {
			row = layersTable.getRowSorter().convertRowIndexToModel(row);
		}
		activateLayer(layers.get(row));
	}

	protected void updateStates() {
		Runnable updateStatesRunner = new Runnable() {
			@Override
			public void run() {
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
			}
		};
		EventQueue.invokeLater(updateStatesRunner);
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
	}

	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setNoUndo(HDS hds) {
		activeLayer.setNoUndo(hds);
	}
	
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS hds) {
		HDS r = activeLayer.get(hds);
		return r;
	}

	public void set(Geometry g) {
		activeLayer.set(g);
	}

	public HalfEdgeDataStructure<?, ?, ?> get() {
		HalfEdgeDataStructure<?, ?, ?> r = activeLayer.get();
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
			return (HDS) template.getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	public void update() {
		activeLayer.update();
	}

	public void updateNoUndo() {
		activeLayer.updateNoUndo();
	}

	public void updateGeometry(Adapter<double[]> positionAdapter) {
		activeLayer.updateGeometry(positionAdapter);
	}

	public void updateGeometryNoUndo(Adapter<double[]> positionAdapter) {
		activeLayer.updateGeometryNoUndo(positionAdapter);
	}

	/**
	 * Returns the persistent adapters of the interface and of the active layer.
	 * In addition the this the adapters are returned that have been volatile
	 * during the last conversion. last conversion
	 * 
	 * @param a
	 */
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(persistentAdapters);
		result.addAll(volatileAdapters);
		result.addAll(activeLayer.getAdapters());
		return result;
	}

	/**
	 * Returns the persistent adapters of the interface and of the active layer
	 * 
	 * @return
	 */
	public AdapterSet getPersistentAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(persistentAdapters);
		result.addAll(activeLayer.getPersistentAdapters());
		return result;
	}

	/**
	 * Returns the adapters that will be volatile during the next conversion
	 * 
	 * @return
	 */
	public AdapterSet getVolatileAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(volatileAdapters);
		result.addAll(activeLayer.getVolatileAdapters());
		return result;
	}

	/**
	 * Returns the adapters that have been volatile during the last conversion
	 * 
	 * @return
	 */
	public AdapterSet getActiveVolatileAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(activeVolatileAdapters);
		result.addAll(activeLayer.getActiveVolatileAdapters());
		return result;
	}

	/**
	 * Returns the adapters of the interface and of the active layer that will
	 * be used during the next conversion
	 * 
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
		if (persistent) {
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
		boolean pa = persistentAdapters.remove(a), va = volatileAdapters
				.remove(a), la = activeLayer.removeAdapter(a);
		fireAdaptersChanged();
		return pa || va || la;
	}

	protected void clearVolatileAdapters() {
		activeVolatileAdapters.clear();
		activeVolatileAdapters.addAll(volatileAdapters);
		volatileAdapters.clear();
		fireAdaptersChanged();
	}

	public void addTemporaryGeometry(final SceneGraphComponent c) {
		getActiveLayer().addTemporaryGeometry(c);
	}

	public void removeTemporaryGeometry(final SceneGraphComponent c) {
		getActiveLayer().removeTemporaryGeometry(c);
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
		chooserDir = c.getProperty(getClass(), "importExportLocation",
				chooserDir);
		File chooserDirFile = new File(chooserDir);
		if (chooserDirFile.exists()) {
			chooser.setCurrentDirectory(chooserDirFile);
		}
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		this.controller = c;
		content = JRViewerUtility.getContentPlugin(c);
		content.addContentChangedListener(contentChangedListener);
		scene = c.getPlugin(Scene.class);
		visualizersManager = c.getPlugin(VisualizersManager.class);
		persistentAdapters.add(new SelectionAdapter(this));
		menuBar = c.getPlugin(ViewMenuBar.class);
		jobQueue = c.getPlugin(JobQueuePlugin.class);
		selectionInterface = c.getPlugin(SelectionInterface.class);

		menuBar.addMenuItem(getClass(), -101, undoAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -100, redoAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -51, exportAction, "Halfedge");
		menuBar.addMenuItem(getClass(), -50, importAction, "Halfedge");
		menuBar.addMenuSeparator(getClass(), -1, "Halfedge");

		layersTable.setModel(new LayerModel());
		layersTable.getColumnModel().getColumn(0).setMaxWidth(30);
		layersTable.getColumnModel().getColumn(2).setMaxWidth(30);
		layersTable.getColumnModel().getColumn(3).setMaxWidth(30);
		layersTable.getColumnModel().getColumn(4).setMaxWidth(30);
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
		info.name = "Halfedge Interface";
		info.vendorName = "Stefan Sechelmann";
		info.icon = ImageHook.getIcon("asterisk_orange.png");
		return info;
	}

	public void createSelectionAppearance(Appearance app, HalfedgeLayer layer, double offset) {
		EffectiveAppearance ea = getEffectiveAppearance(layer.getLayerRoot());
		if(ea == null) return;
		DefaultGeometryShader dgs1 = ShaderUtility .createDefaultGeometryShader(ea);
		DefaultPointShader dps1 = (DefaultPointShader) dgs1.getPointShader();
		DefaultLineShader dls1 = (DefaultLineShader) dgs1.getLineShader();
		app.setAttribute(POINT_SHADER + "." + RADII_WORLD_COORDINATES, dps1.getRadiiWorldCoordinates());
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, dps1.getPointRadius() * (1 + offset));
		app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls1.getTubeRadius() * (1 + offset));
		app.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls1.getRadiiWorldCoordinates());
	}

	public EffectiveAppearance getEffectiveAppearance(SceneGraphComponent sgc) {
		if (scene == null) return null;
		SceneGraphComponent sceneRoot = scene.getSceneRoot();
		List<SceneGraphPath> pathList = SceneGraphUtility.getPathsBetween(sceneRoot, sgc);
		if (pathList.isEmpty())	return null;
		return EffectiveAppearance.create(pathList.get(0));
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
		if (layers.contains(layer))
			return;
		layers.add(0, layer);
		root.addChild(layer.getLayerRoot());
		fireLayerAdded(layer);
		updateStates();
		activateLayer(layer);
	}

	public void removeLayer(HalfedgeLayer layer) {
		if (!layers.contains(layer))
			return;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void mergeLayers(HalfedgeLayer layer, HalfedgeLayer mergeLayer) {
		HalfEdgeDataStructure<?, ?, ?> hds1 = layer.get();
		HalfEdgeDataStructure<?, ?, ?> hds2 = createEmpty(hds1);
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
	
	public List<HalfedgeLayer> getAllLayers() {
		return new LinkedList<HalfedgeLayer>(layers);
	}

	public void checkContent() {
		if (scene == null) return;
		List<SceneGraphPath> paths = getPathsBetween(scene.getSceneRoot(), root);
		if (paths.isEmpty()) {
			content.setContent(root);
		}
	}

	private void normalizeContent() {
		MatrixBuilder mb = MatrixBuilder.euclidean();
		rootTransform.setMatrix(mb.getArray());
		Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(root);
		double maxExtend = bbox.getMaxExtent();
		mb.scale(10 / maxExtend);
		rootTransform.setMatrix(mb.getArray());
		root.setTransformation(rootTransform);
		for (HalfedgeLayer l : layers) {
			l.updateBoundingBox();
		}
	}
	
	public void encompassContent() {
		Runnable encompassJob = new Runnable() {
			@Override
			public void run() {
				normalizeContent();
				JRViewerUtility.encompassEuclidean(scene);
			}
		};
		EventQueue.invokeLater(encompassJob);
	}

	public class HalfedgeContentListener implements ContentChangedListener {

		@Override
		public void contentChanged(ContentChangedEvent cce) {
			if (cce.node == root || cce.node == null) return; // update boomerang
			
			final Map<HalfedgeLayer, Geometry> layersMap = new HashMap<HalfedgeLayer, Geometry>();
			cce.node.accept(new SceneGraphVisitor() {
				private SceneGraphPath path = new SceneGraphPath();

				@Override
				public void visit(SceneGraphComponent c) {
					if (!c.isVisible()) return;
					path.push(c);
					c.childrenAccept(this);
					path.pop();
				}

				@Override
				public void visit(Geometry g) {
					Transformation layerTransform = new Transformation(path.getMatrix(null));
					HalfedgeLayer layer = new HalfedgeLayer(HalfedgeInterface.this);
					layer.setName(g.getName());
					layer.setTransformation(layerTransform);
					layersMap.put(layer, g);
				}
			});
			if (layersMap.isEmpty()) return;
			
			AbstractJob creteLayersJob = new AbstractJob() {
				@Override
				public String getJobName() {
					return "Layer Creation";
				}
				@Override
				protected void executeJob() throws Exception {
					List<HalfedgeLayer> oldLayers = new LinkedList<HalfedgeLayer>(layers);
					double count = 0.0;
					for (HalfedgeLayer l : layersMap.keySet()) {
						l.set(layersMap.get(l));
						addLayer(l);
						fireJobProgress(count++ / layersMap.keySet().size());
					}
					for (HalfedgeLayer l : oldLayers) {
						removeLayer(l);
					}
					
					activateLayer(layers.get(0));
					checkContent();
					updateStates();
					encompassContent();					
				}
			};
			
			jobQueue.queueJob(creteLayersJob);
		}

	}

	public void addHalfedgeListener(HalfedgeListener l) {
		listeners.add(l);
	}

	public void removeHalfedgeListener(HalfedgeListener l) {
		listeners.remove(l);
	}

	protected void fireActiveLayerChanged(final HalfedgeLayer old, final HalfedgeLayer active) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				synchronized (listeners) {
					for (HalfedgeListener l : new LinkedList<HalfedgeListener>(
							listeners)) {
						l.activeLayerChanged(old, active);
					}
				}
			}
		};
		EventQueue.invokeLater(r);
	}

	protected void fireDataChanged() {
		synchronized (listeners) {
			for (final HalfedgeListener l : listeners) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						l.dataChanged(getActiveLayer());
					}
				};
				EventQueue.invokeLater(r);
			}
		}
	}

	protected void fireAdaptersChanged() {
		synchronized (listeners) {
			for (final HalfedgeListener l : listeners) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						l.adaptersChanged(getActiveLayer());
					}
				};
				EventQueue.invokeLater(r);
			}
		}
	}

	protected void fireLayerAdded(final HalfedgeLayer layer) {
		synchronized (listeners) {
			for (final HalfedgeListener l : listeners) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						l.layerCreated(layer);
					}
				};
				EventQueue.invokeLater(r);
			}
		}
	}

	protected void fireLayerRemoved(final HalfedgeLayer layer) {
		synchronized (listeners) {
			for (final HalfedgeListener l : listeners) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						l.layerRemoved(layer);
					}
				};
				EventQueue.invokeLater(r);
			}
		}
	}

	public Selection getSelection() {
		if (selectionInterface == null) {
			return activeLayer.getSelection();
		} else {
			return selectionInterface.getFilteredSelection(activeLayer);
		}
	}

	public <
		S extends TypedSelection<? extends Node<?,?,?>>
	> void setSelection(S s) {
		activeLayer.setSelection(s);
	}
	
	public <
		S extends TypedSelection<? extends Node<?,?,?>>
	> void addSelection(S s) {
		activeLayer.addSelection(s);
	}
	
	public void clearSelection() {
		activeLayer.setSelection(new Selection());
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

	public void setConverterHds2Ifs(ConverterHds2Ifs converterHds2Ifs) {
		this.converterHds2Ifs = converterHds2Ifs;
		activeLayer.setConverterToIFS(converterHds2Ifs);
	}

	public ConverterHds2Ifs getConverterHds2Ifs() {
		return converterHds2Ifs;
	}

	public AlgorithmPlugin getAlgorithm(String name) {
		List<AlgorithmPlugin> algos = controller
				.getPlugins(AlgorithmPlugin.class);
		for (AlgorithmPlugin a : algos) {
			if (a.getAlgorithmName().equalsIgnoreCase(name)) {
				return a;
			}
		}
		return null;
	}

	public SelectionInterface getSelectionInterface() {
		return selectionInterface;
	}
	
}
