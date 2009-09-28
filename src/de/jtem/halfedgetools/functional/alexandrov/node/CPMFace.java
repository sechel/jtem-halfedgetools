package de.jtem.halfedgetools.functional.alexandrov.node;


import javax.vecmath.Color3f;
import javax.vecmath.Point4d;

import de.jtem.halfedge.Face;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasColor;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;


/**
 * The face class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMFace extends Face<CPMVertex, CPMEdge, CPMFace> implements HasXYZW, HasColor{

	private static final long 
		serialVersionUID = 1L;
	private Point4d
		pos = new Point4d();
	private Color3f
		color = new Color3f();

//	@Override
	protected CPMFace getThis() {
		return this;
	}
	
	public Point4d getXYZW() {
		return pos;
	}
	
	public void setXYZW(Point4d p) {
		pos.set(p);
	}

	public void setColor(Color3f c) {
		color.set(c);
	}
	
	public Color3f getColor() {
		return color;
	}
	
}
