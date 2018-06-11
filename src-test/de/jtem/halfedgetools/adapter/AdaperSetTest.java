package de.jtem.halfedgetools.adapter;

import org.junit.Assert;
import org.junit.Test;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.type.Position;

@Position
public class AdaperSetTest {

	private class PriorityAdapter extends AbstractAdapter<Double> {

		private double priority = 0;
		
		public PriorityAdapter(double priority) {
			super(Double.class, true, false);
			this.priority = priority;
		}
		
		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return true;
		}
		
		@Override
		public double getPriority() {
			return this.priority;
		}
		
	}
	
	@Test
	public void adpterPriorityTest1() {
		PriorityAdapter a1 = new PriorityAdapter(1.0);
		PriorityAdapter a2 = new PriorityAdapter(2.0);
		AdapterSet a = new AdapterSet(a1, a2);
		PriorityAdapter result = a.query(PriorityAdapter.class);
		Assert.assertEquals(result, a2);
	}
	
	@Test
	public void adpterPriorityTest2() {
		PriorityAdapter a1 = new PriorityAdapter(2.0);
		PriorityAdapter a2 = new PriorityAdapter(1.0);
		AdapterSet a = new AdapterSet(a1, a2);
		PriorityAdapter result = a.query(PriorityAdapter.class);
		Assert.assertEquals(result, a1);
	}	
	
}
