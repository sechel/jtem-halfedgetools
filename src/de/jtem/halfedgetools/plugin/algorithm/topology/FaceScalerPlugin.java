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

package de.jtem.halfedgetools.plugin.algorithm.topology;

import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class FaceScalerPlugin extends HalfedgeAlgorithmPlugin {

	private double 
		t = 0.5;
	private int 
		twist = 0;
	
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hif) throws CalculatorException {
		Set<F> faces = hif.getSelection().getFaces(hds);
		if (faces.isEmpty()) return;
		VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
		if (vc == null) {
			throw new CalculatorException("No vertex position calculators found for " + this);
		}
		HalfedgeSelection s = new HalfedgeSelection();
		for (F oldF : faces) {
			int n = HalfEdgeUtils.boundaryVertices(oldF).size();
			double[][] oldVs = new double[n][3];
			double[] pos = new double[] {0.0,0.0,0.0};
			int i = 0;
			for(V bv : HalfEdgeUtils.boundaryVertices(oldF)) {
				pos = Rn.add(null, pos, vc.get(bv));
				oldVs[i] = vc.get(bv);
				i++;
			}
			pos = Rn.times(null, 1/((double)n), pos);
			
			F f = TopologyAlgorithms.scaleFace(oldF);
			i = 0;
			for(V v : HalfEdgeUtils.boundaryVertices(f)) {
				vc.set(v, Rn.linearCombination(null, t, pos, 1.0-t, oldVs[(i+twist-1+n)%n]));
				i++;
			}
			s.setSelected(f, true);
		}
		hif.setSelection(s);
		hif.update();
		
		
	}

	@Override
	public String getAlgorithmName() {
		return "Scale Face";
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Face Scaler", "Kristoffer Josefsson");
	}


}