package de.jtem.halfedgetools.symmetry2;

import java.awt.Color;

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
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Viewer;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
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
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;

public class SimpleTest2 {

	double[][] v = {
			{0,0,0},
			{1,0,0},
			{0,1,0},
			{1,1,0}
	};
	int[][] indices = {
			{0,1,2},
			{1,3,2}
	};
	double[][] v2 = {
			{0,0,0},
			{.5,0,0},
			{1,0,0},
			{1/3.0,.5,0},
			{2/3.0,.5,0},
			{0,1,0},
			{.5,1,0},
			{1,1,0}
	};
	int[][] indices2 = {
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

	public  <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>	> void doIt()	{

		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(v.length);
		ifsf.setVertexCoordinates(v);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		
		WallpaperGroup group = WallpaperGroup.instanceOfGroup("O");
		fundDom = (IndexedFaceSet) group.getDefaultFundamentalRegion();
		fundDomP = fundDom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Rn.matrixTimesVector(fundDomP, jitterM, fundDomP);
		System.err.println("Jittered coords = \n"+Rn.toString(fundDomP));
		fundDom.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(fundDomP));
		group.setDefaultFundamentalDomain(fundDom);
		
		AdapterSet a = AdapterSet.createGenericAdapters();
		MappedPositionAdapter positionAdapter = new MappedPositionAdapter();
		a.add(positionAdapter);
		MappedCanonicalPositionAdapter canonicalPositionAdapter = new MappedCanonicalPositionAdapter();
		a.add(canonicalPositionAdapter);
		MappedGroupElementAdapter groupAdapter = new MappedGroupElementAdapter();
		a.add(groupAdapter);
		ConverterJR2Heds converter = new ConverterJR2Heds();
		DHDS dhds = new DHDS();
		converter.ifs2heds(ifsf.getIndexedFaceSet(), dhds, a, null);
		
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
		QuotientMeshUtility.assignCanonicalCoordinates(dhds, a, group, fundDom);
		// store away the canonical position
		System.err.println("setting up canonical positions");
		for (DVertex v : dhds.getVertices())	{
			DiscreteGroupElement dge0 = new DiscreteGroupElement();
			double[] cp0 = a.getD(CanonicalPosition.class, v);
			System.err.println("vertex"+v.getIndex()+" "+Rn.toString(cp0));
		}
		// test that the product of group elements around any face is the identity
		for (DFace f: dhds.getFaces())	{
			DEdge e0 = f.getBoundaryEdge();
			DEdge e = e0;
			DiscreteGroupElement dge = new DiscreteGroupElement();
			do {
				DiscreteGroupElement dgee = a.get(GroupElement.class, e, DiscreteGroupElement.class);
				if (dgee != null) dge.multiplyOnLeft(dgee);
				e = e.getNextEdge();
			} while (e != e0);
			if (!Rn.isIdentityMatrix(dge.getArray(), 10E-8)) {
				System.err.println("Warning: product of matrices not the identity");
			}
		}
		DHDS exploded = new DHDS();
		QuotientMeshUtility.assignCoveringSpaceCoordinates(dhds, a);
		QuotientMeshUtility.explodeFaces(exploded, dhds, a);
		ConverterHeds2JR converter2 = new ConverterHeds2JR();
		IndexedFaceSet ifs = converter2.heds2ifs(exploded, a);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs, Pn.EUCLIDEAN);

		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		SceneGraphComponent dirdom = SceneGraphUtility.createFullSceneGraphComponent("dirdom");
		world.setGeometry(ifs);
		world.addChild(dirdom);
		dirdom.setGeometry(fundDom);
		MatrixBuilder.euclidean().translate(0,0,-.01).assignTo(dirdom);
		dirdom.getAppearance().setAttribute(CommonAttributes.VERTEX_DRAW, false);
		dirdom.getAppearance().setAttribute(CommonAttributes.EDGE_DRAW, false);
		
		Viewer jrv = JRViewer.display(world); //f.getIndexedFaceSet());
		world.getAppearance().setAttribute("polygonShader.diffuseColor", Color.white);
	}
	
	static double[] jitterM;
	{ jitterM = P3.makeTranslationMatrix(null, new double[]{.1, .23, 0}, Pn.EUCLIDEAN); }
	WallpaperGroup group; 
	IndexedFaceSet fundDom;
	double[][] fundDomP; 


}
