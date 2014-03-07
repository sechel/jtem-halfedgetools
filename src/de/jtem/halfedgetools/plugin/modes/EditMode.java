package de.jtem.halfedgetools.plugin.modes;

import de.jtem.halfedgetools.plugin.EditorModePlugin;
import de.jtem.halfedgetools.plugin.MarqueeSelectionPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class EditMode extends EditorModePlugin {

	private MarqueeSelectionPlugin
		marquee = null;
	private boolean
		marqueeWasActive = false;
		
	@Override
	public void enter(Controller c) {
		marqueeWasActive = marquee.isActivated();
		marquee.setActivated(false);
	}

	@Override
	public void exit(Controller c) {
		marquee.setActivated(marqueeWasActive);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		marquee = c.getPlugin(MarqueeSelectionPlugin.class);
	}
	
	
	@Override
	public String getModeName() {
		return "Edit";
	}
	
}
