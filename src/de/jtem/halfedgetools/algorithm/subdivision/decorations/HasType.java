package de.jtem.halfedgetools.algorithm.subdivision.decorations;

public interface HasType {
	
	public enum Type {
		illegal,
		ToBad,
		ToShort,
		good,
		instable,
		oldEdge,
		newEdge,
		test,
		fine,
		end,
		way,
		node,
		splited,
		waypair,
		waysingle,
		nodesplit,
		newOne,
		oldOne
	}

	
	public Type  getType();
	public void setType(Type t);
}
