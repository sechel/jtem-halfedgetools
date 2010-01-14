package de.jtem.halfedgetools.functional.edgelength;

import javax.vecmath.Point3d;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.edgelength.hds.ELEdge;
import de.jtem.halfedgetools.functional.edgelength.hds.ELFace;
import de.jtem.halfedgetools.functional.edgelength.hds.ELHDS;
import de.jtem.halfedgetools.functional.edgelength.hds.ELVertex;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.ConstantWeight;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.L0Adapter;
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.MyDomainValue;

public class EdgeLengthFunctionalTest extends FunctionalTest<ELVertex, ELEdge, ELFace> {

	
	@Override
	public void init() {
		ELHDS hds = new ELHDS();
		createTetrahedron(hds);
		
		double l = 0.0;
		for (ELEdge e : hds.getPositiveEdges()) {
			Point3d s = e.getStartVertex().pos;
			Point3d t = e.getTargetVertex().pos;
			l += s.distance(t);
		}
		l /= hds.numEdges() / 2.0;
		
		L0Adapter l0 = new L0Adapter(l);
		ConstantWeight w = new ConstantWeight(1.0);
		
		Vector result = new DenseVector(hds.numVertices() * 3);
		for (ELVertex v : hds.getVertices()) {
			result.set(v.getIndex() * 3 + 0, v.pos.x);
			result.set(v.getIndex() * 3 + 1, v.pos.y);
			result.set(v.getIndex() * 3 + 2, v.pos.z);
		}	
		MyDomainValue pos = new MyDomainValue(result);
		
		setXGradient(pos);
		setXHessian(pos);
		setHDS(hds);
		setFuctional(new EdgeLengthFunctional<ELVertex, ELEdge, ELFace>(l0, w));
	}
	
	
}
