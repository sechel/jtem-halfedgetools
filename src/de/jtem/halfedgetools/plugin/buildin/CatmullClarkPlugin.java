package de.jtem.halfedgetools.plugin.buildin;

import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.VERTEX_ADAPTER;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.catmullclark.CatmullClarkSubdivision;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class CatmullClarkPlugin extends HalfedgeAlgorithmPlugin {

	private CatmullClarkSubdivision<StandardVertex, StandardEdge, StandardFace> 
		subdivider = new CatmullClarkSubdivision<StandardVertex, StandardEdge, StandardFace>();
	
	
	private static class MyCoorAdapter implements Coord3DAdapter<StandardVertex> {

		public double[] getCoord(StandardVertex v) {
			return v.position;
		}

		public void setCoord(StandardVertex v, double[] c) {
			v.position = c;
		}
		
	}
	
	
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
	public void execute(HalfedgeConnectorPlugin hcp) {
		StandardHDS hds = hcp.getHalfedgeContent(new StandardCoordinateAdapter(VERTEX_ADAPTER));
		if (hds == null) {
			return;
		}
		StandardHDS newHds = new StandardHDS();
		subdivider.subdivide(hds, newHds, new MyCoorAdapter());
		hcp.updateHalfedgeContent(newHds, true, new StandardCoordinateAdapter(VERTEX_ADAPTER));		
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Catmull Clark Subdivision");
		return info;
	}

}
