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


import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;

public class Consistency {

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> boolean checkConsistency(HalfEdgeDataStructure<V, E, F> heds){
		for (V v : heds.getVertices()){
			List<E> cocycle1 = HalfEdgeUtilsExtra.getEdgeStar(v); 
			List<E> cocycle2 = HalfEdgeUtilsExtra.findEdgesWithTarget(v);
			for (E e : cocycle1)
				if (!cocycle2.contains(e) || cocycle1.size() != cocycle2.size()){
					System.err.println("Consistency: " + "e.getEdgeStar != he.findEdgesWithTarget");
					return false;
				}
			if (v.getIncomingEdge() != null && v.getIncomingEdge().getTargetVertex() != v){
				System.err.println("Consistency: " + "e.getEdgeStar != he.findEdgesWithTarget");
				return false;
			}
		}
		for (F f : heds.getFaces()){
			List<E> boundary1 = HalfEdgeUtilsExtra.getBoundary(f);
			List<E> boundary2 = HalfEdgeUtils.boundaryEdges(f);
			for (E e : boundary1)
				if (!boundary2.contains(e) || boundary1.size() != boundary2.size()){
					System.err.println("Consistency: " + "f.getBoundary != he.boundary()");
					return false;
				}	
		}
			
		return true;
	}
	
	
	
}

