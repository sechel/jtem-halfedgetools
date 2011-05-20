package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class NodeColorVisualizer extends DataVisualizerPlugin {

	private class NodeColorVisualization extends AbstractDataVisualization {
		
		public NodeColorVisualization(
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
		
	}
	
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(Number.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("color_swatch.png");
		return info;
	}
	
	@Override
	public String getName() {
		return "Node Colors";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		return new NodeColorVisualization(layer, source, this, type);
	}
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
	}

}
