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

package de.jtem.halfedgetools.util.triangulationutilities;


import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;


/**
 * Basic triangulation consistency
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class ConsistencyCheck {

	// Don't instatiate.
	private ConsistencyCheck() {}
	
	public static boolean isTriangulation(HalfEdgeDataStructure<?, ?, ?> heds){
		for (Face<?, ?, ?> face : heds.getFaces()){
			if (HalfEdgeUtilsExtra.getBoundary(face).size() != 3){
//				message("Face " + face + " has not 3 edges (it has " + HalfEdgeUtilsExtra.getBoundary(face).size() + ")");
				return false;
			}
		}
		return true;
	}
	
//	public static  <
//		V extends Vertex<V, E, F> & HasXYZW,
//		E extends Edge<V, E, F> & HasLength,
//		F extends Face<V, E, F>
//	> boolean checkEdgeLengths(HalfEdgeDataStructure<V, E, F> graph){
//		for (E e : graph.getEdges()){
//			E e1 = e.getNextEdge();
//			E e2 = e.getPreviousEdge();
//			if (e.getLength() > e1.getLength() + e2.getLength())
//				return false;
//		}
//		return true;
//	}
	
	
	/**
	 * Checks if the given heds is a valid sphere 
	 * @param heds
	 * @return the check result
	 */
	public static boolean isSphere(HalfEdgeDataStructure<?, ?, ?> heds){
		//check for border
		for (Edge<?,?,?> edge : heds.getEdges()){
			if (edge.getLeftFace() == null || edge.getOppositeEdge() == null){
//				message("triangulation has border at edge " + edge);
				return false;
			}
		}
		//check if it has only triangles
		if (!isTriangulation(heds))
			return false;
		//check euler characteristics
		if (heds.numVertices() - heds.numEdges() / 2 + heds.numFaces() != 2){
//			message("triangulation is no sphere: V-E+F!=2");
			return false;
		}
		return true;
	}
	
	
	/**
	 * Checks if the given heds is a valid disk 
	 * @param heds
	 * @return the check result
	 */
//	public static boolean isDisk(HalfEdgeDataStructure<?, ?, ?> heds){
//		//check if it has only triangles
//		if (!isTriangulation(heds))
//			return false;
//		//check euler characteristics
//		if (heds.numVertices() - heds.numEdges() / 2 + heds.numFaces() != 1){
//			message("triangulation is no sphere: V-E+F!=1");
//			return false;
//		}
//		return true;
//	}
	
	
	
//	public static boolean getDebug() {
//		return DBGTracer.isActive();
//	}

	
//	private static void message(String m) {
//		if (ConsistencyCheck.getDebug()) {
//			System.err.println(ConsistencyCheck.class.getSimpleName() + ": " + m);
//		}
//	}
}
