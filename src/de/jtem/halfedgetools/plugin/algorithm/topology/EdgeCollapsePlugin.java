package de.jtem.halfedgetools.plugin.algorithm.topology;

import java.util.Set;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeCollapsePlugin extends HalfedgeAlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		Set<E> edges = hcp.getSelection().getEdges(hds);
		if (edges.isEmpty()) return;
		HalfedgeSelection s = new HalfedgeSelection();
		for (E e : edges) {
			if (e.isPositive()) continue;
			TypedAdapterSet<double[]> a = hcp.getAdapters().query(double[].class);
			double[] p1 = a.get(Position.class, e.getTargetVertex());
			double[] p2 = a.get(Position.class, e.getStartVertex());
			V v = TopologyAlgorithms.collapseEdge(e);
			a.set(Position.class, v, Rn.linearCombination(null, 0.5, p1, 0.5, p2));
			s.setSelected(v, true);
		}
		hcp.setSelection(s);
		hcp.update();
	}

	@Override
	public String getAlgorithmName() {
		return "Collapse Edge";
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Edge Collapser", "Kristoffer Josefsson");
	}

}
