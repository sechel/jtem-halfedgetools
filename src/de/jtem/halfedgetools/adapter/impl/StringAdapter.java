package de.jtem.halfedgetools.adapter.impl;

import de.jtem.halfedgetools.adapter.AbstractAdapter;


public abstract class StringAdapter extends AbstractAdapter<String> {

	public StringAdapter(boolean getter, boolean setter) {
		super(String.class, getter, setter);
	}	

}
