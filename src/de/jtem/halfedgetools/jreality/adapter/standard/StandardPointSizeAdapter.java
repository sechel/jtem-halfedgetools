package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.PointSizeAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.PointSizeAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardPointSizeAdapter implements PointSizeAdapter2Ifs<Node<?, ?, ?>> ,PointSizeAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	public AdapterType getAdapterType() {
		return typ;
	}
	/** This adapter can write and read the PointSize
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public StandardPointSizeAdapter(AdapterType typ) {
		this.typ=typ;
	}
	@SuppressWarnings("unchecked")
	public double getPointSize(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			return((JREdge)node).pointSize;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			return((JRFace)node).pointSize;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			return((JRVertex)node).pointSize;
		}
		return 0;
	}
	@SuppressWarnings("unchecked")
	public void setPointSize(Node<?, ?, ?> node, double pointSize) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).pointSize=pointSize;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).pointSize=pointSize;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).pointSize=pointSize;
		}
	}
}
	