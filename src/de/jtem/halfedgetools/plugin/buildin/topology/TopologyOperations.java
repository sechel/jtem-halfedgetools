package de.jtem.halfedgetools.plugin.buildin.topology;

import java.util.HashSet;
import java.util.Set;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.buildin.CatmullClarkPlugin;
import de.jtem.halfedgetools.plugin.buildin.TriangulatePlugin;
import de.jtem.jrworkspace.plugin.Plugin;

public class TopologyOperations {

	public static Set<Plugin> topologicalEditingStandardHDS() {
		
		final class StandardCoordAdapter implements Coord3DAdapter<StandardVertex> {

			public double[] getCoord(StandardVertex v) {
				return v.position;
			}

			public void setCoord(StandardVertex v, double[] c) {
				v.position = c;
			}
			
		}
		
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new VertexCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FaceRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FaceCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardCoordAdapter()));
		hs.add(new FaceScalerPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardCoordAdapter()));
		hs.add(new FaceSplitterPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardCoordAdapter()));
		hs.add(new EdgeCollapserPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardCoordAdapter()));
		hs.add(new EdgeRemoverFillPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeRemoverPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeSplitterPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardCoordAdapter()));
		hs.add(new CatmullClarkPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(new StandardCoordAdapter()));
		hs.add(new TriangulatePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new FillHolesPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		return hs;
	}
	
	
	public static  <
	V extends Vertex<V,E,F>,
	E extends Edge<V,E,F> ,
	F extends Face<V,E,F>,
	HDS extends HalfEdgeDataStructure<V,E,F>
	> Set<Plugin> topologicalEditing(Coord3DAdapter<V> adapter){
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin<V,E,F,HDS>()); 
		hs.add(new VertexCollapserPlugin<V,E,F,HDS>()); 
		hs.add(new FaceRemoverPlugin<V,E,F,HDS>()); 
		hs.add(new FaceCollapserPlugin<V,E,F,HDS>(adapter));
		hs.add(new FaceScalerPlugin<V,E,F,HDS>(adapter));
		hs.add(new FaceSplitterPlugin<V,E,F,HDS>(adapter));
		hs.add(new EdgeCollapserPlugin<V,E,F,HDS>(adapter));
		hs.add(new EdgeRemoverFillPlugin<V,E,F,HDS>()); 
		hs.add(new EdgeRemoverPlugin<V,E,F,HDS>()); 
		hs.add(new EdgeSplitterPlugin<V,E,F,HDS>(adapter));
		hs.add(new CatmullClarkPlugin<V,E,F,HDS>(adapter));
		hs.add(new TriangulatePlugin<V,E,F,HDS>());
		hs.add(new FillHolesPlugin<V,E,F,HDS>());
		return hs;
	}
	
}
