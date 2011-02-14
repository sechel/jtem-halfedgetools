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

package de.jtem.halfedgetools.algorithm.alexandrov.node;

import de.jtem.halfedge.Edge;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.HasAngle;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.IsBoundary;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.IsHidable;
import de.jtem.halfedgetools.algorithm.alexandrov.delaunay.Delaunay;
import de.jtem.halfedgetools.algorithm.alexandrov.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.alexandrov.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.util.TriangulationException;


/**
 * The edge class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMEdge extends Edge<CPMVertex, CPMEdge, CPMFace> implements HasAngle, IsFlippable, IsBoundary, IsHidable, HasLength{

	private static final long 
		serialVersionUID = 1L;
	private Double
		length = 1.0,
		gamma = 0.0;
	private int
		flipCount = 0;
	private Boolean
		border = false;
	private boolean
		isHidden = false;

	
	protected CPMEdge getThis() {
		return this;
	}

	@Override
	public Double getLength() {
		return length;
	}

	@Override
	public void setLength(Double length) {
		this.length = length;
	}
	
	@Override
	public String toString() {
		return super.toString() + " length = " + length + "S: " + getStartVertex() + " T: " + getTargetVertex(); 
	}

	@Override
	public Double getAngle() {
		return gamma;
	}

	@Override
	public void setAngle(Double angle) {
		gamma = angle;
	}


	@Override
	public void flip() throws TriangulationException{
//		if (!ConsistencyCheck.isValidSurface(getHalfEdgeDataStructure()))
//			System.err.println("No valid surface before flip()");
//		if (!Consistency.checkConsistency(getHalfEdgeDataStructure()))
//			System.err.println("surface corrupted before flip()");
		CPMFace leftFace = getLeftFace();
		CPMFace rightFace = getRightFace();
		if (leftFace == rightFace) {
			System.err.println("leftFace == rightFace");
			return;
		}
			
		CPMEdge a1 = getOppositeEdge().getNextEdge();
		CPMEdge a2 = a1.getNextEdge();
		CPMEdge b1 = getNextEdge();
		CPMEdge b2 = b1.getNextEdge();
		
		CPMVertex v1 = getStartVertex();
		CPMVertex v2 = getTargetVertex();
		CPMVertex v3 = a1.getTargetVertex();
		CPMVertex v4 = b1.getTargetVertex();

		//new length for edge
		Double la2 = a2.getLength();
		Double lb1 = b1.getLength();
		Double alpha = Delaunay.getAngle(this) + Delaunay.getAngle(a2);
		Double newLength = Math.sqrt(la2*la2 + lb1*lb1 - 2*lb1*la2*StrictMath.cos(alpha));
		setLength(newLength);
		getOppositeEdge().setLength(newLength);
		
		//new connections
		linkNextEdge(a2);
		linkPreviousEdge(b1);
		getOppositeEdge().linkNextEdge(b2);
		getOppositeEdge().linkPreviousEdge(a1);
		setTargetVertex(v3);
		getOppositeEdge().setTargetVertex(v4);
		
		a2.linkNextEdge(b1);
		b2.linkNextEdge(a1);
		
		//set faces
		b2.setLeftFace(rightFace);
		a2.setLeftFace(leftFace);
		
		//fix vertex edge connections
		//:TODO check constantly
		b2.setTargetVertex(v1);
		a2.setTargetVertex(v2);
		a1.setTargetVertex(v3);
		b1.setTargetVertex(v4);
//		v1.setConnectedEdge(b2);
//		v2.setConnectedEdge(a2);
//		v3.setConnectedEdge(a1);
//		v4.setConnectedEdge(b1);
		flipCount++;
		
//		if (!ConsistencyCheck.isValidSurface(getHalfEdgeDataStructure()))
//			System.err.println("No valid surface after flip()");
//		if (!Consistency.checkConsistency(getHalfEdgeDataStructure()))
//			System.err.println("surface corrupted after flip()");
	}

	@Override
	public int getFlipCount() {
		return flipCount;
	}

	@Override
	public void resetFlipCount() {
		flipCount = 0;
	}

	@Override
	public Boolean isBoundary() {
		return border;
	}

	@Override
	public void setBoundary(Boolean border) {
		this.border = border;
	}

	@Override
	public Boolean isHidden() {
		return isHidden;
	}
	
	@Override
	public void setHidden(Boolean hide){
		isHidden = hide;
	}
	


}
