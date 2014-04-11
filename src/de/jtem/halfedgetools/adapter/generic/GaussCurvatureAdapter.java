package de.jtem.halfedgetools.adapter.generic;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.GaussCurvature;
import de.jtem.halfedgetools.util.AngleUtilities;

@GaussCurvature
public class GaussCurvatureAdapter extends AbstractAdapter<Double> {

	public GaussCurvatureAdapter() {
		super(Double.class, true, false);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass);
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
	> Double getV(V v, AdapterSet a) {
		if(HalfEdgeUtils.isBoundaryVertex(v)) {
			return 0.0;
		}
		E e = v.getIncomingEdge();
		List<E> dualPath = new LinkedList<E>();
		do {
			dualPath.add(e);
			e = e.getNextEdge().getOppositeEdge();
		} while(e != v.getIncomingEdge());
		double angle = (double)AngleUtilities.calculateRotationAngle(dualPath,a);
		return 2*Math.PI-Math.abs(angle);
	}
	
	@Override
	public String toString() {
		return "Gauss Curvature";
	}

}
