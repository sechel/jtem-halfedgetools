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

package de.jtem.halfedgetools.symmetry.util;


import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;

public  class SymmetricHDSUtils
 {
	public static 
	<V extends SymmetricVertex<V,E,F>,
	E extends SymmetricEdge<V,E,F>,
	F extends SymmetricFace<V,E,F>
	> double[] getAverageFaceCurvatureVector(V v) {

		double[] normal = new double[3];
		double m = 0.0;
		List<F> faces = HalfEdgeUtils.facesIncidentWithVertex(v);
		for (F f : faces) {

			// calc n extrinsically
			List<E> boundary = HalfEdgeUtilsExtra.getBoundary(f);
			E e1 = boundary.get(0);
			E e2 = boundary.get(1);

			double[] v1 = e1.getDirection();
			double[] v2 = e2.getDirection();
			double[] n = Rn.crossProduct(null, v1, v2);

			// check if ok
			Rn.normalize(n, n);
			// n.cross(e1.getDirection(), e2.getDirection());
			// n.normalize();

			Rn.add(normal, normal, n);
			m += 1.0;

		}

		Rn.times(normal, 1/m, normal);
		return normal;
//		return Rn.normalize(null, normal);
	}
	
	// FIXME rewrite with getDirecton() and check for cones/boundary
	public static 
	<V extends SymmetricVertex<V,E,F>,
	E extends SymmetricEdge<V,E,F>,
	F extends SymmetricFace<V,E,F>
	> double getExtrinsicSurfaceArea(SymmetricHDS<V,E,F> heds) {
		double a = 0.0;
		
//		for (F f : heds.getFaces()) {
//		
//			E e1 = f.getBoundaryEdge();
//			E e2 = e1.getNextEdge();
//			
//			double[] n = new double[3];
//			
//			RVertex vv1 = intrinsicT.getVertex(e1.getStartVertex().getIndex());
//			RVertex vv2 = intrinsicT.getVertex(e1.getTargetVertex().getIndex());
//			RVertex vv3 = intrinsicT.getVertex(e2.getTargetVertex().getIndex());
//			
//			if (iConeVertices.contains(vv1) || iConeVertices.contains(vv2)
//					 || iConeVertices.contains(vv3)) {
//				// nothing because cone face
//			} else {
//			
//				double[] v1 = e1.getDirection();
//				double[] v2 = e1.getDirection();
//			
//				Rn.crossProduct(n, v1, v2);
//			
//				a += Rn.euclideanNorm(n);
//			}
//		}
//		System.err.println("area is: " + a*0.5);
//		return a * 0.5;
		
		return a;
	}
	
	// FIXME rewrite with getDirection()
	public static 
	<V extends SymmetricVertex<V,E,F>,
	E extends SymmetricEdge<V,E,F>,
	F extends SymmetricFace<V,E,F>
	> double getArea(F f) {
	double a = 0.0;

	double[] n = new double[3];
	E e1 = f.getBoundaryEdge();
	E e2 = e1.getNextEdge();
	V vv1 = e1.getStartVertex();
	V vv2 = e1.getTargetVertex();
	V vv3 = e2.getTargetVertex();
	double[] o = vv1.getEmbedding();
	double[] v1 = vv2.getEmbedding();
	double[] v2 = vv3.getEmbedding();

	double[] w1 = Rn.subtract(null, v1, o);
	double[] w2 = Rn.subtract(null, v2, o);

	Rn.crossProduct(n, w1, w2);

	a = Rn.euclideanNorm(n);
	
	return 0.5*a;
}

}
