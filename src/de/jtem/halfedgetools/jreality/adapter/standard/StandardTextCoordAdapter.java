package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public class StandardTextCoordAdapter implements TextCoordsAdapter2Ifs<Node<?, ?, ?>> ,TextCoordsAdapter2Heds<Node<?, ?, ?>> {
	private final AdapterType typ;
	public AdapterType getAdapterType() {
		return typ;
	}
	public StandardTextCoordAdapter(AdapterType typ) {
		this.typ=typ;
	}
	/** This adapter can write and read TextureCoordinates
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 * @author gonska
	 * @param adapterType
	 */
	@SuppressWarnings("unchecked")
	public double[] getTextCoordinate(Node<?, ?, ?> node) {
		if(typ==AdapterType.EDGE_ADAPTER){
			if(((JREdge)node).textCoord==null)
				return new double[]{0,0,0};
			return((JREdge)node).textCoord;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			if(((JRFace)node).textCoord==null)
				return new double[]{0,0,0};
			return((JRFace)node).textCoord;
		}
		if(typ==AdapterType.VERTEX_ADAPTER){
			if(((JRVertex)node).textCoord==null)
				return new double[]{0,0,0};
			return((JRVertex)node).textCoord;
		}
		return new double[]{0,0,0};
	}
	@SuppressWarnings("unchecked")
	public void setTextCoordinate(Node<?, ?, ?> node, double[] textCoords) {
		if(typ==AdapterType.VERTEX_ADAPTER){
			((JRVertex)node).textCoord=textCoords;
		}
		if(typ==AdapterType.EDGE_ADAPTER){
			((JREdge)node).textCoord=textCoords;
		}
		if(typ==AdapterType.FACE_ADAPTER){
			((JRFace)node).textCoord=textCoords;
		}
	}
	
}
	