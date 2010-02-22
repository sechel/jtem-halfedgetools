package de.jtem.halfedgetools.jreality;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.jreality.math.Rn;
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
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterException;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.Radius;
import de.jtem.halfedgetools.adapter.type.Size;
import de.jtem.halfedgetools.adapter.type.TexCoordinate;


public class ConverterHeds2JR {
	protected List<double[]> coordinates=null;
	protected List<double[]> colors=null;
	protected List<double[]> normals=null;
	protected List<double[]> textCoords=null;
	protected List<String> labels=null;
	protected List<Double> radius=null;
	protected List<Double> pointSize=null;

	public IndexedFaceSet ifs;
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
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> IndexedFaceSet heds2ifs(HalfEdgeDataStructure<V, E, F> heds, AdapterSet adapters, Map<E, Integer> edgeMap) throws AdapterException {
		if (!adapters.isAvailable(Position.class, heds.getVertexClass(), double[].class)) {
			throw new AdapterException("No vertex position adapter found in ConverterHeds2Jr.heds2ifs");
		}
		// seperate adapters
		if (edgeMap != null) {
			edgeMap.clear();
		}
		// some facts
		int numV =heds.numVertices();	
		if (numV==0) {
			return null; 
		}
		int numHE =heds.numEdges();
		int numE =numHE/2;
		int numF =heds.numFaces();
		IndexedFaceSet ifs = new IndexedFaceSet();
		ifs.setNumPoints(numV);
		ifs.setNumEdges(numE);
		ifs.setNumFaces(numF);
		// Vertices
		resetData();
		for (V v : heds.getVertices()){
			readOutData(adapters, v);
		}
		ifs.setVertexAttributes(Attribute.COORDINATES, getdoubleArrayArray(coordinates));
		if(colors.size() > 0) ifs.setVertexAttributes(Attribute.COLORS, getdoubleArrayArray(colors)); 
		if(normals.size() > 0) ifs.setVertexAttributes(Attribute.NORMALS, getdoubleArrayArray(normals)); 
		if(textCoords.size() > 0) ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, getdoubleArrayArray(textCoords)); 
		if(labels.size() > 0) ifs.setVertexAttributes(Attribute.LABELS, getStringArray(labels));
		if(radius.size() > 0) ifs.setVertexAttributes(Attribute.RELATIVE_RADII, getdoubleArray(radius));
		if(pointSize.size() > 0) ifs.setVertexAttributes(Attribute.POINT_SIZE, getdoubleArray(pointSize));
		// Edges
		resetData();
		int[][] edgeIndis= new int[numE][2];
		
		int k = 0;
		for (E e : heds.getPositiveEdges()) {
			if (edgeMap != null) {
				edgeMap.put(e, k);
				edgeMap.put(e.getOppositeEdge(), k);
			}
			edgeIndis[k][0]=e.getOppositeEdge().getTargetVertex().getIndex();
			edgeIndis[k][1]=e.getTargetVertex().getIndex();
			readOutData(adapters, e);
			k++;
		}
		ifs.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgeIndis)); 
		if(coordinates.size() > 0)ifs.setEdgeAttributes(Attribute.COORDINATES, getdoubleArrayArray(coordinates));
		if(colors.size() > 0) ifs.setEdgeAttributes(Attribute.COLORS, getdoubleArrayArray(colors)); 
		if(normals.size() > 0) ifs.setEdgeAttributes(Attribute.NORMALS, getdoubleArrayArray(normals)); 
		if(textCoords.size() > 0) ifs.setEdgeAttributes(Attribute.TEXTURE_COORDINATES, getdoubleArrayArray(textCoords)); 
		if(labels.size() > 0) ifs.setEdgeAttributes(Attribute.LABELS, getStringArray(labels));
		if(radius.size() > 0) ifs.setEdgeAttributes(Attribute.RELATIVE_RADII, getdoubleArray(radius));
		if(pointSize.size() > 0) ifs.setEdgeAttributes(Attribute.POINT_SIZE, getdoubleArray(pointSize));
		// Faces
		///
		resetData();
		int[][] faceIndices = new int[numF][];
		int i = 0;
		for (F f : heds.getFaces()) {
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			int[] face = new int[b.size()];
			int j = 0;
			for (E e : b) {
				face[j++] = e.getTargetVertex().getIndex();
			}
			readOutData(adapters, f);
			faceIndices[i++] = face;
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

	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	>	void readOutData(AdapterSet adapters, N n){
		Adapter<double[]> pos = adapters.query(Position.class, n.getClass(), double[].class);
		List<Adapter<double[]>> cols = adapters.queryAll(Color.class, n.getClass(), double[].class);
		Adapter<double[]> normal = adapters.query(Normal.class, n.getClass(), double[].class);
		Adapter<double[]> texCoord = adapters.query(TexCoordinate.class, n.getClass(), double[].class);
		List<Adapter<String>> labs = adapters.queryAll(Label.class, n.getClass(), String.class);
		Adapter<Double> rad = adapters.query(Radius.class, n.getClass(), Double.class); 
		Adapter<Double> size = adapters.query(Size.class, n.getClass(), Double.class);
		if (pos != null) {
			double[] posArr = pos.get(n, adapters);
			if (posArr != null) {
				coordinates.add(posArr);
			}
		}
		if (!cols.isEmpty()) {
			double[] c = {0, 0, 0};
			boolean colorValid = false;
			for (Adapter<double[]> color : cols) {
				double[] colorArr = color.get(n, adapters);
				if (colorArr == null) continue;
				Rn.add(c, c, colorArr);
				colorValid = true;
			}
			if (colorValid) {
				colors.add(c);
			}
		}
		if (normal != null) {
			double[] normalArr = normal.get(n, adapters);
			if (normalArr != null) {
				normals.add(normalArr);
			}
		}
		if (texCoord != null) {
			double[] texArr = texCoord.get(n, adapters);
			if (texArr != null) {
				textCoords.add(texArr);
			}
		}
		if (!labs.isEmpty()) {
			String lab = "";
			boolean labelValid = false;
			int i = 0;
			for (Adapter<String> label : labs) {
				String l = label.get(n, adapters);
				if (l == null) continue;
				lab += l;
				if (++i < labs.size()) {
					lab += ", ";
				}
				labelValid = true;
			}
			if (labelValid) {
				labels.add(lab);
			}
		}
		if (rad != null) {
			Double radObj = rad.get(n, adapters);
			if (radObj != null) {
				radius.add(radObj);
			}
		}
		if (size != null) {
			Double sizeObj = size.get(n, adapters);
			if (sizeObj != null) {
				pointSize.add(sizeObj);
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
