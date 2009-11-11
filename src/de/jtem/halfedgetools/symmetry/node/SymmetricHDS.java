package de.jtem.halfedgetools.symmetry.node;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.functional.alexandrov.SurfaceUtility;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.halfedgetools.util.PathUtility;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;
import de.jtem.halfedgetools.util.surfaceutilities.SurfaceException;

public class SymmetricHDS<
V extends SymmetricVertex<V, E, F>, 
E extends SymmetricEdge<V, E, F> , 
F extends SymmetricFace<V, E, F>
>  extends HalfEdgeDataStructure<V, E, F>{

	
	public SymmetricHDS(Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		super(vClass, eClass, fClass);
	}

	private DiscreteGroup group = null;
	private CuttingInfo<V, E, F> symmetryCycles = new CuttingInfo<V, E, F>();
	private CuttingInfo<V,E,F> boundaryCycles = new CuttingInfo<V,E,F>();
	
	private Set<V> coneVertices = new HashSet<V>();
	
	public void setGroup(DiscreteGroup g) {
		group = g;
	}
	
	public DiscreteGroup getGroup() {
		return group;
	}

	public void setSymmetryCycles(CuttingInfo<V,E,F> symmetryCycles) {
		this.symmetryCycles = symmetryCycles;
	}

	public CuttingInfo<V, E, F> getSymmetryCycles() {
		return symmetryCycles;
	}

	public void setBoundaryCycles(CuttingInfo<V,E,F> boundaryCycles) {
		this.boundaryCycles = boundaryCycles;
	}

	public CuttingInfo<V,E,F> getBoundaryCycles() {
		return boundaryCycles;
	}
	
	public Set<V> getBoundaryVertices() {
		Set<V> bv = new HashSet<V>();
		
		for(Set<E> path : getBoundaryCycles().paths.keySet()) {
			bv.addAll(PathUtility.getUnorderedVerticesOnPath(path));
		}
		
		return bv;
	}
	
	public void updateWithCones() throws SurfaceException {
//		System.err.println("Orienting boundary");
		SurfaceUtility.linkBoundary(this);

		System.err.println("Filling holes...");
		List<F> holes = SurfaceUtility.fillHoles(this);

		System.err.println("Filled " + holes.size() + " holes");
		
		Set<V> coneVerts = new HashSet<V>();

		for(F face : holes) {

			Set<E> newPath = new HashSet<E>();
			for(E e : HalfEdgeUtilsExtra.getBoundary(face)) {
				newPath.add(e);
			}
//			System.err.println("Constructing cone over face: " + face);
			V cone = HalfEdgeTopologyOperations.splitFace(face);

			coneVerts.add(cone);
			boundaryCycles.paths.put(newPath,true);
		}
		
		setConeVertices(coneVerts);
	}

	public void updateWithoutCones() throws SurfaceException {
//		System.err.println("Adding holes...");
		
		Map<Set<E>,Object> revertedPaths = new HashMap<Set<E>,Object>();
		
		for(Set<E> path : boundaryCycles.paths.keySet()) {
			
			Set<E> revPath = new HashSet<E>();
			
			if(path != null) {
				E e = path.iterator().next();
				V cone = e.getNextEdge().getTargetVertex();
				
				for(E ee : path) {
					revPath.add(ee.getOppositeEdge());
				}
				
				HalfEdgeTopologyOperations.removeVertex(cone);

				revertedPaths.put(revPath,boundaryCycles.paths.get(path));
			}
		}
		
//		boundaryCycles.paths = revertedPaths;
			
//		System.err.println("Orienting boundary");
		SurfaceUtility.linkBoundary(this);

	}

	public void setConeVertices(Set<V> iConeVertices) {
		this.coneVertices = iConeVertices;
	}

	public Set<V> getConeVertices() {
		return coneVertices;
	}
	

}
