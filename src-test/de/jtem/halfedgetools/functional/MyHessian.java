package de.jtem.halfedgetools.functional;

import no.uib.cipr.matrix.Matrix;

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
	
	@Override
	public double get(int i, int j) {
		return H.get(i, j);
	}

}
