package de.jtem.halfedgetools.plugin.algorithm.selection;

import javax.swing.KeyStroke;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class BoundaryEdgeSelection extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Boundary Edges";
	}

	@Override
	public KeyStroke getKeyboardShortcut() {
		return KeyStroke.getKeyStroke('b');
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		Selection sel = hcp.getSelection();
		for (E e : hds.getEdges()){
			if (HalfEdgeUtils.isBoundaryEdge(e)) {
				if (sel.contains(e)) {
					sel.remove(e);
				} else {
					sel.add(e);
				}
			}
		}
		hcp.setSelection(sel);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("bdEdges.png",16,16);
		return info;
	}
}
