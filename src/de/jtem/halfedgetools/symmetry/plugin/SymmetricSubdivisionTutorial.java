package de.jtem.halfedgetools.symmetry.plugin;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionCoord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdge3DAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.plugin.HalfedgeDebuggerPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.buildin.TriangulatePlugin;
import de.jtem.halfedgetools.plugin.buildin.topology.EdgeFlipperPlugin;
import de.jtem.halfedgetools.symmetry.adapters.CyclesAdapter;
import de.jtem.halfedgetools.symmetry.adapters.DebugBundleAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricCoordinateAdapter;
import de.jtem.halfedgetools.symmetry.adapters.SymmetricSymmetryFaceColorAdapter;
import de.jtem.halfedgetools.symmetry.standard.SEdge;
import de.jtem.halfedgetools.symmetry.standard.SFace;
import de.jtem.halfedgetools.symmetry.standard.SHDS;
import de.jtem.halfedgetools.symmetry.standard.SVertex;

public class SymmetricSubdivisionTutorial {

	private static class MyVAdapter implements SubdivisionCoord3DAdapter<SVertex> {

		public double[] getCoord(SVertex v) {
			return v.getEmbedding();
		}

		public void setCoord(SVertex v, double[] c) {
			v.setEmbedding(c);
		}
		
	}
	
	private static class MyEAdapter implements SubdivisionEdge3DAdapter<SEdge> {

		public double[] getCoord(SEdge e, double a, boolean ignore) {

			return e.getEmbeddingOnEdge(a, ignore);
			
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

//		viewer.registerPlugin(new SymmetricCatmullClarkPlugin<SVertex,SEdge,SFace,SHDS>(
//				new MyVAdapter(),
//				new MyEAdapter(),
//				new MyFAdapter()
//				));
//		
		viewer.registerPlugin(new SymmetricLoopPlugin<SVertex,SEdge,SFace,SHDS>(
				new MyVAdapter(),
				new MyEAdapter()
				));
		
		viewer.registerPlugin(new CompactifierPlugin());
		
		viewer.registerPlugin(new EdgeFlipperPlugin<SVertex,SEdge,SFace,SHDS>());
		viewer.registerPlugin(new TriangulatePlugin<SVertex,SEdge,SFace,SHDS>());
//		viewer.registerPlugins(TopologyOperations.topologicalEditing(new MyVAdapter(), new MyEAdapter(), new MyFAdapter()));
		
		viewer.registerPlugin(new HalfedgeDebuggerPlugin<SVertex,SEdge,SFace,SHDS>());
		
		viewer.registerPlugin(new HalfedgeInterfacePlugin
				<SVertex,SEdge,SFace,SHDS>(SHDS.class,
				new SymmetricCoordinateAdapter(AdapterType.VERTEX_ADAPTER),
				new SymmetricSymmetryFaceColorAdapter<SVertex,SEdge,SFace>(),
				new CyclesAdapter<SVertex,SEdge,SFace,SHDS>(),
				new DebugBundleAdapter(AdapterType.EDGE_ADAPTER)));
		
		viewer.startup();
	}
}
