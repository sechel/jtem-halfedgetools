package de.jtem.halfedgetools.plugin.buildin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class TriangulatePlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
>   extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {

	private Triangulator<V, E, F> 
		triangulator = new Triangulator<V, E, F>();
	
	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	@Override
	public String getCategoryName() {
		return "Edit";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Triangulate";
	}
	
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		if (hds == null) {
			System.err.println("hds was null");
			return;
		}
		triangulator.triangulate(hds);
		hcp.updateHalfedgeContentAndActiveGeometry(hds, true);

	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Triangulator");
		return info;
	}

}
