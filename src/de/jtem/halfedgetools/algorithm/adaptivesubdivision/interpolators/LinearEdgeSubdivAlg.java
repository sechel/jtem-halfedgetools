package de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators;

import de.jtem.halfedgetools.algorithm.adaptivesubdivision.util.Calculator;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;
/** Linearen Interpolation zur Bestimmung von 
 * Koordinaten neuer Vertice auf der Kantenmitte.
 * @author Bernd Gonska
 */
public class LinearEdgeSubdivAlg extends Interpolator{
	@Override
	/** Lineare Interpolation setzt dem gegebenen Vertex
	 *  die Koordinaten von der Mitte der Kante.
	 */
	public <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>void interpolate(E e,V v) {	
		v.position=Calculator.linearCombination(
				.5, e.getTargetVertex().position
				,.5,e.getStartVertex().position);
	}
}
