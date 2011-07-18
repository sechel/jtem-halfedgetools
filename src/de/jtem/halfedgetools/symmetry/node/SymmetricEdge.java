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


import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.type.Bundle;
import de.jtem.halfedgetools.adapter.type.Bundle.BundleType;
import de.jtem.halfedgetools.adapter.type.Bundle.DisplayType;
import de.jtem.halfedgetools.algorithm.alexandrov.delaunay.decorations.IsFlippable;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;
import de.jtem.halfedgetools.util.TriangulationException;

public abstract class SymmetricEdge <
V extends SymmetricVertex<V, E, F>, 
E extends SymmetricEdge<V, E, F> , 
F extends SymmetricFace<V, E, F>
// not sure about JREdge vs. Edge
> extends Edge<V, E, F> implements IsFlippable {
	
	protected int	flipCount = 0;
	
	
	public CuttingInfo<V, E, F> getBoundaryCycleInfo() {
		return ((SymmetricHDS<V, E, F>)getHalfEdgeDataStructure()).getBoundaryCycles();
	}
	

	public CuttingInfo<V, E, F> getSymmetryCycleInfo() {
		return ((SymmetricHDS<V, E, F>)getHalfEdgeDataStructure()).getSymmetryCycles();
	}
	
	public void setFlipCount(int n) {
		flipCount = n;
	}
	
	@Override
	@Bundle(dimension=1,type=BundleType.Value, display=DisplayType.List, name="flip")
	public int getFlipCount() {
		return flipCount;
	}

	@Override
	public void resetFlipCount() {
		flipCount = 0;
	}
	
	public int getNr() {
		F f = getLeftFace(); // should not be necessary
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		E e = boundary.get(0);
		
		if(e.isRightIncomingOfSymmetryCycle() == null) {
			e = e.getNextEdge();
		} if(e.isRightIncomingOfSymmetryCycle() == null) {
			e = e.getNextEdge();
		}
		
		return 0;
	}
	
	/**
	 *  TODO only works for triangles! loop through face boundary and sort(?) for general case
	 *  a = 0 corresponds to embedded start
	 *  a = 1 corresponds to embedded target
	 * @param a
	 * @return
	 */
	public double[] getEmbeddingOnEdge(double a, boolean i) {
		
		double[] toRet = null;
		
		double[] t = getTargetVertex().getEmbedding();
		double[] s = getStartVertex().getEmbedding();
		
		SymmetricEdge<V, E, F> e = this;
		
		if(i && isRightIncomingOfSymmetryCycle() != null) {
			e = getOppositeEdge();
		}
		
		DiscreteGroupElement trans = e.isRightOfSymmetryCycle();
		
		if(trans != null) {

		//	System.err.println("we are on right of cycle");

			s = trans.getMatrix().multiplyVector(s);

		}
		
		toRet = Rn.linearCombination(null, 1-a, s, a, t);
		return toRet;
		
//		int n = getNr();
//		F f = getLeftFace();
//		return f.getEmbeddingOnBoundary(n+a);
	}
	
	public double[] getEmbeddingOnEdge(double a) {
		return getEmbeddingOnEdge(a, false);
	}
	
	
	// Note: 
	// the absolute value of this must NOT correspond to the proper length 
	// (in case of intrinsic triangulation).
	public double[] getDirection() {
		
		double[] t = getTargetVertex().getEmbedding();
		double[] s = getStartVertex().getEmbedding();
		
		DiscreteGroupElement trans = isRightOfSymmetryCycle();
		if(trans != null) {
			s = trans.getMatrix().multiplyVector(s);
		} 
		return Rn.subtract(null, t, s);
	}
	
	public double[] getOppositeDirection() {
		
		double[] t = getTargetVertex().getEmbedding();
		double[] s = getStartVertex().getEmbedding();
		
		DiscreteGroupElement trans = isRightOfSymmetryCycle();
		if(trans != null) {
			s = trans.getMatrix().getInverse().multiplyVector(s);
		} 
		return Rn.subtract(null, t, s);
	}
	
	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.List, name="boundary")
	public boolean isBoundaryEdge() {
		
		CuttingInfo<V,E,F> ci = getBoundaryCycleInfo();
		
		if(ci == null) {
			System.err.println("boundary cycle info not set, defaulting to true");
			return false;
		}
		
		for(Set<E> cycle : ci.paths.keySet()) {
						
			if(cycle.contains(getOppositeEdge()) || cycle.contains(this)) {
				return true;
			}
		}
		
		return false;

	}

	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.List, name="cone?")
	public Boolean isConeEdge() {
		
		CuttingInfo<V,E,F> ci = getBoundaryCycleInfo();
		
		Boolean g1 = false;
		Boolean g2 = false;
		
		if(ci == null) {
			System.err.println("boundary cycle info not set, defaulting to true");
			return null;
		} else {
			// FIXME should not be necessary
			E eo = getOppositeEdge();
			E e = eo.getOppositeEdge();
			g1 = (Boolean)ci.isRightIncomingOnCycle(e);
			g2 = (Boolean)ci.isRightIncomingOnCycle(eo);

	
		}
		if(g2 != null)
			return g2; //inverse?
		if(g1 != null)
			return g1;
		return false;
	}
	
	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.List, name="ros?")
	public DiscreteGroupElement isRightOfSymmetryCycle() {

		CuttingInfo<V,E,F> ci = getSymmetryCycleInfo();

		DiscreteGroupElement g1 = null;
		DiscreteGroupElement g2 = null;
		if(ci == null) {
			System.err.println("symmetry cycle info not set, defaulting to true");
			return null;
		} else {
			// FIXME should not be necessary
			E eo = getOppositeEdge();
			E e = eo.getOppositeEdge();
			g1 = (DiscreteGroupElement)(ci.isRightIncomingOnCycle(e));
			g2 = (DiscreteGroupElement)(ci.isRightIncomingOnCycle(eo));
	
		}
		if(g2 != null)
			return g2.getInverse();
		if(g1 != null)
			return g1;
		return null;
	}

	public DiscreteGroupElement isRightIncomingOfSymmetryCycle() {

		CuttingInfo<V,E,F> ci = getSymmetryCycleInfo();

		DiscreteGroupElement g1 = null;
		if(ci == null) {
			System.err.println("symmetry cycle info not set, defaulting to true");
			return null;
		} else {
			// FIXME should not be necessary
			E eo = getOppositeEdge();
			E e = eo.getOppositeEdge();
			g1 = (DiscreteGroupElement)(ci.isRightIncomingOnCycle(e));
	
		}
		if(g1 != null)
			return g1;
		return null;
	}


	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.Label, name="sc")
	public boolean isSymmetryEdge() {
		CuttingInfo<V,E,F> ci = getSymmetryCycleInfo();
		if(ci == null) {
			System.err.println("symmetry cycle info not set, defaulting to true");
			return false;
		}
		
		for(Set<E> cycle : ci.paths.keySet()) {
						
			if(cycle.contains(getOppositeEdge()) || cycle.contains(this)) {
				return true;
			}
		}
		
		return false;

	}
	
	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.Label, name="sc")
	public boolean isSymmetryHalfEdge() {
		CuttingInfo<V,E,F> ci = getSymmetryCycleInfo();
		if(ci == null) {
			System.err.println("symmetry cycle info not set, defaulting to true");
			return false;
		}
		
		for(Set<E> cycle : ci.paths.keySet()) {
						
			if(cycle.contains(this)) {
				return true;
			}
		}
		
		return false;

	}
	
	@Bundle(dimension=1, type=BundleType.Transformation, display=DisplayType.Geometry, name="sc")
	public DiscreteGroupElement getSymmetryCycle() {
		CuttingInfo<V,E,F> ci = getSymmetryCycleInfo();
		if(ci == null) {
			return null;
		}

		for(Set<E> cycle : ci.paths.keySet()) {
			
			if(cycle.contains(getOppositeEdge()) || cycle.contains(this)) {
				return (DiscreteGroupElement)ci.paths.get(cycle);
			}
		}
		
		return null;

	}
	
	
	
	@Override
	public void flip() throws TriangulationException{
//		if (!ConsistencyCheck.isValidSurface(getHalfEdgeDataStructure()))
//			System.err.println("No valid surface before flip()");
//		if (!Consistency.checkConsistency(getHalfEdgeDataStructure()))
//			System.err.println("surface corrupted before flip()");
		F leftFace = getLeftFace();
		F rightFace = getRightFace();
		if (leftFace == rightFace) {
			System.err.println("leftFace == rightFace");
			return;
		}
			
		E a1 = getOppositeEdge().getNextEdge();
		E a2 = a1.getNextEdge();
		E b1 = getNextEdge();
		E b2 = b1.getNextEdge();
		
		V v3 = a1.getTargetVertex();
		V v4 = b1.getTargetVertex();
		
		//new connections
		linkNextEdge(a2);
		linkPreviousEdge(b1);
		getOppositeEdge().linkNextEdge(b2);
		getOppositeEdge().linkPreviousEdge(a1);
		setTargetVertex(v3);
		getOppositeEdge().setTargetVertex(v4);
		
		a2.linkNextEdge(b1);
		b2.linkNextEdge(a1);
		
		//set faces
		b2.setLeftFace(rightFace);
		a2.setLeftFace(leftFace);
		
		// TODO this should add support for "edge on multiple cycles" case
		Set<E> newPath = null;
		Set<E> cycleToReplace = null;
		
		for(Set<E> cycle : getSymmetryCycleInfo().paths.keySet()) {
			
			if(cycle.contains(this)) {
				newPath = new HashSet<E>(cycle);
				cycleToReplace = cycle;
				newPath.remove(this);
				newPath.add(a1);
				newPath.add(a2);
//				double[] np = Rn.add(null, a1.getTargetVertex().getEmbedding(), a1.getDirection());
//				a1.getTargetVertex().setEmbedding(np);
			} else if(cycle.contains(getOppositeEdge())) {
				newPath = new HashSet<E>(cycle);
				cycleToReplace = cycle;
				newPath.remove(getOppositeEdge());
				newPath.add(b1);
				newPath.add(b2);
//				double[] np = Rn.add(null, b1.getTargetVertex().getEmbedding(), b1.getDirection());
//				b1.getTargetVertex().setEmbedding(np);
			}
		}
		
		if(newPath != null) {
			Object o = getSymmetryCycleInfo().paths.get(cycleToReplace);
			getSymmetryCycleInfo().paths.remove(cycleToReplace);
			getSymmetryCycleInfo().paths.put(newPath,o);
		}
		
		
		flipCount++;
		
//		if (!ConsistencyCheck.isValidSurface(getHalfEdgeDataStructure()))
//			System.err.println("No valid surface after flip()");
//		if (!Consistency.checkConsistency(getHalfEdgeDataStructure()))
//			System.err.println("surface corrupted after flip()");
	}
	
}
