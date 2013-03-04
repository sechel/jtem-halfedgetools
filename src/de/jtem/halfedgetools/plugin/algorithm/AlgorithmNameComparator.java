package de.jtem.halfedgetools.plugin.algorithm;

import java.util.Comparator;

public class AlgorithmNameComparator implements Comparator<AlgorithmPlugin> {

	@Override
	public int compare(AlgorithmPlugin a1, AlgorithmPlugin a2) {
		return a2.getAlgorithmName().compareTo(a2.getAlgorithmName());
	}

}
