package de.jtem.halfedgetools.util;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.util.triangulationutilities.ConsistencyCheck;
/**
 * Some utilities, not present in the current HalfEdgeUtils, that we need for alexandrov project
 * Feel free to add to HalfEdgeUtils, if they are considered universal.
 * @author josefsso
 *
 */
public class HalfEdgeUtilsExtra {


	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> findEdgesWithTarget(V v) {
		HalfEdgeDataStructure<V, E, F> surface = v.getHalfEdgeDataStructure();
		ArrayList<E> result = new ArrayList<E>();
		for (E e : surface.getEdges()) {
			if (v == e.getTargetVertex())
				result.add(e);
		}
		return result;
	}

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		
		VV extends Vertex<VV, EE, FF>,
		EE extends Edge<VV, EE, FF>,
		FF extends Face<VV, EE, FF>
	> void copyCombinatorics(HalfEdgeDataStructure<V,E,F> source, HalfEdgeDataStructure<VV,EE,FF> dest) {

		if(source.numVertices() != dest.numVertices()) {
			System.err.println("Couldnt recombinatoric.. not equal nr. of vertices");
			return;
		}
		
		HalfEdgeTopologyOperations.removeAllFaces(dest);
		HalfEdgeTopologyOperations.removeAllEdges(dest);
		
		dest.addNewEdges(source.numEdges());
		dest.addNewFaces(source.numFaces());
		
		List<E> edgeList = source.getEdges();
		
		for (E e : edgeList) {
			E eNext = e.getNextEdge();
			E eOpp = e.getOppositeEdge();
			F f = e.getLeftFace();
			V v = e.getTargetVertex();
			EE ee = dest.getEdge(e.getIndex());
			ee.setIsPositive(e.isPositive());
			if (eNext != null) {
				ee.linkNextEdge(dest.getEdge(eNext.getIndex()));
			}
			if (eOpp != null) {
				ee.linkOppositeEdge(dest.getEdge(eOpp.getIndex()));
			}
			if (f != null) {
				ee.setLeftFace(dest.getFace(f.getIndex()));
			}
			if (v != null) {
				ee.setTargetVertex(dest.getVertex(v.getIndex()));
			}
		}

	}
	
	public enum Noise {UNIFORM, GAUSSIAN, DELTA};
	
//	public static <
//		V extends Vertex<V, E, F> & HasPositionArray,
//		E extends Edge<V, E, F>,
//		F extends Face<V, E, F>
//	> void addNoise(HalfEdgeDataStructure<V,E,F> heds, Noise n) {
//		for(V v : heds.getVertices()) {
//			// ADD VERTEX NOISE
//			
//			double[] noise = new double[]{0.0,0.0,0.0};
//			Random r = new Random();
//			switch(n) {
//				case UNIFORM:
//					noise = new double[]{r.nextDouble()-0.5,r.nextDouble()-0.5,r.nextDouble()-0.5};
//				case GAUSSIAN:
//					for(int a = 0; a < 3; a++) {
//						double t = (r.nextGaussian()+3)/6.0;
//						// TODO: use proper cutoff here
//						if(t < 0.1) t = 0.1;
//						if(t > 0.9) t = 0.9;
//						t = t - 0.5;
//						noise[a] = t;
//					}
//			}
//		
//			double[] pos = v.getPosition();
//			Rn.times(noise, 0.05, noise);
//			Rn.add(pos, noise, pos);
//			
//			v.setPosition(pos);
//		}
//	}

	
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> List<E> get1Ring(V v){
		
		ConsistencyCheck.isTriangulation(v.getHalfEdgeDataStructure());
		List<E> incoming = HalfEdgeUtils.incomingEdges(v);
		
		LinkedList<E> ring = new LinkedList<E>();
		for(E e : incoming)
			ring.add(e.getPreviousEdge());
		
		return ring;
	}
	
	
	@Deprecated
	// use splitFace instead
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V coneOverFace(F f){
		
		HalfEdgeDataStructure<V, E, F> heds = f.getHalfEdgeDataStructure();
		V conePoint = heds.addNewVertex();
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		
		
		
		// do first triangle
		E firstEdge = boundary.get(0);
		E e1 = heds.addNewEdge();
		E e2 = heds.addNewEdge();
		e1.setTargetVertex(conePoint);
		e2.setTargetVertex(firstEdge.getStartVertex());
		firstEdge.linkNextEdge(e1);
		e1.linkNextEdge(e2);
		e2.linkNextEdge(firstEdge);
		
//		F newFace = heds.addNewFace();
		F newFace = f;
		firstEdge.setLeftFace(newFace);
		e1.setLeftFace(newFace);
		e2.setLeftFace(newFace);
		
		E tempEdge = e1;
		
		E firstGlue = e2;
		
		for(E e : boundary.subList(1, boundary.size())) {
			e1 = heds.addNewEdge();
			e2 = heds.addNewEdge();
			
			e1.setTargetVertex(conePoint);
			e2.setTargetVertex(e.getStartVertex());
			
			e.linkNextEdge(e1);
			e1.linkNextEdge(e2);
			e2.linkNextEdge(e);
			
			e2.linkOppositeEdge(tempEdge);
			
			newFace = heds.addNewFace();
			
			e.setLeftFace(newFace);
			e1.setLeftFace(newFace);
			e2.setLeftFace(newFace);
			
			tempEdge = e1;
		}
		
		tempEdge.linkOppositeEdge(firstGlue);
		
//		heds.removeFace(f);
		
		return conePoint;
	}
	
	/**
	 * Find an edge with a given target vertex. May be expensive.
	 * @param v
	 * @return an edge <code>e</code> with target vertex <code>v</code>, 
	 * or <code>null</code> if none exists. 
	 */
	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> E findEdgeWithTarget(V v) {
		HalfEdgeDataStructure<V, E, F> surface = v.getHalfEdgeDataStructure();
		for (E e : surface.getEdges()) {
			if (v == e.getTargetVertex())
				return e;
		}
		return null;
	}

	public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	>  List<E> findEdgesWithTargets(List<V> vlist) {
		HalfEdgeDataStructure<V, E, F> graph = vlist.get(0).getHalfEdgeDataStructure();
		ArrayList<E> result = new ArrayList<E>();
		for (V v : vlist){
			for (E e : graph.getEdges()) {
				if (v == e.getTargetVertex())
					result.add(e);
			}
		}
		if(result.size() == 0)
			System.err.println("Found no neighbors");
		return result;
	}

	/**
	 * Add a new triangulated n-gon, without the boundary.
	 * <p>
	 * Increases the number of faces by n-2, the number of edges by 3*n-6, and the number of vertices by n.
	 * @param <V> the vertex type
	 * @param <E> the edge type
	 * @param <F> the face type
	 * @param heds the half-edge data structure
	 * @param n the number of vertices 
	 * @return the new face
	 */
	static public <V extends Vertex<V,E,F>,E extends Edge<V,E,F>, F extends Face<V,E,F>> List<E> addTriangulatedNGonWithoutBoundary(HalfEdgeDataStructure<V,E,F> heds, int n) {
	
		List<V> vertices = heds.addNewVertices(n);
		V base = vertices.get(0);
		
		List<E> edges = new ArrayList<E>();
		
		E e1 = heds.addNewEdge();
		E e2 = heds.addNewEdge(); e1.linkNextEdge(e2);
		E e3 = heds.addNewEdge(); e2.linkNextEdge(e3);
		                          e3.linkNextEdge(e1);
		                          
		edges.add(e1);
		edges.add(e2);
		edges.add(e3);
		e1.setTargetVertex(vertices.get(1));
		e2.setTargetVertex(vertices.get(2));
		e3.setTargetVertex(base);
		
		F f = heds.addNewFace();
		e1.setLeftFace(f);
		e2.setLeftFace(f);
		e3.setLeftFace(f);
		
		E oldConnect = e3;
		
		for(int i = 2; i < n-1; i++) {
			e1 = heds.addNewEdge();
			e2 = heds.addNewEdge(); e1.linkNextEdge(e2);
			e3 = heds.addNewEdge(); e2.linkNextEdge(e3);
			                        e3.linkNextEdge(e1);
			                        
		                        
			edges.add(e1);
			edges.add(e2);
			edges.add(e3);
			
			e1.setTargetVertex(vertices.get(i));
			e2.setTargetVertex(vertices.get(i+1));
			e3.setTargetVertex(base);
			
			f = heds.addNewFace();
			e1.setLeftFace(f);
			e2.setLeftFace(f);
			e3.setLeftFace(f);
			
			e1.linkOppositeEdge(oldConnect);
			oldConnect = e3;
		}
		return edges;
	}

	/**
	 * Add a new triangulated n-gon, glued onto the given boundary.
	 * <p>
	 * Increases the number of faces by n-2, the number of edges by 3*n-6, and the number of vertices by 0.
	 * @param <V> the vertex type
	 * @param <E> the edge type
	 * @param <F> the face type
	 * @param heds the half-edge data structure
	 * @param n the number of vertices 
	 * @return the new face
	 */
	static public <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> List<E>  addTriangulatedNGonToBoundary(HalfEdgeDataStructure<V,E,F> heds, List<E> boundary) {
	
		int n = boundary.size();
		
		List<V> vertices = new LinkedList<V>();
		List<E> edges = new ArrayList<E>();
		
		for(E e : boundary)
			vertices.add(e.getTargetVertex());
		
		Collections.rotate(vertices, 1);
		Collections.reverse(vertices);
		
		Collections.rotate(boundary, 1);
		Collections.reverse(boundary);
		
		V base = vertices.get(0);
		
		E e1 = heds.addNewEdge(); e1.linkOppositeEdge(boundary.get(0));
		E e2 = heds.addNewEdge(); e2.linkOppositeEdge(boundary.get(1)); e1.linkNextEdge(e2);
		E e3 = heds.addNewEdge();                                       e2.linkNextEdge(e3);
		                                                                e3.linkNextEdge(e1);
		
		edges.add(e1);
		edges.add(e2);
		edges.add(e3);
		
		if(n == 3)
			e3.linkOppositeEdge(boundary.get(2));
		                                                                
		e1.setTargetVertex(vertices.get(1));
		e2.setTargetVertex(vertices.get(2));
		e3.setTargetVertex(base);
		
		F f = heds.addNewFace();
		e1.setLeftFace(f);
		e2.setLeftFace(f);
		e3.setLeftFace(f);
		
		E oldConnect = e3;
		
		for(int i = 2; i < n-1; i++) {
			e1 = heds.addNewEdge(); 
			e2 = heds.addNewEdge(); e2.linkOppositeEdge(boundary.get(i)); e1.linkNextEdge(e2);
			e3 = heds.addNewEdge(); e3.linkOppositeEdge(boundary.get(i+1)); e2.linkNextEdge(e3);
			                                                              e3.linkNextEdge(e1);
	
	  		edges.add(e1);
			edges.add(e2);
			edges.add(e3);	                                                              
			                                                              
			if(findEdgesWithTarget(vertices.get(i)).size() == 3)
				System.err.println("Already 3");
			e1.setTargetVertex(vertices.get(i));
			e2.setTargetVertex(vertices.get(i+1));
			e3.setTargetVertex(base);
			
			f = heds.addNewFace();
			e1.setLeftFace(f);
			e2.setLeftFace(f);
			e3.setLeftFace(f);
			
			e1.linkOppositeEdge(oldConnect);
			oldConnect = e3;
		}
		return edges;
	}

	public static <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
>void clear(HalfEdgeDataStructure<V,E,F> heds){
		List<V> vToRem = new ArrayList<V>();
		List<E> eToRem = new ArrayList<E>();
		List<F> fToRem = new ArrayList<F>();
		
		for(V v : heds.getVertices()) {
				vToRem.add(v);
		}
		
		for(E e: heds.getEdges()) {
				eToRem.add(e);
		}
		
		for(F f : heds.getFaces()) {
				fToRem.add(f);
		}
		
		for(V v : vToRem)
			heds.removeVertex(v);
		
		for(E e : eToRem)
			heds.removeEdge(e);
		
		for(F f : fToRem)
			heds.removeFace(f);
	}

	public static <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> int getDegree(V v) {
		return HalfEdgeUtilsExtra.getEdgeStar(v).size();
	}

	static public boolean isBoundaryEdge(Edge<?,?,?> e){
		return e.getLeftFace() == null || e.getOppositeEdge().getLeftFace() == null;
	}
	
    /**
     * Return the vertex star of the current vertex
     * @return List of vertices adjacent to current vertex
     */
    public static  <
		V extends Vertex<V, E, ?>,
		E extends Edge<V, E, ?>,
		F extends Face<V, E, ?>
    > List<V> getVertexStar(V v) {
        List<V> vertexStar = new ArrayList<V>();
        E actEdge = v.getIncomingEdge();
        do {
        	vertexStar.add(actEdge.getStartVertex());
        	actEdge = actEdge.getNextEdge().getOppositeEdge();
        } while (actEdge != v.getIncomingEdge());
        return vertexStar;
    }

    /**
     * Return the edge star of the current vertex
     * @return List of edges adjacent to current vertex, which is their targetVertex
     */
    public static <
		V extends Vertex<V, E, ?>,
		E extends Edge<V, E, ?>,
		F extends Face<V, E, ?>
    > List<E> getEdgeStar(V v){
    	List<E> edgeStar = new LinkedList<E>();
    	if (v.getIncomingEdge() == null || !v.getIncomingEdge().isValid())
    		return Collections.emptyList();
        E actEdge = v.getIncomingEdge();
        do {
        	if (actEdge == null)
        		return Collections.emptyList();
        	edgeStar.add(actEdge);
        	if (actEdge.getNextEdge() == null)
        		return Collections.emptyList();
        	actEdge = actEdge.getNextEdge().getOppositeEdge();
        } while (actEdge != v.getIncomingEdge());
    	return edgeStar;
    }
    
	public static <
		V extends Vertex<V, E, ?>,
		E extends Edge<V, E, ?>,
		F extends Face<V, E, ?>
	> List<E> getBoundary(F f){
		if (f.getBoundaryEdge() == null || !f.getBoundaryEdge().isValid())
			return Collections.emptyList();
		LinkedList<E> result = new LinkedList<E>();
		E e = f.getBoundaryEdge();
		do {
			if (e == null)
				//TODO: Think about open faces.
				return Collections.emptyList();
			result.add(e);
			e = e.getNextEdge();
		} while (e != f.getBoundaryEdge());
		return result;
	}

	public static <
		V extends Vertex<V, E, ?>,
		E extends Edge<V, E, ?>,
		F extends Face<V, E, ?>
	> boolean isThreeConnected(HalfEdgeDataStructure<V, E, ?> heds){
		for (V v : heds.getVertices()){
			if (getEdgeStar(v).size() < 3)
				return false;
		}
		return true;
	}
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F findCommonFace(E ... edges) {
		if (edges.length < 2)
			return null;
		E e1 = edges[0];
		E e2 = edges[1];
		F tE1 = e1.getLeftFace();
		F sE1 = e1.getRightFace();
		F tE2 = e2.getLeftFace();
		F sE2 = e2.getRightFace();
		
		if( tE1 == sE2 || tE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getRightFace()!=tE1 && edges[i].getLeftFace()!=tE1 )
					return null;
			}
			return tE1;
		} else if( sE1 == sE2 || sE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getRightFace()!=sE1 && edges[i].getLeftFace()!=sE1 )
					return null;
			}
			return sE1;
		} else return null;
	}
	
	public static < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> V findCommonVertex(E ... edges) {
		if (edges.length < 2)
			return null;
		E e1 = edges[0];
		E e2 = edges[1];
		V tE1 = e1.getTargetVertex();
		V sE1 = e1.getStartVertex();
		V tE2 = e2.getTargetVertex();
		V sE2 = e2.getStartVertex();
		
		if( tE1 == sE2 || tE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getStartVertex()!=tE1 && edges[i].getTargetVertex()!=tE1 )
					return null;
			}
			return tE1;
		} else if( sE1 == sE2 || sE1 == tE2 ) {
			for( int i = 2; i < edges.length; i++ ) {
				if( edges[i].getStartVertex()!=sE1 && edges[i].getTargetVertex()!=sE1 )
					return null;
			}
			return sE1;
		} else return null;
	}
	
	/**
     * Return the face star of the current vertex
     * NOTE: untested
     * @return List of faces adjacent to current vertex
     */
    public static 	
	<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
    > List<F> getFaceStar(V v) {
        List<F> faceStar = new ArrayList<F>();
        for (E e : getEdgeStar(v)){
        	if (e.getLeftFace() != null)
        		faceStar.add(e.getLeftFace());
        }
        return faceStar;
    }

	public static <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> F scaleFace(F f) {
		HalfEdgeDataStructure<V, E, F> graph = f.getHalfEdgeDataStructure();
		
		List<E> Ns = new LinkedList<E>();
		List<E> Ps = new LinkedList<E>();
		List<E> OOs = new LinkedList<E>();
		
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		int n = boundary.size();
		
		F midFace = graph.addNewFace();
		
		for(E e : boundary) {
			 E eN = graph.addNewEdge();
			 E eP = graph.addNewEdge();
			 E eO = graph.addNewEdge();
			 E eOO = graph.addNewEdge();
			 
			 F ff = graph.addNewFace();
			 
			 V vN = graph.addNewVertex();
			 
			 V vS = e.getStartVertex();
			 
			 eN.linkNextEdge(eO); eO.linkNextEdge(eP); eP.linkNextEdge(e); e.linkNextEdge(eN);
			 
			 e.setLeftFace(ff); eN.setLeftFace(ff); eP.setLeftFace(ff); eO.setLeftFace(ff);
			 
			 eO.linkOppositeEdge(eOO);
			 eOO.setLeftFace(midFace);
			 
			 eN.setTargetVertex(vN); 
			 eP.setTargetVertex(vS);
			 
			 Ns.add(eN); Ps.add(eP); OOs.add(eOO);
			 
		}
		
		int i = 0;
		for(E en : Ns) {
			E eno = Ps.get((i+1)%n);
			en.linkOppositeEdge(eno);
			i++;
		}
		
		for(E eoo : OOs) {
			E eo = eoo.getOppositeEdge();
			
			V vt = eo.getNextEdge().getOppositeEdge().getTargetVertex();
			eoo.getOppositeEdge().setTargetVertex(vt);
			
			E eoon = eo.getPreviousEdge().getOppositeEdge().getPreviousEdge().getOppositeEdge();
			eoo.linkNextEdge(eoon);
			
			eoo.setTargetVertex(eo.getPreviousEdge().getTargetVertex());
		}
		
		graph.removeFace(f);
		return midFace;
		
	}

	// TODO: check with Boris
	public static 	
	<	V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
    > boolean isRegular(HalfEdgeDataStructure<V,E,F> heds) {
		
		for(E e : heds.getEdges()) {
			if(e.getTargetVertex() == e.getStartVertex())
				return false;
		}
		
		for(F f : heds.getFaces()) {
			List<E> b = getBoundary(f);
			
			int originalSize = b.size();
			HashSet<E> hashSet = new HashSet<E>(b);
			int withoutDuplicates = hashSet.size();
			int numOfDuplicates = originalSize - withoutDuplicates;
			
			if(numOfDuplicates > 0) {
				return false;
			} else {
				List<V> v = new ArrayList<V>();
				for(E e : b) {
					v.add(e.getTargetVertex());
				}
				
				int originalSize2 = v.size();
				HashSet<V> hashSet2 = new HashSet<V>(v);
				int withoutDuplicates2 = hashSet2.size();
				int numOfDuplicates2 = originalSize2 - withoutDuplicates2;
				
				if(numOfDuplicates2 > 0)
					return false;
			}
		} 
		
		
		return true;
	}

//	public static <
//		V extends Vertex<V, E, F>,
//		E extends Edge<V, E, F>,
//		F extends Face<V, E, F>
//	> E getPositiveEdge(HalfEdgeDataStructure<V,E,F> hds, int selectedEdgeIndex) {
//		int i = 0;
//		for(E e : hds.getPositiveEdges()) {
//			if(i == selectedEdgeIndex)
//				return e;
//			i++;
//		}
//		return null;
//	}

	
}
