package de.jtem.halfedgetools.adapter.generic;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.VectorField;
import de.jtem.halfedgetools.adapter.type.generic.EdgeVector;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@EdgeVector
@VectorField
public class EdgeVectorAdapter extends AbstractAdapter<double[]> {

	public EdgeVectorAdapter() {
		super(double[].class, true, false);
	}
	
	@Override
	public <T extends Node<?, ?, ?>> boolean canAccept(Class<T> nodeClass) {
		return Edge.class.isAssignableFrom(nodeClass);
	}
	
	@Override
	public double getPriority() {
		return -1;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] s = a.getDefault(Position3d.class, e.getStartVertex(), new double[] {0,0,0});
		double[] t = a.getDefault(Position3d.class, e.getTargetVertex(), new double[] {0,0,0});
		return Rn.subtract(null, t, s);
	}	

	@Override
	public String toString() {
		return "Edge Vector";
	}
	
}
