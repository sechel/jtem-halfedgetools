package de.jtem.halfedgetools.algorithm.simplification;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.reader.AbstractReader;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.adapter.JRNormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;

public class GarlandHeckbertTest {
	
	private DefaultJRHDS 
		result = new DefaultJRHDS(),
		expected = new DefaultJRHDS();
	private AdapterSet
		adapters = AdapterSet.createGenericAdapters();

	public DefaultJRHDS loadGeometry(String resourceName, AbstractReader reader) throws Exception {
		InputStream stream = GarlandHeckbertTest.class.getResourceAsStream(resourceName);
		Input input = Input.getInput("", stream);
		SceneGraphComponent component = reader.read(input);
		IndexedFaceSet ifs = (IndexedFaceSet)SceneGraphUtility.getFirstGeometry(component);
		ConverterJR2Heds converter = new ConverterJR2Heds();
		DefaultJRHDS result = new DefaultJRHDS();
		converter.ifs2heds(ifs, result, adapters);
		return result;
	}
	
	
	@Before
	public void setUp() throws Exception {
		adapters.add(new JRPositionAdapter());
		adapters.add(new JRNormalAdapter());
		result = loadGeometry("cowSource.obj", new ReaderOBJ());
		expected = loadGeometry("cowExpected.obj", new ReaderOBJ());
	}

	@Test
	public void test() {
		GarlandHeckbert<DefaultJRVertex, DefaultJREdge, DefaultJRFace, DefaultJRHDS> 
			gh = new GarlandHeckbert<DefaultJRVertex, DefaultJREdge, DefaultJRFace, DefaultJRHDS>(result, adapters);
		gh.simplify(100);
		Assert.assertEquals(expected.numVertices(), result.numVertices());
		for (DefaultJRVertex vResult : result.getVertices()) {
			DefaultJRVertex vExpected = expected.getVertex(vResult.getIndex());
			double[] posResult = vResult.position;
			double[] posExpected = vExpected.position;
			Assert.assertArrayEquals("vertex " + vResult.getIndex(), posExpected, posResult, 1E-12);
		}
	}
	
	public static void main(String[] args) throws Exception {
		GarlandHeckbertTest test = new GarlandHeckbertTest();
		test.setUp();
		test.test();
		JRViewer v = new JRViewer();
		v.registerPlugin(HalfedgeInterface.class);
		v.addContentSupport(ContentType.CenteredAndScaled);
		v.addContentUI();
		v.addBasicUI();
		v.startup();
		v.getPlugin(HalfedgeInterface.class).set(test.result);
	}
	
}
