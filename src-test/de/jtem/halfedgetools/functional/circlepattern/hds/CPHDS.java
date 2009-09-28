package de.jtem.halfedgetools.functional.circlepattern.hds;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class CPHDS extends HalfEdgeDataStructure<CPVertex, CPEdge, CPFace> {

	public CPHDS() {
		super(CPVertex.class, CPEdge.class, CPFace.class);
	}
	
}
