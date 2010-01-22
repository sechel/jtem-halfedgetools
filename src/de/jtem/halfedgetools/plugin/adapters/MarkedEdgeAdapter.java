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

package de.jtem.halfedgetools.plugin.adapters;

import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;

public class MarkedEdgeAdapter <N extends JREdge<?,?,?>>implements ColorAdapter2Ifs<N> {
	
	private Set<N> selectedNodes;
	
	public MarkedEdgeAdapter() {

	}
	
	public void setSelectedNodes(Set<N> s) {
		selectedNodes = s;
	}
	
	@Override
	public double[] getColor(N n) {
		
		if(selectedNodes.contains(n)) {
			double[] one = new double[n.color.length];
			for(int i = 0; i < n.color.length; i++) {
				one[i] = 1.0;
			}
			return Rn.subtract(null, one, n.color);
		} else {
			return n.color;
		}
	}
	

	
	@Override
	public AdapterType getAdapterType() {
		return AdapterType.EDGE_ADAPTER;
	}



}
