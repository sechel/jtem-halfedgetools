package de.jtem.halfedgetools.symmetry.adapters;

import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.EDGE_ADAPTER;

import java.util.Set;

import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricHDS;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;
import de.jtem.halfedgetools.util.MarkedEdgesAdapter;

public class CyclesAdapter<V extends SymmetricVertex<V,E,F>,
E extends SymmetricEdge<V,E,F>,
F extends SymmetricFace<V,E,F>,
HDS extends SymmetricHDS<V,E,F>> extends MarkedEdgesAdapter<V, E, F> {
	
	@Override
	public double[] getColor(E e) {
		
		setContext(e.getSymmetryCycleInfo());
		
		for (Set<E> path : context.paths.keySet()) {
			Set<E> coPath = context.pathCutMap.get(path);
			if (path.contains(e) || path.contains(e.getOppositeEdge())) {
				return pathColors.get(path);
			}
			if(coPath != null) {
				if (coPath.contains(e) || coPath.contains(e.getOppositeEdge())) {
					return pathColors.get(path);
				}
			}
		}
		if(context.isRightIncomingOnCycle((E)e)!=null || context.isRightIncomingOnCycle((E)e.getOppositeEdge()) != null) {
			return new double[] {1,1,1,0};
		}
		return normalColor;
	}
	
	@Override
	public double getReelRadius(E e) {
		setContext(e.getSymmetryCycleInfo());
		for (Set<E> path : context.paths.keySet()) {
			Set<E> coPath = context.pathCutMap.get(path);
			if (path.contains(e) || path.contains(e.getOppositeEdge())) {
				return 2.0;
			}
			if(coPath != null) {
				if (coPath.contains(e) || coPath.contains(e.getOppositeEdge())) {
					return 2.0;
				}
			}
		}
		return 0.5;
	}
	
	
	@Override
	public AdapterType getAdapterType() {
		return EDGE_ADAPTER;
	}
}
