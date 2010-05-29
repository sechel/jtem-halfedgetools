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

package de.jtem.halfedgetools.symmetry.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.algorithm.subdivision.Sqrt3;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.symmetry.node.SEdge;
import de.jtem.halfedgetools.symmetry.node.SFace;
import de.jtem.halfedgetools.symmetry.node.SHDS;
import de.jtem.halfedgetools.symmetry.node.SVertex;
import de.jtem.halfedgetools.util.TriangulationException;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SymmetricSqrt3Plugin extends HalfedgeAlgorithmPlugin {
	
	private Sqrt3 	
		subdivider = new Sqrt3();
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Subdivision;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Symmetric Sqrt3";
	}
	
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) {
		SHDS shds = hcp.get(new SHDS());
		SHDS result = new SHDS();
		VertexPositionCalculator vc = c.get(shds.getVertexClass(), VertexPositionCalculator.class);
		EdgeAverageCalculator ec = c.get(shds.getEdgeClass(), EdgeAverageCalculator.class);
		FaceBarycenterCalculator fc = c.get(shds.getFaceClass(), FaceBarycenterCalculator.class);
		if (vc == null || ec == null || fc == null) {
			throw new CalculatorException("No Subdivision calculators found for " + hds);
		}
		Map<SEdge, SEdge> oldToDoubleNew = subdivider.subdivide(shds, result, vc, ec, fc);
		CuttingInfo<SVertex, SEdge, SFace> symmCopy = new CuttingInfo<SVertex, SEdge, SFace>(); 
		CuttingInfo<SVertex, SEdge, SFace> symmOld = shds.getSymmetryCycles();
		if (symmOld != null) {
			for(Set<SEdge> es: symmOld.paths.keySet()) {
				Set<SEdge> newPath = new HashSet<SEdge>();
				for(SEdge e : es) {
					if (!oldToDoubleNew.containsKey(e)) continue;
					newPath.add(oldToDoubleNew.get(e));
				}
				symmCopy.paths.put(newPath, symmOld.paths.get(es));
			}
			result.setSymmetryCycles(symmCopy);
			result.setGroup(shds.getGroup());
		}
		
		//flip
		for (SEdge e : oldToDoubleNew.keySet()){
			if (e.isPositive()){
				SEdge flip = result.getEdge(e.getIndex());
				try {
					flip.flip();
				} catch (TriangulationException e1) {
					e1.printStackTrace();
				}
			}
		}
	
		hcp.set(result);
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Symmetric Root3 Subdivision");
		return info;
	}

}
