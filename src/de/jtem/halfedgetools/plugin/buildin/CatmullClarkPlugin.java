package de.jtem.halfedgetools.plugin.buildin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.catmullclark.CatmullClarkSubdivision;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class CatmullClarkPlugin
	<
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F> ,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {
	
	private Coord3DAdapter<V> vA = null;
	private Coord3DAdapter<E> eA;
	private Coord3DAdapter<F> fA;
	
	public CatmullClarkPlugin(Coord3DAdapter<V> ad, Coord3DAdapter<E> ead, Coord3DAdapter<F> fac) {
		this.vA = ad;
		this.eA = ead;
		this.fA  = fac;
	}

	private CatmullClarkSubdivision<V,E,F> subdivider = new CatmullClarkSubdivision<V,E,F>();
	
	
	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	@Override
	public String getCategoryName() {
		return "Subdivision";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Catmull Clark";
	}
	
	
	@Override
	public void execute(HalfedgeConnectorPlugin<V,E,F,HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		hds.createCombinatoriallyEquivalentCopy(tHDS);
		if (hds == null) {
			return;
		}
		subdivider.subdivide(hds, tHDS, vA, eA, fA);
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS, true);	
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Catmull Clark Subdivision");
		return info;
	}

}
