package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;
public class EdgeCollapserPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	private Coord3DAdapter<V> adapter;

	public EdgeCollapserPlugin(Coord3DAdapter<V> ad) {
		this.adapter = ad;
	}
	
	public void execute(HalfedgeConnectorPlugin<V,E,F,HDS> hcp) {
		
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		E e = hds.getEdge(hcp.getSelectedEdgeIndex());
		double[] p1 = adapter.getCoord(e.getTargetVertex());
		double[] p2 = adapter.getCoord(e.getStartVertex());
		V v = HalfEdgeTopologyOperations.collapseEdge(e);
		adapter.setCoord(v, Rn.linearCombination(null, 0.5, p1, 0.5, p2));
		
//		StandardHDS hds = hedsConnector.getActiveGeometryAsStandardHDS(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
//		StandardEdge e= hds.getEdge(hedsConnector.getSelectedEdgeIndex());
//		double[] p1 = e.getTargetVertex().position;
//		double[] p2 = e.getStartVertex().position;
//		StandardVertex v = HalfEdgeTopologyOperations.collapseEdge(e);
//		v.position = Rn.linearCombination(null, 0.5, p1, 0.5, p2);
		
		hcp.updateHalfedgeContentAndActiveGeometry(hds, true);
		
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
		return new PluginInfo("Edge collapser");
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
