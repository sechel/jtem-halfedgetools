package de.jtem.halfedgetools.functional.alexandrov;


import java.util.HashSet;
import java.util.Stack;

import javax.vecmath.Point4d;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.Vector.Norm;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.delaunay.Delaunay;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasRadius;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;
import de.jtem.halfedgetools.util.Consistency;
import de.jtem.halfedgetools.util.triangulationutilities.ConsistencyCheck;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;
import de.jtem.numericalMethods.calculus.function.RealVectorValuedFunctionOfSeveralVariablesWithJacobien;
import de.jtem.numericalMethods.calculus.rootFinding.Broyden;
import de.varylab.mtjoptimization.FunctionNotDefinedException;
import de.varylab.mtjoptimization.IterationMonitor;
import de.varylab.mtjoptimization.NotConvergentException;
import de.varylab.mtjoptimization.newton.NewtonSolver;

/**
 * Calculates and layouts the polyhedron from the given graph. 
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 */
public class Alexandrov2 {

	private static Double
		solverError = 1E-10;
	
	private static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> void layoutPolyeder(HalfEdgeDataStructure<V, E, F> graph){
		V firstVertex = graph.getVertex(0);
		E firstEdge = firstVertex.getIncomingEdge();
		V secondVertex = firstEdge.getStartVertex();
		
		Double rx = firstVertex.getRadius();
		Double ry = secondVertex.getRadius();
		Double lxy = firstEdge.getLength();
		Double y1 = (rx*rx + ry*ry - lxy*lxy) / (2*rx);
		Double y2 = Math.sqrt(ry*ry - y1*y1);
		
		firstVertex.setXYZW(new Point4d(rx, 0, 0, 1));
		secondVertex.setXYZW(new Point4d(y1, y2, 0, 1));
		
		Stack<E> layoutEdges = new Stack<E>();
		HashSet<V> readyVertices = new HashSet<V>();
		readyVertices.add(firstVertex);
		readyVertices.add(secondVertex);
		layoutEdges.push(firstEdge);
		layoutEdges.push(firstEdge.getOppositeEdge());
		while (!layoutEdges.isEmpty()){
			E edge = layoutEdges.pop();
			E e1 = edge.getNextEdge();
			E e2 = edge.getPreviousEdge();
			V xVertex = e1.getTargetVertex();
			if (readyVertices.contains(xVertex))
				continue;
			xVertex.setXYZW(getPyramideTip(edge));
			layoutEdges.push(e1.getOppositeEdge());
			layoutEdges.push(e2.getOppositeEdge());
			readyVertices.add(xVertex);
		}
	} 

	
	private static class TipRootFinder <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> implements RealVectorValuedFunctionOfSeveralVariablesWithJacobien{

		private double[][]
			x = new double[3][4];
		private double[]
		    r = new double[3];
			
		public TipRootFinder(E edge){
			r[0] = edge.getNextEdge().getLength();
			r[1] = edge.getPreviousEdge().getLength();
			r[2] = edge.getNextEdge().getTargetVertex().getRadius();
			edge.getTargetVertex().getXYZW().get(x[0]);
			edge.getStartVertex().getXYZW().get(x[1]);
			x[2][3] = 1;
			for (int i = 0; i < x.length; i++)
				for (int j = 0; j < 3; j++)
					x[i][j] = x[i][j] / x[i][3];
		}
		
		public void eval(double[] p, double[] fx, int offset) {
			for (int i = 0; i < 3; i++){
				fx[i + offset] = (p[0]-x[i][0])*(p[0]-x[i][0]) + 
								 (p[1]-x[i][1])*(p[1]-x[i][1]) + 
								 (p[2]-x[i][2])*(p[2]-x[i][2]) - 
								 r[i]*r[i];
			}
		}
		
		public void eval(double[] p, double[] fx, int offset, double[][] jac) {
			eval(p, fx, offset);
			for (int i = 0; i < jac.length; i++) {
				for (int j = 0; j < jac[i].length; j++) {
					jac[i][j] = 2*(p[j] - x[i][j]);
				}
			}
		}

		public int getDimensionOfTargetSpace() {
			return 3;
		}

		public int getNumberOfVariables() {
			return 3;
		}

	}
	
	
	private static Point4d cross(Point4d x1, Point4d x2){
		Point4d result = new Point4d(x1.y*x2.z - x1.z*x2.y, x1.z*x2.x - x1.x*x2.z, x1.x*x2.y - x1.y*x2.x, 1);
		result.scale(1 / (x1.w*x2.w));
		return result;
	}
	

	private static  <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Point4d getPyramideTip(E edge){
		TipRootFinder<V, E, F> rootFinder = new TipRootFinder<V, E, F>(edge);
		Point4d x1 = edge.getTargetVertex().getXYZW();
		Point4d x2 = edge.getStartVertex().getXYZW();
		Point4d guess = cross(x1, x2);
		double[] x = new double[]{guess.x, guess.y, guess.z};
		Broyden.search(rootFinder, x);
		return new Point4d(x[0], x[1], x[2], 1);
	}

	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> void constructPolyhedron(HalfEdgeDataStructure<V, E, F> graph, Double error, Integer maxInterations, IterationMonitor mon, RemoteControl rc) 
	throws TriangulationException, NotConvergentException{
		if (mon != null)
			mon.setIteration(0, 0.0);
		if (!Consistency.checkConsistency(graph))
			throw new TriangulationException("Consistency check failed, data structure corrupted");
		if (!ConsistencyCheck.isTriangulation(graph))
			throw new TriangulationException("No triangulation!");
		if (!ConsistencyCheck.isSphere(graph))
			throw new TriangulationException("Triangulation is no sphere!");
		if (!CPMCurvatureFunctional.isMetricConvex(graph))
			throw new TriangulationException("Metric not convex!");
		
		// flip counters are set to zero
		resetFlipStates(graph);
		
		// enshure plane delaunay condition
		Delaunay.constructDelaunay(graph);
		
		if (!Consistency.checkConsistency(graph))
			throw new TriangulationException("Consistency check failed after delaunay, data structure corrupted");
		
		// initial radii for a convex metric
		Vector gamma = CPMCurvatureFunctional.getGamma(graph);
		double initRadius = 1;
		boolean isConvex = false;
		do {
			if (rc != null && rc.isStopAsFastAsPossible())
				return;
			initRadius *= 2;
			for (V v : graph.getVertices())
				v.setRadius(initRadius);
			try {
				isConvex = CPMCurvatureFunctional.isConvex(graph);
				Vector k = CPMCurvatureFunctional.getCurvature(graph);
				for (int i = 0; i < k.size(); i++)
					if (k.get(i) < (2*Math.PI - gamma.get(i)) / 2)
						isConvex = false;
				CPMCurvatureFunctional.getCurvatureDerivative(graph);
			} catch (TriangulationException fnde){
				isConvex = false;
			}
		} while (!isConvex);
//		DBGTracer.msg("Setting initial radii to :" + initRadius);
		
//		Matrix jacobi = CPMCurvatureFunctional.getCurvatureDerivative(graph);
//		DBGTracer.msg("Jacobi matrix is:");
//		DBGTracer.msg(jacobi.toString());
		
		double max_delta = 0.75;
		double boost = 1;
		double delta = max_delta;
		NewtonSolver solver = new NewtonSolver();
		solver.setError(solverError);
		CPMLinearizable<V, E, F> fun = new CPMLinearizable<V, E, F>(graph);
		Vector kappa = CPMCurvatureFunctional.getCurvature(graph);
//		DBGTracer.msg("start kappas:");
//		DBGTracer.msg(kappa.toString());
		Vector stepKappa = kappa.copy().add(-delta, kappa);
		Vector newRadii = new DenseVector(graph.numVertices());
		getRadii(graph, newRadii);
		Integer actInteration = 0;
		if (mon != null)
			mon.start(kappa.norm(Norm.Two));
		Stack<E> flipUndoStack = new Stack<E>();
		while (kappa.norm(Norm.Two) > error && actInteration < maxInterations){
			if (rc != null && rc.isStopAsFastAsPossible())
				return;
			if (mon != null)
				mon.setIteration(actInteration, kappa.norm(Norm.Two));
			if (delta < 1E-50){
				Vector radii = new DenseVector(graph.numVertices());
				getRadii(graph, radii);
//				DBGTracer.msg("Radii: ");
//				DBGTracer.msg(radii.toString());
//				DBGTracer.msg(kappa.toString());
				throw new NotConvergentException("Dead end!", kappa.norm(Norm.Two));
			}
			Vector oldRadii = newRadii.copy();
			try {
				solver.solve(fun, newRadii, stepKappa);
			} catch (FunctionNotDefinedException te){
				delta = Math.pow(delta, 2);
				stepKappa = kappa.copy().add(-delta, kappa);
				newRadii = oldRadii;
//				DBGTracer.msg(te.getMessage() + ", delta = " + delta);
				boost = 1;
				// undo flips
				if (!flipUndoStack.isEmpty()){
//					DBGTracer.msg("Undo flips...");
					while (!flipUndoStack.isEmpty())
						flipUndoStack.pop().flip();
//					DBGTracer.msg("done.");
					flipUndoStack.clear();
				}
				actInteration++;
				continue;
			}
			setRadii(graph, newRadii);
			HashSet<E> concaveEdges = new HashSet<E>();
			for (E e : graph.getPositiveEdges()){
				if (!CPMCurvatureFunctional.isLocallyConvex(e))
					concaveEdges.add(e);	
			}
			// flip
			if (concaveEdges.size() != 0){
				flipUndoStack.clear();
//				DBGTracer.msg("Filpping " + concaveEdges.size() + " edges...");
				for (E flipEdge : concaveEdges){
					flipEdge.flip();
					flipUndoStack.push(flipEdge);
//					DBGTracer.msg("Edge " + flipEdge + "flipped");
				}
//				DBGTracer.msg("done.");
				boost = 1;
				max_delta = delta;
			} else {
				flipUndoStack.clear();
				max_delta = delta;
				kappa.set(stepKappa);
				delta = Math.pow(max_delta, 1.0 / boost);
//				DBGTracer.msg("resetting or boosting delta to " + delta);
				stepKappa = stepKappa.copy().add(-delta, stepKappa);
//				DBGTracer.msg("|Kappa|: " + kappa.norm(Norm.Two) + " boost: " + boost);
				boost *= 2;
				Vector radii = new DenseVector(graph.numVertices());
				getRadii(graph, radii);
//				DBGTracer.msg("Radii:" + radii);
			}
			actInteration++;
		}
		if (mon != null)
			mon.start(kappa.norm(Norm.Two));
//		DBGTracer.msg("Needed " + actInteration + " iterations to complete.");
		if (actInteration >= maxInterations)
			throw new NotConvergentException("Polytop has not been constructed within the maximum iterations! ", kappa.norm(Norm.Two));
	
//		DBGTracer.msg("layouting...");
		layoutPolyeder(graph);
	}

	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & HasLength,
		F extends Face<V, E, F>
	> void setRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : graph.getVertices()){
			v.setRadius(radii.get(i));
			i++;
		}
	}
	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & HasLength,
		F extends Face<V, E, F>
	> void getRadii(HalfEdgeDataStructure<V, E, F> graph, Vector radii){
		int i = 0;
		for (V v : graph.getVertices()){
			radii.set(i, v.getRadius());
			i++;
		}
	}
	
	protected static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	> void resetFlipStates(HalfEdgeDataStructure<V, E, F> graph){
		for (E e : graph.getEdges())
			e.resetFlipCount();
	}
}
