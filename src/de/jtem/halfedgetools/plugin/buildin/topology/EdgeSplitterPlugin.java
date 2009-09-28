package de.jtem.halfedgetools.plugin.buildin.topology;

import de.jreality.math.Rn;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.util.HalfEdgeTopologyOperations;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeSplitterPlugin extends HalfedgeAlgorithmPlugin {

	private Content content = null;
	private HalfedgeConnectorPlugin hedsConnector = null;

	
	public void execute(HalfedgeConnectorPlugin hcp) { 
		StandardHDS hds = hedsConnector.getHalfedgeContent(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		StandardEdge e= hds.getEdge(hedsConnector.getSelectedEdgeIndex());
		
	
		double[] p1 = e.getTargetVertex().position;
		double[] p2 = e.getStartVertex().position;
		StandardVertex v = HalfEdgeTopologyOperations.splitEdge(e);
		v.position = Rn.linearCombination(null, 0.5, p1, 0.5, p2);
		
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
		return "Split edge";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Edge splitter");
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
