package de.jtem.halfedgetools.plugin.buildin.topology;

import java.util.Random;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin.AlgorithmType;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;
public class PerturbPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	private Coord3DAdapter<V> adapter;

	public PerturbPlugin(Coord3DAdapter<V> ad) {
		this.adapter = ad;
	}

	@Override
	public void execute(HalfedgeInterfacePlugin<V, E, F, HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();

		Random r = new Random();
		for(V v : hds.getVertices()) {
			double[] coord = adapter.getCoord(v);
			double[] noise = new double[] {r.nextDouble()-0.5,r.nextDouble()-0.5,r.nextDouble()-0.5};
			noise = Rn.times(null, 0.05, noise);
			adapter.setCoord(v, Rn.add(null, coord, noise));
		}

		hcp.updateHalfedgeContentAndActiveGeometry(hds);
		
	}

	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Perturb vertices";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Vertex perturber");
	}

}
