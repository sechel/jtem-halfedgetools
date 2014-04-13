package de.jtem.halfedgetools.selection;

import de.jtem.halfedgetools.plugin.HalfedgeInterface;

public interface SelectionListener {

	public void selectionChanged(Selection s, HalfedgeInterface sif);
	
}
