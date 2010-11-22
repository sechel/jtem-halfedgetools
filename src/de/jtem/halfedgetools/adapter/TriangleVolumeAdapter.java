package de.jtem.halfedgetools.adapter;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.type.Volume;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@Volume
public class TriangleVolumeAdapter extends AbstractAdapter<Double> {
	
	public TriangleVolumeAdapter() {
		super(Double.class,true,false);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> Double getF(F f, AdapterSet a) {
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
		double[]
		       ec1 = Rn.subtract(null, c2, c1),
		       ec2 = Rn.subtract(null, c3, c1);
		return 0.5*Rn.euclideanNorm(Rn.crossProduct(null, ec1, ec2));
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
}