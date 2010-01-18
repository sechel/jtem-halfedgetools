package de.jtem.halfedgetools.plugin.buildin.topology;

import java.util.HashSet;
import java.util.Set;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.plugin.menu.BackgroundColor;
import de.jreality.plugin.menu.CameraMenu;
import de.jreality.plugin.menu.DisplayOptions;
import de.jreality.plugin.menu.ExportMenu;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.Edge3DAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.adapter.standard.subdivision.standardAdapters;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeInterfacePlugin;
import de.jtem.halfedgetools.plugin.buildin.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.buildin.EdgeQuadPlugin;
import de.jtem.halfedgetools.plugin.buildin.LoopPlugin;
import de.jtem.halfedgetools.plugin.buildin.MedialGraphPlugin;
import de.jtem.halfedgetools.plugin.buildin.RootThreePlugin;
import de.jtem.halfedgetools.plugin.buildin.TriangulatePlugin;
import de.jtem.halfedgetools.plugin.buildin.VertexQuadPlugin;
import de.jtem.jrworkspace.plugin.Plugin;

public class TopologyOperations {

	public static Set<Plugin> topologicalEditingStandardHDS() {
		
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new VertexCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FaceRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FaceCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>()));
		hs.add(new FaceScalerPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>()));
		hs.add(new FaceSplitterPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>()));
		hs.add(new EdgeCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>()));
		hs.add(new EdgeRemoverFillPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeSplitterPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>()));
		hs.add(new CatmullClarkPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>(),new standardAdapters.StandardEAdapter<StandardEdge>(), new standardAdapters.StandardFAdapter<StandardFace, StandardEdge>()));
		hs.add(new TriangulatePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FillHolesPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new LoopPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardSubdivisionVAdapter<StandardVertex>(), new standardAdapters.StandardSubdivisionEAdapter<StandardEdge>()));
		hs.add(new EdgeQuadPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>(), new standardAdapters.StandardMedEAdapter<StandardEdge>(), new standardAdapters.StandardFAdapter<StandardFace, StandardEdge>()));
		hs.add(new VertexQuadPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>(), new standardAdapters.StandardMedEAdapter<StandardEdge>(), new standardAdapters.StandardFAdapter<StandardFace, StandardEdge>()));
		hs.add(new RootThreePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>(), new standardAdapters.StandardMedEAdapter<StandardEdge>(), new standardAdapters.StandardFAdapter<StandardFace, StandardEdge>()));
		hs.add(new MedialGraphPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>(), new standardAdapters.StandardMedEAdapter<StandardEdge>(), new standardAdapters.StandardFAdapter<StandardFace, StandardEdge>()));
		hs.add(new PerturbPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new standardAdapters.StandardVAdapter<StandardVertex>()));
		
		return hs;
	}
	
	public static 
	<
	V extends JRVertex<V,E,F>, 
	E extends JREdge<V,E,F>, 
	F extends JRFace<V,E,F>
	>  Set<Plugin> topologicalEditingJR(V v) {
		
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new VertexCollapserPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new FaceRemoverPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new FaceCollapserPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>()));
		hs.add(new FaceScalerPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>()));
		hs.add(new FaceSplitterPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>()));
		hs.add(new EdgeCollapserPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>()));
		hs.add(new EdgeRemoverFillPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeRemoverPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new EdgeSplitterPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>()));
		hs.add(new CatmullClarkPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>(),new standardAdapters.StandardEAdapter<E>(), new standardAdapters.StandardFAdapter<F, E>()));
		hs.add(new TriangulatePlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new FillHolesPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new LoopPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardSubdivisionVAdapter<V>(), new standardAdapters.StandardSubdivisionEAdapter<E>()));
		hs.add(new EdgeQuadPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>(), new standardAdapters.StandardMedEAdapter<E>(), new standardAdapters.StandardFAdapter<F, E>()));
		hs.add(new VertexQuadPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>(), new standardAdapters.StandardMedEAdapter<E>(), new standardAdapters.StandardFAdapter<F, E>()));
		hs.add(new RootThreePlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>(), new standardAdapters.StandardMedEAdapter<E>(), new standardAdapters.StandardFAdapter<F, E>()));
		hs.add(new MedialGraphPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>(), new standardAdapters.StandardMedEAdapter<E>(), new standardAdapters.StandardFAdapter<F, E>()));
		hs.add(new PerturbPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new standardAdapters.StandardVAdapter<V>()));
		
		return hs;
	}
	
	
	public static  <
	V extends Vertex<V,E,F>,
	E extends Edge<V,E,F> ,
	F extends Face<V,E,F>,
	HDS extends HalfEdgeDataStructure<V,E,F>
	> Set<Plugin> topologicalEditing(Coord3DAdapter<V> vA, Coord3DAdapter<E> eA, Coord3DAdapter<F> fA, Edge3DAdapter<E> eA2){
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin<V,E,F,HDS>()); 
		hs.add(new VertexCollapserPlugin<V,E,F,HDS>()); 
		hs.add(new FaceRemoverPlugin<V,E,F,HDS>()); 
		hs.add(new FaceCollapserPlugin<V,E,F,HDS>(vA));
		hs.add(new FaceScalerPlugin<V,E,F,HDS>(vA));
		hs.add(new FaceSplitterPlugin<V,E,F,HDS>(vA));
		hs.add(new EdgeCollapserPlugin<V,E,F,HDS>(vA));
		hs.add(new EdgeRemoverFillPlugin<V,E,F,HDS>()); 
		hs.add(new EdgeRemoverPlugin<V,E,F,HDS>()); 
		hs.add(new EdgeSplitterPlugin<V,E,F,HDS>(vA));
		hs.add(new CatmullClarkPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new TriangulatePlugin<V,E,F,HDS>());
		hs.add(new FillHolesPlugin<V,E,F,HDS>());
//		hs.add(new LoopPlugin<V,E,F,HDS>(vA,eA2));
		hs.add(new EdgeQuadPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new VertexQuadPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new RootThreePlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new MedialGraphPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new PerturbPlugin<V,E,F,HDS>(vA));
		return hs;
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
		viewer.registerPlugin(new ContentTools());
		viewer.setPropertiesFile("sdfsdf.jrw");
		viewer.registerPlugin(new ContentLoader());
		viewer.setShowPanelSlots(true, false, false, false);
		viewer.setShowToolBar(true);
		viewer.addContentSupport(ContentType.CenteredAndScaled);
		
		viewer.registerPlugins(TopologyOperations.topologicalEditingStandardHDS());
	
		HalfedgeInterfacePlugin<StandardVertex, StandardEdge, StandardFace, StandardHDS> hcp = new HalfedgeInterfacePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(
				StandardHDS.class, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		viewer.registerPlugin(hcp);
		
		
		viewer.startup();
	}
}
