package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardNormalAdapter implements NormalAdapter2Ifs<Node<?, ?, ?>> ,NormalAdapter2Heds<Node<?, ?, ?>> {
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
	public StandardNormalAdapter(AdapterType typ) {
		this.typ=typ;
	}
	@SuppressWarnings("unchecked")
	public double[] getNormal(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			if(((JREdge)node).normal==null)
				return new double[]{0,0,0};
			return((JREdge)node).normal;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			if(((JRFace)node).normal==null)
				return new double[]{0,0,0};
			return((JRFace)node).normal;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((JRVertex)node).normal==null)
				return new double[]{0,0,0};
			return((JRVertex)node).normal;
		}
		return new double[]{0,0,0};
	}
	@SuppressWarnings("unchecked")
	public void setNormal(Node<?, ?, ?> node, double[] normal) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).normal=normal;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).normal=normal;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).normal=normal;
		}
	}
}
