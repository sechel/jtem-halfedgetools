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

package de.jtem.halfedgetools.functional.edgelength.hds;

import no.uib.cipr.matrix.Vector;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.Length;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.WeightFunction;

public class MyELAdapters {

	public static class MyDomainValue implements DomainValue {

		private Vector
			x = null;
		
		public MyDomainValue(Vector x) {
			this.x = x;
		}
		
		@Override
		public void add(int i, double value) {
			x.add(i, value);
		}

		@Override
		public void set(int i, double value) {
			x.set(i, value);
		}

		@Override
		public void setZero() {
			x.zero();
		}

		@Override
		public double get(int i) {
			return x.get(i);
		}
		
	}
	
	public static class LAdapter implements Length<ELEdge> {

		private double 
			l0 = 0.0;
		
		public LAdapter(double l) {
			this.l0 = l;
		}
		
		@Override
		public Double getTargetLength(ELEdge e) {
			return l0;
		}
		
		public void setL0(double l0) {
			this.l0 = l0;
		}
		
	}
	
	
	public static class ConstantWeight implements WeightFunction<ELEdge> {

		public double 
			w = 1.0;
		
		public ConstantWeight(double w) {
			this.w = w;
		}
		
		@Override
		public Double getWeight(ELEdge e) {
			return w;
		}
		
	}
	
	
}
