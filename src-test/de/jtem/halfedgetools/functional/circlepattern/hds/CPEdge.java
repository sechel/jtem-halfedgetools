package de.jtem.halfedgetools.functional.circlepattern.hds;

import static java.lang.Math.PI;
import de.jtem.halfedge.Edge;

public class CPEdge extends Edge<CPVertex, CPEdge, CPFace> {

	private double
		theta = PI / 2; 
	
	public double getTheta() {
		return theta;
	}
	
	public void setTheta(double theta) {
		this.theta = theta;
	}
	
}
