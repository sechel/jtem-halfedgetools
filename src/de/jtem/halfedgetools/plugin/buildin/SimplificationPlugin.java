package de.jtem.halfedgetools.plugin.buildin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.simplification.GarlandHeckbert;
import de.jtem.halfedgetools.algorithm.simplification.adapters.SimplificationAdapters.AreaAdapter;
import de.jtem.halfedgetools.algorithm.simplification.adapters.SimplificationAdapters.NormalAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SimplificationPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
>extends HalfedgeAlgorithmPlugin<V, E, F, HDS>{


	Coord3DAdapter<V> pa;
	AreaAdapter<F> aa;
	NormalAdapter<F> na;
	
	public SimplificationPlugin(Coord3DAdapter<V> pa, NormalAdapter<F> na, AreaAdapter<F> aa) {
		this.pa = pa;
		this.na = na;
		this.aa = aa;
	}
	
	

	@Override
	public void execute(HalfedgeInterfacePlugin<V, E, F, HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		if (hds == null) {
			return;
		}
		
		GarlandHeckbert<V, E, F, HDS> gh = new GarlandHeckbert<V, E, F, HDS>(hds, pa, na, aa);
	
		gh.simplify(5);
		
		hcp.updateHalfedgeContentAndActiveGeometry(hds);
		
	}

	@Override
	public String getAlgorithmName() {
		return "Garland & Heckbert";
	}


	@Override
	public String getCategoryName() {
		return "Simplification";
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Simplification plugin", "Stefan Sechelmann, Kristoffer Josefsson");
		return info;
	}

	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}

}
