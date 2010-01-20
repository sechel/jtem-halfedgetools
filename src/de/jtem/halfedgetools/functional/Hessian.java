package de.jtem.halfedgetools.functional;

public interface Hessian {

	public void set(int i, int j, double value);
	public void add(int i, int j, double value);
	public double get(int i, int j);
	public void setZero();
	
}
