package de.jtem.halfedgetools.symmetry.adapters;

import de.jreality.math.Pn;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;


public class SymmetricCoordinateDehomogenizingAdapter implements CoordinateAdapter2Ifs<Node<?, ?, ?>>, CoordinateAdapter2Heds<Node<?, ?, ?>> {
	
	private final AdapterType typ;
	/** This adapter can write and read Coordinates
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public SymmetricCoordinateDehomogenizingAdapter(AdapterType typ) {
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
			
//			double[] c = Pn.dehomogenize(null,((SymmetricVertex)node).getEmbedding());
//			return new double[] {c[0],c[1],c[2]};
			return ((SymmetricVertex)node).getEmbedding();
		}
		return new double[]{0,0,0};
	}
	/** the coordinates of the node
	 */
	@SuppressWarnings("unchecked")
	public void setCoordinate(Node<?, ?, ?> node, double[] coord) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			double[] c = Pn.dehomogenize(null,coord);
			((SymmetricVertex)node).setEmbedding(new double[] {c[0],c[1],c[2]});
		}

	}
}
