/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

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
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdgeInterpolator;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.adapter.standard.subdivision.SA;
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
		
		return topologicalEditingJR(new StandardVertex());
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
		hs.add(new FaceCollapserPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		hs.add(new FaceScalerPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		hs.add(new FaceSplitterPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		hs.add(new EdgeCollapserPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		hs.add(new EdgeRemoverFillPlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>());
		hs.add(new EdgeRemoverPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new EdgeSplitterPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		hs.add(new CatmullClarkPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>(),new SA.StandardSubdivisionEAdapter<E>(), new SA.StandardSubdivisionFAdapter<F, E>()));
		hs.add(new TriangulatePlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new FillHolesPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>());
		hs.add(new LoopPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>(), new SA.StandardSubdivisionEAdapter<E>(), new SA.StandardSubdivisionFAdapter<F, E>()));
		hs.add(new EdgeQuadPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>(), new SA.StandardSubdivisionEAdapter<E>(), new SA.StandardSubdivisionFAdapter<F, E>()));
		hs.add(new VertexQuadPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>(), new SA.StandardSubdivisionEAdapter<E>(), new SA.StandardSubdivisionFAdapter<F, E>()));
		hs.add(new RootThreePlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>(), new SA.StandardSubdivisionEAdapter<E>(), new SA.StandardSubdivisionFAdapter<F, E>()));
		hs.add(new MedialGraphPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>(), new SA.StandardSubdivisionEAdapter<E>(), new SA.StandardSubdivisionFAdapter<F, E>()));
		hs.add(new PerturbPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		hs.add(new ProjectPlugin<V,E,F,HalfEdgeDataStructure<V,E,F>>(new SA.StandardSubdivisionVAdapter<V>()));
		
		return hs;
	}
	
	
	public static  <
	V extends Vertex<V,E,F>,
	E extends Edge<V,E,F> ,
	F extends Face<V,E,F>,
	HDS extends HalfEdgeDataStructure<V,E,F>
	> Set<Plugin> topologicalEditing(
			SubdivisionVertexAdapter<V> vA, 
			SubdivisionEdgeInterpolator<E> eA, 
			SubdivisionFaceBarycenter<F> fA){
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
		hs.add(new LoopPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new EdgeQuadPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new VertexQuadPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new RootThreePlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new MedialGraphPlugin<V,E,F,HDS>(vA,eA,fA));
		hs.add(new PerturbPlugin<V,E,F,HDS>(vA));
		hs.add(new ProjectPlugin<V,E,F,HDS>(vA));
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
		
		viewer.registerPlugins(TopologyOperations.topologicalEditingJR(new StandardVertex()));
	
		HalfedgeInterfacePlugin<StandardVertex, StandardEdge, StandardFace, StandardHDS> hcp = new HalfedgeInterfacePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS>(
				StandardHDS.class, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
		
		viewer.registerPlugin(hcp);
		
		
		viewer.startup();
	}
}
