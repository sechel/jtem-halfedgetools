package de.jtem.halfedgetools.plugin.data;

import static javax.swing.JTabbedPane.WRAP_TAB_LAYOUT;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.data.DataVisualizer.NodeType;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.plugin.swing.IconCellRenderer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class VisualizationInterface extends ShrinkPanelPlugin implements HalfedgeListener, ListSelectionListener {
	
	private HalfedgeInterface
		hif = null;
	private Controller
		controller = null;
	private JTabbedPane
		tabbedPane = new JTabbedPane(JTabbedPane.TOP, WRAP_TAB_LAYOUT);
	private JTable
		activeTable = new JTable(),
		visualizerTable = new JTable(),
		sourceTable = new JTable();
	private JScrollPane
		activeScroller = new JScrollPane(activeTable),
		visualizerScroller = new JScrollPane(visualizerTable),
		sourceScroller = new JScrollPane(sourceTable);
	private JPanel
		activePanel = new JPanel(), 
		creationPanel = new JPanel(),
		optionsPanel = new JPanel();
	private Icon
		controlIcon = ImageHook.getIcon("control_equalizer_blue.png"),
		removeIcon = ImageHook.getIcon("remove.png"),
		vertexIcon = ImageHook.getIcon("shape_handles.png"),
		edgeIcon = ImageHook.getIcon("shape_edges.png"),
		faceIcon = ImageHook.getIcon("shape_square.png");
	private IconCellRenderer
		iconCellRenderer = new IconCellRenderer();
	
	private Set<Adapter<?>>
		sourceSet = new TreeSet<Adapter<?>>(new AdapterNameComparator());
	private Set<DataVisualizer>
		visualizerSet = new TreeSet<DataVisualizer>(new VisualizerNameComparator());
	private Map<HalfedgeLayer, Set<DataVisualization>>
		activeMap = new HashMap<HalfedgeLayer, Set<DataVisualization>>();
	
	private ButtonCellRenderer
		createCellRenderer = new ButtonCellRenderer(),
		removeCellRenderer = new ButtonCellRenderer();
	private ButtonCellEditor
		createCellEditor = new ButtonCellEditor(),
		removeCellEditor = new ButtonCellEditor();
	private JButton
		noActionButton = new JButton();
	
	public VisualizationInterface() {
		shrinkPanel.setTitle("Halfedge Data Visualitazion");
		setInitialPosition(SHRINKER_TOP);
		
		shrinkPanel.setLayout(new GridBagLayout());
		shrinkPanel.setPreferredSize(new Dimension(600, 300));
		shrinkPanel.setMinimumSize(new Dimension(600, 300));
		GridBagConstraints c1 = new GridBagConstraints();
		c1.fill = GridBagConstraints.BOTH;
		c1.insets = new Insets(2, 2, 2, 2);
		
		activePanel.setLayout(new GridBagLayout());
		activePanel.setPreferredSize(new Dimension(230, 300));
		activeScroller.setPreferredSize(new Dimension(230, 130));
		activeScroller.setMinimumSize(new Dimension(230, 130));
		activeScroller.setBorder(BorderFactory.createEtchedBorder());
		activeTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		activeTable.setRowHeight(22);
		activeTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		activeTable.getSelectionModel().addListSelectionListener(this);
		activeTable.setDefaultRenderer(Icon.class, iconCellRenderer);
		activeTable.setDefaultRenderer(JButton.class, removeCellRenderer);
		activeTable.setDefaultEditor(JButton.class, removeCellEditor);
		TableCellEditor boolEditor = activeTable.getDefaultEditor(Boolean.class);
		boolEditor.addCellEditorListener(new ActivationListener());	
		c1.weightx = 1.0;
		c1.weighty = 1.0;
		c1.gridwidth = GridBagConstraints.REMAINDER;
		activePanel.add(activeScroller, c1);
		
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		optionsPanel.setPreferredSize(new Dimension(230, 150));
		optionsPanel.setMinimumSize(new Dimension(230, 150));
		c1.weighty = 0.0;
		activePanel.add(optionsPanel, c1);
		c1.gridwidth = 1;
		c1.weightx = 0.0;
		shrinkPanel.add(activePanel, c1);
		
		// creation panel
		creationPanel.setLayout(new GridBagLayout());
		sourceScroller.setPreferredSize(new Dimension(200, 150));
		sourceScroller.setMinimumSize(new Dimension(200, 150));
		sourceScroller.setBorder(BorderFactory.createEtchedBorder());
		sourceTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		sourceTable.setRowHeight(22);
		sourceTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		sourceTable.getSelectionModel().addListSelectionListener(this);
		c1.gridwidth = 1;
		c1.weighty = 1.0;
		creationPanel.add(sourceScroller, c1);
		visualizerScroller.setPreferredSize(new Dimension(250, 150));
		visualizerScroller.setMinimumSize(new Dimension(250, 150));
		visualizerScroller.setBorder(BorderFactory.createEtchedBorder());
		visualizerTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		visualizerTable.setRowHeight(22);
		visualizerTable.setDefaultRenderer(Icon.class, iconCellRenderer);
		visualizerTable.setDefaultRenderer(JButton.class, createCellRenderer);
		visualizerTable.setDefaultEditor(JButton.class, createCellEditor);
		visualizerTable.setCellSelectionEnabled(false);
		c1.gridwidth = GridBagConstraints.RELATIVE;
		c1.weightx = 1.0;
		creationPanel.add(visualizerScroller, c1);
		c1.gridwidth = GridBagConstraints.REMAINDER;
		tabbedPane.addTab("Configuration", controlIcon, creationPanel);
		tabbedPane.setPreferredSize(new Dimension(200, 300));
		tabbedPane.setMinimumSize(new Dimension(400, 150));
		shrinkPanel.add(tabbedPane, c1);
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) return;
		if (e.getSource() == sourceTable.getSelectionModel()) {
			updateCompatible();
		}
		if (e.getSource() == activeTable.getSelectionModel()) {
			updateVisualizationOptions();
		}
	}
	
	
	private void updateVisualizerTable() {
		visualizerTable.setModel(new VisualizerModel());
		visualizerTable.getColumnModel().getColumn(0).setMaxWidth(25);
		visualizerTable.getColumnModel().getColumn(0).setMinWidth(25);
		visualizerTable.getColumnModel().getColumn(1).setMaxWidth(150);
		visualizerTable.getColumnModel().getColumn(1).setMinWidth(150);
		visualizerTable.getColumnModel().getColumn(2).setMaxWidth(25);
		visualizerTable.getColumnModel().getColumn(2).setMinWidth(25);
		visualizerTable.getColumnModel().getColumn(3).setMaxWidth(25);
		visualizerTable.getColumnModel().getColumn(3).setMinWidth(25);
		visualizerTable.getColumnModel().getColumn(4).setMaxWidth(25);
		visualizerTable.getColumnModel().getColumn(4).setMinWidth(25);
		visualizerTable.revalidate();
	}
	
	private void updateActiveTable() {
		activeTable.setModel(new ActiveModel());
		activeTable.getColumnModel().getColumn(0).setMaxWidth(25);
		activeTable.getColumnModel().getColumn(1).setMaxWidth(25);
		activeTable.getColumnModel().getColumn(2).setMaxWidth(25);
		activeTable.getColumnModel().getColumn(4).setMaxWidth(25);
		activeTable.revalidate();
		updateVisualizationOptions();
	}
	
	private Set<DataVisualization> getActiveVisualizations() {
		HalfedgeLayer l = hif.getActiveLayer();
		if (!activeMap.containsKey(l)) {
			activeMap.put(l, new TreeSet<DataVisualization>());
		}
		return activeMap.get(l);
	}
	
	public DataVisualization getSelectedVisualization() {
		int row = activeTable.getSelectedRow();
		if (row == -1) return null;
		Object[] objects = getActiveVisualizations().toArray();
		return (DataVisualization)objects[row];
	}
	
	
	private void activateVisualization(DataVisualization v) {
		Set<DataVisualization> vSet = getActiveVisualizations();
		vSet.add(v);
		updateActiveTable();
		updateVisualizerTable();
		updateVisualization(v);
	}
	
	private void removeVisualization(DataVisualization v) {
		DataVisualizer vis = v.getVisualizer();
		Set<DataVisualization> vSet = getActiveVisualizations();
		vSet.remove(v);
		vis.disposeVisualization(v);
		updateActiveTable();
		updateVisualizationOptions();
	}
	
	private void updateVisualizationOptions() {
		optionsPanel.removeAll();
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 0.0;
		DataVisualization vis = getSelectedVisualization();
		if (vis == null) {
			optionsPanel.updateUI();
			return;
		}
		DataVisualizer visualizer = vis.getVisualizer();
		JPanel ui = visualizer.connectUserInterfaceFor(vis);
		if (ui != null) {
			optionsPanel.add(ui, c);
		}
		c.weighty = 1.0;
		optionsPanel.add(new JPanel(), c);
		SwingUtilities.updateComponentTreeUI(optionsPanel);
	}
	
	private void updateActiveVisualizations() {
		for (DataVisualization vis : getActiveVisualizations()) {
			updateVisualization(vis);
		}
	}
	
	private void updateVisualization(DataVisualization vis) {
		vis.update();
	}
	
	
	private void updateCompatible() {
		visualizerSet.clear();
		Adapter<?> source = getSelectedSource();
		if (source == null) {
			visualizerTable.setModel(new VisualizerModel());
			return;
		}
		List<DataVisualizer> dvList = controller.getPlugins(DataVisualizer.class);
		for (DataVisualizer dv : dvList) {
			if (dv.canRead(source, NodeType.Vertex) || 
				dv.canRead(source, NodeType.Edge) || 
				dv.canRead(source, NodeType.Face)
			) {
				visualizerSet.add(dv);
			}
		}
		updateVisualizerTable();
	}
	
	
	@SuppressWarnings("unchecked")
	private void updateAvailable(HalfedgeLayer l) {
		sourceSet.clear();
		HalfEdgeDataStructure<?, ?, ?> hds = l.get();
		AdapterSet aSet = l.getEffectiveAdapters();
		aSet.addAll(hif.getActiveVolatileAdapters());
		for (DataSourceProvider dsp : controller.getPlugins(DataSourceProvider.class)) {
			aSet.addAll(dsp.getDataSources());
		}
		for (Adapter<?> a : aSet) {
			if (a.canAccept(hds.getVertexClass())) {
				sourceSet.add((Adapter<Number>)a);
			}
			if (a.canAccept(hds.getEdgeClass())) {
				sourceSet.add((Adapter<Number>)a);
			}
			if (a.canAccept(hds.getFaceClass())) {
				sourceSet.add((Adapter<Number>)a);
			}
		}
		sourceTable.setModel(new SourceModel());
		updateCompatible();
	}
	
	private void validateActiveVisualizations() {
		HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
		List<DataVisualization> vList = new LinkedList<DataVisualization>();
		vList.addAll(getActiveVisualizations());
		for (DataVisualization vis : vList) {
			Adapter<?> source = vis.getSource();
			DataVisualizer vi = vis.getVisualizer();
			switch (vis.getType()) {
				case Vertex: if (!source.canAccept(hds.getVertexClass())) {
					removeVisualization(vis);
				}
				break;
				case Edge: if (!source.canAccept(hds.getEdgeClass())) {
					removeVisualization(vis);
				}
				break;
				case Face: if (!source.canAccept(hds.getFaceClass())) {
					removeVisualization(vis);
				}
				break;
			}
			if (!vi.canRead(source, vis.getType())) {
				removeVisualization(vis);
			}
		}
	}
	
	
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		updateAvailable(active);
		updateActiveTable();
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		activeLayerChanged(layer, layer);
		validateActiveVisualizations();
		updateActiveTable();
	}
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateAvailable(layer);
		updateActiveTable();
		validateActiveVisualizations();
		updateActiveVisualizations();
	}
	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}
	@Override
	public void layerRemoved(HalfedgeLayer layer) {
		activeMap.remove(layer);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		controller = c;
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	private Adapter<?> getSelectedSource() {
		int row = sourceTable.getSelectedRow();
		if (row == -1) return null;
		Object[] objects = sourceSet.toArray();
		return (Adapter<?>)objects[row];
	}
	
	
	private class SourceModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return sourceSet.size();
		}
		
		@Override
		public int getColumnCount() {
			return 1;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= sourceSet.size()) {
				return "-";
			}
			Object[] objects = sourceSet.toArray();
			Object op = objects[row];
			Object value = null;
			switch (column) {
				case 0:
					return op.toString().replace("Adapter", "");
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
		
	}
	
	
	private class ActiveModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return getActiveVisualizations().size();
		}
		
		@Override
		public int getColumnCount() {
			return 5;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 0: return Icon.class;
			case 1: return Icon.class;
			case 2: return Boolean.class;
			default: return String.class;
			case 4: return JButton.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= sourceSet.size()) {
				return "-";
			}
			Set<DataVisualization> aSet = getActiveVisualizations();
			Object[] objects = aSet.toArray();
			DataVisualization op = (DataVisualization)objects[row];
			switch (column) {
				case 0: return op.getVisualizer().getIcon();
				case 1:
					switch (op.getType()) {
						case Vertex: return vertexIcon;
						case Edge: return edgeIcon;
						case Face: return faceIcon;
					}
				case 2: return op.isActive();
				case 3: return op.getSource().toString().replace("Adapter", "") + " " + op.getVisualizer().getName();
				case 4: return new RemoveVisualizationButton(op);
				default: 
					return "-";
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 2 || column == 4;
		}
		
	}
	
	
	private class CreateVisualizerButton extends JButton implements ActionListener {
		
		private static final long serialVersionUID = 1L;
		private HalfedgeLayer layer = null;
		private Adapter<?> source = null;
		private DataVisualizer visualizer = null;
		private NodeType type = NodeType.Vertex;
		
		public CreateVisualizerButton(
			HalfedgeLayer layer,
			Adapter<?> adapter,
			DataVisualizer visualizer, 
			NodeType type
		) {
			super();
			this.layer = layer;
			this.source = adapter;
			this.visualizer = visualizer;
			this.type = type;
			switch (type) {
				case Vertex: setIcon(vertexIcon);
					break;
				case Edge: setIcon(edgeIcon);
					break;
				case Face: setIcon(faceIcon);
					break;
			}
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (visualizerTable.getCellEditor() != null) {
				visualizerTable.getCellEditor().stopCellEditing();
			}
			DataVisualization dv = visualizer.createVisualization(layer, type, source);
			activateVisualization(dv);
		}
		
	}
	
	
	private class RemoveVisualizationButton extends JButton implements ActionListener {
		
		private static final long serialVersionUID = 1L;
		private DataVisualization visualization = null;

		public RemoveVisualizationButton(DataVisualization visualization) {
			super(removeIcon);
			this.visualization = visualization;
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			removeVisualization(visualization);
		}
		
	}
	
	
	private class ButtonCellRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = 1L;
		private JButton renderButton = new JButton();
		
		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (value instanceof JButton && value != noActionButton) {
				JButton buttonValue = (JButton)value;
				renderButton.setIcon(buttonValue.getIcon());
				renderButton.setText(buttonValue.getText());
				return renderButton;
			} else {
				return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
			}
		}
		
		@Override
		public void updateUI() {
			super.updateUI();
			if (renderButton != null) {
				renderButton.updateUI();
			}
		}
		
	}
	
	private class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {

		private static final long 	
			serialVersionUID = 1L;
		private JLabel
			defaultEditor = new JLabel("-");
		private Object 
			activeValue = null;
		
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			this.activeValue = value;
			if (value instanceof Component) {
				return (Component)value;
			}
			return defaultEditor;
		}
		@Override
		public Object getCellEditorValue() {
			return activeValue;
		}
		
	}
	
	
	
	private class VisualizerModel extends DefaultTableModel {
		
		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return visualizerSet.size();
		}
		
		@Override
		public int getColumnCount() {
			return 6;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Icon.class;
				case 1: return String.class;
				case 2: 
				case 3: 
				case 4: return JButton.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= visualizerSet.size()) {
				return "-";
			}
			HalfedgeLayer layer = hif.getActiveLayer();
			HalfEdgeDataStructure<?, ?, ?> hds = layer.get();
			Adapter<?> source = getSelectedSource();
			assert source != null;
			Object[] objects = visualizerSet.toArray();
			DataVisualizer v = (DataVisualizer)objects[row];
			switch (column) {
				case 0: return v.getIcon();
				case 1: return v.getName();
				case 2: 
					if (v.canRead(source, NodeType.Vertex) && source.canAccept(hds.getVertexClass())) {
						return new CreateVisualizerButton(layer, source, v, NodeType.Vertex);
					} else {
						return noActionButton;
					}
				case 3: 					
					if (v.canRead(source, NodeType.Edge) && source.canAccept(hds.getEdgeClass())) {
						return new CreateVisualizerButton(layer, source, v, NodeType.Edge);
					} else {
						return noActionButton;
					}
				case 4: 
					if (v.canRead(source, NodeType.Face) && source.canAccept(hds.getFaceClass())) {
						return new CreateVisualizerButton(layer, source, v, NodeType.Face);
					} else {
						return noActionButton;
					}
				default: return "";
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 2:
				case 3:
				case 4: return true;
				default: return false;
			}
		}
		
	}
	
	
	private class ActivationListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			DataVisualization vis = getSelectedVisualization();
			if (vis == null) return; 
			vis.setActive(!vis.isActive());
			updateActiveVisualizations();
		}
		
	}
	
	
	
	public void addDataDisplayFor(DataVisualizer dv) {
		JPanel display = dv.getDataDisplay();
		if (display == null) return;
		tabbedPane.addTab(dv.getName(), dv.getIcon(), display);
	}
	
	public void removeDataDisplayFor(DataVisualizer dv) {
		JPanel display = dv.getDataDisplay();
		if (display == null) return;
		int index = -1;
		for (int i = 0; i < tabbedPane.getTabCount(); i++) {
			if (tabbedPane.getTabComponentAt(i) == display) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			tabbedPane.removeTabAt(index);
		}
	}
	
}
