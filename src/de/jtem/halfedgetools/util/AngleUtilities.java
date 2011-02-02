package de.jtem.halfedgetools.util;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.functional.FunctionalUtils;

public class AngleUtilities {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double calculateRotationAngle(List<E> dualPath, AdapterSet a) {
		double totalAngle = 0.0;
		E previousEdge = null;
		for(E e : dualPath) {
			if(previousEdge != null) {
				if(e.getStartVertex() == previousEdge.getStartVertex()) {
					totalAngle += angleAt(e.getLeftFace(),e.getStartVertex(),a);
				} else { //e.getTargetVertex() == previousEdge.getTargetVertex()
					totalAngle -= angleAt(e.getLeftFace(),e.getTargetVertex(),a);
				}
			}
			previousEdge = e;
		}
		assert previousEdge != null;
		E e = dualPath.get(0);
		if(e.getStartVertex() == previousEdge.getStartVertex()) {
			totalAngle += angleAt(e.getLeftFace(),e.getStartVertex(),a);
		} else { //e.getTargetVertex() == previousEdge.getTargetVertex()
			totalAngle -= angleAt(e.getLeftFace(),e.getTargetVertex(),a);
		}
		return totalAngle;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double angleAt(F f, V v, AdapterSet as) {
		E le = f.getBoundaryEdge();
		while(le.getTargetVertex() != v) {
			le = le.getNextEdge();
		}
		E re = le.getNextEdge();
		le = le.getOppositeEdge();
		return angle(le,re,as);
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double angle(E e1, E e2, AdapterSet as) {
		TypedAdapterSet<double[]> tas = as.querySet(double[].class);
		double[] 
		       lec = Rn.subtract(null,
		    		   tas.get(Position3d.class,e1.getTargetVertex()), 
		    		   tas.get(Position3d.class,e1.getStartVertex())),
		       rec = Rn.subtract(null,
		    		   tas.get(Position3d.class,e2.getTargetVertex()), 
		    		   tas.get(Position3d.class,e2.getStartVertex()));
		
		return FunctionalUtils.angle(lec, rec);	
	}
}
