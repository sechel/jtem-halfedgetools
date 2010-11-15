package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter4d;

public class CatmullClarkLinear {

	public <
		V extends Vertex<V, E, F> ,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> HDS execute(
		HDS graph,
		HDS quad, 
		Map<V, V> vertexVertexMap, 
		Map<E, V> edgeVertexMap, 
		Map<F, V> faceVertexMap,
		TypedAdapterSet<double[]> a
	) {
		// vertices
		a.setParameter("alpha", 0.5);
		a.setParameter("ignore", true);
		for (V v : graph.getVertices()){
			V newVertex = quad.addNewVertex();
			a.set(Position.class, newVertex, a.get(BaryCenter4d.class, v));
			vertexVertexMap.put(v, newVertex);
		}
		for (E e : graph.getPositiveEdges()){
			V newVertex = quad.addNewVertex();
			a.set(Position.class, newVertex, a.get(BaryCenter4d.class, e));
			edgeVertexMap.put(e, newVertex);
			edgeVertexMap.put(e.getOppositeEdge(), newVertex);
		}
		for (F f : graph.getFaces()){
			V newVertex = quad.addNewVertex();
			a.set(Position.class, newVertex, a.get(BaryCenter4d.class, f));
			faceVertexMap.put(f, newVertex);
		}
		
		int numLinks = 0;
		// edges vertex connections
		DualHashMap<V, V, E> quadEdgeMap = new DualHashMap<V, V, E>();
		for (E e : graph.getPositiveEdges()){
			V v = edgeVertexMap.get(e);
			V v1 = vertexVertexMap.get(e.getTargetVertex());
			V v3 = vertexVertexMap.get(e.getStartVertex());
			V v4 = faceVertexMap.get(e.getLeftFace());
			V v2 = faceVertexMap.get(e.getRightFace());
			
			E e1 = quad.addNewEdge();
			E e2 = quad.addNewEdge();
			E e3 = quad.addNewEdge();
			E e4 = quad.addNewEdge();
			E e5 = quad.addNewEdge();
			E e6 = quad.addNewEdge();
			E e7 = quad.addNewEdge();
			E e8 = quad.addNewEdge();
			
			e1.setTargetVertex(v1);
			e2.setTargetVertex(v);
			e3.setTargetVertex(v2);
			e4.setTargetVertex(v);
			e5.setTargetVertex(v3);
			e6.setTargetVertex(v);
			e7.setTargetVertex(v4);
			e8.setTargetVertex(v);
			
			e2.linkNextEdge(e3);
			e4.linkNextEdge(e5);
			e6.linkNextEdge(e7);
			e8.linkNextEdge(e1);
			numLinks += 4;
		
			e1.linkOppositeEdge(e2);
			e3.linkOppositeEdge(e4);
			e5.linkOppositeEdge(e6);
			e7.linkOppositeEdge(e8);
			
			quadEdgeMap.put(v, v1, e1);
			quadEdgeMap.put(v1, v, e2);
			quadEdgeMap.put(v, v2, e3);
			quadEdgeMap.put(v2, v, e4);
			quadEdgeMap.put(v, v3, e5);
			quadEdgeMap.put(v3, v, e6);
			quadEdgeMap.put(v, v4, e7);
			quadEdgeMap.put(v4, v, e8);
		}
		
		// face vertex connections
		HashSet<F> readyFaces = new HashSet<F>();
		for (E bEdge : graph.getEdges()){
			F f = bEdge.getLeftFace();
			if (readyFaces.contains(f))
				continue;
			V v = faceVertexMap.get(f);
			V bVertex = edgeVertexMap.get(bEdge);
			E lastEdge = quadEdgeMap.get(bVertex, v);
			E actEdge = bEdge;
			do {
				actEdge = actEdge.getNextEdge();
				V vertex = edgeVertexMap.get(actEdge);
				E edge =  quadEdgeMap.get(vertex, v);
				edge.linkNextEdge(lastEdge.getOppositeEdge());
				numLinks++;
				lastEdge = edge;
			} while (actEdge != bEdge);
			readyFaces.add(f);
		}
		// vertex vertex connections
		for (V v : graph.getVertices()){
			V vertex = vertexVertexMap.get(v);
			Collection<E> vStar = quadEdgeMap.get(vertex);
			for (E edge : vStar){
				E linkEdge = edge.getNextEdge().getNextEdge().getNextEdge(); 
				linkEdge.linkNextEdge(edge);
				numLinks++;
			}
		}
		HalfEdgeUtils.fillAllHoles(quad);
		return quad;
	}
	
	
	
	public static class DualHashMap<K1, K2, V> implements Cloneable{

		private HashMap<K1, HashMap<K2, V>>
			map = new HashMap<K1, HashMap<K2,V>>();
		
		public void clear(){
			map.clear();
		}
		
		public boolean containsKey(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return false;
			else
				return vMap.get(key2) != null;
		}
		
		public boolean containsValue(V value){
			for (K1 key : map.keySet()){
				HashMap<K2,V> vMap = map.get(key);
				if (vMap.containsValue(value))
					return true;
			}
			return false;
		}
		
		public boolean isEmpty(){
			return map.isEmpty();
		}
		
		public V put(K1 key1, K2 key2, V value){
			V previous = get(key1, key2);
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null){
				vMap = new HashMap<K2, V>();
				map.put(key1, vMap);
			}
			vMap.put(key2, value);
			return previous;
		}
		
		
		public V get(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return null;
			else
				return vMap.get(key2);
		}

		public Collection<V> get(K1 key1){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return Collections.emptyList();
			else
				return vMap.values();
		}
		
		
		public V remove(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return null;
			else {
				V result = vMap.remove(key2);
				if (vMap.isEmpty())
					map.remove(key1);
				return result;
			}
		}
		
	}
	
}
