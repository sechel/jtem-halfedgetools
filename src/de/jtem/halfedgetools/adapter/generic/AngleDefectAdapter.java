package de.jtem.halfedgetools.adapter.generic;

import java.util.HashMap;
import java.util.Map;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.GaussCurvature;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.functional.FunctionalUtils;

@GaussCurvature
public class AngleDefectAdapter extends AbstractAdapter<Double> {

	private Map<Edge<?,?,?>,Double> 
		angleDefectMap = new HashMap<Edge<?,?,?>, Double>();
	
	public AngleDefectAdapter() {
		super(Double.class, true, false);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
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
	> Double getE(E e, AdapterSet a) {
		if(HalfEdgeUtils.isBoundaryEdge(e)) {
			return null;
		}
		if(!angleDefectMap.containsKey(e)) {
			double angleDefect = 0.0;
			if(!e.isPositive()) {
				angleDefect = calculateAngleDefect(e.getOppositeEdge(),a);
			} else {
				angleDefect = calculateAngleDefect(e,a);
			}
			double sign = (e.isPositive())?1.0:-1.0;
			angleDefectMap.put(e,sign*angleDefect);
			angleDefectMap.put(e.getOppositeEdge(),-sign*angleDefect);
			
		} 
		System.out.println(e +":"+angleDefectMap.get(e));
		return angleDefectMap.get(e);			
	}
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Double calculateAngleDefect(E e, AdapterSet a) {
		F 	lf = e.getLeftFace(),
			rf = e.getRightFace();
		if(lf == null || rf == null) {
			return null;
		}
		E	le = lf.getBoundaryEdge(),
			re = rf.getBoundaryEdge();
		if(!le.isPositive()) {
			le = le.getOppositeEdge();
		}
		if(!re.isPositive()) {
			re = re.getOppositeEdge();
		}
		TypedAdapterSet<double[]> tas = a.querySet(double[].class);
//		System.out.print(""+le +"-"+ lf +"-"+ e +"-"+ rf +"-"+ re);
		double[] 
		       lec = Rn.subtract(null,
		    		   tas.get(Position3d.class,le.getTargetVertex()), 
		    		   tas.get(Position3d.class,le.getStartVertex())),
		       rec = Rn.subtract(null,
		    		   tas.get(Position3d.class,re.getTargetVertex()), 
		    		   tas.get(Position3d.class,re.getStartVertex())),
		       eec = Rn.subtract(null,
		    		   tas.get(Position3d.class,e.getTargetVertex()), 
		    		   tas.get(Position3d.class,e.getStartVertex()));
		double 
			sra = Math.signum(Rn.innerProduct(tas.get(Normal.class,rf),Rn.crossProduct(null, eec, rec))),
			ra = ((sra==0)?1.0:sra)*FunctionalUtils.angle(eec, rec),
			sla = Math.signum(Rn.innerProduct(tas.get(Normal.class,lf),Rn.crossProduct(null, eec, lec))),
			la = ((sla==0)?1.0:sla)*FunctionalUtils.angle(eec, lec),
			angle = (((ra - la)+2*Math.PI)%(2*Math.PI));
//		System.out.println(e + ":" + angle);// +"="+ ra +"-"+la);
		return (angle>Math.PI)?angle-2*Math.PI:angle;
	}
}
