package de.jtem.halfedgetools.symmetry2;

import java.awt.Color;
import java.util.List;

import sun.management.snmp.AdaptorBootstrap;

import de.discretization.halfedge.hds.DEdge;
import de.discretization.halfedge.hds.DFace;
import de.discretization.halfedge.hds.DHDS;
import de.discretization.halfedge.hds.DVertex;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.CommonAttributes;
import de.jreality.util.SceneGraphUtility;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.CrystallographicGroup;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;

public class SimpleTest2 {

	double[][] v2 = {
			{0,0,0},
			{1,0,0},
			{0,1,0},
			{1,1,0}
	};
	int[][] indices2 = {
			{0,1,2},
			{1,3,2}
	};
	double[][] v = {
			{0,0,0},
			{.5,0,0},
			{1,0,0},
			{1/3.0,.5,0},
			{2/3.0,.5,0},
			{0,1,0},
			{.5,1,0},
			{1,1,0}
	};
	int[][] indices = {
			{0,1,3},
			{1,4,3},
			{2,4,1},
			{0,3,5},
			{3,6,5},
			{4,6,3},
			{4,7,6},
			{2,7,4}
	};
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleTest2 st2 = new SimpleTest2();
		st2.doIt();
	}

	public  	 void doIt()	{

		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(v.length);
		ifsf.setVertexCoordinates(v);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.setGenerateEdgeLabels(true);
		ifsf.setGenerateVertexLabels(true);
		ifsf.update();
		
		WallpaperGroup group = WallpaperGroup.instanceOfGroup("O");
		fundDom = (IndexedFaceSet) group.getDefaultFundamentalRegion();
		fundDomP = fundDom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Rn.matrixTimesVector(fundDomP, jitterM, fundDomP);
		System.err.println("Jittered coords = \n"+Rn.toString(fundDomP));
		fundDom.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(fundDomP));
		group.setDefaultFundamentalDomain(fundDom);
		
		adapterSet = AdapterSet.createGenericAdapters();
		MappedPositionAdapter positionAdapter = new MappedPositionAdapter();
		adapterSet.add(positionAdapter);
		MappedCanonicalPositionAdapter canonicalPositionAdapter = new MappedCanonicalPositionAdapter();
		adapterSet.add(canonicalPositionAdapter);
		MappedGroupElementAdapter groupAdapter = new MappedGroupElementAdapter();
		adapterSet.add(groupAdapter);
		ConverterJR2Heds converter = new ConverterJR2Heds();
		final DHDS dhds = new DHDS();
		converter.ifs2heds(ifsf.getIndexedFaceSet(), dhds, adapterSet, null);
		

		// assign group elements (from the torus group) to edges based on the group wlement
		// required to move from the source to the target
//		for (DEdge e: dhds.getEdges()) {
//			DVertex v0 = e.getStartVertex(), v1 = e.getTargetVertex();
//			double[] p0 = a.getD(Position3d.class, v0);
//			double[] p1 = a.getD(Position3d.class, v1);
//			DiscreteGroupElement dge0 = new DiscreteGroupElement();
//			double[] cp0 = DiscreteGroupUtility.getCanonicalRepresentative2(null, p0, dge0, fundDom, group);
//			DiscreteGroupElement dge1 = new DiscreteGroupElement();
//			double[] cp1 = DiscreteGroupUtility.getCanonicalRepresentative2(null, p1, dge1, fundDom, group);
//			dge1.multiplyOnLeft(dge0.getInverse());
//			a.set(CanonicalPosition.class, v0, cp0);
//			a.set(CanonicalPosition.class, v1, cp1);
//			if (Rn.isIdentityMatrix(dge1.getArray(), 10E-8)) continue;
//			DiscreteGroupElement idge1 = dge1.getInverse();
//			if (idge1.getWord().length() != 0)
//				System.err.println("edge "+e.getIndex()+" "+idge1.getWord()+" "+Rn.toString(p0)+" :: "+Rn.toString(p1));
//			a.set(GroupElement.class, e, idge1);
//		}
		QuotientMeshUtility.assignCanonicalCoordinates(dhds, adapterSet, group, fundDom);
		// store away the canonical position
		QuotientMeshUtility.printEdges(dhds);
		System.err.println("setting up canonical positions");
		for (DVertex v : dhds.getVertices())	{
			double[] cp0 = adapterSet.getD(CanonicalPosition.class, v);
			System.err.println("vertex"+v.getIndex()+" "+Rn.toString(cp0));
		}
		// test that the product of group elements around any face is the identity
		for (DFace f: dhds.getFaces())	{
			DEdge e0 = f.getBoundaryEdge();
			DEdge e = e0;
			DiscreteGroupElement dge = new DiscreteGroupElement();
			do {
				DiscreteGroupElement dgee = adapterSet.get(GroupElement.class, e, DiscreteGroupElement.class);
				if (dgee != null) dge.multiplyOnLeft(dgee);
				e = e.getNextEdge();
			} while (e != e0);
			if (!Rn.isIdentityMatrix(dge.getArray(), 10E-8)) {
				System.err.println("Warning: product of matrices not the identity");
			}
		}
		DHDS exploded = new DHDS();
		QuotientMeshUtility.assignCoveringSpaceCoordinates(dhds, adapterSet);
		QuotientMeshUtility.explodeFaces(exploded, dhds, adapterSet);
		converter2 = new ConverterHeds2JR();
		IndexedFaceSet ifs = converter2.heds2ifs(exploded, adapterSet); //exploded, a);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs, Pn.EUCLIDEAN);
		System.err.println("genus = "+HalfEdgeUtils.getGenus(dhds));
		world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent dirdom = SceneGraphUtility.createFullSceneGraphComponent("dirdom");
		Appearance ap = world.getAppearance();
		ap.setAttribute(CommonAttributes.SCALE, .002);
		world.setGeometry(ifs); //ifsf.getGeometry()); //
		world.addTool(new AbstractTool(InputSlot.LEFT_BUTTON) {
			
			@Override
			public void activate(ToolContext tc) {
				super.activate(tc);
				
			}

			@Override
			public void perform(ToolContext tc) {
				// TODO Auto-generated method stub
				super.perform(tc);
			}

			@Override
			public void deactivate(ToolContext tc) {
				super.deactivate(tc);
				PickResult pr = tc.getCurrentPick();
				if (pr == null || pr.getPickPath() == null) return;
				System.err.println("Pick obj coords = "+Rn.toString(pr.getObjectCoordinates()));
				System.err.println("Face # = "+pr.getIndex());
				insertPointInFace(pr, dhds);
			}
			
		});
		world.addChild(dirdom);
		dirdom.setGeometry(fundDom);
		MatrixBuilder.euclidean().translate(0,0,-.01).assignTo(dirdom);
		dirdom.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		dirdom.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		
		Viewer jrv = JRViewer.display(world); //f.getIndexedFaceSet());
		world.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
	}
	
	protected void insertPointInFace(PickResult pr, DHDS dhds) {
		DFace face = dhds.getFace(pr.getIndex());
		List<DEdge> edges = HalfEdgeUtils.boundaryEdges(face);
		DEdge[] el = new DEdge[3], er = new DEdge[3], e = new DEdge[3];
		DVertex dv = dhds.addNewVertex();
		double[] coords = pr.getObjectCoordinates();
		adapterSet.set(Position.class, dv, coords);
		for (int i = 0; i<3; ++i) {
			el[i] = dhds.addNewEdge();
			adapterSet.set(Position.class, el[i], coords);
			el[i].setTargetVertex(dv);
			er[i] = dhds.addNewEdge();
			e[i] = edges.get(i);
		}
		for (int i = 0; i<3; ++i) {
			double[] coord2 = adapterSet.getD(Position.class, e[(i+3-1)%3]);
			adapterSet.set(Position.class, er[i], coord2);
			DFace df = dhds.addNewFace();
			e[i].linkNextEdge(el[i]);
			el[i].linkNextEdge(er[i]);
			er[i].linkNextEdge(e[i]);
			el[i].linkOppositeEdge(er[(i+1)%2]);
			el[i].setLeftFace(df);
			er[i].setLeftFace(df);
			e[i].setLeftFace(df);
		}
		dhds.removeFace(face);
		// convert to exploded form
		DHDS exploded = new DHDS();
		QuotientMeshUtility.explodeFaces(exploded, dhds, adapterSet);
		
		IndexedFaceSet ifs = converter2.heds2ifs(exploded, adapterSet); //exploded, a);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs, Pn.EUCLIDEAN);
		world.setGeometry(ifs);
	}

	static double[] jitterM;
	{ jitterM = P3.makeTranslationMatrix(null, new double[]{.1, .23, 0}, Pn.EUCLIDEAN); }
	WallpaperGroup group; 
	IndexedFaceSet fundDom;
	double[][] fundDomP;
	private ConverterHeds2JR converter2;
	private AdapterSet adapterSet;
	private SceneGraphComponent world; 


}
