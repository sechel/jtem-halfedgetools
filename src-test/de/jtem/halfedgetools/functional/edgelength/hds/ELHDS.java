package de.jtem.halfedgetools.functional.edgelength.hds;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class ELHDS extends HalfEdgeDataStructure<ELVertex, ELEdge, ELFace> {

	public ELHDS() {
		super(ELVertex.class, ELEdge.class, ELFace.class);
	}
	
}
