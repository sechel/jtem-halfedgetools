package de.jtem.halfedgetools.functional;

import de.jtem.halfedgetools.functional.Energy;

public class MyEnergy implements Energy {

	protected double 
		E = 0.0;
	
	public double get() {
		return E;
	}
	
	@Override
	public void add(double E) {
		this.E += E;
	}
	
	@Override
	public void set(double E) {
		this.E = E;
	}
	
	@Override
	public void setZero() {
		E = 0.0;
	}
	
}
