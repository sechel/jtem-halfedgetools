package de.jtem.halfedgetools.functional.planarfaces;

import de.jtem.halfedge.Face;

public class PlanarFacesAdapters {

	public static interface VolumeWeight <F extends Face<?, ?, F>> {
		public double getWeight(F f);
	}
	
}
