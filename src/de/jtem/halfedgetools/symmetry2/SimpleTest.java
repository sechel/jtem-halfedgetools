package de.jtem.halfedgetools.symmetry2;

import de.discretization.halfedge.hds.DEdge;
import de.discretization.halfedge.hds.DFace;
import de.discretization.halfedge.hds.DHDS;
import de.discretization.halfedge.hds.DVertex;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.Pn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupElement;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.discretegroup.core.DiscreteGroupUtility;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;

public class SimpleTest {

	public static void main(String[] args) {
		// setup a torus
		DHDS dhds = new DHDS();
		DHDS exploded = new DHDS();
		DVertex dv = dhds.addNewVertex();
		
		DEdge de0l = dhds.addNewEdge();
		DEdge de0r = dhds.addNewEdge();
		DEdge de1l = dhds.addNewEdge();
		DEdge de1r = dhds.addNewEdge();
		DEdge de2l = dhds.addNewEdge();
		DEdge de2r = dhds.addNewEdge();
		DFace df0 = dhds.addNewFace();
		DFace df1 = dhds.addNewFace();
		for (DEdge de : dhds.getEdges())	{
			de.setTargetVertex(dv);
		}
		de0l.setLeftFace(df0);
		de1l.setLeftFace(df0);
		de2l.setLeftFace(df0);
		de0r.setLeftFace(df1);
		de1r.setLeftFace(df1);
		de2r.setLeftFace(df1);
		de0l.linkNextEdge(de2l);
		de2l.linkNextEdge(de1l);
		de1l.linkNextEdge(de0l);
		de0r.linkNextEdge(de2r);
		de2r.linkNextEdge(de1r);
		de1r.linkNextEdge(de0r);
		de0l.linkOppositeEdge(de0r);
		de1l.linkOppositeEdge(de1r);
		de2l.linkOppositeEdge(de2r);
		
		System.err.println("genus = "+HalfEdgeUtils.getGenus(dhds));
		System.err.println("is valid surface = "+HalfEdgeUtils.isValidSurface(dhds, true));
		
		// construct the corresponding discrete group
//		DiscreteGroup dg = new DiscreteGroup();
//		dg.setDimension(2);
//		gens[0] = new DiscreteGroupElement();
//		gens[0].setWord("a");
//		Matrix m = MatrixBuilder.euclidean().translate(1,0,0).getMatrix();
//		gens[0].setArray(m.getArray());
//		gens[1] = gens[0].getInverse();
//		gens[2] = new DiscreteGroupElement();
//		gens[2].setWord("b");
//		m = MatrixBuilder.euclidean().translate(0,1,0).getMatrix();
//		gens[2].setArray(m.getArray());
//		gens[3] = gens[2].getInverse();
//		dg.setGenerators(gens);
		DiscreteGroup dg = WallpaperGroup.instanceOfGroup("O");
		DiscreteGroupElement[] gens = dg.getGenerators();
		DiscreteGroupElement Ab = DiscreteGroupUtility.elementFromWord(dg, "Ab"); //new DiscreteGroupElement(gens[1]);
//		Ab.multiplyOnRight(gens[2]);
		DiscreteGroupElement aB = Ab.getInverse();
		
		MappedGroupElementAdapter groupAdapter = new MappedGroupElementAdapter();
		MappedPositionAdapter positionAdapter = new MappedPositionAdapter();
		AdapterSet a = new AdapterSet();
		a.add(groupAdapter);
		a.add(positionAdapter);
		
		// these set()'s will use the groupAdapter to set the values: 
		// that is, it will put them into the hash map in the groupAdapter
		a.set(GroupElement.class, de0l, gens[0]);
		a.set(GroupElement.class, de1l, Ab);
		a.set(GroupElement.class, de2l, gens[3]);
		a.set(GroupElement.class, de0r, gens[2]);
		a.set(GroupElement.class, de1r, aB);
		a.set(GroupElement.class, de2r, gens[1]);
		
		// this algorithm gets the data out of the map we just put it in
		QuotientMeshUtility.assignCoveringSpaceCoordinates(dhds, a);
		QuotientMeshUtility.explodeFaces(exploded, dhds, a);
		ConverterHeds2JR converter = new ConverterHeds2JR();
		IndexedFaceSet ifs = converter.heds2ifs(exploded, a);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(ifs, Pn.EUCLIDEAN);
		WallpaperGroup group = WallpaperGroup.instanceOfGroup("O");
		group.setConstraint(new DiscreteGroupSimpleConstraint(100));
		group.update();
		DiscreteGroupSceneGraphRepresentation dgsgr = new DiscreteGroupSceneGraphRepresentation(group);
		SceneGraphComponent fd = new SceneGraphComponent("fd");
		fd.setGeometry(ifs);
		dgsgr.setWorldNode(fd);
		dgsgr.update();
		
		JRViewer.display(dgsgr.getRepresentationRoot());
	}
}
