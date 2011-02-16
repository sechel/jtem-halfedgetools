package de.jtem.halfedgetools.adapter.generic;

import java.util.List;
import java.util.Vector;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Volume;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@Volume
public class FaceVolumeAdapter extends AbstractAdapter<Double> {

	public FaceVolumeAdapter() {
		super(Double.class, true, false);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

	@Override
	public <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>> Double getF(
			F f, AdapterSet a) {

		// get all edges
		List<double[]> e = new Vector<double[]>(5);
		E start = f.getBoundaryEdge();
		E curr = start;
		int k = 0;
		do {
			e.add(k++, a.get(Position3d.class, curr.getStartVertex(),
					double[].class));
			curr = curr.getNextEdge();
		} while (!curr.equals(start));

		// calculate the area vector
		int size = e.size();
		double[] p, q, s_part;
		double[] s = new double[3];
		for (int i = 0; i < size; i++) {
			p = e.get(i);
			q = e.get((i + 1) % size);
			s_part = Rn.crossProduct(null, p, Rn.subtract(null, q, p));
			for (int j = 0; j < s.length; j++) {
				s[j] += s_part[j] * .5;
			}
		}

		// return the length of the area vector
		return Rn.euclideanNorm(s);
	}

	@Override
	public double getPriority() {
		return 0;
	}
}
