package de.jtem.halfedgetools.jreality.adapter;

import de.jtem.halfedge.Node;

public interface ColorAdapter2Heds <T extends Node<?, ?, ?>> extends Adapter {
	
	/** Adapters are necessary to access the Data of the HDS
	 *  @see Adapter
	 *  @author gonska
	 */
	public void setColor(T node, double[] color);
	
}
