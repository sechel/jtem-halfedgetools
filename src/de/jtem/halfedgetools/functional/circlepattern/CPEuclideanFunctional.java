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

package de.jtem.halfedgetools.functional.circlepattern;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.cosh;
import static java.lang.Math.sin;

import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.halfedgetools.functional.circlepattern.CPAdapters.Phi;
import de.jtem.halfedgetools.functional.circlepattern.CPAdapters.Theta;


public class CPEuclideanFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private Theta<E> 
		theta = null; 
	private Phi<F>
		phi = null;
	
	
	public CPEuclideanFunctional(Theta<E> theta, Phi<F> phi) {
		this.theta = theta;
		this.phi = phi;
	}
		
		
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V,E,F>
	> void evaluate(
		// input	
			HDS hds, 
			DomainValue x,
		// output	
			Energy E, 
			Gradient G, 
			Hessian H
	) {
		if (G != null || E != null) {
			evaluateEnergyAndGradient(hds, x, E, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
	};
	
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V,E,F>
	> int getDimension(HDS hds) {
		return hds.numFaces();
	};
	
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V,E,F>
	> int[][] getNonZeroPattern(HDS hds) {
		int n = getDimension(hds);
		int[][] nz = new int[n][];
		for (F f : hds.getFaces()) {
			int i = f.getIndex();
			List<Integer> nzList = new LinkedList<Integer>();
			nzList.add(i);
			for (E e : HalfEdgeUtils.boundaryEdges(f)) {
				F neighbourFace = e.getRightFace();
				if (neighbourFace != null) {
					nzList.add(neighbourFace.getIndex());	
				}
			}
			nz[i] = new int[nzList.size()];
			int j = 0;
			for (Integer index : nzList) {
				nz[i][j++] = index;
			}
		}
		return nz;
	}
	

	
	private void evaluateHessian(
		// input	
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		// output
			Hessian H
	) {
		H.setZero();
	    for (E edge : hds.getPositiveEdges()) {
			if (!HalfEdgeUtils.isInteriorEdge(edge)) {
				continue;
			}
			F jFace = edge.getLeftFace();
			F kFace = edge.getOppositeEdge().getLeftFace();
			Integer j = jFace.getIndex();
			Integer k = kFace.getIndex();
			
			double rhok = x.get(kFace.getIndex());
			double rhoj = x.get(jFace.getIndex());
			if (k == 0) {
				rhok = 0.0;
			}
			if (j == 0) {
				rhoj = 0.0;
			}
			Double th = theta.getTheta(edge);
			Double hjk = sin(th) / (cosh(rhok - rhoj) - cos(th));

			if (j != 0) {
				H.add(j, j, hjk);
			}
			if (k != 0) {
				H.add(k, k, hjk);
			}
			if (j != 0 && k != 0) {
				H.add(j, k, -hjk);
				H.add(k, j, -hjk);
			}
		}
	}
	
	
	
	private void evaluateEnergyAndGradient(
		// input	
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		// output
			Energy E,
			Gradient G
	) {
		if (E != null) {
			E.setZero();
		}
		int n = getDimension(hds);
		double[] grad = new double[n];
		for (F face : hds.getFaces()) {
			if (face.getIndex() == 0) {
				continue;
			}
			double facePhi = phi.getPhi(face);
			if (E != null) {
				E.add(facePhi * x.get(face.getIndex()));
			}
			if (G != null) {
				grad[face.getIndex()] = facePhi;
			}
		}

		for (E edge : hds.getEdges()) {
			F leftFace = edge.getLeftFace();
			if (leftFace == null) {
				continue;
			}
			Double th = theta.getTheta(edge); 
			Double thStar = PI - th;
			Double leftRho = x.get(leftFace.getIndex());
			if (leftFace.getIndex() == 0) {
				leftRho = 0.0;
			}
			F rightFace = edge.getOppositeEdge().getLeftFace();
			if (rightFace == null) {
				// boundary face
				if (E != null) {
					E.add(-2 * thStar * leftRho);
				}
				if (G != null) {
					grad[leftFace.getIndex()] -= 2 * thStar;
				}
			} else {
				// interior face
				Double rightRho = x.get(rightFace.getIndex());
				if (rightFace.getIndex() == 0) {
					rightRho = 0.0;
				}
				Double diffRho = rightRho - leftRho;
				Double p = p(thStar, diffRho);
				if (E != null) {
					E.add(0.5 * p * diffRho);
					E.add(Clausen.clausen(thStar + p));
					E.add(-thStar * leftRho);
				}
				if (G != null) {
					grad[leftFace.getIndex()] -= p + thStar;
				}
			}
		}
		if (G != null) {
			for (int i = 0; i < n; i++) {
				G.set(i, grad[i]);
			}
			G.set(0, 0.0); // skip the first variable
		}
	}

	
    private Double p(Double thStar, Double diffRho) {
        Double exp = Math.exp(diffRho);
        Double tanhDiffRhoHalf = (exp - 1.0) / (exp + 1.0);
        return  2.0 * Math.atan(Math.tan(0.5 * thStar) * tanhDiffRhoHalf);
    }
    
    @Override
    public boolean hasHessian() {
    	return true;
    }
    
	
}
