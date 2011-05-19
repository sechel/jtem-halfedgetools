package de.jtem.halfedgetools.plugin.data.visualizer;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.jtem.halfedgetools.adapter.Adapter;
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
	private JTable
		table = new JTable();
	private JScrollPane
		tableScroller = new JScrollPane(table);
	
	public TableDataVisualizer() {
		tablePanel.setLayout(new GridLayout());
		tablePanel.add(tableScroller);
	}
	
	
	private class TableVisualization extends AbstractDataVisualization {
		
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
		}
		
		@Override
		public void remove() {
		}
		
	}
	
	@Override
	public JPanel getDataDisplay() {
		return tablePanel;
	}
	
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return true;
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
		return new TableVisualization(layer, source, this, type);
	}
	
}
