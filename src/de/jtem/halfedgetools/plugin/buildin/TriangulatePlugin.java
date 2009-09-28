package de.jtem.halfedgetools.plugin.buildin;

import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.VERTEX_ADAPTER;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class TriangulatePlugin extends HalfedgeAlgorithmPlugin {

	private Triangulator<StandardVertex, StandardEdge, StandardFace> 
		triangulator = new Triangulator<StandardVertex, StandardEdge, StandardFace>();
	
	@Override
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	@Override
	public String getCategoryName() {
		return "Edit";
	}
	
	@Override
	public String getAlgorithmName() {
		return "Triangulate";
	}
	
	
	@Override
	public void execute(HalfedgeConnectorPlugin hcp) {
		StandardHDS hds = hcp.getHalfedgeContent(new StandardCoordinateAdapter(VERTEX_ADAPTER));
		if (hds == null) {
			return;
		}
		triangulator.triangulate(hds);
		hcp.updateHalfedgeContent(hds, true, new StandardCoordinateAdapter(VERTEX_ADAPTER));		
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Triangulator");
		return info;
	}

}
