package de.jtem.halfedgetools.functional.edgelength;


public class EdgeLengthAdapters {

	public static interface Length {
		public Double getL0();
	}
	
	public static interface WeightFunction {
		public Double evalWeight(Double l);
	}
	
}
