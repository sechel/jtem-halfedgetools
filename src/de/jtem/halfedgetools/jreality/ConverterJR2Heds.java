package de.jtem.halfedgetools.jreality;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.Radius;
import de.jtem.halfedgetools.adapter.type.Size;
import de.jtem.halfedgetools.adapter.type.TexturePosition;


public class ConverterJR2Heds {

	private static Logger
		log = Logger.getLogger(ConverterJR2Heds.class.getName());
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void ifs2heds(IndexedFaceSet ifs, HDS heds, AdapterSet adapters) {
		ifs2heds(ifs, heds, adapters, null);
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void ifs2heds(IndexedFaceSet ifs, HDS heds, AdapterSet a, Map<Integer, Edge<?,?,?>> edgeMap) {
		heds.clear();
		if (edgeMap != null) edgeMap.clear();
		AdapterSet vAdapters = a.querySet(heds.getVertexClass(), double[].class);
		AdapterSet eAdapters = a.querySet(heds.getEdgeClass(), double[].class);
		AdapterSet fAdapters = a.querySet(heds.getFaceClass(), double[].class);
		
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
			ddData = (DoubleArrayArray)AData.getList(Attribute.COORDINATES);
			if (ddData!=null) {
				coords[typNum]= ddData.toDoubleArrayArray(null);
			}
			if (a.isAvailable(Color.class, nodeClass, double[].class)) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.COLORS);
				if (ddData!=null) {
					colors[typNum]= ddData.toDoubleArrayArray(null);
				}
			}
			if (a.isAvailable(Label.class, nodeClass, double[].class)) {
				sData= (StringArray)AData.getList(Attribute.LABELS);
				if (sData!=null) {
					labels[typNum]= sData.toStringArray(null);
				}
			}
			if (a.isAvailable(Normal.class, nodeClass, double[].class)) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.NORMALS);
				if (ddData!=null) {
					normals[typNum]= ddData.toDoubleArrayArray(null);
				}
			}
			if (a.isAvailable(Size.class, nodeClass, double[].class)) {
				dData= (DoubleArray)AData.getList(Attribute.POINT_SIZE);
				if (dData!=null) {
					pSize[typNum]= dData.toDoubleArray(null);
				}
			}
			if (a.isAvailable(Radius.class, nodeClass, double[].class)) {
				dData= (DoubleArray)AData.getList(Attribute.RELATIVE_RADII);
				if (dData!=null) {
					radii[typNum]= dData.toDoubleArray(null);
				}
			}
			if (a.isAvailable(TexturePosition.class, nodeClass, double[].class)) {
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

		if (numV == 0) return;
		
		/// vertices
		for (int i = 0; i < numV; i++){
			V v = heds.addNewVertex();
			if (coords[0] != null) vAdapters.set(Position.class, v, coords[0][i]);
			if (colors[0] != null) vAdapters.set(Color.class, v, colors[0][i]);
			if (labels[0] != null) vAdapters.set(Label.class, v, labels[0][i]);
			if (normals[0] != null) vAdapters.set(Normal.class, v, normals[0][i]);
			if (pSize[0] != null) vAdapters.set(Size.class, v, pSize[0][i]);
			if (radii[0] != null) vAdapters.set(Radius.class, v, radii[0][i]);
			if (textCoords[0] != null) vAdapters.set(TexturePosition.class, v, textCoords[0][i]);
		}
		
		// edges (from faces)
		DualHashMap<Integer, Integer, E> vertexEdgeMap = new DualHashMap<Integer, Integer, E>();
		Set<Integer> skippedFaces = new HashSet<Integer>();
		for (int i = 0; i < numF; i++){
			int[] f = indices[2][i];
			if (f.length < 3) continue;
			DualHashMap<Integer, Integer, E> vEMap = new DualHashMap<Integer, Integer, E>();
			Set<E> edges = new HashSet<E>();
			boolean faceValid = true;
			for (int j = 0; j < f.length; j++){
				int s = f[j];
				int t = f[(j + 1) % f.length];
				if (s == t) continue;
				if (vertexEdgeMap.get(s, t) != null) {
					// remove already generated face edges
					skippedFaces.add(i);
					for (E e : edges) heds.removeEdge(e);
					faceValid = false;
					break;
				}
				E e = heds.addNewEdge();
				e.setTargetVertex(heds.getVertex(t));
				vEMap.put(s, t, e);
				if (coords[1] != null) eAdapters.set(Position.class, e, coords[1][i]);
				if (colors[1] != null) eAdapters.set(Color.class, e, colors[1][i]);
				if (labels[1] != null) eAdapters.set(Label.class, e, labels[1][i]);
				if (normals[1] != null) eAdapters.set(Normal.class, e, normals[1][i]);
				if (pSize[1] != null) eAdapters.set(Size.class, e, pSize[1][i]);
				if (radii[1] != null) eAdapters.set(Radius.class, e, radii[1][i]);
				if (textCoords[1] != null) eAdapters.set(TexturePosition.class, e, textCoords[1][i]);
				edges.add(e);
			}
			if (faceValid) vertexEdgeMap.putAll(vEMap);
		}
		if (!skippedFaces.isEmpty()) {
			log.warning("skipped faces: " + skippedFaces.size());
		}
		
		// additional edges (from edges) create and link
		for (int i = 0; i < numE; i++){
			int[] e = indices[1][i];
			for (int j = 0; j < e.length-1; j++){
				int s = e[j];
				int t = e[(j + 1)];
				if (s == t) continue;
				if (vertexEdgeMap.containsKey(s, t) || vertexEdgeMap.containsKey(t, s)){
					if (edgeMap == null) continue;
					if (vertexEdgeMap.containsKey(s, t)) {
						E edge = vertexEdgeMap.get(s, t);
						edgeMap.put(i, edge);
					} 
					if (vertexEdgeMap.containsKey(t, s)) {
						E edge = vertexEdgeMap.get(t, s);
						edgeMap.put(i, edge);
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
					if (textCoords[1] != null) eAdapters.set(TexturePosition.class, ed, textCoords[1][i]);
					if (coords[1] != null) eAdapters.set(Position.class, edOp, coords[1][i]);
					if (colors[1] != null) eAdapters.set(Color.class, edOp, colors[1][i]);
					if (labels[1] != null) eAdapters.set(Label.class, edOp, labels[1][i]);
					if (normals[1] != null) eAdapters.set(Normal.class, edOp, normals[1][i]);
					if (pSize[1] != null) eAdapters.set(Size.class, edOp, pSize[1][i]);
					if (radii[1] != null) eAdapters.set(Radius.class, edOp, radii[1][i]);
					if (textCoords[1] != null) eAdapters.set(TexturePosition.class, edOp, textCoords[1][i]);
					
					if (edgeMap == null) continue;
					edgeMap.put(i, ed);
				}
			}
		}
		
		// faces, linkage, and boundary edges
		for (int i = 0; i < numF; i++){
			if (skippedFaces.contains(i)) continue;
			int[] face = indices[2][i];
			if (face.length < 3) continue;
			F f = null;
			for (int j = 0; j < face.length; j++){
				int s = face[j];
				int t = face[(j + 1) % face.length];
				if (s == t) continue;
				int next = face[(j + 2) % face.length];
				if (next == t) {
					next = face[(j + 3) % face.length];
				}
				E faceEdge = vertexEdgeMap.get(s, t);
				E oppEdge = vertexEdgeMap.get(t, s);
				if (oppEdge == null){
					oppEdge = heds.addNewEdge();
					oppEdge.setTargetVertex(heds.getVertex(s));
					E old = vertexEdgeMap.put(t, s, oppEdge);
					if (old != null) {
						log.warning("more than one edge between vertex " + s + " and " + t);
					}
				}
				E nextEdge = vertexEdgeMap.get(t, next);
				if (faceEdge == oppEdge) {
					log.severe("cannot link edge as opposite to itself");
					break;
				}
				faceEdge.linkOppositeEdge(oppEdge);
				faceEdge.linkNextEdge(nextEdge);
				if (f == null) {
					f = heds.addNewFace();
				}
				faceEdge.setLeftFace(f);
			}	
			if (f != null) {
				if (coords[2] != null) fAdapters.set(Position.class, f, coords[2][i]);
				if (colors[2] != null) fAdapters.set(Color.class, f, colors[2][i]);
				if (labels[2] != null) fAdapters.set(Label.class, f, labels[2][i]);
				if (normals[2] != null) fAdapters.set(Normal.class, f, normals[2][i]);
				if (pSize[2] != null) fAdapters.set(Size.class, f, pSize[2][i]);
				if (radii[2] != null) fAdapters.set(Radius.class, f, radii[2][i]);
				if (textCoords[2] != null) fAdapters.set(TexturePosition.class, f, textCoords[2][i]);
			}
		}
		
		// link boundary
		for (E e : heds.getEdges()) {
			if (e.getLeftFace() != null) continue;
			E temp = e.getOppositeEdge();
			while (temp.getLeftFace()!=null){
				temp = temp.getPreviousEdge();
				temp = temp.getOppositeEdge();
			}
			e.linkNextEdge(temp);
		}		
		
		HalfEdgeUtils.isValidSurface(heds);
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
		
		public void putAll(DualHashMap<K1, K2, V> map) {
			for (K1 k1 : map.map.keySet()) {
				Map<K2, V> mm = map.map.get(k1);
				for (K2 k2 : mm.keySet()) {
					V v = mm.get(k2);
					put(k1, k2, v);
				}
			}
		}

	}
}
