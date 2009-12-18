package de.jtem.halfedgetools.algorithm.adaptivesubdivision.interpolators;

import de.jtem.halfedgetools.algorithm.adaptivesubdivision.util.Calculator;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

/** Spline Interpolation zur Bestimmung von 
 * Koordinaten von neuen Vertice auf der Kantenmitte.
 * @author Bernd Gonska
 */
public class SplineInterpolator extends Interpolator{
	@Override
	/** Interpoliert mithilfe der Normalen auf basis eines Splines */
	public <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>void interpolate(E e, V v) {
		V v0=e.getStartVertex();
		V v1=e.getTargetVertex();
		v.position=Calculator.interpolate(v0.position, v1.position, v0.normal, v1.normal);
	}
}
