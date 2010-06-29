package de.jtem.halfedgetools.plugin;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public class GeneratorsToolBar extends ToolBarAggregator {

	@Override
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

}
