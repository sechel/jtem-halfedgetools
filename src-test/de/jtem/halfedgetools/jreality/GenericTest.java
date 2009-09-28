package de.jtem.halfedgetools.jreality;


import java.io.File;

import de.jreality.geometry.GeometryMergeFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.ReaderVRML;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.viewerapp.ViewerApp;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardColorAdapter;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardLabelAdapter;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardNormalAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;

public class GenericTest {
	
	public  static void main2(String[] args) {
//		HalfEdgeDataStructure<StdVertex,StdEdge,StdFace> heds = 
//			new HalfEdgeDataStructure<StdVertex,StdEdge,StdFace>(StdVertex.class,StdEdge.class,StdFace.class);
//
//		StdEdge e0; 
//		StdEdge e1;
//		StdEdge e2; 
//		StdEdge e3;
//		StdEdge e4; 
//		StdEdge e5;
//		StdEdge e6; 
//		StdEdge e7;
//		
//		heds.addNewVertex();
//		heds.addNewVertex();
//		heds.addNewVertex();
//		heds.addNewVertex();
//		e0 =	heds.addNewEdge();
//		e1 =	heds.addNewEdge();
//		e2 =	heds.addNewEdge();
//		e3 =	heds.addNewEdge();
//		e4 =	heds.addNewEdge();
//		e5 =	heds.addNewEdge();
//		e6 =	heds.addNewEdge();
//		e7 =	heds.addNewEdge();		//ViewerApp.display(result);
		//ViewerApp vr= ViewerVR.mainImpl(null);
//		
//		e0.linkNextEdge(heds.getEdge(1));
//		e1.linkNextEdge(heds.getEdge(2));
//		e2.linkNextEdge(heds.getEdge(0));
//		
//		e3.linkNextEdge(heds.getEdge(5));
//		e4.linkNextEdge(heds.getEdge(6));
//		e5.linkNextEdge(heds.getEdge(4));
//		
//		e6.linkNextEdge(heds.getEdge(7));
//		e7.linkNextEdge(heds.getEdge(3));
//		
//		e0.setTargetVertex(heds.getVertex(1));
//		e1.setTargetVertex(heds.getVertex(2));
//		e2.setTargetVertex(heds.getVertex(0));
//		e3.setTargetVertex(heds.getVertex(0));
//		e4.setTargetVertex(heds.getVertex(1));
//		e5.setTargetVertex(heds.getVertex(2));
//		
//		e6.setTargetVertex(heds.getVertex(3));
//		e7.setTargetVertex(heds.getVertex(1));
//		
//		e0.linkOppositeEdge(heds.getEdge(3));
//		e1.linkOppositeEdge(heds.getEdge(4));
//		e2.linkOppositeEdge(heds.getEdge(5));
//		
//		e6.linkOppositeEdge(heds.getEdge(7));
//		
//		heds.addNewFace();
//		heds.addNewFace();
//		e0.setLeftFace(heds.getFace(0));
//		e1.setLeftFace(heds.getFace(0));
//		e2.setLeftFace(heds.getFace(0));		
//		//e5.setLeftFace(heds.getFace(1));
//		
//		ViewerApp.display(Converter.heds2ifs(heds));
//		
	}
	public static void main(String[] args) {
//		IndexedFaceSet i= Primitives.box(1, 2, 3, false);
//		IndexedFaceSet i= Primitives.icosahedron();
//		IndexedFaceSet i= Primitives.cylinder(1000);
//		IndexedFaceSet i= Primitives.sphere(200);
//		IndexedFaceSet i= Primitives.cone(10000);
//		IndexedFaceSet i= Primitives.pyramid(
//				new double[][]{
//						{1,0,0},
//						{1,1,0},
//						{0,1,0}},
//						new double[]{0,0,-1});

		ReaderVRML readerVRML= new ReaderVRML();
//		ReaderMATHEMATICA readerMATHEMATICA= new ReaderMATHEMATICA();
//		ReaderOBJ readerOBJ= new ReaderOBJ();
		SceneGraphComponent c=new SceneGraphComponent();
		SceneGraphComponent root= new SceneGraphComponent();

		System.out.println("test.main(try read)"+c);
		try {
			//c=readerMATHEMATICA.read(new File("/homes/geometer/gonska/workspace/MyOwn/testAll.m"));
			//c=readerMATHEMATICA.read(new File("/homes/geometer/gonska/workspace/MyOwn/test7c.m"));
			c=readerVRML.read(new File("/homes/geometer/gonska/VrmlFiles/geoTest.wrl"));
			//c=readerVRML.read(new File("/homes/geometer/gonska/VrmlFiles/lasertrk.wrl"));
			//c=readerVRML.read(new File("/homes/geometer/gonska/VrmlFiles/lightTest.wrl"));
			//c=readerOBJ.read(new File("/net/MathVis/data/testData3D/obj/"));
			//c=readerOBJ.read(new File("/net/MathVis/data/testData3D/obj/Chen-Gackstatter-4.obj"));
			//c=readerOBJ.read(new File("/net/MathVis/data/testData3D/obj/"));
			System.out.println("test.main(read finished)"+(c!=null));
			if(c==null)throw new Exception("nullGeo");
			
		} catch (Exception e) {
			System.out.println("test.main(read failed)");
			c= new SceneGraphComponent();
		}		
		
		
		GeometryMergeFactory g= new GeometryMergeFactory();
		IndexedFaceSet i= g.mergeIndexedFaceSets(c);
		System.out.println("test.main(graphics merged): "+i);

//		root.setGeometry(i);
//    	ViewerApp.display(root);
//    	if (true)return;
    	
		
		
//		IndexedFaceSetFactory fac= new IndexedFaceSetFactory();
//		fac.setVertexCount(5);
//		fac.setFaceCount(2);
//		int [][] fIndis= new int[][]{
//				{0,1,2},{2,3,4}
//		};
//		double[][] verts=new double[][]{
//				{0,0,0},
//				{0,2,0},
//				{1,1,0},
//				{2,0,0},
//				{2,2,0}
//		} ;
//		fac.setVertexCoordinates(verts);
//		fac.setFaceIndices(fIndis);
//		fac.update();
//		IndexedFaceSet i= fac.getIndexedFaceSet();
		
		
		if(!IndexedFaceSetUtility.makeConsistentOrientation(i)){
			System.out.println("test.main(orientation failed)");
			return;
		}
		System.out.println("test.main(orientation ok & done)");
	//	RemoveDublicateInfo.removeCycleDefinition(fIndices);
	//	RemoveDublicateInfo.removeNoFaceVertices(vertexIndices, fIndices);

// ------ converting ------------
		HalfEdgeDataStructure<StandardVertex,StandardEdge,StandardFace> heds;
		IndexedFaceSet result;
		
		ConverterHeds2JR<StandardVertex, StandardEdge,StandardFace > convBack =
			new ConverterHeds2JR<StandardVertex, StandardEdge, StandardFace>();
		ConverterJR2Heds<StandardVertex, StandardEdge,StandardFace > convTo =
			new ConverterJR2Heds<StandardVertex, StandardEdge, StandardFace>(StandardVertex.class,StandardEdge.class,StandardFace.class);
		
		heds=convTo.ifs2heds(i
//				, new MyCompleteAdapter(AdapterType.VERTEX_ADAPTER)
//		, new MyCompleteAdapter(AdapterType.EDGE_ADAPTER)
//		, new MyCompleteAdapter(AdapterType.FACE_ADAPTER)

		,new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER )
		,new StandardColorAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardColorAdapter(AdapterType.EDGE_ADAPTER)
		,new StandardColorAdapter(AdapterType.FACE_ADAPTER)
		,new StandardLabelAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardLabelAdapter(AdapterType.EDGE_ADAPTER)
		,new StandardLabelAdapter(AdapterType.FACE_ADAPTER)
		,new StandardNormalAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardNormalAdapter(AdapterType.FACE_ADAPTER)
		);
		
		System.out.println("test.main(convert TO done): "+heds);
		System.out.println("test.main(valid?): "+HalfEdgeUtils.isValidSurface(heds,true));
		
		result=convBack.heds2ifs(heds
		//		, new MyCompleteAdapter(AdapterType.VERTEX_ADAPTER)
		//		, new MyCompleteAdapter(AdapterType.EDGE_ADAPTER)
		//		, new MyCompleteAdapter(AdapterType.FACE_ADAPTER)
		,new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardColorAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardColorAdapter(AdapterType.EDGE_ADAPTER)
		,new StandardColorAdapter(AdapterType.FACE_ADAPTER)
		,new StandardLabelAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardLabelAdapter(AdapterType.EDGE_ADAPTER)
		,new StandardLabelAdapter(AdapterType.FACE_ADAPTER)
		,new StandardNormalAdapter(AdapterType.VERTEX_ADAPTER)
		,new StandardNormalAdapter(AdapterType.FACE_ADAPTER)
		);
		System.out.println("test.main(convert BACK done): "+result);
		
		// view result
		root.setGeometry(result);
		ViewerApp.display(root);
		
	}
		
}
