package de.jtem.halfedgetools.symmetry.standard;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;

@XStreamAlias("hds")
public class SHDS extends SymmetricHDS<SVertex, SEdge, SFace>{


	public SHDS() {
		super(SVertex.class, SEdge.class, SFace.class);

	}
	
//	private DiscreteGroup group = null;
//	
//	public void setGroup(DiscreteGroup g) {
//		group = g;
//	}
//	
//	public DiscreteGroup getGroup() {
//		return group;
//	}

}
