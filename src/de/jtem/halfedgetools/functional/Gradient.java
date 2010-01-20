package de.jtem.halfedgetools.functional;

public interface Gradient {
	
	public void set(int i, double value);
	public void add(int i, double value);
	public double get(int i);
	public void setZero();
	
}
