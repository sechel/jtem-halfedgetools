package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
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

public class VertexCollapserPlugin extends HalfedgeAlgorithmPlugin {

	private Content content = null;
	private HalfedgeConnectorPlugin hedsConnector = null;

	
	public void execute(HalfedgeConnectorPlugin hcp) { 
		StandardHDS hds = hedsConnector.getHalfedgeContent(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		StandardVertex v = hds.getVertex(hedsConnector.getSelectedVertexIndex());
		
		StandardFace f = HalfEdgeTopologyOperations.collapseVertex(v);

		hedsConnector.updateHalfedgeContent(hds, true, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		hedsConnector.setSelectedFaceIndex(f.getIndex());
		
		content.fireContentChanged();
	}

	
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Collapse vertex";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Vertex collapser");
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
