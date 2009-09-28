package de.jtem.halfedgetools.plugin;

import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;

public class GeomObject {

	protected SceneGraphComponent
		cgc = null;
	
	public GeomObject(SceneGraphComponent cgc) {
		this.cgc = cgc;
	}

	@Override
	public String toString() {
		IndexedFaceSet ifs = (IndexedFaceSet)cgc.getGeometry();
		return cgc.getGeometry().getName() + " - V:" + ifs.getNumPoints() + " E:" + ifs.getNumEdges() + " F:" + ifs.getNumFaces();
	}
	
}
