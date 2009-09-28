package de.jtem.halfedgetools.jreality.adapter;

import de.jtem.halfedge.Node;


public interface CoordinateAdapter2Ifs <T extends Node<?, ?, ?>> extends Adapter {
	
	/** Adapters are necessary to access the Data of the H.E.D.S.
	 *  @see Adapter
	 *  @author gonska
	 */
	public double[] getCoordinate(T node); 
	
}
