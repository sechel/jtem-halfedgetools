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
import de.jtem.halfedgetools.functional.alexandrov.decorations.IsBoundary;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;

public class CapCurvatureFunctional {

	private static Double
		eps = 1E-2;
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> List<V> getInnerVertices(HalfEdgeDataStructure<V, E, F> graph){
		LinkedList<V> list = new LinkedList<V>();
		for (V v : graph.getVertices())
			if (!isBorderVertex(v))
				list.add(v);
		return list;
	}
	
	
	private static Double cot(Double phi){
		return -tan(phi + PI/2);
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Boolean isDegenerated(E edge_ki){
		try {
			if (edge_ki.getLeftFace() != null)
				return getOmega(edge_ki.getNextEdge()) > Math.PI - eps;
			else
				return getOmega(edge_ki.getOppositeEdge().getNextEdge()) > Math.PI - eps;
		} catch (TriangulationException e){
			return false;
		}
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Boolean isFaceDegenerated(E edge) {
		if (isDegenerated(edge))
			return true;
		if (isDegenerated(edge.getNextEdge()))
			return true;
		if (isDegenerated(edge.getPreviousEdge()))
			return true;
		return false;
	}	
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Boolean isMetricConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (V v : graph.getVertices()){
			Double gamma = getGammaAt(v);
			if (isBorderVertex(v)){
				if (gamma > PI + eps)
					return false;
			} else {
				if (gamma >= 2*PI + eps)
					return false;
			}
		}
		return true;
	}
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Double getGammaAt(V vertex) throws TriangulationException{
		List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(vertex);
		Double gamma = 0.0;
		for (E e : cocycle){
			if (e.getLeftFace() != null)
				gamma += Delaunay.getAngle(e);
		}
		return gamma;
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> boolean isLocallyConvex(E edge) throws TriangulationException{
		double theta = getTheta(edge);
		if (edge.isBoundary()){
			return theta <= PI/2 + eps;
		} else {
			return theta <= PI + eps;
		}
	}	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> boolean isConvex(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		for (E edge : graph.getEdges())
			if (!isLocallyConvex(edge))
				return false;
		return true;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Vector getCurvature(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		List<V> innerVertices = getInnerVertices(graph);
		Vector result = new DenseVector(innerVertices.size());
		int index = 0;
		for (V v : innerVertices){
			result.set(index, getKappa(v, graph));
			index++;
		}
		return result;
	}

	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Matrix getCurvatureDerivative(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		List<V> innerVertices = getInnerVertices(graph);
		Matrix result = new DenseMatrix(innerVertices.size(), innerVertices.size());
		for (int i = 0; i < innerVertices.size(); i++){
			for (int j = 0; j < innerVertices.size(); j++){
				int realI = innerVertices.get(i).getIndex();
				int realJ = innerVertices.get(j).getIndex();
				result.set(i, j, getCurvaturePartialDerivative(graph, realI, realJ));
			}
		}
		return result;
	}
	
	
	/**
	 * Returns the partial derivative of kappa_i with respect to the height j
	 * @param graph
	 * @param i
	 * @param j
	 * @throws TriangulationException
	 */
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Double getCurvaturePartialDerivative(HalfEdgeDataStructure<V, E, F> graph, int i, int j) throws TriangulationException{
		V b = graph.getVertex(i);
		List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(b);
		Double result = 0.0;
		for (E e : cocycle){
			V a = e.getStartVertex();
			double alpha_e1 = getAlpha(e);
			double alpha_e2 = getAlpha(e.getOppositeEdge());
			double rho_e = getRho(e);
			double dkdrho = (cot(alpha_e1) + cot(alpha_e2)) / sin(rho_e);
			double drhodh = 0.0;
			if (a.getIndex() == j && j != b.getIndex()){
				drhodh = 1 / (e.getLength() * sin(rho_e));
			} else if (a.getIndex() != j && j == b.getIndex()){
				drhodh = -1 / (e.getLength() * sin(rho_e));
			} else {
				drhodh = 0.0;
			}
			result += dkdrho * drhodh;	
		}
		
		return result;
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Double getKappa(V vertex, HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(vertex);
		Double omega_i = 0.0; 
		for (E e : cocycle)
			omega_i += getOmega(e);
		return 2*PI - omega_i;
	}
	
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Double getFunctional(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Double result = 0.0;
		// internal vertices
		for (V v : graph.getVertices()){
			if (isBorderVertex(v))
				continue;
			result += v.getRadius() * getKappa(v, graph);
		}
		// edges
		for (E e : graph.getPositiveEdges()){
			result += e.getLength() * (PI - getTheta(e));
		}
		return result;
	}

	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary&HasLength,
		F extends Face<V, E, F>
	> Double getTheta(E edge) throws TriangulationException{
		if (!edge.isBoundary())
			return getAlpha(edge) + getAlpha(edge.getOppositeEdge());
		else {
			if (edge.getLeftFace() != null)
				return getAlpha(edge) - PI/2;
			else
				return getAlpha(edge.getOppositeEdge()) - PI/2;
		}
	}
	
	
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
		if (cosAlpha > 1 + eps)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		return acos(cosAlpha);
	}
	
	
	protected static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getRho(E edge) throws TriangulationException{
		Double ri = edge.getTargetVertex().getRadius();
		Double rj = edge.getStartVertex().getRadius();
		Double lij = edge.getLength();
		Double cosRho = (ri - rj) / lij;
		if (cosRho > 1 + eps){
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge);
		}
		return acos(cosRho);
	}
	
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
		if (cosOmega > 1 + eps)
			throw new TriangulationException("Triangle inequation doesn't hold pyramide side at edge " + edge_ki);
		return acos(cosOmega);
	}
	
	
	public static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable & IsBoundary,
		F extends Face<V, E, F>
	> Boolean isBorderVertex(V vertex){
		List<E> cocycle = HalfEdgeUtilsExtra.getEdgeStar(vertex);
		for (E e : cocycle)
			if (e.isBoundary())
				return true;
		return false;
	}
	
	
	
}
