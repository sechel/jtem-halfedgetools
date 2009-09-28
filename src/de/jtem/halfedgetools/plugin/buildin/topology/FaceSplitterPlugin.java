package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class FaceSplitterPlugin extends HalfedgeAlgorithmPlugin {

	private Content content = null;
	private HalfedgeConnectorPlugin hedsConnector = null;

	
	public void execute(HalfedgeConnectorPlugin hcp) { 
		StandardHDS hds = hedsConnector.getHalfedgeContent(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		StandardFace f = hds.getFace(hedsConnector.getSelectedFaceIndex());
		
		// barycentric coordinate
		double[] pos = new double[] {0.0,0.0,0.0};
		double n = 0.0;
		for(StandardVertex bv : HalfEdgeUtils.boundaryVertices(f)) {
			pos = Rn.add(null, pos, bv.position);
			n+=1.0;
		}
		pos = Rn.times(null, 1/n, pos);
		StandardVertex v = HalfEdgeTopologyOperations.splitFace(f);
		v.position = pos;
		
		hedsConnector.updateHalfedgeContent(hds, true, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		hedsConnector.setSelectedVertexIndex(v.getIndex());
		
		content.fireContentChanged();
	}

	
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Split face";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Face splitter");
	}
	
	
	public void install(Controller c) throws Exception {
		super.install(c);
		
		content = JRViewerUtility.getContentPlugin(c);
		hedsConnector = c.getPlugin(HalfedgeConnectorPlugin.class);

	}
	
	
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

	

}
