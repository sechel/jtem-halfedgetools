package de.jtem.halfedgetools.adapter;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Node;

public class CalculatorSet extends HashSet<Calculator> {

	private static final long 
		serialVersionUID = 1L;

	public CalculatorSet() {
		super();
	}

	public CalculatorSet(Collection<? extends Calculator> c) {
		super(c);
	}

	
	@SuppressWarnings("unchecked")
	public <
		C extends Calculator,
		N extends Node<?,?,?>
	> C get(Class<N> nClass, Class<C> cClass) {
		List<C> compatibleList = new LinkedList<C>();
		for (Calculator c : this) {
			if (cClass.isAssignableFrom(c.getClass()) && c.canAccept(nClass)) {
				compatibleList.add((C)c);
			}
		}
		Collections.sort(compatibleList, new CalculatorComparator());
		if (compatibleList.isEmpty()) {
			return null;
		} else {
			return compatibleList.get(0);
		}
	}
	
	
	public <
		C extends Calculator,
		N extends Node<?,?,?>
	> boolean isAvailable(Class<N> nClass, Class<C> cClass) {
		return get(nClass, cClass) != null;
	}
	
	
	public <
		N extends Node<?,?,?>
	> boolean isAvailable(Class<N> nClass, Class<? extends Calculator>... cClasses) {
		boolean result = true;
		for (Class<? extends Calculator> c : cClasses) {
			result &= isAvailable(nClass, c);
		}
		return result;
	}
	
	
	private class CalculatorComparator implements Comparator<Calculator> {
		
		@Override
		public int compare(Calculator o1, Calculator o2) {
			return o1.getPriority() < o2.getPriority() ? -1 : 1;
		}
		
	}
	

}
