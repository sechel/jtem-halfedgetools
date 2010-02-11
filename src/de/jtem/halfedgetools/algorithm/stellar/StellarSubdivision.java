package de.jtem.halfedgetools.algorithm.stellar;

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
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;

public class StellarSubdivision<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HEDS extends HalfEdgeDataStructure<V, E, F>>
  {

	private Map<F, Set<F>> oldFtoNewFs = new HashMap<F,Set<F>>();
	
	public Map<F, Set<F>> subdivide(
		HEDS oldHeds, 
		HEDS newHeds, 
		SubdivisionVertexAdapter<V> vA,
		SubdivisionFaceBarycenter<F> fA)
	{
		oldHeds.createCombinatoriallyEquivalentCopy(newHeds);
		for(V v : oldHeds.getVertices()) {
			vA.setData(newHeds.getVertex(v.getIndex()), vA.getData(v));
		}
		List<F> fList = new LinkedList<F>(newHeds.getFaces());
		for(F f : fList) {
			Set<F> newFaces = new HashSet<F>();
			
			V v = newHeds.addNewVertex();
			vA.setData(v, fA.getData(f));
			
			E e1 = f.getBoundaryEdge();
			boolean positive = e1.isPositive();
			E e2 = e1.getNextEdge();
			E e3 = e2.getNextEdge();
			
			F f1 = newHeds.addNewFace();
			F f2 = newHeds.addNewFace();
			F f3 = newHeds.addNewFace();
			newFaces.add(f1);
			newFaces.add(f2);
			newFaces.add(f3);
			
			E e12 = newHeds.addNewEdge();
			E e12op = newHeds.addNewEdge();
			E e23 = newHeds.addNewEdge();
			E e23op = newHeds.addNewEdge();
			E e31 = newHeds.addNewEdge();
			E e31op = newHeds.addNewEdge();
			
			//triangle f1 
			e1.linkNextEdge(e12);
			e1.setLeftFace(f1);
			
			e12.setTargetVertex(v);
			e12.linkNextEdge(e31op);
			e12.linkPreviousEdge(e1);
			e12.linkOppositeEdge(e12op);
			e12.setLeftFace(f1);
			
			e31op.setTargetVertex(e1.getStartVertex());
			e31op.linkNextEdge(e1);
			e31op.linkPreviousEdge(e12);
			e31op.linkOppositeEdge(e31);
			e31op.setLeftFace(f1);
			
			//triangle f2
			e2.linkNextEdge(e23);
			e2.setLeftFace(f2);
			
			e23.setTargetVertex(v);
			e23.linkNextEdge(e12op);
			e23.linkPreviousEdge(e2);
			e23.linkOppositeEdge(e23op);
			e23.setLeftFace(f2);
			
			e12op.setTargetVertex(e2.getStartVertex());
			e12op.linkNextEdge(e2);
			e12op.linkPreviousEdge(e23);
			e12op.linkOppositeEdge(e12);
			e12op.setLeftFace(f2);
			
			//triangle f3
			e3.linkNextEdge(e31);
			e3.setLeftFace(f3);
			
			e31.setTargetVertex(v);
			e31.linkNextEdge(e23op);
			e31.linkPreviousEdge(e3);
			e31.linkOppositeEdge(e31op);
			e31.setLeftFace(f3);
			
			e23op.setTargetVertex(e3.getStartVertex());
			e23op.linkNextEdge(e3);
			e23op.linkPreviousEdge(e31);
			e23op.linkOppositeEdge(e23);
			e23op.setLeftFace(f3);
			
			e12.setIsPositive(positive);
			e23.setIsPositive(positive);
			e31.setIsPositive(positive);
			
			newHeds.removeFace(f);
			
			oldFtoNewFs.put(f, newFaces);
		}
		return oldFtoNewFs;
	}
	
	
}
