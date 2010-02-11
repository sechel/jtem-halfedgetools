package de.jtem.halfedgetools.functional.formfinding;

import java.util.Random;

import javax.vecmath.Point3d;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.FunctionalTestData;
import de.jtem.halfedgetools.functional.edgelength.hds.ELEdge;
import de.jtem.halfedgetools.functional.edgelength.hds.ELFace;
import de.jtem.halfedgetools.functional.edgelength.hds.ELHDS;
import de.jtem.halfedgetools.functional.edgelength.hds.ELVertex;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.ConstantWeight;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.LAdapter;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.MyDomainValue;

public class GravitySpringFunctionalTest
		extends
		FunctionalTest<ELVertex, ELEdge, ELFace> {

	@Override
	public void init() {
		ELHDS hds = new ELHDS();
	
		FunctionalTestData.createCombinatorialOctahedron(hds);
		Random random = new Random(123);
		for (ELVertex v : hds.getVertices()) {
			v.pos = new Point3d(new double[]{random.nextDouble(), random.nextDouble(), random.nextDouble()});
		}
		
//		hds.getVertex(0).position = new double[]{.0,.0,.0};
//		hds.getVertex(1).position = new double[]{2.0,2.0,.0};
//		hds.getVertex(2).position = new double[]{.0,2.0,.0};
//		hds.getVertex(3).position = new double[]{2.0,.0,.0};
//		hds.getVertex(4).position = new double[]{1.0,1.0,1.0};
//		hds.getVertex(5).position = new double[]{1.0,1.0,-1.0};

		Vector result = new DenseVector(hds.numVertices() * 3);
		for (ELVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.pos.x);
			result.set(v.getIndex() * 3 + 1, v.pos.y);
			result.set(v.getIndex() * 3 + 2, v.pos.z);
		}	
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
		// setXHessian(pos);
		setHDS(hds);
		LAdapter la = new LAdapter(0.0);
		setFuctional(new GravitySpringFunctional<ELVertex, ELEdge, ELFace>(
				la,
				new ConstantWeight(1),
				9.81*1E-3,
				new double[]{0.0,0.0,1.0}));
	}

}
