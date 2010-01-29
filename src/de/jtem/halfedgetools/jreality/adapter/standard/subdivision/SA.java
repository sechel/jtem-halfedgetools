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

package de.jtem.halfedgetools.jreality.adapter.standard.subdivision;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdgeInterpolator;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public interface SA {
	final class StandardVAdapter <V extends JRVertex<V,?,?>> implements Coord3DAdapter<V> {
		public double[] getCoord(V v) {
			return v.position.clone();
		}
		public void setCoord(V v, double[] c) {
			v.position = c;
		}
	}
	
	final class StandardEAdapter  <E extends JREdge<?,E,?>> implements Coord3DAdapter<E> {
		public double[] getCoord(E e) {
			return e.getTargetVertex().position.clone();
		}
		public void setCoord(E e, double[] c) {
			e.getTargetVertex().position = c;
		}
	}
	
	final class StandardMedEAdapter <E extends JREdge<?,E,?>> implements Coord3DAdapter<E> {
		public double[] getCoord(E e) {
			return Rn.linearCombination(null, 0.5, e.getTargetVertex().position, 0.5, e.getStartVertex().position);
		}
		public void setCoord(E e, double[] c) {
			e.getTargetVertex().position = c;
		}
	}
	
	final class StandardSubdivisionVAdapter <V extends JRVertex<V,?,?>> implements SubdivisionVertexAdapter<V> {
		public double[] getData(V v) {
			return v.position.clone();
		}
		public void setData(V v, double[] c) {
			v.position = c;
		}
	}
	
	final class StandardSubdivisionEAdapter <E extends JREdge<?,E,?>> implements SubdivisionEdgeInterpolator<E> {
		public double[] getData(E e, double a, boolean i) {
			return Rn.linearCombination(null, a, e.getTargetVertex().position.clone(), 1-a, e.getStartVertex().position.clone());
		}
	}
	

	
	final class StandardSubdivisionFAdapter <F extends JRFace<?,E,F>, E extends JREdge<?,E,F>> implements SubdivisionFaceBarycenter<F> {
		public double[] getData(F f) {
			double[] sum = {0, 0, 0};
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			int size = 0;
			for (E e : b) {
				Rn.add(sum, sum, e.getTargetVertex().position);
				size++;
			}
			Rn.times(sum, 1.0 / size, sum);
			return sum;
			
		}

	}
}
