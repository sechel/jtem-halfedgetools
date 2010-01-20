package de.jtem.halfedgetools.functional.edgelength;

import de.jtem.halfedge.Edge;


public class EdgeLengthAdapters {

	public static interface Length<E extends Edge<?, E, ?>> {
		public Double getTargetLength(E e);
	}
	
	public static interface WeightFunction<E extends Edge<?, E, ?>> {
		public Double getWeight(E e);
	}
	
}
