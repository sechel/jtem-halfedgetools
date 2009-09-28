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

public class FaceScalerPlugin extends HalfedgeAlgorithmPlugin{

	private Content content;
	private HalfedgeConnectorPlugin hedsConnector;

	public void execute(HalfedgeConnectorPlugin hcp) { 
		
		// parameters!
		double t = 0.5;
		int twist = 0;
		
		StandardHDS hds = hedsConnector.getHalfedgeContent(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		StandardFace oldF = hds.getFace(hedsConnector.getSelectedFaceIndex());
		
		int n = HalfEdgeUtils.boundaryVertices(oldF).size();
		
		double[][] oldVs = new double[n][3];
		
		
		// barycentric coordinate
		double[] pos = new double[] {0.0,0.0,0.0};
		int i = 0;
		for(StandardVertex bv : HalfEdgeUtils.boundaryVertices(oldF)) {
			pos = Rn.add(null, pos, bv.position);
			oldVs[i] = bv.position;
			i++;
		}
		pos = Rn.times(null, 1/((double)n), pos);
		
		StandardFace f = HalfEdgeTopologyOperations.scaleFace(oldF);
		
		i = 0;
		for(StandardVertex v : HalfEdgeUtils.boundaryVertices(f)) {
			v.position = Rn.linearCombination(null, t, pos, 1.0-t, oldVs[(i+twist-1+n)%n]);
			i++;
		}
		
		hedsConnector.updateHalfedgeContent(hds, true, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		hedsConnector.setSelectedFaceIndex(f.getIndex());
		
		
		content.fireContentChanged();
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
	
	public void install(Controller c) throws Exception {
		super.install(c);
		
		content = JRViewerUtility.getContentPlugin(c);
		hedsConnector = c.getPlugin(HalfedgeConnectorPlugin.class);

	}
	
	
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
	}

}
