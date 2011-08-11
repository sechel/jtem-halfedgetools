package de.jtem.halfedgetools.plugin.data.visualizer;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Parameter;
import de.jtem.halfedgetools.adapter.type.BeadPosition;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@BeadPosition
public class SimpleBeadsPositionAdapter extends AbstractAdapter<double[]> {

	private int
		beadsPerNode = 1,
		beadIndex = 0;
	
	public SimpleBeadsPositionAdapter() {
		super(double[].class, true, false);
	}
	
	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> double[] getE(
			E e, de.jtem.halfedgetools.adapter.AdapterSet a) {
		double[] p = a
				.get(Position3d.class, e.getStartVertex(), double[].class);
		double[] v = a.get(EdgeVector.class, e, double[].class);
		double[] s = a.get(BaryCenter3d.class, e.getLeftFace(), double[].class);

		double[][] points = new double[3][3];
		points[0] = Rn.linearCombination(null, 1., p, ((double) beadIndex)
				/ beadsPerNode, v);
		points[1] = Rn.linearCombination(null, 1., p, ((double) beadIndex + 1.)
				/ beadsPerNode, v);
		points[2] = s;

		return getBarycenter(points);
	}
	
	// TODO: implement getV, getF
	
	public double[] getBarycenter(double[][] points) {
		double[] pos= new double[3];
		for (double[] p : points) {
			Rn.add(pos, pos, p);
		}
		return Rn.times(pos, 1.0 / points.length, pos);
	}
	
	@Parameter(name = "beadIndex")
	public void setBeadIndex(int beadIndex) {
		if (beadsPerNode <= beadIndex)
			throw new RuntimeException(
					"cannot set index higher than number of beads per node!");
		this.beadIndex = beadIndex;
	}

	@Parameter(name = "beadsPerNode")
	public void setBeadsPerNode(int beadsPerNode) {
		if (beadsPerNode < 0)
			throw new RuntimeException(
					"cannot accept less than zero beads per node!");

		this.beadsPerNode = beadsPerNode;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return true; // can accept all
	}

}
