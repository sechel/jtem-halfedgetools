package de.jtem.halfedgetools.tutorial;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class VHDS extends HalfEdgeDataStructure<VV, VE, VF> {

	public VHDS() {
		super(VV.class, VE.class, VF.class);
	}
	
}
