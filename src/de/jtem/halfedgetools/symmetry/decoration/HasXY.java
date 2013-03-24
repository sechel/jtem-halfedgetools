package de.jtem.halfedgetools.symmetry.decoration;


import javax.vecmath.Point2d;


/**
 * Implementers will have the getXY and setXY methods. It uses the
 * vecmath.Poiny2D class 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see javax.vecmath.Point2d
 */
public interface HasXY {

	public void setXY(Point2d p);
	
	public Point2d getXY();
	
}
