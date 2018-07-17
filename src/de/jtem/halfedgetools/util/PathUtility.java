/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
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

package de.jtem.halfedgetools.util;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;

public class PathUtility {

	
	/**
	 * Returns the vertices on a given path
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param path
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Set<V> getVerticesOnPath(Set<E> path) {
		Set<V> result = new TreeSet<V>(new NodeComparator<V>());
		for (E e : path) {
			result.add(e.getStartVertex());
			result.add(e.getTargetVertex());
		}
		return result;
	}
	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Set<V> getUnorderedVerticesOnPath(Set<E> path) {
		Set<V> result = new HashSet<V>();
		for (E e : path) {
			result.add(e.getStartVertex());
			result.add(e.getTargetVertex());
		}
		return result;
	}
	
	
	/**
	 * Returns a path which includes the opposite edges of each edge
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param path
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Set<E> getFullPath(Set<E> path) {
		Set<E> result = new TreeSet<E>(new NodeComparator<E>());
		for (E e : path) {
			result.add(e);
			result.add(e.getOppositeEdge());
		}
		return result;
	}
	
	
	/**
	 * Checks whether a given cycle is a simple path 
	 * i.e. it has no self-intersections
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param cycle a cycle of consistently oriented half-edges
	 * @return
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean isCycleSimple(Set<E> cycle) {
		Set<V> vSet = new HashSet<V>();
		for (E e : cycle) {
			V v = e.getTargetVertex();
			if (vSet.contains(v)) {
				return false;
			}
			vSet.add(v);
		}
		return true;
	}
	

}
