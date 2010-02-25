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
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;

public class StellarLinear {
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HEDS extends HalfEdgeDataStructure<V, E, F>
	> Map<F, Set<F>> execute(
		HEDS oldHeds, 
		HEDS newHeds, 
		VertexPositionCalculator vA,
		FaceBarycenterCalculator fA)
	{
		oldHeds.createCombinatoriallyEquivalentCopy(newHeds);
		Map<F, Set<F>> oldFtoNewFs = new HashMap<F,Set<F>>();
		for(V v : oldHeds.getVertices()) {
			vA.set(newHeds.getVertex(v.getIndex()), vA.get(v));
		}
		List<F> fList = new LinkedList<F>(newHeds.getFaces());
		for(F f : fList) {
			Set<F> newFaces = new HashSet<F>();
			
			V v = newHeds.addNewVertex();
			vA.set(v, fA.get(f));
			
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
			
			oldFtoNewFs.put(f, newFaces);
		}
		return oldFtoNewFs;
	}
	
	
}
