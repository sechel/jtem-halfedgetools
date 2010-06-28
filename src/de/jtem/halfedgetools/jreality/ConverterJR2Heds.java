package de.jtem.halfedgetools.jreality;

import java.util.HashMap;
import java.util.Map;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.DataListSet;
import de.jreality.scene.data.DoubleArray;
import de.jreality.scene.data.DoubleArrayArray;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.data.StringArray;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.Radius;
import de.jtem.halfedgetools.adapter.type.Size;
import de.jtem.halfedgetools.adapter.type.TexCoordinate;


public class ConverterJR2Heds {

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> void ifs2heds(IndexedFaceSet ifs, HalfEdgeDataStructure<V, E, F> heds, AdapterSet adapters) {
		ifs2heds(ifs, heds, adapters, null);
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F> 
	> void ifs2heds(IndexedFaceSet ifs, HalfEdgeDataStructure<V, E, F> heds, AdapterSet adapters, Map<E, Integer> edgeMap) {
		heds.clear();
		AdapterSet vAdapters = adapters.querySet(heds.getVertexClass(), double[].class);
		AdapterSet eAdapters = adapters.querySet(heds.getEdgeClass(), double[].class);
		AdapterSet fAdapters = adapters.querySet(heds.getFaceClass(), double[].class);
		
		double[][][] coords=new double[3][][];		
		int[][][] indices=new int[3][][];
		double[][][] normals=new double[3][][];
		double[][][] textCoords=new double[3][][];
		double[][][] colors=new double[3][][];
		double[][] radii=new double[3][];
		double[][] pSize=new double[3][];
		String[][] labels=new String[3][];

		DoubleArrayArray ddData=null;
		DoubleArray dData=null;
		IntArrayArray iiData=null;
		StringArray sData=null;
		
		 
		Class<? extends Node<?,?,?>>[] nodeClasses = new Class[] {heds.getVertexClass(), heds.getEdgeClass(), heds.getFaceClass()};
		for (Class<? extends Node<?,?,?>> nodeClass : nodeClasses) {
			DataListSet AData= getDataListOfTyp(nodeClass, ifs);
			int typNum=getTypNum(nodeClass);
			if (adapters.isAvailable(Position.class, nodeClass, double[].class)) {
				ddData = (DoubleArrayArray)AData.getList(Attribute.COORDINATES);
				if (ddData!=null) {
					coords[typNum]= ddData.toDoubleArrayArray(null);
				}
			}
			if (adapters.isAvailable(Color.class, nodeClass, double[].class)) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.COLORS);
				if (ddData!=null) {
					colors[typNum]= ddData.toDoubleArrayArray(null);
				}
			}
			if (adapters.isAvailable(Label.class, nodeClass, double[].class)) {
				sData= (StringArray)AData.getList(Attribute.LABELS);
				if (sData!=null) {
					labels[typNum]= sData.toStringArray(null);
				}
			}
			if (adapters.isAvailable(Normal.class, nodeClass, double[].class)) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.NORMALS);
				if (ddData!=null) {
					normals[typNum]= ddData.toDoubleArrayArray(null);
				}
			}
			if (adapters.isAvailable(Size.class, nodeClass, double[].class)) {
				dData= (DoubleArray)AData.getList(Attribute.POINT_SIZE);
				if (dData!=null) {
					pSize[typNum]= dData.toDoubleArray(null);
				}
			}
			if (adapters.isAvailable(Radius.class, nodeClass, double[].class)) {
				dData= (DoubleArray)AData.getList(Attribute.RELATIVE_RADII);
				if (dData!=null) {
					radii[typNum]= dData.toDoubleArray(null);
				}
			}
			if (adapters.isAvailable(TexCoordinate.class, nodeClass, double[].class)) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.TEXTURE_COORDINATES);
				if (ddData!=null) {
					textCoords[typNum]= ddData.toDoubleArrayArray(null);
				}
			}
		}
		// indices:
		iiData= (IntArrayArray)ifs.getEdgeAttributes(Attribute.INDICES);
		if (iiData!=null) {
			indices[1]= iiData.toIntArrayArray(null);
		}
		iiData= (IntArrayArray)ifs.getFaceAttributes(Attribute.INDICES);
		if (iiData!=null) {
			indices[2]= iiData.toIntArrayArray(null);
		}
		
		/// some facts:
		int numV = 0;
		if (coords[0] != null) numV = coords[0].length;
		int numE = 0;
		if (indices[1] != null) numE = indices[1].length;
		int numF = 0;
		if (indices[2] != null) numF = indices[2].length;		

		if (numV == 0) {
			return;
		}
		
		/// vertices
		for (int i = 0; i < numV; i++){
			V v = heds.addNewVertex();
			if (coords[0] != null) vAdapters.set(Position.class, v, coords[0][i]);
			if (colors[0] != null) vAdapters.set(Color.class, v, colors[0][i]);
			if (labels[0] != null) vAdapters.set(Label.class, v, labels[0][i]);
			if (normals[0] != null) vAdapters.set(Normal.class, v, normals[0][i]);
			if (pSize[0] != null) vAdapters.set(Size.class, v, pSize[0][i]);
			if (radii[0] != null) vAdapters.set(Radius.class, v, radii[0][i]);
			if (textCoords[0] != null) vAdapters.set(TexCoordinate.class, v, textCoords[0][i]);
		}
		
		// edges (from faces)
		DualHashMap<Integer, Integer, E> vertexEdgeMap = new DualHashMap<Integer, Integer, E>();
		for (int i = 0; i < numF; i++){
			int[] f = indices[2][i];
			for (int j = 0; j < f.length; j++){
				Integer s=f[j];
				Integer t=f[(j + 1) % f.length];
				if (vertexEdgeMap.containsKey(s,t)) {
					throw new RuntimeException("Inconsistently oriented face found in ifs2HEDS, discontinued!");
				}
				E e = heds.addNewEdge();
				e.setTargetVertex(heds.getVertex(t));
				vertexEdgeMap.put(s, t, e);
				if (coords[1] != null) eAdapters.set(Position.class, e, coords[1][i]);
				if (colors[1] != null) eAdapters.set(Color.class, e, colors[1][i]);
				if (labels[1] != null) eAdapters.set(Label.class, e, labels[1][i]);
				if (normals[1] != null) eAdapters.set(Normal.class, e, normals[1][i]);
				if (pSize[1] != null) eAdapters.set(Size.class, e, pSize[1][i]);
				if (radii[1] != null) eAdapters.set(Radius.class, e, radii[1][i]);
				if (textCoords[1] != null) eAdapters.set(TexCoordinate.class, e, textCoords[1][i]);
			}
		}
		
		// additional edges (from edges) create and link
		for (int i = 0; i < numE; i++){
			int[] e = indices[1][i];
			for (int j = 0; j < e.length-1; j++){
//				V s = heds.getVertex(e[j]);
//				V t = heds.getVertex(e[(j + 1)]);
				Integer s=e[j];
				Integer t=e[(j + 1)];
				if (vertexEdgeMap.containsKey(s, t) || vertexEdgeMap.containsKey(t, s)){
					if (edgeMap == null) continue;
					if (vertexEdgeMap.containsKey(s, t)) {
						E edge = vertexEdgeMap.get(s, t);
						edgeMap.put(edge, i);
					} 
					if (vertexEdgeMap.containsKey(t, s)) {
						E edge = vertexEdgeMap.get(t, s);
						edgeMap.put(edge, i);
					}
				} else {
					E ed = heds.addNewEdge();
					ed.setTargetVertex(heds.getVertex(t));
					vertexEdgeMap.put(s, t, ed);	
					E edOp = heds.addNewEdge();
					edOp.setTargetVertex(heds.getVertex(s));
					vertexEdgeMap.put(t, s, ed);
					ed.linkOppositeEdge(edOp);
					
					if (coords[1] != null) eAdapters.set(Position.class, ed, coords[1][i]);
					if (colors[1] != null) eAdapters.set(Color.class, ed, colors[1][i]);
					if (labels[1] != null) eAdapters.set(Label.class, ed, labels[1][i]);
					if (normals[1] != null) eAdapters.set(Normal.class, ed, normals[1][i]);
					if (pSize[1] != null) eAdapters.set(Size.class, ed, pSize[1][i]);
					if (radii[1] != null) eAdapters.set(Radius.class, ed, radii[1][i]);
					if (textCoords[1] != null) eAdapters.set(TexCoordinate.class, ed, textCoords[1][i]);
					if (coords[1] != null) eAdapters.set(Position.class, edOp, coords[1][i]);
					if (colors[1] != null) eAdapters.set(Color.class, edOp, colors[1][i]);
					if (labels[1] != null) eAdapters.set(Label.class, edOp, labels[1][i]);
					if (normals[1] != null) eAdapters.set(Normal.class, edOp, normals[1][i]);
					if (pSize[1] != null) eAdapters.set(Size.class, edOp, pSize[1][i]);
					if (radii[1] != null) eAdapters.set(Radius.class, edOp, radii[1][i]);
					if (textCoords[1] != null) eAdapters.set(TexCoordinate.class, edOp, textCoords[1][i]);
					
					if (edgeMap == null) continue;
					edgeMap.put(ed, i);
					edgeMap.put(edOp, i);
				}
			}
		}
		
		// faces, linkage, and boundary edges
		for (int i = 0; i < numF; i++){
			int[] face = indices[2][i];
			F f = heds.addNewFace();
			for (int j = 0; j < face.length; j++){
//				V s = heds.getVertex(face[j]);
//				V t = heds.getVertex(face[(j + 1) % face.length]);
				Integer s=face[j];
				Integer t=face[(j + 1) % face.length];

//				V next = heds.getVertex(face[(j + 2) % face.length]);
				Integer next=face[(j + 2) % face.length];
				E faceEdge = vertexEdgeMap.get(s, t);
				E oppEdge = vertexEdgeMap.get(t, s);
				if (oppEdge == null){
					oppEdge = heds.addNewEdge();
					oppEdge.setTargetVertex(heds.getVertex(s));
					vertexEdgeMap.put(t, s, oppEdge);
					if (edgeMap != null) {
						edgeMap.put(oppEdge, edgeMap.get(faceEdge));
					}
				}
				E nextEdge = vertexEdgeMap.get(t, next);
				faceEdge.linkOppositeEdge(oppEdge);
				faceEdge.linkNextEdge(nextEdge);
				faceEdge.setLeftFace(f);
			}	
			if (coords[2] != null) fAdapters.set(Position.class, f, coords[2][i]);
			if (colors[2] != null) fAdapters.set(Color.class, f, colors[2][i]);
			if (labels[2] != null) fAdapters.set(Label.class, f, labels[2][i]);
			if (normals[2] != null) fAdapters.set(Normal.class, f, normals[2][i]);
			if (pSize[2] != null) fAdapters.set(Size.class, f, pSize[2][i]);
			if (radii[2] != null) fAdapters.set(Radius.class, f, radii[2][i]);
			if (textCoords[2] != null) fAdapters.set(TexCoordinate.class, f, textCoords[2][i]);
			
		}
		
		// link boundary
		for (E e : heds.getEdges()) {
			if (e.getLeftFace() != null) 
				continue;
			E temp= e.getOppositeEdge();
			while (temp.getLeftFace()!=null){
				temp= temp.getPreviousEdge();
				temp= temp.getOppositeEdge();
			}
			e.linkNextEdge(temp);
		}		
		
	}


	private int getTypNum(Class<? extends Node<?,?,?>> nodeClass){
		if(Vertex.class.isAssignableFrom(nodeClass)) {
			return 0;
		}
		if(Edge.class.isAssignableFrom(nodeClass)) {
			return 1;
		}
		return 2;
	}
	private DataListSet getDataListOfTyp(Class<? extends Node<?,?,?>> nodeClass, IndexedFaceSet ifs){
		if(Vertex.class.isAssignableFrom(nodeClass)) {
			return ifs.getVertexAttributes();
		}
		if(Edge.class.isAssignableFrom(nodeClass)) {
			return ifs.getEdgeAttributes();
		}
		return ifs.getFaceAttributes();
	}
	
	
	private static class DualHashMap<K1, K2, V> implements Cloneable{

		private HashMap<K1, HashMap<K2, V>>
		map = new HashMap<K1, HashMap<K2,V>>();


		public boolean containsKey(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return false;
			else
				return vMap.get(key2) != null;
		}


		public V put(K1 key1, K2 key2, V value){
			V previous = get(key1, key2);
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null){
				vMap = new HashMap<K2, V>();
				map.put(key1, vMap);
			}
			vMap.put(key2, value);
			return previous;
		}

		public V get(K1 key1, K2 key2){
			HashMap<K2, V> vMap = map.get(key1);
			if (vMap == null)
				return null;
			else
				return vMap.get(key2);
		}

	}
}
