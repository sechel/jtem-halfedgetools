package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class VertexRemoverPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) { 
		
//		StandardHDS hds = hedsConnector.getActiveGeometryAsStandardHDS(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
//		StandardVertex v = hds.getVertex(hedsConnector.getSelectedVertexIndex());
		
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		V v = hds.getVertex(hcp.getSelectedVertexIndex());
		
		HalfEdgeTopologyOperations.removeVertex(v);

		hcp.updateHalfedgeContentAndActiveGeometry(hds);
		
	}

	
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Remove vertex";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Vertex remover");
	}

}
