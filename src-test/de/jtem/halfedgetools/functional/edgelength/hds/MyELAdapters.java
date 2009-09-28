package de.jtem.halfedgetools.functional.edgelength.hds;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.Length;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.PositionDomainValue;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.WeightFunction;

public class MyELAdapters {

	public static class MyPositionDomainValue extends PositionDomainValue<ELVertex> {

		private Vector
			x = null;
		
		public MyPositionDomainValue(Vector x) {
			this.x = x;
		}
		
		@Override
		public void add(int i, double value) {
			x.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			x.set(i, value);
		}

		@Override
		public void setZero() {
			x.zero();
		}

		@Override
		public double get(int i) {
			return x.get(i);
		}
		
	}
	
	public static class L0Adapter implements Length {

		private double 
			l0 = 0.0;
		
		public L0Adapter(double l0) {
			this.l0 = l0;
		}
		
		@Override
		public Double getL0() {
			return l0;
		}
		
		public void setL0(double l0) {
			this.l0 = l0;
		}
		
	}
	
	
	public static class ConstantWeight implements WeightFunction {

		public double 
			w = 1.0;
		
		public ConstantWeight(double w) {
			this.w = w;
		}
		
		@Override
		public Double evalWeight(Double l) {
			return w;
		}
		
	}
	
	
}
