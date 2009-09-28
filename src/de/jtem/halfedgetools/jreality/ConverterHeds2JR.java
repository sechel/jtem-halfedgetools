package de.jtem.halfedgetools.jreality;

import java.util.LinkedList;
import java.util.List;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataList;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.PointSizeAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Ifs;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;


public class ConverterHeds2JR 
<
V extends Vertex<V, E, F>,
E extends Edge<V, E, F>, 
F extends Face<V, E, F> > {
	protected List<double[]> coordinates=null;
	protected List<double[]> colors=null;
	protected List<double[]> normals=null;
	protected List<double[]> textCoords=null;
	protected List<String> labels=null;
	protected List<Double> radius=null;
	protected List<Double> pointSize=null;

	public IndexedFaceSet ifs;
	public HalfEdgeDataStructure<V,E,F> heds;
	/** 
	 * can convert a HalfEdgeDataStructure 
	 * to an IndexedFaceSet(JReality) 
	 * 
	 * the H.E.D.S. must be parametrised
	 * with the same classes as the converter
	 */
	public ConverterHeds2JR() {
	}

	/**
	 * this converts a given H.E.D.S to an IndexedFaceSet
	 * 
	 * remark:Adapters are nescecary to access the Data of the H.E.D.S.
	 *  you can use adapters as subtypes of the following types:
	 *  ColorAdapter2Ifs			CoordinateAdapter2Ifs 
	 *  LabelAdapter2Ifs			NormalAdapter2Ifs
	 *  PointSizeAdapter2Ifs		RelRadiusAdapter2Ifs
	 *  TextCoordsAdapter2Ifs
	 *  
	 * remark:every adapter supports only one geometry part:
	 *   Vertices, Edges or Faces
	 *    
	 * if there is no adapter for an attribute of a geometry part, 
	 *  then this attribute will not appear in the result under this 
	 *  geometry part
	 *  
	 * @param heds
	 * @param adapters (a CoordinateAdapter2Ifs for Vertices must be given)
	 * @return converted heds as IndexedFaceSet
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public IndexedFaceSet heds2ifs(HalfEdgeDataStructure<V, E, F> heds
			, Adapter... adapters) throws IllegalArgumentException{
		// seperate adapters
		List<Adapter> vertexAdapters = new LinkedList<Adapter>(); 
		List<Adapter> faceAdapters = new LinkedList<Adapter>(); 
		List<Adapter> edgeAdapters = new LinkedList<Adapter>();
		boolean hasCoords=false;
		for(Adapter a: adapters){
			if(a.getAdapterType()==AdapterType.VERTEX_ADAPTER){
				vertexAdapters.add(a);
				if (a instanceof CoordinateAdapter2Ifs)
					hasCoords=true;
			}
			else if (a.getAdapterType()==AdapterType.EDGE_ADAPTER)
				edgeAdapters.add(a);
			else if (a.getAdapterType()==AdapterType.FACE_ADAPTER)
				faceAdapters.add(a);
		}	
		if(!hasCoords) throw new IllegalArgumentException("No coordinateAdapter found");
		// some facts
		int numV =heds.numVertices();	if (numV==0) return null; 
		int numHE =heds.numEdges();
		int numE =numHE/2;
		int numF =heds.numFaces();
		IndexedFaceSet ifs = new IndexedFaceSet();
		ifs.setNumPoints(numV);
		ifs.setNumEdges(numE);
		ifs.setNumFaces(numF);
		// Vertices
		resetData();
		for (int i = 0; i < numV; i++){
			V v= heds.getVertex(i);
			readOutData(vertexAdapters,v);
		}
		ifs.setVertexAttributes(Attribute.COORDINATES,getdoubleArrayArray(coordinates));
		if(colors.size()>0) ifs.setVertexAttributes(Attribute.COLORS,getdoubleArrayArray(colors)); 
		if(normals.size()>0) ifs.setVertexAttributes(Attribute.NORMALS,getdoubleArrayArray(normals)); 
		if(textCoords.size()>0) ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES,getdoubleArrayArray(textCoords)); 
		if(labels.size()>0) ifs.setVertexAttributes(Attribute.LABELS,getStringArray(labels));
		if(radius.size()>0) ifs.setVertexAttributes(Attribute.RELATIVE_RADII,getdoubleArray(radius));
		if(pointSize.size()>0) ifs.setVertexAttributes(Attribute.POINT_SIZE,getdoubleArray(pointSize));
		// Edges
		resetData();
		int[][] edgeIndis= new int[numE][2];
		
		int k=0;
		for (int i = 0; i < numHE; i++) {
			E e= heds.getEdge(i);
			if(e.isPositive()){
				edgeIndis[k][0]=e.getOppositeEdge().getTargetVertex().getIndex();
				edgeIndis[k][1]=e.getTargetVertex().getIndex();
				k++;
				readOutData(edgeAdapters, e);
			}
		}
		ifs.setEdgeAttributes(Attribute.INDICES,new IntArrayArray.Array(edgeIndis)); 
		if(coordinates.size()>0)ifs.setEdgeAttributes(Attribute.COORDINATES,getdoubleArrayArray(coordinates));
		if(colors.size()>0) ifs.setEdgeAttributes(Attribute.COLORS,getdoubleArrayArray(colors)); 
		if(normals.size()>0) ifs.setEdgeAttributes(Attribute.NORMALS,getdoubleArrayArray(normals)); 
		if(textCoords.size()>0) ifs.setEdgeAttributes(Attribute.TEXTURE_COORDINATES,getdoubleArrayArray(textCoords)); 
		if(labels.size()>0) ifs.setEdgeAttributes(Attribute.LABELS,getStringArray(labels));
		if(radius.size()>0) ifs.setEdgeAttributes(Attribute.RELATIVE_RADII,getdoubleArray(radius));
		if(pointSize.size()>0) ifs.setEdgeAttributes(Attribute.POINT_SIZE,getdoubleArray(pointSize));
		// Faces
		///
		resetData();
		int[][] faceIndices = new int[numF][];
		for (int i = 0; i < numF; i++) {
			F f = heds.getFace(i);
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			int[] face = new int[b.size()];
			int j = 0;
			for (E e : b) {
				face[j] = e.getTargetVertex().getIndex();
				j++;
			}
			readOutData(faceAdapters, f);
			faceIndices[i] = face;
		}
		ifs.setFaceAttributes(Attribute.INDICES,new IntArrayArray.Array(faceIndices)); 
		if(coordinates.size()>0)ifs.setFaceAttributes(Attribute.COORDINATES,getdoubleArrayArray(coordinates));
		if(colors.size()>0) ifs.setFaceAttributes(Attribute.COLORS,getdoubleArrayArray(colors)); 
		if(normals.size()>0) ifs.setFaceAttributes(Attribute.NORMALS,getdoubleArrayArray(normals)); 
		if(textCoords.size()>0) ifs.setFaceAttributes(Attribute.TEXTURE_COORDINATES,getdoubleArrayArray(textCoords)); 
		if(labels.size()>0) ifs.setFaceAttributes(Attribute.LABELS,getStringArray(labels));
		if(radius.size()>0) ifs.setFaceAttributes(Attribute.RELATIVE_RADII,getdoubleArray(radius));
		if(pointSize.size()>0) ifs.setFaceAttributes(Attribute.POINT_SIZE,getdoubleArray(pointSize));
		// 
		return ifs;
	}
	private static DataList getdoubleArrayArray(List<double[]> dl){
		int len= dl.size();
		double[][] data= new double[len][];
		int i=0;
		for(double[] doub : dl){
			data[i]=doub;
			i++;
		}
		return new DoubleArrayArray.Array(data);
	}
	private static DataList getdoubleArray(List<Double> dl){
		int len= dl.size();
		double[]data= new double[len];
		int i=0;
		for(double doub : dl){
			data[i]=doub;
			i++;
		}
		return new DoubleArray(data);
	}
	private static DataList getStringArray(List<String> dl){
		int len= dl.size();
		String[]data= new String[len];
		int i=0;
		for(String s : dl){
			data[i]=s;
			i++;
		}
		return new StringArray(data);
	}
	
	@SuppressWarnings("unchecked")
	private void readOutData(List<Adapter> adapters,Node<?,?,?> n){
		for(Adapter a: adapters){
			if (a instanceof CoordinateAdapter2Ifs) {
				CoordinateAdapter2Ifs<Node<?, ?, ?>> c = (CoordinateAdapter2Ifs) a;
				coordinates.add(c.getCoordinate(n));
			}
			if (a instanceof ColorAdapter2Ifs) {
				ColorAdapter2Ifs<Node<?, ?, ?>> c = (ColorAdapter2Ifs) a;
				colors.add(c.getColor(n));
			}
			if (a instanceof NormalAdapter2Ifs){
				NormalAdapter2Ifs<Node<?, ?, ?>> c = (NormalAdapter2Ifs) a;
				normals.add(c.getNormal(n));
			}
			if (a instanceof TextCoordsAdapter2Ifs ){
				TextCoordsAdapter2Ifs<Node<?, ?, ?>> c = (TextCoordsAdapter2Ifs) a;
				textCoords.add(c.getTextCoordinate(n));
			}
			if (a instanceof LabelAdapter2Ifs){
				LabelAdapter2Ifs<Node<?, ?, ?>> c = (LabelAdapter2Ifs) a;
				labels.add(c.getLabel(n));
			}
			if (a instanceof RelRadiusAdapter2Ifs){
				RelRadiusAdapter2Ifs<Node<?, ?, ?>> c = (RelRadiusAdapter2Ifs) a;
				radius.add(c.getReelRadius(n));
			}
			if (a instanceof PointSizeAdapter2Ifs){
				PointSizeAdapter2Ifs<Node<?, ?, ?>> c = (PointSizeAdapter2Ifs) a;
				pointSize.add(c.getPointSize(n));
			}
		}
	}
	
	private void resetData(){
		coordinates=new LinkedList<double[]>();
		colors=new LinkedList<double[]>();
		normals=new LinkedList<double[]>();
		textCoords=new LinkedList<double[]>();
		labels=new LinkedList<String>();
		radius=new LinkedList<Double>();
		pointSize=new LinkedList<Double>();
	}
}
