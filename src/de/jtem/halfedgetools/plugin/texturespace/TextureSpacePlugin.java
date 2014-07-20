package de.jtem.halfedgetools.plugin.texturespace;

import de.jtem.java2d.SceneComponent;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;

public interface TextureSpacePlugin {

	public SceneComponent getSceneComponent();
	public ShrinkPanel getOptionPanel();
	public boolean getRenderOnTop();
	
}
