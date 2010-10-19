package de.jtem.halfedgetools.plugin;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class EditorModePlugin extends Plugin {

	protected EditorManager 
		manager = null;
	
	public abstract void enter(Controller c); 
	public abstract void exit(Controller c); 
	
	public String getModeName() {
		return getPluginInfo().name;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		manager = c.getPlugin(EditorManager.class);
		manager.addMode(this);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		manager.removeMode(this);
	}
	
	@Override
	public String toString() {
		return getModeName();
	}
	
}
