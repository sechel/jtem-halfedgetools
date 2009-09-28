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
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;
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
