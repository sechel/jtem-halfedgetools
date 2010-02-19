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

package de.jtem.halfedgetools.functional.alexandrov;

import static java.lang.Math.PI;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.delaunay.Delaunay;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasRadius;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.halfedgetools.util.TriangulationException;



/**
 * A discrete curvature functional and its derivative
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see alexandrov.math.CPMLinearizable
 */
public class CPMCurvatureFunctional {

	private static Double cot(Double phi){
		return -tan(phi + PI/2);
	}


	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> boolean isMetricConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (V v : graph.getVertices()){
			Double gamma = getGammaAt(v);
			if (gamma >= 2*PI){
				return false;
			}
		}
		return true;
	}
	

	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getGammaAt(V vertex) throws TriangulationException{
		List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(vertex);
		Double gamma = 0.0;
		for (E e : cocycle){
			gamma += Delaunay.getAngle(e);
		}
		return gamma;
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Vector getGamma(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Vector result = new DenseVector(graph.numVertices());
		for (V v : graph.getVertices()){
			List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(v);
			Double gamma = 0.0;
			for (E e : cocycle){
				gamma += Delaunay.getAngle(e);
			}
			result.set(v.getIndex(), gamma);
		}
		return result;
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> boolean isLocallyConvex(E edge) throws TriangulationException{
		return getAlpha(edge) + getAlpha(edge.getOppositeEdge()) <= PI;
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> boolean isConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (E edge : graph.getEdges())
			if (!isLocallyConvex(edge))
				return false;
		return true;
	}

	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>	
	> Matrix getCurvatureDerivative(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Matrix result = new DenseMatrix(graph.numVertices(), graph.numVertices());
		for (int i = 0; i < graph.numVertices(); i++){
			for (int j = 0; j < graph.numVertices(); j++){
				result.set(i, j, getCurvaturePartialDerivative(graph, i, j));
			}
		}
		return result;
	}
	
	
	/**
	 * Returns the partial derivative of kappa_i with respect to the radus j
	 * @param graph
	 * @param i
	 * @param j
	 * @throws TriangulationException
	 */
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getCurvaturePartialDerivative(HalfEdgeDataStructure<V, E, F> graph, int i, int j) throws TriangulationException{
		if (i == j){
			V v = graph.getVertex(i);
			List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(v);
			Double result = 0.0;
			for (E ki : cocycle){
				int k = ki.getStartVertex().getIndex();
				Double sum = 0.0;
				if (k == i){ //loop
					Double sin_rho_e = sin(getRho(ki));
					Double l = ki.getLength();
					Double alphaE = getAlpha(ki);
					Double alphaEMin = getAlpha(ki.getOppositeEdge());
					Double ri = v.getRadius();
					sum =  l * (cot(alphaE) + cot(alphaEMin)) / (ri*ri * sin_rho_e*sin_rho_e) / 2;
				} else {    //no loop
					Double phi_ij = getPhi(ki);
					sum = cos(phi_ij)*getCurvaturePartialDerivative(graph, i, k);
				}
				result += sum;
			}
			return -result;
		} else {
			//find edge ij
			List<E> jiList = new LinkedList<E>();
			for (E edge : graph.getEdges()){
				if (edge.getTargetVertex().getIndex() == i && edge.getStartVertex().getIndex() == j)
					jiList.add(edge);
			}
			if (jiList.size() == 0)
				return 0.0;
			Double result = 0.0;
			for (E ji : jiList){
				E ij = ji.getOppositeEdge(); 
				Double alpha_ij = getAlpha(ji);
				Double alpha_ji = getAlpha(ij);
				Double rho_ij = getRho(ji);
				Double rho_ji = getRho(ij);
				Double lij = ij.getLength();
				result += (cot(alpha_ij) + cot(alpha_ji)) / (lij * sin(rho_ij) * sin(rho_ji));
			}
			return result;
		}
	}
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getCurvature(V v) throws TriangulationException{
		List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(v);
		Double omega_i = 0.0; 
		for (E e : cocycle)
			omega_i += getOmega(e);
		return 2*PI - omega_i;
	}	
	
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Vector getCurvature(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Vector result = new DenseVector(graph.numVertices());
		for (V v : graph.getVertices())
			result.set(v.getIndex(), getCurvature(v));
		return result;
	}

	
	/**
	 * Returns Rho_ji if edge starts at i and ends at j
	 * @param edge the edge between i and j
	 * @return the angle between edge and the radius on j
	 */
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getRho(E edge) throws TriangulationException{
		Double ri = edge.getTargetVertex().getRadius();
		Double rj = edge.getStartVertex().getRadius();
		Double lij = edge.getLength();
		Double cosRho = (lij*lij + ri*ri - rj*rj) / (2*lij*ri);
		if (cosRho > 1)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		return acos(cosRho);
	}
	
	/**
	 * Returns the angle between the two radii of i and j
	 * @param edge
	 * @return
	 */
	protected static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getPhi(E edge) throws TriangulationException{
		return PI - getRho(edge) - getRho(edge.getOppositeEdge());
	}
	
	/**
	 * Returns the angle between the triangle of edge and the triangle 
	 * of the radi of i and j and edge. 
	 * @param edge
	 * @return alpha
	 * @throws TriangulationException
	 */
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getAlpha(E edge) throws TriangulationException{
		E edgeji = edge.getOppositeEdge();
		E edgeki = edge.getPreviousEdge();
		Double gammajik = Delaunay.getAngle(edgeki);
		Double rhoik = getRho(edgeki);
		Double rhoij = getRho(edgeji);
		Double cosAlpha = (cos(rhoik) - cos(gammajik)*cos(rhoij)) / (sin(gammajik)*sin(rhoij));
		if (cosAlpha > 1)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		return acos(cosAlpha);
	}
	
	
	/**
	 * Returns the angle omega
	 * @param edge_ki the edge from k to i
	 * @return
	 * @throws TriangulationException
	 */
	protected static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getOmega(E edge_ki) throws TriangulationException{
		E edge_ji = edge_ki.getNextEdge().getOppositeEdge();
		Double gamma_jik = Delaunay.getAngle(edge_ki);
		Double rho_ij = getRho(edge_ji);
		Double rho_ik = getRho(edge_ki);
		Double cosOmega = (cos(gamma_jik) - cos(rho_ij)*cos(rho_ik)) / (sin(rho_ij)*sin(rho_ik));
		if (cosOmega > 1)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge_ki);
		return acos(cosOmega);
	}
	
}
