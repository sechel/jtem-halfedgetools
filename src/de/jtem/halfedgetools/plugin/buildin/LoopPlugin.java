package de.jtem.halfedgetools.plugin.buildin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.loop.LoopSubdivision;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin.AlgorithmType;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class LoopPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V, E, F, HDS> {

	private Coord3DAdapter<V> vA;
	private Coord3DAdapter<E> eA;
	
	private LoopSubdivision<V,E,F,HDS> subdivider = new LoopSubdivision<V, E, F, HDS>();

	public LoopPlugin(Coord3DAdapter<V> ad, Coord3DAdapter<E> ead){
		this.vA = ad;
		this.eA = ead;
	}
	
	@Override
	public void execute(HalfedgeConnectorPlugin<V, E, F, HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		if (hds == null) {
			return;
		}
		
		subdivider.subdivide(hds, tHDS, vA, eA);
		
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS, true);	
		
	}

	@Override
	public de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin.AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	@Override
	public String getCategoryName() {
		return "Subdivision";
	}

	@Override
	public String getAlgorithmName() {
		return "Loop";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Loop Subdivision");
		return info;
	}

}
