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

package de.jtem.halfedgetools.jreality.node;

import de.jtem.halfedge.Vertex;
/** this is a HalfEdgeDataStruckture component (a Vertex)
 *  which supports the typical JReality IndexedFaceSet 
 *  Attributes  
 *
 *  you can simply use the classes <code>My..Adapter</code> 
 *  as adapters for reading and writing.
 *       
 * @author gonska
 */

public abstract class JRVertex <
	V extends JRVertex<V, E, F>,
	E extends JREdge<V, E, F>,
	F extends JRFace<V, E, F>
> extends Vertex<V, E, F> {
	
	public double[] 
	    position = null,
	    normal = null,
	    color = null,
	    textCoord = null;
	public String 
		label = null;
	public double 
		radius = 1,
		pointSize = 1;
	
	@Override
	public void copyData(V v) {
		if (v.position != null) {
			position = v.position.clone();
		}
		if (v.normal != null) {
			normal = v.normal.clone();
		}
		if (v.color != null) {
			color = v.color.clone();
		}
		if (v.normal != null) {
			textCoord = v.textCoord.clone();
		}
		this.label = v.label;
		this.radius = v.radius;
		this.pointSize = v.pointSize;
	}
	
}