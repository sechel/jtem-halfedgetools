package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;

public class TableDataVisualizer extends DataVisualizerPlugin {

	private class TableVisualization extends AbstractDataVisualization {
		
		public TableVisualization(Adapter<?> source, DataVisualizer visualizer, NodeType type) {
			super(source, visualizer, type);
		}

		@Override
		public void update() {
		}
		
		@Override
		public void remove() {
		}
		
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return true;
	}
	
	@Override
	public String getName() {
		return "Table";
	}

	@Override
	public DataVisualization createVisualization(NodeType type, Adapter<?> source) {
		return new TableVisualization(source, this, type);
	}
	
}
