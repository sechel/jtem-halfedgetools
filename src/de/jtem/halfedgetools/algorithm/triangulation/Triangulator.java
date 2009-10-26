package de.jtem.halfedgetools.algorithm.triangulation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;

public class Triangulator 
<
	V extends Vertex<V, E,F>,
	E extends Edge<V, E,F>,
	F extends Face<V, E,F>
> 
{

//	public <
//	HDS extends HalfEdgeDataStructure<V, E, F>
//	> void triangulate(
//			HDS hds
//	) {
//		triangulate(hds);
//	}
	
	public
//	<
//		HDS extends HalfEdgeDataStructure<V, E, F>
//	> 
	List<E> triangulate(
		HalfEdgeDataStructure<V,E,F> hds
	) {
		List<E> newEdges = new ArrayList<E>();
		List<F> fList = new LinkedList<F>(hds.getFaces());
		for (F f : fList) {
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			if (b.size() == 3) {
				continue;
			} 
			if (b.size() < 3) {
				throw new RuntimeException("Face boundary with less than 3 edges not allowed in triangulate()");
			}
			E last = f.getBoundaryEdge();
			E first = last.getPreviousEdge();
			E opp = last.getNextEdge();
			V v0 = last.getStartVertex();
			while (opp.getNextEdge() != first) {
				E nextOpp = opp.getNextEdge();
				E newEdge = hds.addNewEdge();
				E newEdgeOpp = hds.addNewEdge();
				newEdge.linkOppositeEdge(newEdgeOpp);
				newEdge.linkNextEdge(last);
				newEdgeOpp.linkNextEdge(nextOpp);
				newEdgeOpp.linkPreviousEdge(first);
				opp.linkNextEdge(newEdge);

				V v2 = opp.getTargetVertex();
				newEdge.setTargetVertex(v0);
				newEdgeOpp.setTargetVertex(v2);
				
				newEdges.add(newEdge);
				newEdges.add(newEdgeOpp);
				
				F newFace = hds.addNewFace();
				last.setLeftFace(newFace);
				opp.setLeftFace(newFace);
				newEdge.setLeftFace(newFace);
				newEdgeOpp.setLeftFace(f);
				
				opp = nextOpp;
				last = newEdgeOpp;
			}
		}
		return newEdges;
	}
	
}
