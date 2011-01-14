package de.jtem.halfedgetools.tutorial;


import java.util.Random;

import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class TestVisualizer extends VisualizerPlugin {

	@Color
	private class RandomColorAdapter extends AbstractAdapter<double[]> {
	
		public RandomColorAdapter() {
			super(double[].class, true, false);
		}

		private Random rnd = new Random();
		@Override
		public <
			V extends de.jtem.halfedge.Vertex<V,E,F>,
			E extends de.jtem.halfedge.Edge<V,E,F>, 
			F extends de.jtem.halfedge.Face<V,E,F>
		> double[] getF(F v, AdapterSet a) {
			return new double[]{rnd.nextDouble(), rnd.nextDouble(), rnd.nextDouble()};
		}
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		};
	}
	
	@Override
	public AdapterSet getAdapters() {
		return new AdapterSet(new RandomColorAdapter());
	}
	
	@Override
	public String getName() {
		return "Random Color Visualizer";
	}

}
