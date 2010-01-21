package de.jtem.halfedgetools.plugin;

import java.util.Set;

import javax.swing.JPanel;

import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class VisualizerPlugin extends Plugin {

	public abstract Set<? extends Adapter> getAdapters();
	
	protected VisualizersManager
		manager = null;
	
	public JPanel getOptionPanel() {
		return null;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		manager = c.getPlugin(VisualizersManager.class);
		manager.addVisualizerPlugin(this);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		manager.removeVisualizerPlugin(this);
	}
	
	public abstract String getName();
	
	
	public void updateContent() {
		manager.updateContent();
	}
	
	
	@Override
	public String toString() {
		return getName();
	}
	
}
