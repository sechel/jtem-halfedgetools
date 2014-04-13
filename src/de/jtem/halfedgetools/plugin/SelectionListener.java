package de.jtem.halfedgetools.plugin;

import de.jtem.halfedgetools.selection.Selection;

public interface SelectionListener {

	public void selectionChanged(Selection s, HalfedgeInterface sif);
	
}
