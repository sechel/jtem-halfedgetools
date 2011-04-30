package de.jtem.halfedgetools.jreality;

import org.junit.Before;
import org.junit.Test;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.adapter.JRColorAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRTexturePositionAdapter;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;

public class TestConverterJR2Heds {

	private IndexedFaceSet
		g = null;
	
	@Before
	public void setUp() throws Exception {
//		IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
//		double[][] vertexData = {
//			{0,0,0,1},
//			{0,1,0,1},
//			{1,1,0,1},
//			{1,0,0,1}
//		};
//		int[][] faceData = {
//			{0,0,1,2},	
//			{0,2,3},
//			{0,1},
//			{0}
//		};
//		ifsf.setVertexCount(4);
//		ifsf.setFaceCount(4);
//		ifsf.setVertexCoordinates(vertexData);
//		ifsf.setFaceIndices(faceData);
//		ifsf.update();
//		g = ifsf.getIndexedFaceSet();
//		IndexedFaceSetUtility.makeConsistentOrientation(g);
		ReaderOBJ r = new ReaderOBJ();
		SceneGraphComponent c = r.read(getClass().getResource("roofquads_curvatue01.obj"));
		g = (IndexedFaceSet)SceneGraphUtility.getFirstGeometry(c);
		IndexedFaceSetUtility.makeConsistentOrientation(g);
	}

	@Test
	public void testIfs2hedsIndexedFaceSetHDSAdapterSet() {
		ConverterJR2Heds c = new ConverterJR2Heds();
		DefaultJRHDS hds = new DefaultJRHDS();
		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new JRPositionAdapter());
		a.add(new JRTexturePositionAdapter());
		a.add(new JRColorAdapter());
		c.ifs2heds(g, hds, a);
	}

}
