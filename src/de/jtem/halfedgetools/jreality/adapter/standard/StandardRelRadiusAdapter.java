package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardRelRadiusAdapter implements RelRadiusAdapter2Ifs<Node<?, ?, ?>>, RelRadiusAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	public AdapterType getAdapterType() {
		return typ;
	}
	/** This adapter can write and read the RelativeRadii  
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public StandardRelRadiusAdapter(AdapterType typ) {
		this.typ=typ;
	}
	@SuppressWarnings("unchecked")
	public double getReelRadius(Node<?, ?, ?> node){
		if(typ==AdapterType.EDGE_ADAPTER){
			return((JREdge)node).radius;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			return((JRFace)node).radius;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			return((JRVertex)node).radius;
		}
		return 0;
	}
	@SuppressWarnings("unchecked")
	public void setRelRadius(Node<?, ?, ?> node, double relRadius) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).radius=relRadius;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).radius=relRadius;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).radius=relRadius;
		}
	}
}