package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class ColoredBeadsVisualizer extends DataVisualizerPlugin {

	private class ColoredBeadsVisualization extends AbstractDataVisualization {
		
		public ColoredBeadsVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source, 
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
		}

		@Override
		public void update() {
			System.out
					.println("ColoredBeadsVisualizer.ColoredBeadsVisualization.update()");
		}
		
		@Override
		public void remove() {
			System.out
					.println("FaceColorVisualizer.FaceColorVisualization.remove()");
		}
		
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(Number.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("bullets.png");
		return info;
	}
	
	@Override
	public String getName() {
		return "Colored Beads";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		return new ColoredBeadsVisualization(layer, source, this, type);
	}

}
