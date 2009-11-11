package de.jtem.halfedgetools.symmetry.adapters;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Ifs;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;

public class SymmetricNormalAdapter implements NormalAdapter2Ifs<Node<?, ?, ?>> ,NormalAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	public AdapterType getAdapterType() {
		return typ;
	}
	/** This adapter can write and read Normals
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public SymmetricNormalAdapter(AdapterType typ) {
		this.typ=typ;
	}
	@SuppressWarnings("unchecked")
	public double[] getNormal(Node<?, ?, ?> node) {

		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((SymmetricVertex)node).normal==null)
				return new double[]{0,0,0};
			return((SymmetricVertex)node).normal;
		}
		return new double[]{0,0,0};
	}
	public void setNormal(Node<?, ?, ?> node, double[] normal) {
		if(typ==AdapterType.VERTEX_ADAPTER){
//			((SymmetricVertex)node).normal=normal;
		}
	}
}
