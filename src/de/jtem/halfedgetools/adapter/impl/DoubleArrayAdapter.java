package de.jtem.halfedgetools.adapter.impl;

import de.jtem.halfedgetools.adapter.AbstractAdapter;


public abstract class DoubleArrayAdapter extends AbstractAdapter<double[]> {

	public DoubleArrayAdapter(boolean getter, boolean setter) {
		super(double[].class, getter, setter);
	}	

}
