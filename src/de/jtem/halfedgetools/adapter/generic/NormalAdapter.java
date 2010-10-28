package de.jtem.halfedgetools.adapter.generic;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.VectorField;

@Normal
@VectorField
public class NormalAdapter extends AbstractAdapter<double[]>  {


	public NormalAdapter() {
		super(double[].class, true, false);
	}
	
	public NormalAdapter(Class<? extends double[]> typeClass, boolean getter, boolean setter) {
		super(typeClass, getter, setter);
	}
	
	@Override
	public double getPriority() {
		return 0;
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		double[] normal = new double[3];
		List<E> star = HalfEdgeUtils.incomingEdges(v);
		int numFaces = 0;
		for (E e : star) {
			if (e.getLeftFace() == null) continue;
			double[] n = getF(e.getLeftFace(), a);
			Rn.add(normal, n, normal);
			numFaces++;
		}
		return Rn.times(normal, 1.0 / numFaces, normal);
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] normal = new double[3];
		int numFaces = 0;
		if (e.getLeftFace() != null) {
			Rn.add(normal, normal, getF(e.getLeftFace(), a));
			numFaces++;
		}
		if (e.getRightFace() != null) {
			Rn.add(normal, normal, getF(e.getRightFace(), a));
			numFaces++;
		}
		if (numFaces != 0) {
			Rn.times(normal, 1.0 / numFaces, normal);
		}
		return normal;
	}	
	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] n = new double[3];
		for (E b1 : HalfEdgeUtils.boundaryEdges(f)) {
			E b2 = b1.getPreviousEdge().getOppositeEdge();
			double[] s1 = a.get(Position.class, b1.getTargetVertex(), double[].class);
			double[] s2 = a.get(Position.class, b2.getTargetVertex(), double[].class);
			double[] t = a.get(Position.class, b1.getStartVertex(), double[].class);
			double[] v1 = Rn.subtract(null, s1, t);
			double[] v2 = Rn.subtract(null, s2, t);
			double[] nf = Rn.crossProduct(null, v1, v2);
			Rn.add(n, n, nf);
		}
		return Rn.normalize(n, n);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return true;
	}
	
}
