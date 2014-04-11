package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.CircumCenter;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.util.GeometryUtility;

@CircumCenter
public class CircumCenterAdapter extends AbstractAdapter<double[]> {
	
	public CircumCenterAdapter() {
		super(double[].class,true,false);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> double[] getF(F f, AdapterSet a) {
		E 	e1 = f.getBoundaryEdge(),
			e2 = e1.getNextEdge(),
			e3 = e2.getNextEdge();
		V	v1 = e1.getStartVertex(),
			v2 = e2.getStartVertex(),
			v3 = e3.getStartVertex();
		double[]
			c1 = a.get(Position3d.class, v1, double[].class),
			c2 = a.get(Position3d.class, v2, double[].class),
			c3 = a.get(Position3d.class, v3, double[].class);
		return GeometryUtility.circumCenter(c1, c2, c3);
	}

	@Override
	public String toString() {
		return "Circum Center";
	}

	@Override
	public double getPriority() {
		return 0;
	}
}