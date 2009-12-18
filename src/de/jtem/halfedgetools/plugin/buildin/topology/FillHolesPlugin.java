package de.jtem.halfedgetools.plugin.buildin.topology;

import java.util.List;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.functional.alexandrov.SurfaceUtility;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.util.surfaceutilities.SurfaceException;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class FillHolesPlugin<
V extends Vertex<V,E,F>,
E extends Edge<V,E,F>  ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
> extends HalfedgeAlgorithmPlugin<V,E,F,HDS>{

	
	public void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp) { 
		HDS hds = hcp.getCachedHalfEdgeDataStructure();
		
		try {
			SurfaceUtility.linkBoundary(hds);
		} catch (SurfaceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.err.println("Filling holes...");
		List<F> holes;
		try {
			holes = SurfaceUtility.fillHoles(hds);

			System.err.println("Filled " + holes.size() + " holes");
		} catch (SurfaceException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		hcp.updateHalfedgeContentAndActiveGeometry(hds, true);
		
		
	}

	
	public AlgorithmType getAlgorithmType() {
		return AlgorithmType.Geometry;
	}
	
	
	public String getCategoryName() {
		return "Editing";
	}
	
	
	public String getAlgorithmName() {
		return "Fill holes";
	}

	
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Holes filler");
	}
	
	

}
