package de.jtem.halfedge.functional;

import no.uib.cipr.matrix.Matrix;
import de.jtem.halfedgetools.functional.Hessian;

public class MyHessian implements Hessian {

	protected Matrix
		H = null;
	
	public MyHessian(Matrix H) {
		this.H = H;
	}
	
	@Override
	public void add(int i, int j, double value) {
		H.add(i, j, value);
	}

	@Override
	public void set(int i, int j, double value) {
		H.set(i, j, value);
	}

	@Override
	public void setZero() {
		H.zero();
	}

}
