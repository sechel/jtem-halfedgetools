package de.jtem.halfedgetools.selection;

import java.util.Collection;

import de.jtem.halfedge.Node;

public class Selection extends TypedSelection<Node<?,?,?>> {

	private static final long 
		serialVersionUID = -8430686267812674219L;

	public Selection() {
		super();
	}
	public <N extends Node<?,?,?>> Selection(Collection<N> c) {
		super(c);
	}
	public <N extends Node<?,?,?>> Selection(TypedSelection<N> c) {
		super(c);
	}
	@SafeVarargs
	public <N extends Node<?,?,?>> Selection(N... nArr) {
		super(nArr);
	}

}
