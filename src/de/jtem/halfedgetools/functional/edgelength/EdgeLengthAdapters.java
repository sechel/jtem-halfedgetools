package de.jtem.halfedgetools.functional.edgelength;

import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.DomainValue;

public class EdgeLengthAdapters {

	public static abstract class PositionDomainValue <V extends Vertex<V, ?, ?>> implements DomainValue {
		public void getPosition(V v, double[] pos) {
			pos[0] = get(v.getIndex() * 3 + 0);
			pos[1] = get(v.getIndex() * 3 + 1);
			pos[2] = get(v.getIndex() * 3 + 2);
		}
	}
	
	public static interface Length {
		public Double getL0();
	}
	
	public static interface WeightFunction {
		public Double evalWeight(Double l);
	}
	
}
