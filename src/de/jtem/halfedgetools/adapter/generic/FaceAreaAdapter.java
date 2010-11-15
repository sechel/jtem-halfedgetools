package de.jtem.halfedgetools.adapter.generic;

import static java.lang.Math.sqrt;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Area;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

@Area
public class FaceAreaAdapter extends AbstractAdapter<Double> {

	public FaceAreaAdapter() {
		super(Double.class, true, false);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double getF(F f, AdapterSet ad) {
		E e1 = f.getBoundaryEdge();
		E e2 = f.getBoundaryEdge().getNextEdge();
		double[] v1 = ad.get(Position3d.class, e1.getTargetVertex(), double[].class);
		double[] v2 = ad.get(Position3d.class, e2.getTargetVertex(), double[].class);
		double[] v3 = ad.get(Position3d.class, e1.getStartVertex(), double[].class);
		double a = 0,b = 0,c = 0;
		for (int k=0; k<3; k++) {
           a += (v1[k] - v2[k]) * (v1[k] - v2[k]);
           b += (v1[k] - v2[k]) * (v3[k] - v2[k]);
           c += (v3[k] - v2[k]) * (v3[k] - v2[k]);
		}
		double area = a*c-b*b;
		if (area <= 0.0) {
           return 0.0;
		} else {
           return sqrt(area) / 2.0;
		}
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Face.class.isAssignableFrom(nodeClass);
	}

}
