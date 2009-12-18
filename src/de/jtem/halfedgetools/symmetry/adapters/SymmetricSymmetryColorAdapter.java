package de.jtem.halfedgetools.symmetry.adapters;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
public class SymmetricSymmetryColorAdapter implements ColorAdapter2Ifs<Node<?, ?, ?>>  {
	private final AdapterType typ;

	public SymmetricSymmetryColorAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}

	@SuppressWarnings("unchecked")
	public double[] getColor(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			if(((SymmetricEdge)node).isRightOfSymmetryCycle() != null)
				return new double[]{1,0,0,1};
			else
				return new double[] {1,1,1,1};
		}

		return new double[]{0,0,1,1};
	}

}
