package de.jtem.halfedgetools.functional.alexandrov.decorations;


/**
 * Implementers will have the getRadius and setRadius methods 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface HasRadius {

	public void setRadius(Double r);
	
	public Double getRadius();
	
}
