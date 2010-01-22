/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2009, Technische Universität Berlin, jTEM
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
import de.jtem.halfedgetools.functional.edgelength.hds.MyELAdapters.LAdapter;
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
		
		LAdapter l0 = new LAdapter(l);
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
