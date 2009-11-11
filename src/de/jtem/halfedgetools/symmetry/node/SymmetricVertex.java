package de.jtem.halfedgetools.symmetry.node;

import java.util.Set;

import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.jreality.Bundle;
import de.jtem.halfedgetools.jreality.Bundle.BundleType;
import de.jtem.halfedgetools.jreality.Bundle.DisplayType;
import de.jtem.halfedgetools.util.PathUtility;

public abstract class SymmetricVertex <
V extends SymmetricVertex<V, E, F>, 
E extends SymmetricEdge<V, E, F>, 
F extends SymmetricFace<V, E, F>
> extends Vertex<V, E, F> {
	
	private double[] position = null;
	public double[] normal;

	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.Debug, name="boundary?")
	public boolean isBoundaryVertex() {
		for(Set<E> p : getIncomingEdge().getBoundaryCycleInfo().paths.keySet()) {
			if(PathUtility.getUnorderedVerticesOnPath(p).contains(this)) {
				return true;
			}
		}
		return false;
	}
	
	@Bundle(dimension=1, type=BundleType.Value, display=DisplayType.Debug, name="cone?")
	public boolean isConeVertex() {
		E in = getIncomingEdge();
		if(in.isConeEdge() && in.getPreviousEdge().getPreviousEdge().isConeEdge()) {
			return true;
		}
		return false;
	}
	
	@Bundle(dimension=3, type = BundleType.Affine, display=DisplayType.Geometry, name="coord")
	public double[] getEmbedding() {
		return position;
	}
	
	public void setEmbedding(double[] p) {
		position = p;
	}
	
}
