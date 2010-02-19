package de.jtem.halfedgetools.adapter;

import java.util.Collection;
import java.util.HashSet;

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
		C result = null;
		for (Calculator c : this) {
			if (cClass.isAssignableFrom(c.getClass()) && c.canAccept(nClass)) {
				result = (C)c;
				break;
			}
		}
		return result;
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
	

}
