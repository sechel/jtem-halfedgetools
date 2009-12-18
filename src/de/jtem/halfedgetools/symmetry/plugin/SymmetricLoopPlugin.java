package de.jtem.halfedgetools.symmetry.plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.jtem.halfedgetools.algorithm.loop.LoopSubdivision;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionCoord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdge3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.CuttingUtility.CuttingInfo;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SymmetricLoopPlugin
	<
		V extends SymmetricVertex<V,E,F>,
		E extends SymmetricEdge<V,E,F> ,
		F extends SymmetricFace<V,E,F>,
		HDS extends SymmetricHDS<V,E,F>
	> extends HalfedgeAlgorithmPlugin<V,E,F,HDS> {
	
	private SubdivisionCoord3DAdapter<V> vA = null;
	private SubdivisionEdge3DAdapter<E> eA;
	
	public SymmetricLoopPlugin(SubdivisionCoord3DAdapter<V> ad, SubdivisionEdge3DAdapter<E> ead) {
		vA = ad;
		this.eA = ead;
	}

	private LoopSubdivision<V,E,F,HDS> subdivider = new LoopSubdivision<V,E,F,HDS>();
	
	
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
		return "Symmetric Loop";
	}
	
	
	@Override
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) {
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		HDS tHDS = hcp.getBlankHDS();
		hds.createCombinatoriallyEquivalentCopy(tHDS);
		if (hds == null) {
			return;
		}
		
		Map<E,Set<E>> oldToDoubleNew = subdivider.subdivide(hds, tHDS, vA,eA);
		
		CuttingInfo<V, E, F> symmCopy = new CuttingInfo<V, E, F>(); 
		CuttingInfo<V, E, F> symmOld = hds.getSymmetryCycles();
		
		for(Set<E> es: symmOld.paths.keySet()) {
			Set<E> newPath = new HashSet<E>();
			for(E e : es) {	
				Set<E> toAdd = oldToDoubleNew.get(e);
//				if(toAdd != null)
					newPath.addAll(toAdd);
			}
			symmCopy.paths.put(newPath, symmOld.paths.get(es));
		}
		
		tHDS.setSymmetryCycles(symmCopy);
		
		tHDS.setGroup(hds.getGroup());
		
		hcp.updateHalfedgeContentAndActiveGeometry(tHDS, true);	
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Symmetric Loop Subdivision");
		return info;
	}

}
