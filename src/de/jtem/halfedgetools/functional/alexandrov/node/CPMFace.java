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

package de.jtem.halfedgetools.functional.alexandrov.node;


import javax.vecmath.Color3f;
import javax.vecmath.Point4d;

import de.jtem.halfedge.Face;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasColor;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;


/**
 * The face class for the alexandrov project
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class CPMFace extends Face<CPMVertex, CPMEdge, CPMFace> implements HasXYZW, HasColor{

	private static final long 
		serialVersionUID = 1L;
	private Point4d
		pos = new Point4d();
	private Color3f
		color = new Color3f();

//	@Override
	protected CPMFace getThis() {
		return this;
	}
	
	public Point4d getXYZW() {
		return pos;
	}
	
	public void setXYZW(Point4d p) {
		pos.set(p);
	}

	public void setColor(Color3f c) {
		color.set(c);
	}
	
	public Color3f getColor() {
		return color;
	}
	
}
