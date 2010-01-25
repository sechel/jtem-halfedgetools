package de.jtem.halfedgetools.functional.willmore;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.MyDomainValue;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;

public class WillmoreFunctionalTest
		extends
		FunctionalTest<StandardVertex, StandardEdge, StandardFace> {

	@Override
	public void init() {
		StandardHDS hds = new StandardHDS();
		FunctionalTestData.createCombinatorialPyrWithBnd(hds);
		
//		FunctionalTestData.createCombinatorialOctahedron(hds);
		Random random = new Random(123);
		for (StandardVertex v : hds.getVertices()) {
			v.position = new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()};
		}
		
//		hds.getVertex(0).position = new double[]{.0,.0,.0};
//		hds.getVertex(1).position = new double[]{2.0,2.0,.0};
//		hds.getVertex(2).position = new double[]{.0,2.0,.0};
//		hds.getVertex(3).position = new double[]{2.0,.0,.0};
//		hds.getVertex(4).position = new double[]{0.0,0.0,2.0};
		

		Vector result = new DenseVector(hds.numVertices() * 3);
		for (StandardVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.position[0]);
			result.set(v.getIndex() * 3 + 1, v.position[1]);
			result.set(v.getIndex() * 3 + 2, v.position[2]);
		}	
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
		// setXHessian(pos);
		setHDS(hds);
		setFuctional(new WillmoreFunctional<StandardVertex, StandardEdge, StandardFace>());
	}

}
