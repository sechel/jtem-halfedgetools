package de.jtem.halfedgetools.jreality.node.standard;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class StandardHDS extends HalfEdgeDataStructure<StandardVertex, StandardEdge, StandardFace> {

	public StandardHDS() {
		super(StandardVertex.class, StandardEdge.class, StandardFace.class);
	}

}
