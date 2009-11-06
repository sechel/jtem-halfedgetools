package de.jtem.halfedgetools.plugin.buildin;

import java.util.HashMap;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.stefansub.Subdivision;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.util.surfaceutilities.SurfaceException;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class VertexQuadPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V, E, F, HDS> {

	private Coord3DAdapter<F> fA;
	private Coord3DAdapter<E> eA;
	private Coord3DAdapter<V> vA;

	public VertexQuadPlugin(Coord3DAdapter<V> vA, Coord3DAdapter<E> eA, Coord3DAdapter<F> fA){
		this.vA = vA;
		this.eA = eA;
		this.fA = fA;
	}
	
	@Override
	public void execute(HalfedgeConnectorPlugin<V, E, F, HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		
		if (hds == null) {
			return;
		}
		
		try {
			Subdivision.createVertexQuadGraph(hds, tHDS, new HashMap<V,V>(), new HashMap<F,V>(), vA, eA, fA);
		} catch (SurfaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		return "Vertex quad";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Vertex quad Subdivision");
		return info;
	}

}
