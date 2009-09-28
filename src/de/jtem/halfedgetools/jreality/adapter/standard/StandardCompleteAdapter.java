package de.jtem.halfedgetools.jreality.adapter.standard;

import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.PointSizeAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.PointSizeAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;
// zu krass: die unnoetige Datenfuelle macht alles zu langsam 
public class StandardCompleteAdapter implements 
	ColorAdapter2Ifs<Node<?, ?, ?>>,ColorAdapter2Heds<Node<?, ?, ?>>,
	CoordinateAdapter2Ifs<Node<?, ?, ?>>,CoordinateAdapter2Heds<Node<?, ?, ?>>,
	LabelAdapter2Ifs<Node<?, ?, ?>>,LabelAdapter2Heds<Node<?, ?, ?>>,
	NormalAdapter2Ifs<Node<?, ?, ?>>,NormalAdapter2Heds<Node<?, ?, ?>>,
	PointSizeAdapter2Ifs<Node<?, ?, ?>>,PointSizeAdapter2Heds<Node<?, ?, ?>>,
	RelRadiusAdapter2Ifs<Node<?, ?, ?>>, RelRadiusAdapter2Heds<Node<?, ?, ?>>,
	TextCoordsAdapter2Ifs<Node<?, ?, ?>>,TextCoordsAdapter2Heds<Node<?, ?, ?>>
{
	private final AdapterType typ;
	/** This adapter can write and read all std IndexedFaceSet Attributes
	 *  from StdJRVertex,StdJREdge,StdJRFace  
	 *  it has a final blank for the adapter type
	 *   wich it should support 
	 *  remark:
	 *   this adapter is in general to mighty and produces 
	 *   to much unused data.
	 *   
	 * @author gonska
	 * @param adapterType
	 */
	public StandardCompleteAdapter(AdapterType typ) {
		this.typ=typ;
	}
	public AdapterType getAdapterType() {
		return typ;
	}
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
