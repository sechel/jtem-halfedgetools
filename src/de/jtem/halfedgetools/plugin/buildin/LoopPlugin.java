package de.jtem.halfedgetools.plugin.buildin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.Edge3DAdapter;
import de.jtem.halfedgetools.algorithm.loop.LoopSubdivision;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionCoord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdge3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin.AlgorithmType;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class LoopPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V, E, F, HDS> {

	private SubdivisionCoord3DAdapter<V> vA;
	private SubdivisionEdge3DAdapter<E> eA;
	
	private LoopSubdivision<V,E,F,HDS> subdivider = new LoopSubdivision<V, E, F, HDS>();

	public LoopPlugin(SubdivisionCoord3DAdapter<V> ad, SubdivisionEdge3DAdapter<E> ead){
		this.vA = ad;
		this.eA = ead;
	}
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V, E, F, HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		if (hds == null) {
			return;
		}
		
		subdivider.subdivide(hds, tHDS, vA, eA);
		
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS);	
		
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
