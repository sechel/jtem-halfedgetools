package de.jtem.halfedgetools.functional.circlepattern;

import java.util.Random;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.FunctionalTest;
import de.jtem.halfedgetools.functional.MyDomainValue;
import de.jtem.halfedgetools.functional.circlepattern.hds.CPEdge;
import de.jtem.halfedgetools.functional.circlepattern.hds.CPFace;
import de.jtem.halfedgetools.functional.circlepattern.hds.CPHDS;
import de.jtem.halfedgetools.functional.circlepattern.hds.CPVertex;
import de.jtem.halfedgetools.functional.circlepattern.hds.MyCPAdapters.MyPhi;
import de.jtem.halfedgetools.functional.circlepattern.hds.MyCPAdapters.MyTheta;

public class CPEuclideanFunctionalTest extends FunctionalTest<CPVertex, CPEdge, CPFace, MyDomainValue> {

	@Override
	public void init() {
		CPHDS hds = new CPHDS();
		HalfEdgeUtils.addDodecahedron(hds);
		hds.removeFace(hds.getFace(0));
		
		MyTheta theta = new MyTheta();
		MyPhi phi = new MyPhi();
		
		CPEuclideanFunctional<CPVertex, CPEdge, CPFace, MyDomainValue>
			functional = new CPEuclideanFunctional<CPVertex, CPEdge, CPFace, MyDomainValue>(theta, phi);
		
		int n = functional.getDimension(hds);
		
		Random rnd = new Random(); 
		rnd.setSeed(1);
		Vector x = new DenseVector(n);
		for (Integer i = 0; i < x.size(); i++) {
			x.set(i, rnd.nextDouble() - 0.5);
		}
		
		MyDomainValue rho = new MyDomainValue(x);
		
		setFuctional(functional);
		setXGradient(rho);
		setXHessian(rho);
		setHDS(hds);
	}

}
