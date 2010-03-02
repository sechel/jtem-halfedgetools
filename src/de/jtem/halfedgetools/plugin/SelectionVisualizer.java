package de.jtem.halfedgetools.plugin;

import de.jreality.scene.SceneGraphComponent;

public class SelectionVisualizer extends VisualizerPlugin {

	private SceneGraphComponent 
		root = new SceneGraphComponent("Selection");
	
	
	
	@Override
	public SceneGraphComponent getComponent() {
		return root;
	}
	
	
	@Override
	public String getName() {
		return "Halfedge Selection";
	}

}
