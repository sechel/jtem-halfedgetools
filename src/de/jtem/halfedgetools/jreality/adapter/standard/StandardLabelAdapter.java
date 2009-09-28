package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardLabelAdapter implements LabelAdapter2Ifs<Node<?, ?, ?>>, LabelAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	/** This adapter can write and read Labels
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public StandardLabelAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}
	@SuppressWarnings("unchecked")
	public String getLabel(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			return((JREdge)node).label;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			return((JRFace)node).label;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			return((JRVertex)node).label;
		}
		return "";
		
	}
	@SuppressWarnings("unchecked")
	public void setLabel(Node<?, ?, ?> node, String label) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).label=label;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).label=label;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).label=label;
		}
	}

}
