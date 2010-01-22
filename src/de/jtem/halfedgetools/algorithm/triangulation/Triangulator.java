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
