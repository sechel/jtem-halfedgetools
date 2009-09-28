package de.jtem.halfedgetools.functional.circlepattern;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;

public class CPAdapters {
	
	public static interface Rho <F extends Face<?, ?, F>> {
		public double getRho(F f);
	}
	
	public static interface Theta <E extends Edge<?,E,?>> {
		public double getTheta(E e);
	}
	
	public static interface Phi <F extends Face<?,?,F>> {
		public double getPhi(F e);
	}
	
}
