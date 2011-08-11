package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Parameter;

public class SimpleBeadsPositionAdapter extends AbstractAdapter<double[]> {

	private int
		beadsPerNode = 1,
		beadIndex = 0;
	
	public SimpleBeadsPositionAdapter() {
		super(double[].class, true, false);
	}
	
	@Parameter(name="beadIndex")
	public void setBeadIndex(int beadIndex) {
		this.beadIndex = beadIndex;
	}
	@Parameter(name="beadsPerNode")
	public void setBeadsPerNode(int beadsPerNode) {
		this.beadsPerNode = beadsPerNode;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		
		return false;
	}

}
