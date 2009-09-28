package de.jtem.halfedge.functional;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.Gradient;

public class MyGradient implements Gradient {

	protected Vector
		G = null;
	
	public MyGradient(Vector G) {
		this.G = G;
	}
	
	@Override
	public void add(int i, double value) {
		G.add(i, value);
	}

	@Override
	public void set(int i, double value) {
		G.set(i, value);
	}

	@Override
	public void setZero() {
		G.zero();
	}

}
