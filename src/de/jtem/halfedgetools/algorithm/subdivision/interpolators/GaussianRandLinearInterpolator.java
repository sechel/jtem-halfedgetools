package de.jtem.halfedgetools.algorithm.subdivision.interpolators;

import java.util.Random;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.algorithm.delaunay.decorations.HasLengthSquared;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class GaussianRandLinearInterpolator extends Interpolator{
	@Override
	public<
	V extends JRVertex<V,E,F>,
	E extends JREdge<V,E,F> & HasLengthSquared,
	F extends JRFace<V,E,F>
	> void interpolate(E e,V v) {	
		
	
		Random r = new Random();
		
		double p = (r.nextGaussian()+3)/6.0;
		// TODO: use proper cutoff here
		if(p < 0.1) p = 0.1;
		if(p > 0.9) p = 0.9;
		
		v.position=Rn.linearCombination(null
				, p, e.getTargetVertex().position
				,1-p,e.getStartVertex().position);
	}
}
