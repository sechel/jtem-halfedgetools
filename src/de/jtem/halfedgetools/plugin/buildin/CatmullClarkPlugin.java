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

package de.jtem.halfedgetools.plugin.buildin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.catmullclark.CatmullClarkSubdivision;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class CatmullClarkPlugin
	<
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F> ,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {
	
	private Coord3DAdapter<V> vA = null;
	private Coord3DAdapter<E> eA;
	private Coord3DAdapter<F> fA;
	
	
	public CatmullClarkPlugin(Coord3DAdapter<V> ad, Coord3DAdapter<E> ead, Coord3DAdapter<F> fac) {
		this.vA = ad;
		this.eA = ead;
		this.fA  = fac;
	}

	private CatmullClarkSubdivision<V,E,F> subdivider = new CatmullClarkSubdivision<V,E,F>();
	
	
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
		return "Catmull Clark";
	}
	
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		hds.createCombinatoriallyEquivalentCopy(tHDS);
		if (hds == null) {
			return;
		}
		subdivider.subdivide(hds, tHDS, vA, eA, fA);
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS);	
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Catmull Clark Subdivision");
		return info;
	}

}
