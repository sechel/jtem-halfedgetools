package de.jtem.halfedge.functional;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.DomainValue;

public class MyDomainValue implements DomainValue {

	protected Vector
		u = null;
	
	public MyDomainValue(Vector u) {
		this.u = u;
	}
	
	@Override
	public void add(int i, double value) {
		u.add(i, value);
	}

	@Override
	public void set(int i, double value) {
		u.set(i, value);
	}

	@Override
	public void setZero() {
		u.zero();
	}

	@Override
	public double get(int i) {
		return u.get(i);
	}
	
}