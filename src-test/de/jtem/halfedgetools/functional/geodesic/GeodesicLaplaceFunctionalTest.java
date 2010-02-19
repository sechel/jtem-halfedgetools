package de.jtem.halfedgetools.functional.geodesic;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.MyDomainValue;
import de.jtem.halfedgetools.jreality.node.DefaultJREdge;
import de.jtem.halfedgetools.jreality.node.DefaultJRFace;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;

public class GeodesicLaplaceFunctionalTest
		extends
		FunctionalTest<DefaultJRVertex, DefaultJREdge, DefaultJRFace> {

	@Override
	public void init() {
		DefaultJRHDS hds = new DefaultJRHDS();
	
		FunctionalTestData.createCombinatorialOctahedron(hds);
		Random random = new Random(123);
		for (DefaultJRVertex v : hds.getVertices()) {
			v.position = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
		}
		
//		hds.getVertex(0).position = new double[]{.0,.0,.0};
//		hds.getVertex(1).position = new double[]{2.0,2.0,.0};
//		hds.getVertex(2).position = new double[]{.0,2.0,.0};
//		hds.getVertex(3).position = new double[]{2.0,.0,.0};
//		hds.getVertex(4).position = new double[]{1.0,1.0,1.0};
//		hds.getVertex(5).position = new double[]{1.0,1.0,-1.0};

		Vector result = new DenseVector(hds.numVertices() * 3);
		for (DefaultJRVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.position[0]);
			result.set(v.getIndex() * 3 + 1, v.position[1]);
			result.set(v.getIndex() * 3 + 2, v.position[2]);
		}	
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
		// setXHessian(pos);
		setHDS(hds);
		setFuctional(new GeodesicLaplaceFunctional<DefaultJRVertex, DefaultJREdge, DefaultJRFace>());
	}

}
