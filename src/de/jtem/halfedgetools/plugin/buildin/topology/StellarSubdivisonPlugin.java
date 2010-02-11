package de.jtem.halfedgetools.plugin.buildin.topology;


import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.stellar.StellarSubdivision;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.image.ImageHook;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class StellarSubdivisonPlugin<
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V,E,F>>
	extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {

	private SubdivisionVertexAdapter<V> 
		vA = null;
	private SubdivisionFaceBarycenter<F> 
		fA = null;
	private StellarSubdivision<V, E, F, HDS>
		subdivider = new StellarSubdivision<V, E, F, HDS>();
	
	public StellarSubdivisonPlugin(
		SubdivisionVertexAdapter<V> vA,
		SubdivisionFaceBarycenter<F> fA)
	{
		this.vA = vA;
		this.fA = fA;
	}
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V, E, F, HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS newHds = hcp.getBlankHDS();
		subdivider.subdivide(hds, newHds, vA, fA);
		hcp.updateHalfedgeContentAndActiveGeometry(newHds);
	}

	@Override
	public String getAlgorithmName() {
		return "Stellar Subdivision";
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
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Stellar Subdivision", "Thilo Roerig");
		info.icon = ImageHook.getIcon("stellar.png");
		return info;
	}

}
