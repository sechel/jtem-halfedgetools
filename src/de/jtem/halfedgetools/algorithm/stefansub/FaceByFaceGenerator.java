package de.jtem.halfedgetools.algorithm.stefansub;

import java.util.ArrayList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;

/**
 * Utility class for editing/generating half edge structures by adding 
 * consistently oriented faces, which are given by their vertices.
 * Adding a face will generate and identify edges in an efficient manner
 * by the usage of a vertex-edge look-up-table 
 * ( @link #vertexEdgeLookUp(HalfEdgeDataStructure) ). Thus if you do changes
 * to the half edge structure not using the FaceByFaceGenerator you
 * have to call @link #update() before you can use it again. 
 * Currently this class can only add faces (@link #addFace(V[])). 
 * Thus the vertices have to be created outside this class.
 *
 * This class offers also a convinient API for the vertex-edge look-up-table:
 * @link #findEdge(V, V), @link #findEdgesWithTarget(V).
 * 
 * @author schmies
 *
 * @param <V>
 * @param <E>
 * @param <F>
 */
public class FaceByFaceGenerator<
V extends Vertex<V, E, F>,
E extends Edge<V, E, F>, 
F extends Face<V, E, F> 
>  {

	HalfEdgeDataStructure<V, E, F> g;
	
	List<List<E>> lookUp;
	
	public FaceByFaceGenerator( HalfEdgeDataStructure<V, E, F> g ) {
		this.g = g;
		
		update();
	}
	
//	public FaceByFaceGenerator() {
//		this( new HalfEdgeDataStructure<V,E,F>());
//	}
	
	/**
	 * Updates the vertex-edge look up.
	 * Call this if you do changes to the half edge structure outside
	 * this class, like adding new vertices.
	 */
	public void update() {
		lookUp = vertexEdgeLookUp(g);
	}
	
	/**
	 * Generates a new face given by its vertices.
	 * The orientation has to be given consistently.
	 * This method will generate and identify edges.
	 * @param vertex list vertices (atleast 2)
	 * @return
	 */
	public F addFace( V ... vertex ) {
		return addFace(g,lookUp,vertex);
	}
	
	/**
	 * Returns the half edge structure of this generator.
	 */
	public HalfEdgeDataStructure<V, E, F> getHalfEdgeStructure() {
		return g;
	}
	
	/**
	 * Returns edge with given start and target vertex, if
	 * it exists, otherwise it return null.
	 * @param startVertex
	 * @param targetVertex
	 */
	public E findEdge( V startVertex, V targetVertex ) {
		return findEdge(g,lookUp,startVertex,targetVertex);
	}
	
	/**
	 * Returns alle edges with given target vertex.
	 * @param targetVertex
	 */
	public List<E> findEdgesWithTarget( V targetVertex ) {
		return lookUp.get(targetVertex.getIndex());
	}
	
	/**
	 * Computes a vertex-edge look-up-table.
	 * The methods generates for each vertex
	 * a list of all edges having this vertex
	 * as targetVertex. 
	 * (At some point the List should be
	 * changed into a HashSet.)
	 * @param <V>
	 * @param <E>
	 * @param <F>
	 * @param g 
	 * @return A list of lists of edges. The length of the list equals #vertices. 
	 */
	public static
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> List<List<E>>vertexEdgeLookUp(HalfEdgeDataStructure<V, E, F> g) {
		int nov = g.numVertices();
		ArrayList<List<E>> lookUp = new ArrayList<List<E>>(nov);
		
		for( int i=0; i<nov; i++ ) {
			lookUp.add( new ArrayList<E>());
		}
		
		for( E e : g.getEdges() ) {
			lookUp.get(e.getTargetVertex().getIndex()).add(e);		
		}
				
		return lookUp;
	}

	static
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> E addEdge(HalfEdgeDataStructure<V, E, F> g, List<List<E>>lookUp, V startVertex, V targetVertex ) {
		E e = findEdge(g,lookUp, startVertex, targetVertex );
		
		if( e != null ) {
			return e;
		}
		
		e = g.addNewEdge(); 
		e.setTargetVertex(targetVertex);
		e.setIsPositive(true);
		
		E o = g.addNewEdge();
		o.setTargetVertex(startVertex);
		o.setIsPositive(false);
		
		e.linkOppositeEdge(o);
		
		lookUp.get(e.getTargetVertex().getIndex()).add(e);
		lookUp.get(o.getTargetVertex().getIndex()).add(o);
		
		return e;
	}

	static
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> E findEdge(HalfEdgeDataStructure<V, E, F> g, List<List<E>>lookUp, V startVertex, V targetVertex ) {
		for( E e : lookUp.get(targetVertex.getIndex())) {
			if( e.getStartVertex() == startVertex )
				return e;
		}
		return null;
	}

	private static
	<
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
	> F addFace(HalfEdgeDataStructure<V, E, F> g, List<List<E>>lookUp, V ... vertex ) {
		
		int nov = vertex.length;
		
		if( nov < 2 )
			throw new IllegalArgumentException( "need atleast two points for a face");
		
		F f = g.addNewFace();
		
		ArrayList<E> b = new ArrayList<E>(nov);
		
		b.add( addEdge( g, lookUp, vertex[nov-1], vertex[0] ) );
		
		for( int i=1; i<nov; i++ ) {	
			E e = addEdge( g, lookUp, vertex[i-1], vertex[i] );
			b.add(e);
		}
		
		for( int i=0; i<nov; i++ ) {
			b.get(i).setLeftFace(f);
			b.get(i).linkNextEdge( b.get((i+1)%nov) );
		}
		
		return f;
	}
}
