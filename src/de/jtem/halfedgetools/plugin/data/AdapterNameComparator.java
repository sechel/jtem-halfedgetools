package de.jtem.halfedgetools.plugin.data;

import java.util.Comparator;

import de.jtem.halfedgetools.adapter.Adapter;

public class AdapterNameComparator implements Comparator<Adapter<?>> {

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Adapter<?> o1, Adapter<?> o2) {
		if (o1.toString().equals(o2.toString())) {
			return ((Adapter<Object>)o1).compareTo((Adapter<Object>)o2);
		} else {
			return o1.toString().compareTo(o2.toString());
		}
	}

}
