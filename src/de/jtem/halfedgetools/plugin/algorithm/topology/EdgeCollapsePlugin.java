package de.jtem.halfedgetools.plugin.algorithm.topology;

import java.awt.event.InputEvent;

import javax.swing.KeyStroke;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.TexturePosition;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition2d;
import de.jtem.halfedgetools.algorithm.topology.TopologyAlgorithms;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.TypedSelection;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class EdgeCollapsePlugin extends AlgorithmPlugin {

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		TypedSelection<E> edges = hcp.getSelection().getEdges(hds);
		if (edges.isEmpty()) return;
		Selection s = new Selection();
		for (E e : edges) {
			if (e.isPositive()) continue;
			double[] p = a.getD(BaryCenter3d.class, e);
			double[] tp = a.getD(TexturePosition2d.class, e);
			V v = TopologyAlgorithms.collapseEdge(e);
			TopologyAlgorithms.removeDigonsAt(v);
			a.set(Position.class, v, p);
			a.set(TexturePosition.class, v, tp);
			s.add(v);
		}
		hcp.update();
		hcp.setSelection(s);
	}
	
	@Override
	public String getAlgorithmName() {
		return "Collapse Edge";
	}
	
	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('E', InputEvent.CTRL_DOWN_MASK);
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
