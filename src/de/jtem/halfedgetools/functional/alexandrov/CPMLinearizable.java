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

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasRadius;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;
import de.jtem.halfedgetools.util.TriangulationException;
import de.varylab.mtjoptimization.FunctionNotDefinedException;
import de.varylab.mtjoptimization.Linearizable;


/**
 * An implementation of the Linearizable interface for use with the 
 * Solver in math.optimization.
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see math.optimization.Linearizable
 * @see dform.math.optimization.newton.NewtonDenseSolver
 */
public class CPMLinearizable
<
	V extends Vertex<V, E, F> & HasXYZW & HasRadius,
	E extends Edge<V, E, F> & IsFlippable&HasLength,
	F extends Face<V, E, F>
> implements Linearizable {


	private HalfEdgeDataStructure<V, E, F>
		graph = null;
	
	
	public CPMLinearizable(HalfEdgeDataStructure<V, E, F> graph){
		this.graph = graph;
	}
	
	
	public void evaluate(Vector x, Vector fx, Vector offset) throws FunctionNotDefinedException{
		for (int i = 0; i < graph.numVertices(); i++)
			graph.getVertex(i).setRadius(x.get(i));
		try {
			fx.set(CPMCurvatureFunctional.getCurvature(graph).add(-1, offset));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	public void evaluate(Vector x, Vector fx, Vector offset, Matrix jacobian) throws FunctionNotDefinedException{
		evaluate(x, fx, offset);
		try {
			jacobian.set(CPMCurvatureFunctional.getCurvatureDerivative(graph));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	public void evaluate(Vector x, Matrix jacobian) throws FunctionNotDefinedException{
		for (int i = 0; i < graph.numVertices(); i++)
			graph.getVertex(i).setRadius(x.get(i));
		try {
			jacobian.set(CPMCurvatureFunctional.getCurvatureDerivative(graph));
		} catch (TriangulationException e) {
			throw new FunctionNotDefinedException(e.getMessage());
		}
	}

	public Integer getDomainDimension() {
		return graph.numVertices();
	}

	public Integer getCoDomainDimension() {
		return graph.numVertices();
	}

}
