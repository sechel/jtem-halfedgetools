package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.Map;
import java.util.TreeMap;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;

public class DualGraphSubdivision {

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void subdivide(
		HDS graph, 
		HDS r,
		AdapterSet a,
		Map<F, V> faceVertexMap,
		Map<E, E> edgeEdgeMap,
		Map<V, F> vertexFaceMap
	) {
		if (faceVertexMap == null) { 
			faceVertexMap = new TreeMap<>();
		}
		if (edgeEdgeMap == null) { 
			edgeEdgeMap = new TreeMap<>();
		}
		if (vertexFaceMap == null) { 
			vertexFaceMap = new TreeMap<>();
		}
		for (F f : graph.getFaces()) {
			V v = r.addNewVertex();
			a.set(Position.class, v, a.getD(Position.class, f));
			faceVertexMap.put(f, v);
		}
		for (E e : graph.getEdges()) {
			if (HalfEdgeUtils.isBoundaryEdge(e)) { continue; }
			E ee = r.addNewEdge();
			edgeEdgeMap.put(e, ee);
		}
		for (V v : graph.getVertices()) {
			if (HalfEdgeUtils.isBoundaryVertex(v)) { continue; }
			F f = r.addNewFace();
			vertexFaceMap.put(v, f);
		}
		
		// linkage
		for (E e : graph.getEdges()) {
			if (HalfEdgeUtils.isBoundaryEdge(e)) continue;
			E ee = edgeEdgeMap.get(e);
			E eeOpp = edgeEdgeMap.get(e.getOppositeEdge());
			ee.linkOppositeEdge(eeOpp);
			ee.setTargetVertex(faceVertexMap.get(e.getLeftFace()));
			ee.setLeftFace(vertexFaceMap.get(e.getStartVertex()));
			E ePrev = e.getPreviousEdge();
			while (HalfEdgeUtils.isBoundaryEdge(ePrev)) {
				// find next on boundary
				ePrev = ePrev.getPreviousEdge();
			}
			ee.linkNextEdge(edgeEdgeMap.get(ePrev.getOppositeEdge()));
		}
	}
	
}
