package de.jtem.halfedgetools.symmetry2;

import org.junit.Assert;
import org.junit.Test;

import de.discretization.halfedge.hds.DHDS;
import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.StorageModel;
import de.jtem.discretegroup.groups.WallpaperGroup;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;

public class QuotientMeshUtilityTest {
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

	@Test
	public void test() {
		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
		ifsf.setVertexCount(v.length);
		ifsf.setVertexCoordinates(v);
		ifsf.setFaceCount(indices.length);
		ifsf.setFaceIndices(indices);
		ifsf.setGenerateFaceNormals(true);
		ifsf.setGenerateEdgesFromFaces(true);
		ifsf.update();
		
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
		System.err.println("genus = "+HalfEdgeUtils.getGenus(dhds));
		System.err.println("is valid surface = "+HalfEdgeUtils.isValidSurface(dhds, true));
		
		WallpaperGroup group = WallpaperGroup.instanceOfGroup("O");
		IndexedFaceSet fundDom = (IndexedFaceSet) group.getDefaultFundamentalRegion();
		double[] jitterM = P3.makeTranslationMatrix(null, new double[]{.1, .23, 0}, Pn.EUCLIDEAN);
		 double[][] fundDomP = fundDom.getVertexAttributes(Attribute.COORDINATES).toDoubleArrayArray(null);
		Rn.matrixTimesVector(fundDomP, jitterM, fundDomP);
		fundDom.setVertexAttributes(Attribute.COORDINATES, StorageModel.DOUBLE_ARRAY_ARRAY.createReadOnly(fundDomP));
		group.setDefaultFundamentalDomain(fundDom);

		QuotientMeshUtility.assignCanonicalCoordinates(dhds, a, group, fundDom);
		System.err.println("genus = "+HalfEdgeUtils.getGenus(dhds));
		System.err.println("is valid surface = "+HalfEdgeUtils.isValidSurface(dhds, true));
		Assert.assertEquals(1, HalfEdgeUtils.getGenus(dhds));
		Assert.assertTrue(HalfEdgeUtils.isValidSurface(dhds, true));
	}

}
