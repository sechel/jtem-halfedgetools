package de.jtem.halfedgetools.algorithm.triangulation;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;

public class TriangulatorTest {

	@Test
	public void testCutCorner() throws Exception {
		DefaultJRHDS hds = new DefaultJRHDS();
		DefaultJRFace f = HalfEdgeUtils.addNGon(hds, 4);
		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new JRPositionAdapter());
		
		DefaultJRVertex v0 = hds.getVertex(0);
		DefaultJRVertex v1 = hds.getVertex(1);
		DefaultJRVertex v2 = hds.getVertex(2);
		DefaultJRVertex v3 = hds.getVertex(3);
		a.set(Position.class, v0, new double[] {0, 0});
		a.set(Position.class, v1, new double[] {1, 0});
		a.set(Position.class, v2, new double[] {0, 1});
		a.set(Position.class, v3, new double[] {-1, 0});
		DefaultJREdge e = Triangulator.cutCorner(f, a);
		Set<DefaultJRVertex> splitSet = new HashSet<DefaultJRVertex>();
		splitSet.add(e.getStartVertex());
		splitSet.add(e.getTargetVertex());
		Assert.assertTrue(splitSet.contains(v0));
		Assert.assertTrue(splitSet.contains(v2));
	}
	
	@Test
	public void testTriangulateByCuttingCorners() throws Exception {
		DefaultJRHDS hds = new DefaultJRHDS();
		DefaultJRFace f = HalfEdgeUtils.addNGon(hds, 4);
		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new JRPositionAdapter());
		DefaultJRVertex v0 = hds.getVertex(0);
		DefaultJRVertex v1 = hds.getVertex(1);
		DefaultJRVertex v2 = hds.getVertex(2);
		DefaultJRVertex v3 = hds.getVertex(3);
		a.set(Position.class, v0, new double[] {0, 0});
		a.set(Position.class, v1, new double[] {1, 0});
		a.set(Position.class, v2, new double[] {0, 1});
		a.set(Position.class, v3, new double[] {-1, 0});
		
		Triangulator.triangulateByCuttingCorners(f, a);
		Assert.assertEquals(3, HalfEdgeUtils.boundaryVertices(f).size());
	}
	
	@Test
	public void testTriangulateByCuttingCornersDegenerateSide() throws Exception {
		DefaultJRHDS hds = new DefaultJRHDS();
		DefaultJRFace f = HalfEdgeUtils.addNGon(hds, 5);
		AdapterSet a = AdapterSet.createGenericAdapters();
		a.add(new JRPositionAdapter());
		DefaultJRVertex v0 = hds.getVertex(0);
		DefaultJRVertex v1 = hds.getVertex(1);
		DefaultJRVertex v2 = hds.getVertex(2);
		DefaultJRVertex v3 = hds.getVertex(3);
		DefaultJRVertex v4 = hds.getVertex(4);
		a.set(Position.class, v0, new double[] {0, 0});
		a.set(Position.class, v1, new double[] {1, 0});
		a.set(Position.class, v2, new double[] {2, 0});
		a.set(Position.class, v3, new double[] {3, 0});
		a.set(Position.class, v4, new double[] {1.5, 1});		
		
		Triangulator.triangulateByCuttingCorners(f, a);
		DefaultJREdge e0 = HalfEdgeUtils.findEdgeBetweenVertices(v1, v4);
		DefaultJREdge e1 = HalfEdgeUtils.findEdgeBetweenVertices(v2, v4);
		Assert.assertNotNull(e0);
		Assert.assertNotNull(e1);
	}
	
}
