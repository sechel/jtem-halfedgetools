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

package de.jtem.halfedgetools.functional.planarfaces;

import java.util.List;

import de.jreality.math.Matrix;
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
import de.jtem.halfedgetools.functional.planarfaces.PlanarFacesAdapters.VolumeWeight;

public class VolumeFuctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private VolumeWeight<F>
		weight = null;
	private double
		scale = 1.0,
		alpha = 0.0;
	
	public VolumeFuctional(VolumeWeight<F> weight, double scale, double alpha) {
		this.weight = weight;
		this.scale = scale;
		this.alpha = alpha;
	}
	
	
	@Override
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void evaluate(
		HDS hds,
		DomainValue x, 
		Energy E, 
		Gradient G, 
		Hessian H
	) {
		E.setZero();
		for (F f : hds.getFaces()) { // flatness
			E.add(getDet2(x, f) * weight.getWeight(f) * weight.getWeight(f) * scale);
		}
		if (G != null) {
			evaluateGradient(hds, x, G, null, alpha);
		}
//		if (alpha != 0.0) {
//			for (V v : hds.getVertices()) { // minimal movement
//				if (HalfEdgeUtils.isBoundaryVertex(v)) continue;
//				int i = v.getIndex();
//				double[] startPoint = {startData[i * 3 + 0], startData[i * 3 + 1], startData[i * 3 + 2], 1.0};
//				result += alpha * Pn.distanceBetween(startPoint, v.getPosition(), Pn.EUCLIDEAN);
//			}
//		}
	}

	
	public double getDet2(DomainValue x, F f){
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		if (boundary.size() != 4) return 0.0;
		de.jreality.math.Matrix mat = new de.jreality.math.Matrix();
		int i = 0;
		for (E edge : boundary){
			double[] vertexPos = getPosition(x, edge.getTargetVertex());
			mat.setColumn(i, vertexPos);
			i++;
		}
		double det = mat.getDeterminant();
		return det * det;
	}
	
	
	private double[] getPosition(DomainValue x, V v) {
		double[] pos = new double[4];
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
		pos[3] = 1.0;
		return pos;
	}
	
	

	public void evaluateGradient(
		HalfEdgeDataStructure<V, E, F> data, 
		DomainValue x, 
		Gradient G, 
		double[] startData, 
		double alpha
	){
		G.setZero();
		for (F f : data.getFaces()){ // flatness
			List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
			if (boundary.size() != 4)
				continue;
			Matrix mat = new Matrix();
			int i = 0;
			for (E edge : boundary){
				double[] vertexPos = getPosition(x, edge.getTargetVertex());
				mat.setColumn(i, vertexPos);
				i++;
			}
			int j = 0;
			for (E edge : boundary){
				int vertexIndex = edge.getTargetVertex().getIndex();
				double weight2 = weight.getWeight(f) * weight.getWeight(f);
				G.add(vertexIndex * 3 + 0, differentiateDet2(mat, 0, j) * weight2 * scale);
				G.add(vertexIndex * 3 + 1, differentiateDet2(mat, 1, j) * weight2 * scale);
				G.add(vertexIndex * 3 + 2, differentiateDet2(mat, 2, j) * weight2 * scale);
				j++;
			}
		}
//		if (alpha != 0.0) {
//			for (V v : data.getVertices()) { // minimal movement
//				if (HalfEdgeUtils.isBoundaryVertex(v)) continue;
//				int i = v.getIndex();
//				double[] pos = v.getPosition();
//				double[] startPoint = {startData[i * 3 + 0], startData[i * 3 + 1], startData[i * 3 + 2], 1.0};
//				double factor = Pn.distanceBetween(startPoint, v.getPosition(), Pn.EUCLIDEAN);
//				if (factor > 1E-4) {
//					result[i * 3 + 0] += alpha * (pos[0] - startPoint[0]) / factor;
//					result[i * 3 + 1] += alpha * (pos[1] - startPoint[1]) / factor;
//					result[i * 3 + 2] += alpha * (pos[2] - startPoint[2]) / factor;
//				}
//			}
//		}
	}
	
	
	
	private static double differentiateDet2(Matrix A, int i, int j){
		double a_11 = A.getEntry(0, 0);
		double a_12 = A.getEntry(0, 1);
		double a_13 = A.getEntry(0, 2);
		double a_14 = A.getEntry(0, 3);
		double a_21 = A.getEntry(1, 0);
		double a_22 = A.getEntry(1, 1);
		double a_23 = A.getEntry(1, 2);
		double a_24 = A.getEntry(1, 3);
		double a_31 = A.getEntry(2, 0);
		double a_32 = A.getEntry(2, 1);
		double a_33 = A.getEntry(2, 2);
		double a_34 = A.getEntry(2, 3);
		double detA = (a_11 * a_22 * a_33 - a_11 * a_22 * a_34 - a_11 * a_32 * a_23 + a_11 * a_32 * a_24 + a_11 * a_23 * a_34 - a_11 * a_24 * a_33
			     - a_21 * a_12 * a_33 + a_21 * a_12 * a_34 + a_21 * a_32 * a_13 - a_21 * a_32 * a_14 - a_21 * a_13 * a_34 + a_21 * a_14 * a_33
			     + a_31 * a_12 * a_23 - a_31 * a_12 * a_24 - a_31 * a_22 * a_13 + a_31 * a_22 * a_14 + a_31 * a_13 * a_24 - a_31 * a_14 * a_23
			     - a_12 * a_23 * a_34 + a_12 * a_24 * a_33 + a_22 * a_13 * a_34 - a_22 * a_14 * a_33 - a_32 * a_13 * a_24 + a_32 * a_14 * a_23);
		if (i == 0 && j == 0){
			return 2 * detA * (a_22 * a_33 - a_22 * a_34 - a_32 * a_23 + a_32 * a_24 + a_23 * a_34 - a_24 * a_33);
		} else if (i == 0 && j == 1){
			return 2 * detA * (-a_21 * a_33 + a_21 * a_34 + a_31 * a_23 - a_31 * a_24 - a_23 * a_34 + a_24 * a_33);
		} else if (i == 0 && j == 2){
			return 2 * detA * (a_21 * a_32 - a_21 * a_34 - a_31 * a_22 + a_31 * a_24 + a_22 * a_34 - a_32 * a_24);
		} else if (i == 0 && j == 3){
			return 2 * detA * (-a_21 * a_32 + a_21 * a_33 + a_31 * a_22 - a_31 * a_23 - a_22 * a_33 + a_32 * a_23);
		} else if (i == 1 && j == 0){
			return 2 * detA * (-a_12 * a_33 + a_12 * a_34 + a_32 * a_13 - a_32 * a_14 - a_13 * a_34 + a_14 * a_33);
		} else if (i == 1 && j == 1){
			return 2 * detA * (a_11 * a_33 - a_11 * a_34 - a_31 * a_13 + a_31 * a_14 + a_13 * a_34 - a_14 * a_33);
		} else if (i == 1 && j == 2){
			return 2 * detA * (-a_11 * a_32 + a_11 * a_34 + a_31 * a_12 - a_31 * a_14 - a_12 * a_34 + a_32 * a_14);
		} else if (i == 1 && j == 3){
			return 2 * detA * (a_11 * a_32 - a_11 * a_33 - a_31 * a_12 + a_31 * a_13 + a_12 * a_33 - a_32 * a_13);
		} else if (i == 2 && j == 0){
			return 2 * detA * (a_12 * a_23 - a_12 * a_24 - a_22 * a_13 + a_22 * a_14 + a_13 * a_24 - a_14 * a_23);
		} else if (i == 2 && j == 1){
			return 2 * detA * (-a_11 * a_23 + a_11 * a_24 + a_21 * a_13 - a_21 * a_14 - a_13 * a_24 + a_14 * a_23);
		} else if (i == 2 && j == 2){
			return 2 * detA * (a_11 * a_22 - a_11 * a_24 - a_21 * a_12 + a_21 * a_14 + a_12 * a_24 - a_22 * a_14);
		} else if (i == 2 && j == 3){
			return 2 * detA * (-a_11 * a_22 + a_11 * a_23 + a_21 * a_12 - a_21 * a_13 - a_12 * a_23 + a_22 * a_13);
		}
		return 0.0;
	}

	
	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int[][] getNonZeroPattern(HDS hds) {
		return null;
	}
	
    
    @Override
    public boolean hasHessian() {
    	return false;
    }
    
}
