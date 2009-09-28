package de.jtem.halfedgetools.jreality.adapter;

import de.jtem.halfedge.Node;

public interface RelRadiusAdapter2Heds <T extends Node<?, ?, ?>> extends Adapter {
	
	/** Adapters are necessary to access the Data of the H.E.D.S.
	 *  @see Adapter
	 *  @author gonska
	 */
	public void setRelRadius(Node<?, ?, ?> node, double relRadius);
	
}
