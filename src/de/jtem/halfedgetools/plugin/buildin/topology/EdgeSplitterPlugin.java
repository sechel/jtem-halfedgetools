package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeSplitterPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	private Coord3DAdapter<V> adapter;
	
	public EdgeSplitterPlugin(Coord3DAdapter<V> ad) {
		this.adapter = ad;
	}
	
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) { 
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		
		E e= hds.getEdge(hcp.getSelectedEdgeIndex());
		
	
		double[] p1 = adapter.getCoord(e.getTargetVertex());
		double[] p2 = adapter.getCoord(e.getStartVertex());
		V v = HalfEdgeTopologyOperations.splitEdge(e);
		adapter.setCoord(v, Rn.linearCombination(null, 0.5, p1, 0.5, p2));
		
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
		return "Split edge";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Edge splitter");
	}
	


}
