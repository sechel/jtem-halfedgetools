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

package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardTextCoordAdapter implements TextCoordsAdapter2Ifs<Node<?, ?, ?>> ,TextCoordsAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	public AdapterType getAdapterType() {
		return typ;
	}
	public StandardTextCoordAdapter(AdapterType typ) {
		this.typ=typ;
	}
	/** This adapter can write and read TextureCoordinates
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	@SuppressWarnings("unchecked")
	public double[] getTextCoordinate(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			if(((JREdge)node).textCoord==null)
				return new double[]{0,0,0};
			return((JREdge)node).textCoord;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			if(((JRFace)node).textCoord==null)
				return new double[]{0,0,0};
			return((JRFace)node).textCoord;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((JRVertex)node).textCoord==null)
				return new double[]{0,0,0};
			return((JRVertex)node).textCoord;
		}
		return new double[]{0,0,0};
	}
	@SuppressWarnings("unchecked")
	public void setTextCoordinate(Node<?, ?, ?> node, double[] textCoords) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).textCoord=textCoords;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).textCoord=textCoords;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).textCoord=textCoords;
		}
	}
	
}
	