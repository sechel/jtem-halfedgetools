package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class FaceSplitterPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	private Coord3DAdapter<V> adapter;

	
	public FaceSplitterPlugin(Coord3DAdapter<V> ad) {
		this.adapter = ad;
	}
	
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) { 
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		
		F f = hds.getFace(hcp.getSelectedFaceIndex());
		
		// barycentric coordinate
		double[] pos = new double[] {0.0,0.0,0.0};
		double n = 0.0;
		for(V bv : HalfEdgeUtils.boundaryVertices(f)) {
			pos = Rn.add(null, pos, adapter.getCoord(bv));
			n+=1.0;
		}
		pos = Rn.times(null, 1/n, pos);
		V v = HalfEdgeTopologyOperations.splitFace(f);
		adapter.setCoord(v, pos);
		
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
		return "Split face";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Face splitter");
	}


}
