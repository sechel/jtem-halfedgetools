package de.jtem.halfedgetools.plugin.adapters;

import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.jreality.adapter.ColorAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JRFace;

public class MarkedFaceAdapter <N extends JRFace<?,?,?>>implements ColorAdapter2Ifs<N> {
	
	private Set<N> selectedNodes;
	
	public MarkedFaceAdapter() {

	}
	
	public void setSelectedNodes(Set<N> s) {
		selectedNodes = s;
	}
	
	@Override
	public double[] getColor(N n) {
		
		if(selectedNodes.contains(n)) {
			double[] one = new double[n.color.length];
			for(int i = 0; i < n.color.length; i++) {
				one[i] = 1.0;
			}
			return Rn.subtract(null, one, n.color);
		} else {
			return n.color;
		}
	}
	

	
	@Override
	public AdapterType getAdapterType() {
		return AdapterType.FACE_ADAPTER;
	}



}
