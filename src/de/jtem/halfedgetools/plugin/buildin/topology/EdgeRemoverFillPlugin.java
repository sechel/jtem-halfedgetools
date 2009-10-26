package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeRemoverFillPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	
	public void execute(HalfedgeConnectorPlugin<V,E,F,HDS> hcp) { 
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		
		E e= hds.getEdge(hcp.getSelectedEdgeIndex());
		
		F f = HalfEdgeTopologyOperations.removeEdgeFill(e);
		
		hcp.updateHalfedgeContentAndActiveGeometry(hds, true);
		
		hcp.setSelectedFaceIndex(f.getIndex());
		
	}

	
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Remove&fill edge";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Edge remover&filler");
	}

	

}
