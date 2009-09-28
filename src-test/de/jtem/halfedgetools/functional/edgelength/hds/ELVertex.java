package de.jtem.halfedgetools.functional.edgelength.hds;

import javax.vecmath.Point3d;

import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.HasPosition;


public class ELVertex extends Vertex<ELVertex, ELEdge, ELFace> implements HasPosition {

	public Point3d
		pos = new Point3d();

	@Override
	public Point3d getPosition() {
		return pos;
	}

	@Override
	public void setPosition(Point3d p) {
		pos.set(p);
	}
	
}
