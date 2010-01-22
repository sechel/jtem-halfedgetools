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

package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardRelRadiusAdapter implements RelRadiusAdapter2Ifs<Node<?, ?, ?>>, RelRadiusAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	public AdapterType getAdapterType() {
		return typ;
	}
	/** This adapter can write and read the RelativeRadii  
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public StandardRelRadiusAdapter(AdapterType typ) {
		this.typ=typ;
	}
	@SuppressWarnings("unchecked")
	public double getReelRadius(Node<?, ?, ?> node){
		if(typ==AdapterType.EDGE_ADAPTER){
			return((JREdge)node).radius;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			return((JRFace)node).radius;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			return((JRVertex)node).radius;
		}
		return 0;
	}
	@SuppressWarnings("unchecked")
	public void setRelRadius(Node<?, ?, ?> node, double relRadius) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).radius=relRadius;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).radius=relRadius;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).radius=relRadius;
		}
	}
}