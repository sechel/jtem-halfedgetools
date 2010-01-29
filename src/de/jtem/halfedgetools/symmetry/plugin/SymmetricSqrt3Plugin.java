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

import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdgeInterpolator;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.sqrtroot3.Sqrt3Subdivision;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;
import de.jtem.halfedgetools.util.triangulationutilities.TriangulationException;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SymmetricSqrt3Plugin
	<
		V extends SymmetricVertex<V,E,F>,
		E extends SymmetricEdge<V,E,F> ,
		F extends SymmetricFace<V,E,F>,
		HDS extends SymmetricHDS<V,E,F>
	> extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {
	
	private SubdivisionVertexAdapter<V> adapter = null;
	private SubdivisionEdgeInterpolator<E> ead;
	private SubdivisionFaceBarycenter<F> fac;
	
	public SymmetricSqrt3Plugin(SubdivisionVertexAdapter<V> ad, SubdivisionEdgeInterpolator<E> ead, SubdivisionFaceBarycenter<F> fac) {
		adapter = ad;
		this.ead = ead;
		this.fac  = fac;
	}

	private Sqrt3Subdivision<V,E,F,HDS> subdivider = new Sqrt3Subdivision<V,E,F,HDS>();
	
	
	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	@Override
	public String getCategoryName() {
		return "Subdivision";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Symmetric Sqrt(3)";
	}
	
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		hds.createCombinatoriallyEquivalentCopy(tHDS);
		if (hds == null) {
			return;
		}
		
		Map<E, Set<E>> oldToDoubleNew;
		try {
			oldToDoubleNew = subdivider.subdivide(hds, tHDS, adapter,ead,fac);
		
		
			CuttingInfo<V, E, F> symmCopy = new CuttingInfo<V, E, F>(); 
			CuttingInfo<V, E, F> symmOld = hds.getSymmetryCycles();
			
			for(Set<E> es: symmOld.paths.keySet()) {
				Set<E> newPath = new HashSet<E>();
				for(E e : es) {	
					Set<E> toAdd = oldToDoubleNew.get(e);
//					if(toAdd != null)
						newPath.addAll(toAdd);
				}
				symmCopy.paths.put(newPath, symmOld.paths.get(es));
			}
			
			tHDS.setSymmetryCycles(symmCopy);
			
			tHDS.setGroup(hds.getGroup());
			
			for(Set<E> es : oldToDoubleNew.values()) {
				E e = es.iterator().next();
				e.flip();
			}
		
		} catch (TriangulationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS);	
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Symmetric Catmull Clark Subdivision");
		return info;
	}

}
