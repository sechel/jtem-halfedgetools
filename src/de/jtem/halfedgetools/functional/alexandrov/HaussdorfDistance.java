package de.jtem.halfedgetools.functional.alexandrov;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLength;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasRadius;
import de.jtem.halfedgetools.functional.alexandrov.decorations.HasXYZW;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;

public class HaussdorfDistance {

	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	> Double getHeight(F face, HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		E edgeij = face.getBoundaryEdge();
		Double rj = edgeij.getTargetVertex().getRadius();
		Double hij = rj * Math.sin(CPMCurvatureFunctional.getRho(edgeij));
		Double alphaij = CPMCurvatureFunctional.getAlpha(edgeij);
		return hij * Math.sin(alphaij);
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable,
		F extends Face<V, E, F>
	>  Double getMaxRadius(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Double max = 0.0;
		for (V v : graph.getVertices())
			max = max < v.getRadius() ? v.getRadius() : max;
		return max;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	>  Double getMinHeight(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		Double min = Double.MAX_VALUE;
		for (F f : graph.getFaces()){
			Double height = getHeight(f, graph);
			min = min > height ? height : min;
		}
		return min;
	}
	
	
	public static <
		V extends Vertex<V, E, F> & HasXYZW & HasRadius,
		E extends Edge<V, E, F> & IsFlippable&HasLength,
		F extends Face<V, E, F>
	>  Double getDistanceToSphere(HalfEdgeDataStructure<V, E, F> graph) throws TriangulationException{
		double outterRadius = getMaxRadius(graph);
		double innerRadius = getMinHeight(graph);
		double sphereRadius = (outterRadius + innerRadius) / 2;
		return (outterRadius - sphereRadius) / sphereRadius;
	}
		
}
