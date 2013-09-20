package de.jtem.halfedgetools.algorithm.subdivision;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;

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
		CatmullClark cc = new CatmullClark();
		cc.subdivide(graph,quad,a,null,false,false,false,true,faceVertexMap,edgeVertexMap,vertexVertexMap);
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
