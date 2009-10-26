package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class FaceScalerPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	private Coord3DAdapter<V> adapter;

	public FaceScalerPlugin(Coord3DAdapter<V> ad) {
		this.adapter = ad;
	}
	
	public void execute(HalfedgeConnectorPlugin<V,E,F,HDS> hcp) { 
		
		// parameters!
		double t = 0.5;
		int twist = 0;
		
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		
		F oldF = hds.getFace(hcp.getSelectedFaceIndex());
		
		int n = HalfEdgeUtils.boundaryVertices(oldF).size();
		
		double[][] oldVs = new double[n][3];
		
		// barycentric coordinate
		double[] pos = new double[] {0.0,0.0,0.0};
		int i = 0;
		for(V bv : HalfEdgeUtils.boundaryVertices(oldF)) {
			pos = Rn.add(null, pos, adapter.getCoord(bv));
			oldVs[i] = adapter.getCoord(bv);
			i++;
		}
		pos = Rn.times(null, 1/((double)n), pos);
		
		F f = HalfEdgeTopologyOperations.scaleFace(oldF);
		
		i = 0;
		for(V v : HalfEdgeUtils.boundaryVertices(f)) {
			adapter.setCoord(v, Rn.linearCombination(null, t, pos, 1.0-t, oldVs[(i+twist-1+n)%n]));
			i++;
		}
		
		hcp.updateHalfedgeContentAndActiveGeometry(hds, true);
		
		hcp.setSelectedFaceIndex(f.getIndex());
		
		
	}

	@Override
	public String getAlgorithmName() {
		return "Scale a face";
	}

	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}

	@Override
	public String getCategoryName() {
		return "Editing";
	}

	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Face scaler");
	}


}
