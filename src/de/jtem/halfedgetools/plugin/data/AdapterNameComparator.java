package de.jtem.halfedgetools.plugin.data;

import java.util.Comparator;

import de.jtem.halfedgetools.adapter.Adapter;

public class AdapterNameComparator implements Comparator<Adapter<?>> {

	@Override
	public int compare(Adapter<?> o1, Adapter<?> o2) {
		return o1.toString().compareTo(o2.toString());
	}

}
