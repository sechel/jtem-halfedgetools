package de.jtem.halfedgetools.algorithm.sqrtroot3;

import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;

public class Sqrt3Subdivision <
V extends Vertex<V, E, F>,
E extends Edge<V, E, F> & IsFlippable,
F extends Face<V, E, F>,
HEDS extends HalfEdgeDataStructure<V, E, F>>
 {
	
	
	public Map<E, Set<E>> subdivide(HEDS oldHeds, HEDS newHeds, Coord3DAdapter<V> vA, Coord3DAdapter<E> eA, Coord3DAdapter<F> fA) throws TriangulationException {
	
		// TODO
		
		// 1 calc new coordinates into maps
		
		// 2 create/change combinatorics of the new heds (including flips i.e. e.flip()	
		
		// 3 set new coordingate from the maps of step 1
		
		// 4 create and return map from old edges to set of new edges (to keep cycles)

		
		return null;
	}
	

 }