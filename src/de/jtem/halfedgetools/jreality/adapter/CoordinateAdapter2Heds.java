package de.jtem.halfedgetools.jreality.adapter;

import de.jtem.halfedge.Node;

public interface CoordinateAdapter2Heds <T extends Node<?, ?, ?>> extends Adapter {
	
	/** Adapters are nescecary to access the Data of the H.E.D.S.
	 *  @see Adapter
	 *  @author gonska
	 */
	public void setCoordinate(T node,double[] coord); 
	
}
