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
	
	public Set<V> getInteriorVertices() {
		Set<V> bv = getBoundaryVertices();
		Set<V> allV = new HashSet<V>(getVertices());
		allV.removeAll(bv);
		Set<V> cone = getConeVertices();
		allV.removeAll(cone);
				
		return allV;
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
