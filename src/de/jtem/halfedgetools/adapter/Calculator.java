package de.jtem.halfedgetools.adapter;

import de.jtem.halfedge.Node;

public interface Calculator {
	
	public <
		N extends Node<?, ?, ?>
	> boolean canAccept(Class<N> nodeClass);
	
	public double getPriority();
	
}
