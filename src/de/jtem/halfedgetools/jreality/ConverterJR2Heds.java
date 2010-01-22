/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.jreality;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.CoordinateAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.NormalAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.PointSizeAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.RelRadiusAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.TextCoordsAdapter2Heds;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;


public class ConverterJR2Heds<
V extends Vertex<V, E, F>,
E extends Edge<V, E, F>, 
F extends Face<V, E, F> > {

	private Class<V> vClass=null;
	private Class<E> eClass=null;
	private Class<F> fClass=null;

	/** 
	 * can convert an IndexedFaceSet(jreality) 
	 * to a HalfEdgeDataStructure
	 * 
	 * the H.E.D.S. must be parametrised
	 * with the same classes as the converter
	 */
	public ConverterJR2Heds(Class<V> vClass, Class<E> eClass, Class<F> fClass) {
		this.vClass=vClass;
		this.eClass=eClass;
		this.fClass=fClass;
	}

	private int getTypNum(Adapter a){
		if(a.getAdapterType()==AdapterType.VERTEX_ADAPTER)
			return 0;
		if(a.getAdapterType()==AdapterType.EDGE_ADAPTER)
			return 1;
		return 2;
	}
	private DataListSet getDataListOfTyp(Adapter a, IndexedFaceSet ifs){
		if(a.getAdapterType().equals(AdapterType.VERTEX_ADAPTER))
			return ifs.getVertexAttributes();
		if(a.getAdapterType().equals(AdapterType.EDGE_ADAPTER))
			return ifs.getEdgeAttributes();
		return ifs.getFaceAttributes();
	}
	
	
	/**
	 * this converts a given IndexedFaceSet to a H.E.D.S.
	 * 
	 * remark:Adapters are nescecary to access the Data of the H.E.D.S.
	 *  you can use adapters as subtypes of the following types:
	 *  ColorAdapter2Heds			CoordinateAdapter2Heds 
	 *  LabelAdapter2Heds			NormalAdapter2Heds
	 *  PointSizeAdapter2Heds		RelRadiusAdapter2Heds
	 *  TextCoordsAdapter2Heds
	 *  
	 * remark:every adapter supports only one geometry part:
	 *   Vertices, Edges or Faces
	 *    
	 * if there is no adapter for an attribute of a geometry part, 
	 *  then this attribute will not be written 
	 *  under this geometry part
	 *  
	 * @param heds
	 * @param adapters (a CoordinateAdapter2Ifs for Vertices must be given)
	 * @return converted IndexedFaceSet as HalfEdgeDataStructure
	 * @throws IllegalArgumentException
	 */
	public HalfEdgeDataStructure<V,E,F> ifs2heds(
			IndexedFaceSet ifs, Adapter... adapters) 
			throws IllegalArgumentException{
		HalfEdgeDataStructure<V, E, F> heds = new HalfEdgeDataStructure<V, E, F>(vClass,eClass,fClass);
		ifs2heds(ifs, heds, adapters);
		return heds;
	}
	
	/**
	 * this converts a given IndexedFaceSet to a H.E.D.S.
	 * 
	 * remark:Adapters are nescecary to access the Data of the H.E.D.S.
	 *  you can use adapters as subtypes of the following types:
	 *  ColorAdapter2Heds			CoordinateAdapter2Heds 
	 *  LabelAdapter2Heds			NormalAdapter2Heds
	 *  PointSizeAdapter2Heds		RelRadiusAdapter2Heds
	 *  TextCoordsAdapter2Heds
	 *  
	 * remark:every adapter supports only one geometry part:
	 *   Vertices, Edges or Faces
	 *    
	 * if there is no adapter for an attribute of a geometry part, 
	 *  then this attribute will not be written 
	 *  under this geometry part
	 *  
	 * @param heds the resulting Halfedge Datastructure
	 * @param adapters (a CoordinateAdapter2Ifs for Vertices must be given)
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	public void ifs2heds(
			IndexedFaceSet ifs, HalfEdgeDataStructure<V,E,F> heds, Adapter... adapters) 
			throws IllegalArgumentException{
		
		int initialVertices = heds.numVertices();
		
		// collect the adapterTypes
		List<Adapter> vertexAdapters = new LinkedList<Adapter>(); 
		List<Adapter> faceAdapters = new LinkedList<Adapter>(); 
		List<Adapter> edgeAdapters = new LinkedList<Adapter>();
		boolean hasCoords=false;
		for(Adapter a: adapters){
			if(a.getAdapterType()==AdapterType.VERTEX_ADAPTER){
				vertexAdapters.add(a);
				if (a instanceof CoordinateAdapter2Heds && a.getAdapterType()==AdapterType.VERTEX_ADAPTER)
					hasCoords=true;
			}
			else if (a.getAdapterType()==AdapterType.EDGE_ADAPTER)
				edgeAdapters.add(a);
			else if (a.getAdapterType()==AdapterType.FACE_ADAPTER)
				faceAdapters.add(a);
		}	
		if(!hasCoords) throw new IllegalArgumentException("No coordinateAdapter found");
		
		/// read out Data:
		//		 first argument: 0=Vertex; 1=Edge; 2=Face; 
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
		
		for (Adapter vA : adapters) {
			DataListSet AData= getDataListOfTyp(vA, ifs);
			int typNum=getTypNum(vA);
			if (vA instanceof CoordinateAdapter2Heds) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.COORDINATES);
				if (ddData!=null)
					coords[typNum]= ddData.toDoubleArrayArray(null);
			}
			if (vA instanceof ColorAdapter2Heds) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.COLORS);
				if (ddData!=null)
					colors[typNum]= ddData.toDoubleArrayArray(null);
			}
			if (vA instanceof LabelAdapter2Heds) {
				sData= (StringArray)AData.getList(Attribute.LABELS);
				if (sData!=null)
					labels[typNum]= sData.toStringArray(null);
			}
			if (vA instanceof NormalAdapter2Heds) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.NORMALS);
				if (ddData!=null)
					normals[typNum]= ddData.toDoubleArrayArray(null);
			}
			if (vA instanceof PointSizeAdapter2Heds) {
				dData= (DoubleArray)AData.getList(Attribute.POINT_SIZE);
				if (dData!=null)
					pSize[typNum]= dData.toDoubleArray(null);
			}
			if (vA instanceof RelRadiusAdapter2Heds) {
				dData= (DoubleArray)AData.getList(Attribute.RELATIVE_RADII);
				if (dData!=null)
					radii[typNum]= dData.toDoubleArray(null);
			}
			if (vA instanceof TextCoordsAdapter2Heds) {
				ddData= (DoubleArrayArray)AData.getList(Attribute.TEXTURE_COORDINATES);
				if (ddData!=null)
					textCoords[typNum]= ddData.toDoubleArrayArray(null);
			}
		}
		
		// indices:
		iiData= (IntArrayArray)ifs.getEdgeAttributes(Attribute.INDICES);
		if (iiData!=null)
			indices[1]= iiData.toIntArrayArray(null);
		iiData= (IntArrayArray)ifs.getFaceAttributes(Attribute.INDICES);
		if (iiData!=null)
			indices[2]= iiData.toIntArrayArray(null);
		
		/// some facts:
		int numV = 0;
		if (coords[0] != null) numV = coords[0].length;
		int numE = 0;
		if (indices[1] != null) numE = indices[1].length;
		int numF = 0;
		if (indices[2] != null) numF = indices[2].length;		
		
		/// vertices
	
		for (int i = 0; i < numV; i++){
			V v = heds.addNewVertex();
			// set attributes
			for (Adapter vA : vertexAdapters) {
				if (vA instanceof CoordinateAdapter2Heds && coords[0]!=null) {
//					CoordinateAdapter2Heds vAcoord = (CoordinateAdapter2Heds) vA;
//					if (coords[0][i].length == 4)
//						vAcoord.setCoordinate(v, coords[0][i]);
//					else {
//						double[] d= new double[]{
//								coords[0][i][0],
//								coords[0][i][1],
//								coords[0][i][2],
//								1.0			};
//						vAcoord.setCoordinate(v, d);
//					}
					((CoordinateAdapter2Heds) vA).setCoordinate(v, coords[0][i]);
				}
				if (vA instanceof ColorAdapter2Heds && colors[0]!=null) 
					((ColorAdapter2Heds) vA).setColor(v, colors[0][i]);
				if (vA instanceof LabelAdapter2Heds && labels[0]!=null)
					((LabelAdapter2Heds) vA).setLabel(v, labels[0][i]);
				if (vA instanceof NormalAdapter2Heds && normals[0]!=null)
					((NormalAdapter2Heds) vA).setNormal(v, normals[0][i]);
				if (vA instanceof PointSizeAdapter2Heds && pSize[0]!=null)
					((PointSizeAdapter2Heds) vA).setPointSize(v, pSize[0][i]);
				if (vA instanceof RelRadiusAdapter2Heds && radii[0]!=null)
					((RelRadiusAdapter2Heds) vA).setRelRadius(v, radii[0][i]);
				if (vA instanceof TextCoordsAdapter2Heds && textCoords[0]!=null)
					((TextCoordsAdapter2Heds) vA).setTextCoordinate(v, textCoords[0][i]);
			}
		}
		
		// edges (from faces)
		DualHashMap<Integer, Integer, E> vertexEdgeMap = new DualHashMap<Integer, Integer, E>();
		for (int i = 0; i < numF; i++){
			int[] f = indices[2][i];
			for (int j = 0; j < f.length; j++){
				//V s = heds.getVertex(f[j]);
				//V t = heds.getVertex(f[(j + 1) % f.length]);
				Integer s=f[j]+initialVertices;
				Integer t=f[(j + 1) % f.length]+initialVertices;
				if (vertexEdgeMap.containsKey(s,t))
					throw new RuntimeException("Inconsistently oriented face found in ifs2HEDS, discontinued!");
				E e = heds.addNewEdge();
				e.setTargetVertex(heds.getVertex(t));
				vertexEdgeMap.put(s, t, e);
				
				// TODO:  <==>-----<<
				for (Adapter vA : edgeAdapters) {
					if (vA instanceof CoordinateAdapter2Heds && coords[1]!=null)
						((CoordinateAdapter2Heds) vA).setCoordinate(e, coords[1][i]);
					if (vA instanceof ColorAdapter2Heds && colors[1]!=null)
						((ColorAdapter2Heds) vA).setColor(e, colors[1][i]);
					if (vA instanceof LabelAdapter2Heds && labels[1]!=null)
						((LabelAdapter2Heds) vA).setLabel(e, labels[1][i]);
					if (vA instanceof NormalAdapter2Heds && normals[1]!=null)
						((NormalAdapter2Heds) vA).setNormal(e, normals[1][i]);
					if (vA instanceof PointSizeAdapter2Heds && pSize[1]!=null)
						((PointSizeAdapter2Heds) vA).setPointSize(e, pSize[1][i]);
					if (vA instanceof RelRadiusAdapter2Heds && radii[1]!=null)
						((RelRadiusAdapter2Heds) vA).setRelRadius(e, radii[1][i]);
					if (vA instanceof TextCoordsAdapter2Heds && textCoords[1]!=null)
						((TextCoordsAdapter2Heds) vA).setTextCoordinate(e, textCoords[1][i]);
				}
				// TODO here make default Edge settings
			}
		}
		
		// additional edges (from edges) create and link
		for (int i = 0; i < numE; i++){
			int[] e = indices[1][i];
			for (int j = 0; j < e.length-1; j++){
//				V s = heds.getVertex(e[j]);
//				V t = heds.getVertex(e[(j + 1)]);
				Integer s=e[j]+initialVertices;
				Integer t=e[(j + 1)]+initialVertices;
				if (vertexEdgeMap.containsKey(s, t)||vertexEdgeMap.containsKey(t, s)){}
				else {
					E ed = heds.addNewEdge();
					ed.setTargetVertex(heds.getVertex(t));
					vertexEdgeMap.put(s, t, ed);	
					E edOp = heds.addNewEdge();
					edOp.setTargetVertex(heds.getVertex(s));
					vertexEdgeMap.put(t, s, ed);
					ed.linkOppositeEdge(edOp);
					
					// make Edge settings
					for (Adapter vA : edgeAdapters) {
						if (vA instanceof CoordinateAdapter2Heds && coords[1]!=null){
							((CoordinateAdapter2Heds) vA).setCoordinate(ed, coords[1][i]);
							((CoordinateAdapter2Heds) vA).setCoordinate(edOp, coords[1][i]);
						}
						if (vA instanceof ColorAdapter2Heds && colors[1]!=null){
							((ColorAdapter2Heds) vA).setColor(ed, colors[1][i]);
							((ColorAdapter2Heds) vA).setColor(edOp, colors[1][i]);
						}
						if (vA instanceof LabelAdapter2Heds && labels[1]!=null){
							((LabelAdapter2Heds) vA).setLabel(ed, labels[1][i]);
							((LabelAdapter2Heds) vA).setLabel(edOp, labels[1][i]);
						}
						if (vA instanceof NormalAdapter2Heds && normals[1]!=null){
							((NormalAdapter2Heds) vA).setNormal(ed, normals[1][i]);
							((NormalAdapter2Heds) vA).setNormal(edOp, normals[1][i]);
						}
						if (vA instanceof PointSizeAdapter2Heds && pSize[1]!=null){
							((PointSizeAdapter2Heds) vA).setPointSize(ed, pSize[1][i]);
							((PointSizeAdapter2Heds) vA).setPointSize(edOp, pSize[1][i]);
						}
						if (vA instanceof RelRadiusAdapter2Heds && radii[1]!=null){
							((RelRadiusAdapter2Heds) vA).setRelRadius(ed, radii[1][i]);
							((RelRadiusAdapter2Heds) vA).setRelRadius(edOp, radii[1][i]);
						}
						if (vA instanceof TextCoordsAdapter2Heds && textCoords[1]!=null){
							((TextCoordsAdapter2Heds) vA).setTextCoordinate(ed, textCoords[1][i]);
							((TextCoordsAdapter2Heds) vA).setTextCoordinate(edOp, textCoords[1][i]);
						}
					}	
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
				Integer s=face[j]+initialVertices;
				Integer t=face[(j + 1) % face.length]+initialVertices;

//				V next = heds.getVertex(face[(j + 2) % face.length]);
				Integer next=face[(j + 2) % face.length]+initialVertices;
				E faceEdge = vertexEdgeMap.get(s, t);
				E oppEdge = vertexEdgeMap.get(t, s);
				if (oppEdge == null){
					oppEdge = heds.addNewEdge();
					oppEdge.setTargetVertex(heds.getVertex(s));
					vertexEdgeMap.put(t, s, oppEdge);
				}
				E nextEdge = vertexEdgeMap.get(t, next);
				faceEdge.linkOppositeEdge(oppEdge);
				faceEdge.linkNextEdge(nextEdge);
				faceEdge.setLeftFace(f);
			}	
			
			// set attributes
			for (Adapter vA : faceAdapters) {
				if (vA instanceof CoordinateAdapter2Heds && coords[2]!=null)
					((CoordinateAdapter2Heds) vA).setCoordinate(f, coords[2][i]);
				if (vA instanceof ColorAdapter2Heds && colors[2]!=null)
					((ColorAdapter2Heds) vA).setColor(f, colors[2][i]);
				if (vA instanceof LabelAdapter2Heds && labels[2]!=null)
					((LabelAdapter2Heds) vA).setLabel(f, labels[2][i]);
				if (vA instanceof NormalAdapter2Heds && normals[2]!=null)
					((NormalAdapter2Heds) vA).setNormal(f, normals[2][i]);
				if (vA instanceof PointSizeAdapter2Heds && pSize[2]!=null)
					((PointSizeAdapter2Heds) vA).setPointSize(f, pSize[2][i]);
				if (vA instanceof RelRadiusAdapter2Heds && radii[2]!=null)
					((RelRadiusAdapter2Heds) vA).setRelRadius(f, radii[2][i]);
				if (vA instanceof TextCoordsAdapter2Heds && textCoords[2]!=null)
					((TextCoordsAdapter2Heds) vA).setTextCoordinate(f, textCoords[2][i]);
			}
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
