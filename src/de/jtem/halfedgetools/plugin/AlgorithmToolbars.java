package de.jtem.halfedgetools.plugin;

import de.jreality.plugin.basic.View;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;

public final class AlgorithmToolbars {

	
	public static class FileCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}

	}
	
	public static class SelectionCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
	
	public static class SubdivisionCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}

	
	public static class TextureRemeshingCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
	public static class SimplificationCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
	public static class EditingCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
	public static class TopologyCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
	public static class GeometryCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
	public static class GeneratorCategoryToolbar extends ToolBarAggregator {

		@Override
		public Class<? extends PerspectiveFlavor> getPerspective() {
			return View.class;
		}
		
	}
	
}
