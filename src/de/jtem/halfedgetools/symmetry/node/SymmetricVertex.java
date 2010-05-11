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

package de.jtem.halfedgetools.symmetry.node;

import java.util.Set;

import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Bundle;
import de.jtem.halfedgetools.adapter.Bundle.BundleType;
import de.jtem.halfedgetools.adapter.Bundle.DisplayType;
import de.jtem.halfedgetools.util.PathUtility;

public abstract class SymmetricVertex <
V extends SymmetricVertex<V, E, F>, 
E extends SymmetricEdge<V, E, F>, 
F extends SymmetricFace<V, E, F>
> extends Vertex<V, E, F> {
	
	private double[] position = null;
	public double[] normal;

	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.Label, name="boundary?")
	public boolean isBoundaryVertex() {
		for(Set<E> p : getIncomingEdge().getBoundaryCycleInfo().paths.keySet()) {
			if(PathUtility.getUnorderedVerticesOnPath(p).contains(this)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSymmetryVertex() {
		for(Set<E> p : getIncomingEdge().getSymmetryCycleInfo().paths.keySet()) {
			if(PathUtility.getUnorderedVerticesOnPath(p).contains(this)) {
				return true;
			}
		}
		return false;
	}
	
	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.Label, name="cone?")
	public boolean isConeVertex() {
		E in = getIncomingEdge();
		if(in.isConeEdge() && in.getPreviousEdge().getPreviousEdge().isConeEdge()) {
			return true;
		}
		return false;
	}
	
	@Bundle(dimension=3, type = BundleType.Affine, display=DisplayType.Geometry, name="coord")
	public double[] getEmbedding() {
		return position;
	}
	
	public void setEmbedding(double[] p) {
		position = p;
	}
	
}
