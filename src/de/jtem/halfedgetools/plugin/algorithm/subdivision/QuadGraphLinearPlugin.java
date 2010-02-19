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

package de.jtem.halfedgetools.plugin.algorithm.subdivision;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.algorithm.subdivision.QuadGraphLinear;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.PluginInfo;


public class QuadGraphLinearPlugin extends HalfedgeAlgorithmPlugin {

	private QuadGraphLinear
		subdivider = new QuadGraphLinear();
		
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		HDS hds2 = hcp.createEmpty(hds);
		VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
		EdgeAverageCalculator ec = c.get(hds.getEdgeClass(), EdgeAverageCalculator.class);
		FaceBarycenterCalculator fc = c.get(hds.getFaceClass(), FaceBarycenterCalculator.class);
		if (vc == null || ec == null || fc == null) {
			throw new CalculatorException("No Subdivision calculators found for " + hds);
		}
		Map<V, V> vMap = new HashMap<V, V>();
		Map<F, V> fMap = new HashMap<F, V>();
		subdivider.execute(hds, hds2, vMap, fMap, vc, fc);
		hcp.set(hds2);
	}
	
	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	@Override
	public String getCategoryName() {
		return "Linear Subdivision";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Linear Quad-Graph";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Linear Quad-Graph Subdivision");
		return info;
	}

}
