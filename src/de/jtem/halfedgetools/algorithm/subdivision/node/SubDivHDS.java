package de.jtem.halfedgetools.algorithm.subdivision.node;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class SubDivHDS extends HalfEdgeDataStructure<SubDivVertex, SubDivEdge, SubDivFace>{


	public SubDivHDS() {
		super(SubDivVertex.class, SubDivEdge.class, SubDivFace.class);

	}
}
