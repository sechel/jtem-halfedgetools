package de.jtem.halfedgetools.functional.circlepattern.hds;

import static java.lang.Math.PI;
import de.jtem.halfedge.Face;

public class CPFace extends Face<CPVertex, CPEdge, CPFace> {

	private double
		phi = 2 * PI;
	
	public double getPhi() {
		return phi;
	}
	
	public void setPhi(double phi) {
		this.phi = phi;
	}
	
}
