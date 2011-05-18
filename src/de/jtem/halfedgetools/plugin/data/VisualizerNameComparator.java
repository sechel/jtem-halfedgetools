package de.jtem.halfedgetools.plugin.data;

import java.util.Comparator;

public class VisualizerNameComparator implements Comparator<DataVisualizer> {

	@Override
	public int compare(DataVisualizer o1, DataVisualizer o2) {
		return o1.toString().compareTo(o2.toString());
	}

}
