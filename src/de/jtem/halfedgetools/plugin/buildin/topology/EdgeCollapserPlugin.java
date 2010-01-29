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

package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;
public class EdgeCollapserPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	private SubdivisionVertexAdapter<V> adapter;

	public EdgeCollapserPlugin(SubdivisionVertexAdapter<V> ad) {
		this.adapter = ad;
	}
	
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) {
		
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		E e = hds.getEdge(hcp.getSelectedEdgeIndex());
		double[] p1 = adapter.getData(e.getTargetVertex());
		double[] p2 = adapter.getData(e.getStartVertex());
		V v = HalfEdgeTopologyOperations.collapseEdge(e);
		adapter.setData(v, Rn.linearCombination(null, 0.5, p1, 0.5, p2));
		
//		StandardHDS hds = hedsConnector.getActiveGeometryAsStandardHDS(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
//		StandardEdge e= hds.getEdge(hedsConnector.getSelectedEdgeIndex());
//		double[] p1 = e.getTargetVertex().position;
//		double[] p2 = e.getStartVertex().position;
//		StandardVertex v = HalfEdgeTopologyOperations.collapseEdge(e);
//		v.position = Rn.linearCombination(null, 0.5, p1, 0.5, p2);
		
		hcp.updateHalfedgeContentAndActiveGeometry(hds);
		
		hcp.setSelectedVertexIndex(v.getIndex());
		
	}

	
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Colllapse edge";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Edge collapser", "Kristoffer Josefsson");
	}


	

}
//public class EdgeCollapserPlugin extends HalfedgeAlgorithmPlugin<StandardVertex,StandardEdge,StandardFace, StandardHDS>{
//
//	private Content content = null; 
//	private HalfedgeConnectorPlugin<StandardVertex,StandardEdge,StandardFace, StandardHDS> hedsConnector = null;
//
//	
//	public void execute(HalfedgeConnectorPlugin<StandardVertex,StandardEdge,StandardFace, StandardHDS> hcp) {
//		StandardHDS hds = hedsConnector.getActiveGeometryAsStandardHDS(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
//		
//		StandardEdge e= hds.getEdge(hedsConnector.getSelectedEdgeIndex());
//	
//		double[] p1 = e.getTargetVertex().position;
//		double[] p2 = e.getStartVertex().position;
//		StandardVertex v = HalfEdgeTopologyOperations.collapseEdge(e);
//		v.position = Rn.linearCombination(null, 0.5, p1, 0.5, p2);
//		
//		hedsConnector.updateHalfedgeContent(hds, true, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
//		
//		hedsConnector.setSelectedVertexIndex(v.getIndex());
//		
//		content.fireContentChanged();
//	}
//
//	
//	public AlgorithmType getAlgorithmType() {
//		return AlgorithmType.Geometry;
//	}
//	
//	
//	public String getCategoryName() {
//		return "Editing";
//	}
//	
//	
//	public String getAlgorithmName() {
//		return "Colllapse edge";
//	}
//
//	
//	public PluginInfo getPluginInfo() {
//		return new PluginInfo("Edge collapser");
//	}
//	
//	
//	public void install(Controller c) throws Exception {
//		super.install(c);
//		
//		content = JRViewerUtility.getContentPlugin(c);
//		hedsConnector = c.getPlugin(HalfedgeConnectorPlugin.class);
//
//	}
//	
//	
//	public void uninstall(Controller c) throws Exception {
//		super.uninstall(c);
//	}
//
//	
//
//}
