package de.jtem.halfedgetools.symmetry.adapters;

import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
public class SymmetricSymmetryFaceColorAdapter
<V extends SymmetricVertex<V,E,F>, E extends SymmetricEdge<V,E,F>, F extends SymmetricFace<V,E,F>>
	implements ColorAdapter2Ifs<F>  {
	private final AdapterType typ = AdapterType.FACE_ADAPTER;

	public AdapterType getAdapterType() {
		return typ;
	}

	@SuppressWarnings("unchecked")
	public double[] getColor(F node) {

		if(typ==AdapterType.FACE_ADAPTER){
			for(E e : HalfEdgeUtilsExtra.getBoundary(node)) {
				if(e.isRightOfSymmetryCycle() != null)
					return new double[] {0, 1,0,0};
			}
			
			return new double[]{0.8,0.8,0.8,1};
		}

		return new double[]{0,0,1,1};
	}


}
