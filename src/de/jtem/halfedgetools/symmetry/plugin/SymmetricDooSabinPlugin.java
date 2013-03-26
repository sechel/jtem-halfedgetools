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

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.subdivision.DooSabin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SymmetricDooSabinPlugin extends AlgorithmPlugin {
	
	private DooSabin
		subdivider = new DooSabin();
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Subdivision;
	}
	
	@Override
	public String getAlgorithmName() {
		return "Symmetric Doo Sabin";
	}
	
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
//		SHDS shds = hcp.get(new SHDS());
//		SHDS result = new SHDS();
//		TypedAdapterSet<double[]> da = a.querySet(double[].class);
//		Map<SEdge, Set<SEdge>> oldToDoubleNew = subdivider.subdivide(shds, result, da);
//		CuttingInfo<SVertex, SEdge, SFace> symmCopy = new CuttingInfo<SVertex, SEdge, SFace>(); 
//		CuttingInfo<SVertex, SEdge, SFace> symmOld = shds.getSymmetryCycles();
//		if (symmOld != null) {
//			for(Set<SEdge> es: symmOld.paths.keySet()) {
//				Set<SEdge> newPath = new HashSet<SEdge>();
//				for(SEdge e : es) {
//					if (!oldToDoubleNew.containsKey(e)) continue;
//					for(SEdge ee : oldToDoubleNew.get(e)) {
//						newPath.add(ee);
//					}
//				}
//				symmCopy.paths.put(newPath, symmOld.paths.get(es));
//			}
//			result.setSymmetryCycles(symmCopy);
//			result.setGroup(shds.getGroup());
//		}
//		hcp.set(result);
		throw new RuntimeException("unsupported " + subdivider.toString());
	}
	
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Symmetric Doo Sabin Subdivision");
		return info;
	}

}
