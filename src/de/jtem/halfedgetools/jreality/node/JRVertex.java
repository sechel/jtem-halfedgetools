/**
 * 
 */
package de.jtem.halfedgetools.jreality.node;

import de.jtem.halfedge.Vertex;
/** this is a HalfEdgeDataStruckture component (a Vertex)
 *  which supports the typical JReality IndexedFaceSet 
 *  Attributes  
 *
 *  you can simply use the classes <code>My..Adapter</code> 
 *  as adapters for reading and writing.
 *       
 * @author gonska
 */

public abstract class JRVertex <
	V extends JRVertex<V, E, F>,
	E extends JREdge<V, E, F>,
	F extends JRFace<V, E, F>
> extends Vertex<V, E, F> {
	
	public double[] 
	    position = null,
	    normal = null,
	    color = null,
	    textCoord = null;
	public String 
		label = "";
	public double 
		radius = 1,
		pointSize = 1;
	
}