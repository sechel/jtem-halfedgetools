package de.jtem.halfedgetools.jreality.adapter;


public interface Adapter {
	/** 
	 * Adapters are nescecary to access the Data of the H.E.D.S.
	 *  you can use adapters as subtypes of the following types:
	 *  ColorAdapter2Ifs			ColorAdapter2Heds
	 *  CoordinateAdapter2Ifs 		CoordinateAdapter2Heds 
	 *  LabelAdapter2Ifs			LabelAdapter2Heds
	 *  NormalAdapter2Ifs			NormalAdapter2Heds
	 *  PointSizeAdapter2Ifs		PointSizeAdapter2Heds
	 *  RelRadiusAdapter2Ifs		RelRadiusAdapter2Heds
	 *  TextCoordsAdapter2Ifs		TextCoordsAdapter2Heds
	 *  
	 *  ..2Ifs are used for reading Data 
	 *  ..2Heds are used for writing Data 
	 *  every adapter supports one of the following:
	 *   vertices, edges or faces
	 *  
	 * @author gonska
	 *
	 */
	public static enum AdapterType{
		VERTEX_ADAPTER,
		EDGE_ADAPTER,
		FACE_ADAPTER
	}
	public AdapterType getAdapterType();
	
}
