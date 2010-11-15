package de.jtem.halfedgetools.jreality;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
import de.jtem.halfedgetools.adapter.type.TexturePosition;


public class ConverterHeds2JR {
	
	protected List<double[]> 
		coordinates = null,
		colors = null,
		normals = null,
		textCoords = null;
	protected List<String> 
		labels = null;
	protected List<Double> 
		radius = null,
		pointSize = null;

	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> IndexedFaceSet heds2ifs(HDS heds, AdapterSet adapters) throws AdapterException {
		return heds2ifs(heds, adapters, null);
	}

	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> IndexedFaceSet heds2ifs(HDS hds, AdapterSet adapters, Map<Integer, Edge<?,?,?>> edgeMap) throws AdapterException {
		if (!adapters.isAvailable(Position.class, hds.getVertexClass(), double[].class)) {
			throw new AdapterException("No vertex position adapter found in ConverterHeds2Jr.heds2ifs");
		}
		if (edgeMap != null) {
			edgeMap.clear();
		}
		// some facts
		int numV =hds.numVertices();	
		if (numV==0) {
			return new IndexedFaceSet(); 
		}
		int numHE =hds.numEdges();
		int numE =numHE/2;
		int numF =hds.numFaces();
		IndexedFaceSet ifs = new IndexedFaceSet();
		ifs.setNumPoints(numV);
		ifs.setNumEdges(numE);
		ifs.setNumFaces(numF);
		// Vertices
		resetData();
		for (V v : hds.getVertices()){
			try {
				readOutData(adapters, v);
			} catch (Exception e) {
				System.err.println("Error reading vertex data: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
		ifs.setVertexAttributes(Attribute.COORDINATES, getdoubleArrayArray(coordinates));
		if(colors.size() == hds.numVertices()) ifs.setVertexAttributes(Attribute.COLORS, getdoubleArrayArray(colors)); 
		if(normals.size() == hds.numVertices()) ifs.setVertexAttributes(Attribute.NORMALS, getdoubleArrayArray(normals)); 
		if(textCoords.size() == hds.numVertices()) ifs.setVertexAttributes(Attribute.TEXTURE_COORDINATES, getdoubleArrayArray(textCoords)); 
		if(labels.size() == hds.numVertices()) ifs.setVertexAttributes(Attribute.LABELS, getStringArray(labels));
		if(radius.size() == hds.numVertices()) ifs.setVertexAttributes(Attribute.RELATIVE_RADII, getdoubleArray(radius));
		if(pointSize.size() == hds.numVertices()) ifs.setVertexAttributes(Attribute.POINT_SIZE, getdoubleArray(pointSize));
		// Edges
		resetData();
		int[][] edgeIndis= new int[numE][2];
		
		int k = 0;
		for (E e : hds.getPositiveEdges()) {
			if (edgeMap != null) {
				edgeMap.put(k, e);
			}
			edgeIndis[k][0]=e.getOppositeEdge().getTargetVertex().getIndex();
			edgeIndis[k][1]=e.getTargetVertex().getIndex();
			try {
				readOutData(adapters, e);
			} catch (Exception ex) {
				System.err.println("Error reading edge data: " + ex.getLocalizedMessage());
				ex.printStackTrace();
			}
			k++;
		}
		ifs.setEdgeAttributes(Attribute.INDICES, new IntArrayArray.Array(edgeIndis)); 
		if(coordinates.size() == hds.numEdges()/2)ifs.setEdgeAttributes(Attribute.COORDINATES, getdoubleArrayArray(coordinates));
		if(colors.size() == hds.numEdges()/2) ifs.setEdgeAttributes(Attribute.COLORS, getdoubleArrayArray(colors)); 
		if(normals.size() == hds.numEdges()/2) ifs.setEdgeAttributes(Attribute.NORMALS, getdoubleArrayArray(normals)); 
		if(textCoords.size() == hds.numEdges()/2) ifs.setEdgeAttributes(Attribute.TEXTURE_COORDINATES, getdoubleArrayArray(textCoords)); 
		if(labels.size() == hds.numEdges()/2) ifs.setEdgeAttributes(Attribute.LABELS, getStringArray(labels));
		if(radius.size() == hds.numEdges()/2) ifs.setEdgeAttributes(Attribute.RELATIVE_RADII, getdoubleArray(radius));
		if(pointSize.size() == hds.numEdges()/2) ifs.setEdgeAttributes(Attribute.POINT_SIZE, getdoubleArray(pointSize));
		// Faces
		///
		resetData();
		int[][] faceIndices = new int[numF][];
		int i = 0;
		for (F f : hds.getFaces()) {
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			int[] face = new int[b.size()];
			int j = 0;
			for (E e : b) {
				face[j++] = e.getTargetVertex().getIndex();
			}
			try {
				readOutData(adapters, f);
			} catch (Exception e) {
				System.err.println("Error reading face data: " + e.getLocalizedMessage());
				e.printStackTrace();
			}
			faceIndices[i++] = face;
		}
		ifs.setFaceAttributes(Attribute.INDICES,new IntArrayArray.Array(faceIndices)); 
		if(coordinates.size() == hds.numFaces())ifs.setFaceAttributes(Attribute.COORDINATES,getdoubleArrayArray(coordinates));
		if(colors.size() == hds.numFaces()) ifs.setFaceAttributes(Attribute.COLORS,getdoubleArrayArray(colors)); 
		if(normals.size() == hds.numFaces()) ifs.setFaceAttributes(Attribute.NORMALS,getdoubleArrayArray(normals)); 
		if(textCoords.size() == hds.numFaces()) ifs.setFaceAttributes(Attribute.TEXTURE_COORDINATES,getdoubleArrayArray(textCoords)); 
		if(labels.size() == hds.numFaces()) ifs.setFaceAttributes(Attribute.LABELS,getStringArray(labels));
		if(radius.size() == hds.numFaces()) ifs.setFaceAttributes(Attribute.RELATIVE_RADII,getdoubleArray(radius));
		if(pointSize.size() == hds.numFaces()) ifs.setFaceAttributes(Attribute.POINT_SIZE,getdoubleArray(pointSize));
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
		Adapter<double[]> texCoord = adapters.query(TexturePosition.class, n.getClass(), double[].class);
		List<Adapter<String>> labs = adapters.queryAll(Label.class, n.getClass(), String.class);
		Adapter<Double> rad = adapters.query(Radius.class, n.getClass(), Double.class); 
		Adapter<Double> size = adapters.query(Size.class, n.getClass(), Double.class);
		if (pos != null) {
			double[] posArr = pos.get(n, adapters);
			if (posArr != null) {
				removeNaN(posArr);
				coordinates.add(posArr);
			}
		}
		if (!cols.isEmpty()) {
			double[] c = {1, 1, 1, 1};
			boolean colorValid = false;
			for (Adapter<double[]> color : cols) {
				double[] colorArr = color.get(n, adapters);
				removeNaN(colorArr);
				if (colorArr == null) continue;
				for (int i = 0; i < Math.min(4, colorArr.length); i++) {
					c[i] = c[i] * colorArr[i];
				}
				colorValid = true;
			}
			if (colorValid) {
				colors.add(c);
			}
		}
		if (normal != null) {
			double[] normalArr = normal.get(n, adapters);
			if (normalArr != null) {
				removeNaN(normalArr);
				normals.add(normalArr);
			}
		}
		if (texCoord != null) {
			double[] texArr = texCoord.get(n, adapters);
			if (texArr != null) {
				removeNaN(texArr);
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
				if (radObj.isNaN()) {
					radObj = 0.0;
				}
				radius.add(radObj);
			}
		}
		if (size != null) {
			Double sizeObj = size.get(n, adapters);
			if (sizeObj != null) {
				if (sizeObj.isNaN()) {
					sizeObj = 0.0;
				}
				pointSize.add(sizeObj);
			}
		}
	}
	
	
	private void removeNaN(double[] c) {
		for (int i = 0; i < c.length; i++) {
			if (Double.isNaN(c[i])) {
				c[i] = 0;
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
