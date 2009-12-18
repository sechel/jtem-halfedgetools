package de.jtem.halfedgetools.algorithm.adaptivesubdivision.node;

import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;

/** Teil der Datenstruktur die fuer die Verwaltung des Subdividers genutzt wird.
 * Eine Halbkante.
 * @author Bernd Gonska
 */
public class SubDivEdge extends JREdge<SubDivVertex,SubDivEdge,SubDivFace> implements HasLengthSquared{

	private double lengthS = -1;
	@Override
	public Double getLengthSquared() {
		return lengthS;
	}
	@Override
	public void setLengthSquared(Double length) {
		lengthS = length;
		
	}

}