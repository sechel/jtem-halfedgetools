package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;

public class ColoredBeadsVisualizer extends DataVisualizerPlugin {

	private class ColoredBeadsVisualization extends AbstractDataVisualization {
		
		public ColoredBeadsVisualization(Adapter<?> source, DataVisualizer visualizer, NodeType type) {
			super(source, visualizer, type);
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
	public String getName() {
		return "Colored Beads";
	}

	@Override
	public DataVisualization createVisualization(NodeType type, Adapter<?> source) {
		return new ColoredBeadsVisualization(source, this, type);
	}

}
