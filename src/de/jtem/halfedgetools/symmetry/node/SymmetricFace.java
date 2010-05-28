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

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.util.HalfEdgeUtils;

public abstract class SymmetricFace < 
V extends SymmetricVertex<V, E, F>, 
E extends SymmetricEdge<V, E, F> , 
F extends SymmetricFace<V, E, F>
> extends Face<V, E, F> {

	
	protected DiscreteGroupElement groupSymmetry = null;
	
	public DiscreteGroupElement getSymmetry() {
		return groupSymmetry;	
	}
	
	public void setSymmetry(DiscreteGroupElement s) {
		groupSymmetry = s;
	}
	
	public boolean hasSymmetry() {
		return groupSymmetry != null;
	}

	// TODO fix for n>3
	public double[] getEmbeddingOnBoundary(double t, boolean ignore) {
		F f = getBoundaryEdge().getLeftFace(); // should not be necessary
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		E e = boundary.get(0);
		
		int n = boundary.size();
		//if (n > 3) return new double[] {0,0,0,0};
		
		if(e.isRightIncomingOfSymmetryCycle() == null)
			e = e.getNextEdge();
		if(e.isRightIncomingOfSymmetryCycle() == null)
			e = e.getNextEdge();
		
		double[][] coords = new double[n][];
		

		if (n==3){
			if(ignore == false){
				coords[0] = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
				coords[1] = Rn.add(null, coords[0], e.getNextEdge().getDirection());
				coords[2] = Rn.add(null, coords[1], e.getPreviousEdge().getDirection());
			} else {
				e = e.getOppositeEdge();
				coords[0] = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
				coords[1] = Rn.add(null, coords[0], e.getNextEdge().getDirection());
				coords[2] = Rn.add(null, coords[1], e.getPreviousEdge().getDirection());
			}
		} else {
		// TODO fix it, seems already buggy
			if(ignore == false){
				for (int i=0;i<n;i++){
					if (i==0){
						coords[i] = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
					} else {
						coords[i] = Rn.add(null, coords[i-1], e.getDirection());
					}
					e= e.getNextEdge();					
				}
			} else {
				e = e.getOppositeEdge();
				for (int i=0;i<n;i++){
					if (i==0){
						coords[i] = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
					} else {
						coords[i] = Rn.add(null, coords[i-1], e.getDirection());
					}
					e= e.getNextEdge();					
				}
			}
		}

		
		int sel = ((int)Math.floor(t)) % n;
		
		double rest = t - Math.floor(t);
		return Rn.linearCombination(null, rest, coords[sel%n], 1-rest, coords[(sel+1)%n]);
	}
}
