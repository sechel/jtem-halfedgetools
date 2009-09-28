package de.jtem.halfedgetools.functional.alexandrov.decorations;

import javax.vecmath.Point4d;


/**
 * Implementers will have the getXYZW and setXYZW methods. It uses the
 * vecmath.Poiny4D class 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see javax.vecmath.Point4d
 */
public interface HasXYZW {

	public Point4d getXYZW();
	
	public void setXYZW(Point4d p);
	
	
}
