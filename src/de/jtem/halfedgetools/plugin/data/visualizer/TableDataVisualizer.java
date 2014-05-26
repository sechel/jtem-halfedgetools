package de.jtem.halfedgetools.plugin.data.visualizer;

import static java.lang.Double.parseDouble;

import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class TableDataVisualizer extends DataVisualizerPlugin {

	private JPanel
		tablePanel = new JPanel();
	private JTabbedPane
		layerTabPanel = new JTabbedPane();
	
	public TableDataVisualizer() {
		tablePanel.setLayout(new GridLayout());
		tablePanel.add(layerTabPanel);
	}
	
	
	private class TableVisualization extends AbstractDataVisualization {
		
		private JTable
			table = new JTable();
		private JScrollPane
			tableScroller = new JScrollPane(table);
		
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
		
		public JComponent getVisualizationComponent() {
			return tableScroller;
		}
		
		private double[] getData(int index) {
			HalfedgeLayer l = getLayer();
			AdapterSet a = l.getEffectiveAdapters();
			HalfEdgeDataStructure<?, ?, ?> hds = l.get();
			Node<?,?,?> node = null;
			switch (getType()) {
			case Vertex:
				node = hds.getVertex(index); 
				break;
			case Edge:
				node = hds.getEdge(index);
				break;
			case Face:
				node = hds.getFace(index);
				break;
			}
			return (double[])getSource().get(node, a);
		}
		
		@SuppressWarnings("unchecked")
		private <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			N extends Node<V, E, F>,
			HDS extends HalfEdgeDataStructure<V, E, F>
		> void setData(int index, double[] data) {
			HalfedgeLayer l = getLayer();
			AdapterSet a = l.getEffectiveAdapters();
			HDS hds = (HDS)l.get();
			N node = null;
			switch (getType()) {
			case Vertex:
				node = (N)hds.getVertex(index);
				break;
			case Edge:
				node = (N)hds.getEdge(index);
				break;
			case Face:
				node = (N)hds.getFace(index);
				break;
			}
			((Adapter<double[]>)getSource()).set(node, data, a);
			l.update();
		}
		
		private int getDataSize() {
			switch (getType()) {
			case Vertex:
				return getLayer().get().numVertices();
			case Edge:
				return getLayer().get().numEdges();
			case Face:
			default:
				return getLayer().get().numFaces();
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
					return row;
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
		layerTabPanel.addTab(source.toString(), v.getVisualizationComponent());
		return v;
	}
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
		layerTabPanel.remove(((TableVisualization)vis).getVisualizationComponent());
	}
	
}
