package de.jtem.halfedgetools.symmetry.node;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.util.HalfEdgeUtils;

public abstract class SymmetricFace < 
V extends SymmetricVertex<V, E, F>, 
E extends SymmetricEdge<V, E, F> , 
F extends SymmetricFace<V, E, F>
> extends Face<V, E, F> {

	
	protected DiscreteGroupElement groupSymmetry = null;
	
	public DiscreteGroupElement getSymmetry() {
		return groupSymmetry;	
	}
	
	public void setSymmetry(DiscreteGroupElement s) {
		groupSymmetry = s;
	}
	
	public boolean hasSymmetry() {
		return groupSymmetry != null;
	}

	// TODO fix for n>3
	public double[] getEmbeddingOnBoundary(double t) {
		F f = getBoundaryEdge().getLeftFace(); // should not be necessary
		List<E> boundary = HalfEdgeUtils.boundaryEdges(f);
		E e = boundary.get(0);
		
		int n = boundary.size();
		
		if(e.isRightIncomingOfSymmetryCycle() == null)
			e = e.getNextEdge();
		if(e.isRightIncomingOfSymmetryCycle() == null)
			e = e.getNextEdge();
		
		double[][] coords = new double[n][];
		
		coords[0]   = Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
		coords[1] = Rn.add(null, coords[0], e.getNextEdge().getDirection());
		coords[2] = Rn.add(null, coords[1], e.getPreviousEdge().getDirection());
		
		int sel = ((int)Math.floor(t)) % n;
		
		double rest = t - Math.floor(t);
		return Rn.linearCombination(null, rest, coords[sel%n], 1-rest, coords[(sel+1)%n]);
	}
}
