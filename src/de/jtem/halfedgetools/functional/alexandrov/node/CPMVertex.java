package de.jtem.halfedgetools.functional.alexandrov.node;

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasCurvature;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasRadius;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXY;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;

/**
 * The vertex class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMVertex extends Vertex<CPMVertex, CPMEdge, CPMFace> implements HasXYZW, HasXY, HasRadius, HasCurvature{

	private static final long 
		serialVersionUID = 1L;
	private Point4d
		pos4d = new Point4d();
	private Point2d
		pos2d = new Point2d();
	private Double
		radius = 100.0;
	
//	/*
//	 * Unfold project only
//	 */
//	private Boolean curvature = true;
//	

	public Boolean hasCurvature() {
		return true;
	}
	
	public void setCurvature(Boolean c) {
//		curvature = c;
	}
	
//	@Override
	protected CPMVertex getThis() {
		return this;
	}
	
	public Point4d getXYZW() {
		return pos4d;
	}

	public void setXYZW(Point4d p) {
		pos4d.set(p);
	}

	public void setXY(Point2d p) {
		pos2d.set(p);
	}

	public Point2d getXY() {
		return pos2d;
	}

	public Double getRadius() {
		if (radius == null)
			radius = 100.0;
		return radius;
	}

	public void setRadius(Double radius) {
		this.radius = radius;
	}

	@Override
	public String toString() {
		return super.toString() + " radius = " + radius;
	}

	public double[] getPosition() {
		return new double[]{pos4d.x, pos4d.y, pos4d.z, pos4d.w}; 
	}
	public void setPosition(double[] pos) {
		setXYZW(new Point4d(pos[0], pos[1], pos[2], pos[3]));
	}
	
}
