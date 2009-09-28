package de.jtem.halfedgetools.functional.circlepattern.hds;

import de.jtem.halfedgetools.functional.circlepattern.CPAdapters.Phi;
import de.jtem.halfedgetools.functional.circlepattern.CPAdapters.Theta;

public class MyCPAdapters {

	public static class MyTheta implements Theta<CPEdge> {

		@Override
		public double getTheta(CPEdge e) {
			return e.getTheta();
		}

		public void setTheta(CPEdge e, double theta) {
			e.setTheta(theta);
		}

	}
	
	
	public static class MyPhi implements Phi<CPFace> {

		@Override
		public double getPhi(CPFace e) {
			return e.getPhi();
		}
		
	}
	
}
