package de.jtem.halfedgetools.plugin.buildin.topology;

import java.util.HashSet;
import java.util.Set;

import de.jtem.jrworkspace.plugin.Plugin;

public class TopologyOperations {

	public static Set<Plugin> topologicalEditing() {
		HashSet<Plugin> hs = new HashSet<Plugin>();
		hs.add(new VertexRemoverPlugin()); 
		hs.add(new VertexCollapserPlugin());
		hs.add(new FaceRemoverPlugin());
		hs.add(new FaceCollapserPlugin());
		hs.add(new FaceScalerPlugin());
		hs.add(new FaceSplitterPlugin());
		hs.add(new EdgeCollapserPlugin());
		hs.add(new EdgeRemoverFillPlugin());
		hs.add(new EdgeRemoverPlugin());
		hs.add(new EdgeSplitterPlugin());
		return hs;
	}
	
}
