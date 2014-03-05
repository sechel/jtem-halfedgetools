package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphNode;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SceneGraphNodeVisualizer extends DataVisualizerPlugin {

	private HalfedgeInterface
		hif = null;
	
	private class SceneGraphComponentVisualization extends AbstractDataVisualization {

		private SceneGraphComponent
			root = new SceneGraphComponent();
		
		public SceneGraphComponentVisualization(HalfedgeLayer layer, Adapter<?> source, DataVisualizer visualizer, NodeType type) {
			super(layer, source, visualizer, type);
			root.setName(source.toString());
		}

		@Override
		public void update() {
			dispose();
			AdapterSet a = hif.getAdapters();
			root.removeAllChildren();
			switch (getType()) {
			case Vertex:
				for (Vertex<?, ?, ?> v : getLayer().get().getVertices()) {
					SceneGraphNode node = (SceneGraphNode)getSource().get(v, a);
					if (node != null) {
						SceneGraphUtility.addChildNode(root, node);
					}
				}
				break;
			case Edge:
				for (Edge<?, ?, ?> e : getLayer().get().getEdges()) {
					SceneGraphNode node = (SceneGraphNode)getSource().get(e, a);
					if (node != null) {
						SceneGraphUtility.addChildNode(root, node);
					}
				}
				break;
			case Face:
				for (Face<?, ?, ?> f : getLayer().get().getFaces()) {
					SceneGraphNode node = (SceneGraphNode)getSource().get(f, a);
					if (node != null) {
						SceneGraphUtility.addChildNode(root, node);
					}
				}
				break;
			}
			getLayer().addTemporaryGeometry(root);
		}
		
		@Override
		public void dispose() {
			getLayer().removeTemporaryGeometry(root);
		}
		
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(SceneGraphNode.class);
	}
	
	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		return new SceneGraphComponentVisualization(layer, source, this, type);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("cog_go.png");
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}

}
