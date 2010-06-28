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

import javax.vecmath.Point2d;
import javax.vecmath.Point4d;

import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.HasCurvature;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.HasRadius;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.HasXY;
import de.jtem.halfedgetools.algorithm.alexandrov.decorations.HasXYZW;

/**
 * The vertex class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMVertex extends Vertex<CPMVertex, CPMEdge, CPMFace> implements HasXYZW, HasXY, HasRadius, HasCurvature{

	private static final long 
		serialVersionUID = 1L;
	private Point4d
		pos4d = new Point4d();
	private Point2d
		pos2d = new Point2d();
	private Double
		radius = 100.0;
	
//	/*
//	 * Unfold project only
//	 */
//	private Boolean curvature = true;
//	

	@Override
	public Boolean hasCurvature() {
		return true;
	}
	
	@Override
	public void setCurvature(Boolean c) {
//		curvature = c;
	}
	
//	@Override
	protected CPMVertex getThis() {
		return this;
	}
	
	@Override
	public Point4d getXYZW() {
		return pos4d;
	}

	@Override
	public void setXYZW(Point4d p) {
		pos4d.set(p);
	}

	@Override
	public void setXY(Point2d p) {
		pos2d.set(p);
	}

	@Override
	public Point2d getXY() {
		return pos2d;
	}

	@Override
	public Double getRadius() {
		if (radius == null)
			radius = 100.0;
		return radius;
	}

	@Override
	public void setRadius(Double radius) {
		this.radius = radius;
	}

	@Override
	public String toString() {
		return super.toString() + " radius = " + radius;
	}

	public double[] getPosition() {
		return new double[]{pos4d.x, pos4d.y, pos4d.z, pos4d.w}; 
	}
	public void setPosition(double[] pos) {
		setXYZW(new Point4d(pos[0], pos[1], pos[2], pos[3]));
	}
	
}
