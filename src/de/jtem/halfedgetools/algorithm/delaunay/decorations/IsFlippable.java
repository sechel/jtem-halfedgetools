package de.jtem.halfedgetools.algorithm.delaunay.decorations;

import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;



/**
 * Implementers will have the flip, getFlipCount and resetFlipCount methods. 
 * It's supposed to work for delaunay flips.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public interface IsFlippable  {

	public void flip() throws TriangulationException;
	
	public int getFlipCount();
	
	public void resetFlipCount();
	
}
