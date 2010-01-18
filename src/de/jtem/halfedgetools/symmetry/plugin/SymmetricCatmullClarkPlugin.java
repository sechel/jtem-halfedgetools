package de.jtem.halfedgetools.symmetry.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.catmullclark.CatmullClarkSubdivision;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SymmetricCatmullClarkPlugin
	<
		V extends SymmetricVertex<V,E,F>,
		E extends SymmetricEdge<V,E,F> ,
		F extends SymmetricFace<V,E,F>,
		HDS extends SymmetricHDS<V,E,F>
	> extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {
	
	private Coord3DAdapter<V> adapter = null;
	private Coord3DAdapter<E> ead;
	private Coord3DAdapter<F> fac;
	
	public SymmetricCatmullClarkPlugin(Coord3DAdapter<V> ad, Coord3DAdapter<E> ead, Coord3DAdapter<F> fac) {
		adapter = ad;
		this.ead = ead;
		this.fac  = fac;
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
		return "Symmetric Catmull Clark";
	}
	
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		hds.createCombinatoriallyEquivalentCopy(tHDS);
		if (hds == null) {
			return;
		}
		
		Map<E,Set<E>> oldToDoubleNew = subdivider.subdivide(hds, tHDS, adapter,ead,fac);
		
		CuttingInfo<V, E, F> symmCopy = new CuttingInfo<V, E, F>(); 
		CuttingInfo<V, E, F> symmOld = hds.getSymmetryCycles();
		for(Set<E> es: symmOld.paths.keySet()) {
			Set<E> newPath = new HashSet<E>();
			for(E e : es) {
				for(E ee : oldToDoubleNew.get(e)) {
					newPath.add(ee);
				}
			}
			symmCopy.paths.put(newPath, symmOld.paths.get(es));
		}
		
		tHDS.setSymmetryCycles(symmCopy);
		
		tHDS.setGroup(hds.getGroup());
		
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS);	
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Symmetric Catmull Clark Subdivision");
		return info;
	}

}
