package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;


public class StandardCoordinateAdapter implements CoordinateAdapter2Ifs<Node<?, ?, ?>>, CoordinateAdapter2Heds<Node<?, ?, ?>> {
	
	private final AdapterType typ;
	/** This adapter can write and read Coordinates
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public StandardCoordinateAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}
	/** the coordinates of the node
	 */
	@SuppressWarnings("unchecked")
	public double[] getCoordinate(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			if(((JREdge)node).position==null)
				return new double[]{0,0,0};
			return((JREdge)node).position;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			if(((JRFace)node).position==null)
				return new double[]{0,0,0};
			return((JRFace)node).position;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((JRVertex)node).position==null)
				return new double[]{0,0,0};
			return((JRVertex)node).position;
		}
		return new double[]{0,0,0,0};
	}
	/** the coordinates of the node
	 */
	@SuppressWarnings("unchecked")
	public void setCoordinate(Node<?, ?, ?> node, double[] coord) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).position=coord;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).position=coord;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).position=coord;
		}
	}
}
