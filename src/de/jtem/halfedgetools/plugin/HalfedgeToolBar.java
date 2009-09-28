package de.jtem.halfedgetools.plugin;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class HalfedgeToolBar extends ToolBarAggregator {

	public PluginInfo getPluginInfo() {
		return new PluginInfo("Halfedge Edit Toolbar");
	}

	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}
	
	public double getToolBarPriority() {
		return 100.0;
	}
	
}
