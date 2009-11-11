package de.jtem.halfedgetools.symmetry.adapters;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;


public class SymmetricCoordinateAdapter implements CoordinateAdapter2Ifs<Node<?, ?, ?>>, CoordinateAdapter2Heds<Node<?, ?, ?>> {
	
	private final AdapterType typ;
	/** This adapter can write and read Coordinates
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public SymmetricCoordinateAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}
	/** the coordinates of the node
	 */
	@SuppressWarnings("unchecked")
	public double[] getCoordinate(Node<?, ?, ?> node) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((SymmetricVertex)node).getEmbedding()==null)
				return new double[]{0,0,0};
			return((SymmetricVertex)node).getEmbedding();
		}
		return new double[]{0,0,0};
	}
	/** the coordinates of the node
	 */
	@SuppressWarnings("unchecked")
	public void setCoordinate(Node<?, ?, ?> node, double[] coord) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((SymmetricVertex)node).setEmbedding(coord);
		}

	}
}
