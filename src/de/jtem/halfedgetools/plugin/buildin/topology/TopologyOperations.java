package de.jtem.halfedgetools.plugin.buildin.topology;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.jreality.geometry.Primitives;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Inspector;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jreality.plugin.content.ContentAppearance;
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
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.HalfedgeConnectorPlugin;
import de.jtem.halfedgetools.plugin.buildin.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.buildin.EdgeQuadPlugin;
import de.jtem.halfedgetools.plugin.buildin.LoopPlugin;
import de.jtem.halfedgetools.plugin.buildin.MedialGraphPlugin;
import de.jtem.halfedgetools.plugin.buildin.QuadStripPlugin;
import de.jtem.halfedgetools.plugin.buildin.TriangulatePlugin;
import de.jtem.halfedgetools.plugin.buildin.VertexQuadPlugin;
import de.jtem.jrworkspace.plugin.Plugin;

public class TopologyOperations {

	public static Set<Plugin> topologicalEditingStandardHDS() {
		
		final class StandardVAdapter implements Coord3DAdapter<StandardVertex> {
			public double[] getCoord(StandardVertex v) {
				return v.position.clone();
			}
			public void setCoord(StandardVertex v, double[] c) {
				v.position = c;
			}
		}
		
		final class StandardEAdapter implements Coord3DAdapter<StandardEdge> {
			public double[] getCoord(StandardEdge e) {
				return e.getTargetVertex().position.clone();
			}
			public void setCoord(StandardEdge e, double[] c) {
				e.getTargetVertex().position = c;
			}
		}
		
		final class StandardMedEAdapter implements Coord3DAdapter<StandardEdge> {
			public double[] getCoord(StandardEdge e) {
				return Rn.linearCombination(null, 0.5, e.getTargetVertex().position, 0.5, e.getStartVertex().position);
			}
			public void setCoord(StandardEdge e, double[] c) {
				e.getTargetVertex().position = c;
			}
		}
		
		final class StandardFAdapter implements Coord3DAdapter<StandardFace> {
			public double[] getCoord(StandardFace f) {
				double[] sum = {0, 0, 0};
				List<StandardEdge> b = HalfEdgeUtils.boundaryEdges(f);
				int size = 0;
				for (StandardEdge e : b) {
					Rn.add(sum, sum, e.getTargetVertex().position);
					size++;
				}
				Rn.times(sum, 1.0 / size, sum);
				return sum;
				
			}
			public void setCoord(StandardFace f, double[] c) {
			}
		}
		
		
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new VertexCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FaceRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FaceCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter()));
		hs.add(new FaceScalerPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter()));
		hs.add(new FaceSplitterPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter()));
		hs.add(new EdgeCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter()));
		hs.add(new EdgeRemoverFillPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeSplitterPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter()));
		hs.add(new CatmullClarkPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter(),new StandardEAdapter(), new StandardFAdapter()));
		hs.add(new TriangulatePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FillHolesPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new LoopPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter(), new StandardEAdapter()));
		hs.add(new EdgeQuadPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter(), new StandardMedEAdapter(), new StandardFAdapter()));
		hs.add(new VertexQuadPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter(), new StandardMedEAdapter(), new StandardFAdapter()));
		hs.add(new MedialGraphPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter(), new StandardMedEAdapter(), new StandardFAdapter()));
		hs.add(new QuadStripPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardVAdapter(), new StandardMedEAdapter(), new StandardFAdapter()));
		return hs;
	}
	
	
	public static  <
	V extends Vertex<V,E,F>,
	E extends Edge<V,E,F> ,
	F extends Face<V,E,F>,
	HDS extends HalfEdgeDataStructure<V,E,F>
	> Set<Plugin> topologicalEditing(Coord3DAdapter<V> vA, Coord3DAdapter<E> eA, Coord3DAdapter<F> fA){
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
		hs.add(new LoopPlugin<V,E,F,HDS>(vA,eA));
		hs.add(new EdgeQuadPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new VertexQuadPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new MedialGraphPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new QuadStripPlugin<V,E,F,HDS>(vA,eA,fA));
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
	
		HalfedgeConnectorPlugin<StandardVertex, StandardEdge, StandardFace, StandardHDS> hcp = new HalfedgeConnectorPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(
				StandardHDS.class, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		viewer.registerPlugin(hcp);
		
		
		viewer.startup();
	}
}
