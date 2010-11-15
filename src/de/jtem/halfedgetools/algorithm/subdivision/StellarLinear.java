package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;

public class StellarLinear {
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> Map<F, Set<F>> execute(
		HEDS oldHeds, 
		HEDS newHeds, 
		TypedAdapterSet<double[]> a
	){
		oldHeds.createCombinatoriallyEquivalentCopy(newHeds);
		Map<F, Set<F>> oldFtoNewFs = new HashMap<F,Set<F>>();
		for(V v : oldHeds.getVertices()) {
			double[] p = a.get(BaryCenter4d.class, v); 
			a.set(Position.class, newHeds.getVertex(v.getIndex()), p);
		}
		List<F> fList = new LinkedList<F>(newHeds.getFaces());
		for(F f : fList) {
			Set<F> newFaces = subdivideFace(newHeds, f, a);
			
			oldFtoNewFs.put(f, newFaces);
		}
		return oldFtoNewFs;
	}

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> Set<F> subdivideFace(
		HEDS newHeds,
		F f,
		TypedAdapterSet<double[]> a
	){
		Set<F> newFaces = new HashSet<F>();
		
		V v = newHeds.addNewVertex();
		a.set(Position.class, v, a.get(BaryCenter4d.class, f));
		
		E se = f.getBoundaryEdge(); //startEdge
		E be = se;
		E 	ne = null,
			e1 = null,
			e2 = null;
		do {
			ne = be.getNextEdge();
			F nf = newHeds.addNewFace();
			be.setLeftFace(nf);
			newFaces.add(nf);
			e2 = newHeds.addNewEdge();
			e2.setTargetVertex(be.getStartVertex());
			if(e1 != null) {
				e2.linkOppositeEdge(e1);
			}
			e1 = newHeds.addNewEdge();
			e1.setTargetVertex(v);
			be.linkNextEdge(e1);
			e1.linkNextEdge(e2);
			e2.linkNextEdge(be);
			
			e1.setLeftFace(nf);
			e2.setLeftFace(nf);
			
			be = ne;
		} while (be != null);
		e1.linkOppositeEdge(se.getPreviousEdge());
		
		newHeds.removeFace(f);
		return newFaces;
	}
}
