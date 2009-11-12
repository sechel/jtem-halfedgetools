package de.jtem.halfedgetools.symmetry.plugin;

import java.util.List;


import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jtem.discretegroup.plugin.TermesSpherePlugin;
import de.jtem.discretegroup.plugin.TessellatedContent;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeDebuggerPlugin;
import de.jtem.halfedgetools.plugin.buildin.topology.EdgeFlipperPlugin;
import de.jtem.halfedgetools.plugin.buildin.topology.TopologyOperations;
import de.jtem.halfedgetools.symmetry.adapters.CyclesAdapter;
import de.jtem.halfedgetools.symmetry.adapters.DebugBundleAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricCoordinateAdapter;
import de.jtem.halfedgetools.symmetry.standard.SEdge;
import de.jtem.halfedgetools.symmetry.standard.SFace;
import de.jtem.halfedgetools.symmetry.standard.SHDS;
import de.jtem.halfedgetools.symmetry.standard.SVertex;

public class SymmetricSubdivisionTutorial {

	private static class MyCoordAdapter implements Coord3DAdapter<SVertex> {

		public double[] getCoord(SVertex v) {
			return v.getEmbedding();
		}

		public void setCoord(SVertex v, double[] c) {
			v.setEmbedding(c);
		}
		
	}
	
	private static class MyEAdapter implements Coord3DAdapter<SEdge> {

		public double[] getCoord(SEdge e) {
//			return Rn.subtract(null, e.getDirection(), e.getStartVertex().getEmbedding());
//			return e.getTargetVertex().getEmbedding();
			return Rn.add(null, e.getStartVertex().getEmbedding(), e.getDirection());
//			return e.getDirection();
		}

		public void setCoord(SEdge e, double[] c) {
			e.getTargetVertex().setEmbedding(c);
		}
		
	}
	
	private static class MyFAdapter implements Coord3DAdapter<SFace> {

		public double[] getCoord(SFace f) {
			double[] sum = {0, 0, 0};
			List<SEdge> b = HalfEdgeUtils.boundaryEdges(f);
			int size = 0;
			for (SEdge e : b) {
				Rn.add(sum, sum, e.getTargetVertex().getEmbedding());
				size++;
			}
			Rn.times(sum, 1.0 / size, sum);
			return sum;
		}

		public void setCoord(SFace f, double[] c) {
		}
		
	}
	

	public static void main(String[] args) {
	
		JRViewer viewer = new JRViewer();
		viewer.registerPlugin(new Inspector());
		
		viewer.registerPlugin(new BackgroundColor());
		viewer.registerPlugin(new DisplayOptions());
		viewer.registerPlugin(new ViewMenuBar());
		viewer.registerPlugin(new ViewToolBar());
		
		viewer.registerPlugin(new ExportMenu());
		viewer.registerPlugin(new CameraMenu());
		viewer.addContentUI();
		viewer.setShowPanelSlots(true, false, false, false);
		viewer.setShowToolBar(true);
		viewer.setPropertiesFile("test.jrw");

		viewer.registerPlugin(new SymmetricCatmullClarkPlugin<SVertex,SEdge,SFace,SHDS>(
				new MyCoordAdapter(),
				new MyEAdapter(),
				new MyFAdapter()
				));
		
		viewer.registerPlugin(new SymmetricLoopPlugin<SVertex,SEdge,SFace,SHDS>(
				new MyCoordAdapter(),
				new MyEAdapter()
				));
		
		viewer.registerPlugin(new CompactifierPlugin());
		
		viewer.registerPlugin(new EdgeFlipperPlugin<SVertex,SEdge,SFace,SHDS>());
		viewer.registerPlugins(TopologyOperations.topologicalEditing(new MyCoordAdapter(), new MyEAdapter(), new MyFAdapter()));
		
		viewer.registerPlugin(new HalfedgeDebuggerPlugin<SVertex,SEdge,SFace,SHDS>());
		
		viewer.registerPlugin(new HalfedgeConnectorPlugin
				<SVertex,SEdge,SFace,SHDS>(SHDS.class,
				new SymmetricCoordinateAdapter(AdapterType.VERTEX_ADAPTER),
				new CyclesAdapter<SVertex,SEdge,SFace,SHDS>(),
				new DebugBundleAdapter(AdapterType.EDGE_ADAPTER)));
		
		viewer.startup();
	}
}
