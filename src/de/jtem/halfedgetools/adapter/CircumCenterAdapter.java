package de.jtem.halfedgetools.adapter;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.type.CircumCenter;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@CircumCenter
public class CircumCenterAdapter extends AbstractAdapter<double[]> {
	
	public CircumCenterAdapter() {
		super(double[].class,true,false);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

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
		double[]
		       ec1 = Rn.subtract(null, c2, c1),
		       ec2 = Rn.subtract(null, c3, c2),
		       ec3 = Rn.subtract(null, c1, c3);
		double
			alpha = 
				Rn.euclideanNormSquared(ec2)*(-1)*Rn.innerProduct(ec1, ec3)/
				(2*Rn.euclideanNormSquared(Rn.crossProduct(null, ec1, ec2))),
			beta = 
				Rn.euclideanNormSquared(ec3)*(-1)*Rn.innerProduct(ec1, ec2)/
				(2*Rn.euclideanNormSquared(Rn.crossProduct(null, ec1, ec2))),
			gamma = 
				Rn.euclideanNormSquared(ec1)*(-1)*Rn.innerProduct(ec3, ec2)/
				(2*Rn.euclideanNormSquared(Rn.crossProduct(null, ec1, ec2)));
		double[]
		       ac1 = Rn.times(null, alpha, c1),
		       bc2 = Rn.times(null, beta, c2),
		       gc3 = Rn.times(null, gamma, c3);
		return Rn.add(null, ac1, Rn.add(null,bc2,gc3));
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
}