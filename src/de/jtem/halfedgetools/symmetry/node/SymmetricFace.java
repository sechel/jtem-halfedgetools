package de.jtem.halfedgetools.symmetry.node;

import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.halfedge.Face;

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
}
