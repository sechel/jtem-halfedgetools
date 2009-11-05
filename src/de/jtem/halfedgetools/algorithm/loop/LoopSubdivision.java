package de.jtem.halfedgetools.algorithm.loop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;

public class LoopSubdivision <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> ,
F extends Face<V, E, F>,
HEDS extends HalfEdgeDataStructure<V, E, F>>
 {

	private Map<E, double[]> oldEtoPos;
	private HashMap<V, double[]> oldVtoPos;
	private Map<V,E> newVtoOldE;
	
	private Map<E, Set<E>> oldEtoNewEs;
	
	//return new HEDS approximated using dyadic scheme
	public Map<E, Set<E>> subdivide(HEDS oldHeds, HEDS newHeds, Coord3DAdapter<V> vc, Coord3DAdapter<E> ec){
				
		
		oldEtoPos = new HashMap<E, double[]>();
		for(E e : oldHeds.getPositiveEdges()) {

			double[] pos = new double[3];
			double[] a = ec.getCoord(e.getPreviousEdge());
			double[] b = ec.getCoord(e.getOppositeEdge().getPreviousEdge());
			double[] c = ec.getCoord(e.getOppositeEdge().getNextEdge());
			double[] d = ec.getCoord(e.getNextEdge());
			
			Rn.times(a,3,a);
			Rn.times(b,3,b);
			Rn.add(pos, b, a);
			Rn.add(pos, c, pos);
			Rn.add(pos, d, pos);
			Rn.times(pos, 1.0/8.0, pos);
			
			oldEtoPos.put(e, pos);
			
		}
		
		oldVtoPos = new HashMap<V, double[]>();
		for(V v : oldHeds.getVertices()) {
			List<E> star = HalfEdgeUtils.incomingEdges(v);
			int deg = star.size();
			
			double[] mid = new double[]{0,0,0};
			for(E e : star) {
				E eo = e.getOppositeEdge();
				Rn.add(mid, ec.getCoord(eo), mid);
			}
			Rn.times(mid, 1.0 / deg, mid);	
			
			double[] newpos = new double[] {0,0,0};
			double alpha = alphaLoop(deg);
			
			Rn.linearCombination(newpos, 1.0 - alpha, vc.getCoord(v), alpha, mid);
			
			oldVtoPos.put(v, newpos);			
		}
		
		dyadicSubdiv(oldHeds, newHeds);
		
		for(V v : newVtoOldE.keySet()) {
			double[] pos = oldEtoPos.get(newVtoOldE.get(v));
			vc.setCoord(v, pos);
		}
		
		for(V v : oldVtoPos.keySet()) {
			double[] pos = oldVtoPos.get(v);
			V newV = newHeds.getVertex(v.getIndex());
			vc.setCoord(newV, pos);
		}
		
		return oldEtoNewEs;

	};
	
	//return new HEDS subdivided
	private void dyadicSubdiv(HEDS alt, HEDS neu){
		//struktur kopieren
		alt.createCombinatoriallyEquivalentCopy(neu);

		newVtoOldE = new HashMap<V,E>();
		oldEtoNewEs = new HashMap<E,Set<E>>();
		
		for(E oe : alt.getPositiveEdges()){
			E e = neu.getEdge(oe.getIndex());	

			V mv = neu.addNewVertex();
			newVtoOldE.put(mv, oe);
			
			V et = e.getTargetVertex();
			V es = e.getStartVertex();
			E eo = e.getOppositeEdge();
			
			E e2 = neu.addNewEdge();
			E eo2 = neu.addNewEdge();	
			E en = e.getNextEdge();
			E eon = eo.getNextEdge();		
			
		//	re-link edges	
			e.setTargetVertex(mv);
			e.linkNextEdge(e2);
			e.linkOppositeEdge(eo2);
			e2.linkNextEdge(en);
			e2.setTargetVertex(et);
			eo.setTargetVertex(mv);
			eo.linkNextEdge(eo2);
			eo.linkOppositeEdge(e2);
			eo2.linkNextEdge(eon);
			eo2.setTargetVertex(es);
			
			HashSet<E> newEs = new HashSet<E>();
			newEs.add(e); newEs.add(e2);
			
			oldEtoNewEs.put(oe, newEs);

		}
		
		
	//	end : edge cut
		
	//	rearrange interior
		for(F of : alt.getFaces()){
			F f = neu.getFace(of.getIndex());
			List<E> e = new ArrayList<E>(0);
			List<E> eb = new ArrayList<E>(0);
			List<F> fn = neu.addNewFaces(3);
			
//			List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
			
			e.add(f.getBoundaryEdge());

			eb.add(e.get(0).getNextEdge());
			e.add(eb.get(0).getNextEdge());
			eb.add(e.get(1).getNextEdge());
			e.add(eb.get(1).getNextEdge());
			eb.add(e.get(2).getNextEdge());
			
			List<E> inner = neu.addNewEdges(3);
			List<E> outer = neu.addNewEdges(3);
			
			for (int i=0 ; i<3; i++){
				inner.get(i).setLeftFace(f);
				inner.get(i).setTargetVertex(e.get(i).getTargetVertex());
				inner.get(i).linkOppositeEdge(outer.get(i));
				inner.get(i).linkNextEdge(inner.get((i+1)%3));
				
				e.get(i).linkNextEdge(outer.get(i));
				outer.get(i).linkNextEdge(eb.get((i+2)%3));
				outer.get(i).setTargetVertex(e.get((i+2)%3).getTargetVertex());
				e.get(i).setLeftFace(fn.get(i));
				outer.get(i).setLeftFace(fn.get(i));
				eb.get((i+2)%3).setLeftFace(fn.get(i));
				
			};
			
		}
		
	};

	
	private double alphaLoop(int degv){
		double alpha = 3.0/8.0;
		if (degv == 6){}
		else{
		alpha = 5.0/8.0 - Math.pow(3.0/8.0 + 1.0/4.0 * Math.cos(2*Math.PI/degv),2);
		};
		return alpha;
	}
	
	
}
