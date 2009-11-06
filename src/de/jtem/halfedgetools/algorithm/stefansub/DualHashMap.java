package de.jtem.halfedgetools.algorithm.stefansub;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class DualHashMap<K1, K2, V> implements Cloneable{

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
	
	
	public static void main(String[] args) {
		System.err.println("DualHashMap Test");
		DualHashMap<Integer, Integer, Double> testMap = new DualHashMap<Integer, Integer, Double>();
		testMap.put(0, 1, 1.0);
		testMap.put(1, 0, 2.0);
		testMap.put(0, 2, 3.0);
		testMap.put(2, 0, 4.0);
		System.err.println(testMap.get(0, 1));
		System.err.println(testMap.get(1, 0));
		System.err.println(testMap.get(0, 2));
		System.err.println(testMap.get(2, 0));
		
		System.err.println("contains (1, 0): " + testMap.containsKey(0, 2));
		System.err.println("remove (1, 0): " + testMap.remove(0, 2));
		System.err.println("contains (1, 0): " + testMap.containsKey(0, 2));
		
		System.err.println(testMap.get(0, 1));
		System.err.println(testMap.get(1, 0));
		System.err.println(testMap.get(0, 2));
		System.err.println(testMap.get(2, 0));
		System.err.println(testMap.get(0));
	}
	
	
}
