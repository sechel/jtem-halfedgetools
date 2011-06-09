/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

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

public class CPEuclideanFunctionalTest extends FunctionalTest<CPVertex, CPEdge, CPFace> {

	@Override
	public void init() {
		CPHDS hds = new CPHDS();
		HalfEdgeUtils.addDodecahedron(hds);
		hds.removeFace(hds.getFace(0));
		
		MyTheta theta = new MyTheta();
		MyPhi phi = new MyPhi();
		
		CPEuclideanFunctional<CPVertex, CPEdge, CPFace>
			functional = new CPEuclideanFunctional<CPVertex, CPEdge, CPFace>(theta, phi);
		
		int n = functional.getDimension(hds);
		
		Random rnd = new Random(); 
		rnd.setSeed(1);
		Vector x = new DenseVector(n);
		for (Integer i = 0; i < x.size(); i++) {
			x.set(i, rnd.nextDouble() - 0.5);
		}
		
		MyDomainValue rho = new MyDomainValue(x);
		
		setFunctional(functional);
		setXGradient(rho);
		setXHessian(rho);
		setHDS(hds);
	}

}
