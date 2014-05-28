package de.jtem.halfedgetools.plugin.data.visualizer;

import static java.lang.Double.parseDouble;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.SelectionListener;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class TableDataVisualizer extends DataVisualizerPlugin implements ActionListener, SelectionListener {

	private JPanel
		tablePanel = new JPanel();
	private JTabbedPane
		layerTabPanel = new JTabbedPane();
	private JPanel
		optionPanel = new JPanel();
	private JCheckBox
		selectionOnlyChecker = new JCheckBox("Selection");
	private List<TableVisualization>
		visualizationList = new ArrayList<TableVisualization>();
	private TableVisualization	
		activeVisualization = null;
	private boolean
		listenersDisabled = false;
	
	public TableDataVisualizer() {
		tablePanel.setLayout(new GridLayout());
		tablePanel.add(layerTabPanel);
		optionPanel.add(selectionOnlyChecker);
		selectionOnlyChecker.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (activeVisualization == null || listenersDisabled) return;
		activeVisualization.setSelectionOnly(selectionOnlyChecker.isSelected());
		activeVisualization.update();
	}
	
	@Override
	public void selectionChanged(Selection s, HalfedgeInterface sif) {
		if (listenersDisabled) return;
		for (TableVisualization v : visualizationList) {
			v.update();
		}
	}
	
	private class TableVisualization extends AbstractDataVisualization implements SelectionListener {
		
		private JTable
			table = new JTable();
		private JScrollPane
			tableScroller = new JScrollPane(table);
		private boolean
			selectionOnly = false;
		
		public TableVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source,
			DataVisualizer visualizer,
			NodeType type
		) {
			super(layer, source, visualizer, type);
		}

		@Override
		public void update() {
			table.setModel(new DataTableModel());
			table.getColumnModel().getColumn(0).setMaxWidth(100);
		}
		
		@Override
		public void selectionChanged(Selection s, HalfedgeInterface sif) {
			if (selectionOnly) {
				update();
			}
		}
		
		public JComponent getVisualizationComponent() {
			return tableScroller;
		}
		
		private List<Node<?,?,?>> getNodes() {
			HalfedgeLayer l = getLayer();
			List<Node<?,?,?>> result = new ArrayList<Node<?,?,?>>();
			switch (getType()) {
			case Vertex:
				if (selectionOnly) {
					result.addAll(l.getSelection().getVertices());
				} else {
					result.addAll(l.get().getVertices());
				}
				break;
			case Edge:
				if (selectionOnly) {
					result.addAll(l.getSelection().getEdges());
				} else {
					result.addAll(l.get().getEdges());
				}
				break;
			case Face:
				if (selectionOnly) {
					result.addAll(l.getSelection().getFaces());
				} else {
					result.addAll(l.get().getFaces());
				}
				break;
			}
			return result;
		}
		
		private double[] getData(int index) {
			HalfedgeLayer l = getLayer();
			AdapterSet a = l.getEffectiveAdapters();
			List<Node<?,?,?>> nodes = getNodes();
			Node<?,?,?> node = nodes.get(index);
			return (double[])getSource().get(node, a);
		}
		
		@SuppressWarnings("unchecked")
		private void setData(int index, double[] data) {
			HalfedgeLayer l = getLayer();
			AdapterSet a = l.getEffectiveAdapters();
			List<Node<?,?,?>> nodes = getNodes();
			Node<?,?,?> node = nodes.get(index);
			((Adapter<double[]>)getSource()).set(node, data, a);
			l.update();
		}
		
		private int getDataSize() {
			switch (getType()) {
			case Vertex:
				if (selectionOnly) {
					return getLayer().getSelection().getVertices().size();
				} else {
					return getLayer().get().numVertices();
				}
			case Edge:
				if (selectionOnly) {
					return getLayer().getSelection().getEdges().size();
				} else {
					return getLayer().get().numEdges();
				}
			case Face:
			default:
				if (selectionOnly) {
					return getLayer().getSelection().getFaces().size();
				} else {
					return getLayer().get().numFaces();
				}
			}
		}
		
		public class DataTableModel extends AbstractTableModel {

			private static final long 
				serialVersionUID = 1L;

			@Override
			public int getColumnCount() {
				if (getRowCount() <= 0) return 1;
				return getData(0).length + 1;
			}

			@Override
			public int getRowCount() {
				return getDataSize();
			}
			
			@Override
			public String getColumnName(int col) {
				switch (col) {
				case 0:
					return "Index";
				default:
					return "Value " + (col - 1);
				}
			}

			@Override
			public Object getValueAt(int row, int col) {
				switch (col) {
				case 0:
					Node<?,?,?> n = getNodes().get(row);
					return n.getIndex();
				default:
					return getData(row)[col - 1];
				}
			}
			
			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return getSource().isSetter() && columnIndex > 0;
			}
			
			@Override
			public void setValueAt(Object aValue, int row, int col) {
				double[] data = getData(row);
				data[col - 1] = parseDouble((String)aValue);
				setData(row, data);
			}
			
		}

		public boolean isSelectionOnly() {
			return selectionOnly;
		}

		public void setSelectionOnly(boolean selectionOnly) {
			this.selectionOnly = selectionOnly;
		}
		
	}
	
	@Override
	public JPanel getDataDisplay() {
		return tablePanel;
	}
	
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(double[].class);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		c.getPlugin(HalfedgeInterface.class).addSelectionListener(this);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("table.png");
		return info;
	}
	
	@Override
	public String getName() {
		return "Table";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		TableVisualization v = new TableVisualization(layer, source, this, type);
		visualizationList.add(v);
		v.setSelectionOnly(selectionOnlyChecker.isSelected());
		layerTabPanel.addTab(source.toString().replace("Adapter", ""), v.getVisualizationComponent());
		return v;
	}
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
		layerTabPanel.remove(((TableVisualization)vis).getVisualizationComponent());
		visualizationList.remove(vis);
	}
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		activeVisualization = (TableVisualization)visualization;
		listenersDisabled = true;
		selectionOnlyChecker.setSelected(activeVisualization.isSelectionOnly());
		listenersDisabled = false;
		return optionPanel;
	}
	
}
