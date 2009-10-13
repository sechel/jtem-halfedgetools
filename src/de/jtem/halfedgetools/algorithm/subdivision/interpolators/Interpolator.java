package de.jtem.halfedgetools.algorithm.subdivision.interpolators;

import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

/** Interpoliert Daten fuer einen Vertex auf der Kantenmitte.
 * @author Bernd Gonska
 */
public abstract class Interpolator {
	/** Interpoliert Daten fuer einen Vertex auf der Kantenmitte.
	 *  Die interpolierten Deten werden dem Vertex gesetzt.
	 * @param e die zu teilende Kante
	 * @param v Der neue Vertex
	 */
	public <
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	>void interpolate(E e,V v){
		double[] t=e.getTargetVertex().position;
		double[] s=e.getStartVertex().position;
		double[] coords=new double[t.length];
		for (int i = 0; i < coords.length; i++) {
			coords[i]=(t[i]+s[i])/2;
		}
		v.position=coords;
	}
}
