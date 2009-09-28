package de.jtem.halfedgetools.functional;

public interface DomainValue {

	public double get(int i);
	public void set(int i, double value);
	public void add(int i, double value);
	public void setZero();
	
}
