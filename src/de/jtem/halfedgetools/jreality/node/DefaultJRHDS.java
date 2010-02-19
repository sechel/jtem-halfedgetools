package de.jtem.halfedgetools.jreality.node;

import de.jtem.halfedge.HalfEdgeDataStructure;

public class DefaultJRHDS extends HalfEdgeDataStructure<DefaultJRVertex, DefaultJREdge, DefaultJRFace> {

	public DefaultJRHDS() {
		super(DefaultJRVertex.class, DefaultJREdge.class, DefaultJRFace.class);
	}

}
