package de.jtem.halfedgetools.util.triangulationutilities;

/**
 * Thrown if a datastructure represents no triangulation.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see halfedge.HalfEdgeDataStructure
 * @see alexandrov.Alexandrov
 */
public class TriangulationException extends Exception {

	private static final long 
		serialVersionUID = 1L;
	
	public TriangulationException(String msg){
		super(msg);
	}
}
