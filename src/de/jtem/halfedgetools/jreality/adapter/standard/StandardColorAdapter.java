package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;
public class StandardColorAdapter implements ColorAdapter2Ifs<Node<?, ?, ?>> ,ColorAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	/** This adapter can write and read Colors  
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	public StandardColorAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}
	/** the color of the node
	 */
	@SuppressWarnings("unchecked")
	public double[] getColor(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			if(((JREdge)node).color==null)
				return new double[]{0,0,0};
			return((JREdge)node).color;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			if(((JRFace)node).color==null)
				return new double[]{0,0,0};
			return((JRFace)node).color;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((JRVertex)node).color==null)
				return new double[]{0,0,0};
			return((JRVertex)node).color;
		}
		return new double[]{0,0,0};
	}
	/** the color of the node
	 */
	@SuppressWarnings("unchecked")
	public void setColor(Node<?, ?, ?> node, double[] color) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).color=color;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).color=color;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).color=color;
		}
	}
}
