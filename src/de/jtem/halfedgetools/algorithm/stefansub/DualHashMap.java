/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2009, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

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
