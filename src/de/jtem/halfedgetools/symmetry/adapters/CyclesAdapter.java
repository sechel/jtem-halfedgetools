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

package de.jtem.halfedgetools.symmetry.adapters;

import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.EDGE_ADAPTER;

import java.util.Set;

import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.MarkedEdgesAdapter;

public class CyclesAdapter<V extends SymmetricVertex<V,E,F>,
E extends SymmetricEdge<V,E,F>,
F extends SymmetricFace<V,E,F>,
HDS extends SymmetricHDS<V,E,F>> extends MarkedEdgesAdapter<V, E, F> {
	
	@Override
	public double[] getColor(E e) {
		
		setContext(e.getSymmetryCycleInfo());
		
		for (Set<E> path : context.paths.keySet()) {
			Set<E> coPath = context.pathCutMap.get(path);
			if (path.contains(e) || path.contains(e.getOppositeEdge())) {
				return pathColors.get(path);
			}
			if(coPath != null) {
				if (coPath.contains(e) || coPath.contains(e.getOppositeEdge())) {
					return pathColors.get(path);
				}
			}
		}
		if(context.isRightIncomingOnCycle((E)e)!=null || context.isRightIncomingOnCycle((E)e.getOppositeEdge()) != null) {
			return new double[] {1,1,1,0};
		}
		return normalColor;
	}
	
	@Override
	public double getReelRadius(E e) {
		setContext(e.getSymmetryCycleInfo());
		for (Set<E> path : context.paths.keySet()) {
			Set<E> coPath = context.pathCutMap.get(path);
			if (path.contains(e) || path.contains(e.getOppositeEdge())) {
				return 2.0;
			}
			if(coPath != null) {
				if (coPath.contains(e) || coPath.contains(e.getOppositeEdge())) {
					return 2.0;
				}
			}
		}
		return 0.5;
	}
	
	
	@Override
	public AdapterType getAdapterType() {
		return EDGE_ADAPTER;
	}
}
