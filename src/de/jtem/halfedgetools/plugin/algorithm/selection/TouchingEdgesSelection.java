package de.jtem.halfedgetools.plugin.algorithm.selection;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class TouchingEdgesSelection extends AlgorithmPlugin {
	
	private HalfedgeSelection oldSel = null;

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Touching Edges";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
		> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		if(oldSel == null) {
			oldSel = hcp.getSelection();
		}
		HalfedgeSelection faceSel = selectEdges(hcp.get());
		for(E e : faceSel.getEdges(hds)){
			oldSel.setSelected(e, true);
		}
			
		hcp.setSelection(oldSel);
	}
	
	private<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
		>HalfedgeSelection selectEdges(HDS hds) {
		HalfedgeSelection selEdges = hcp.getSelection();
		for (V v : selEdges.getVertices(hds)){
			for(E e : HalfEdgeUtils.incomingEdges(v) ){
				selEdges.setSelected(e,true);
				selEdges.setSelected(e.getOppositeEdge(),true);
			}
		}
		for (V v : selEdges.getVertices(hds)){
			selEdges.setSelected(v, false);
		}
		return selEdges;
	}


	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("InvertPt.png",16,16);  //???
		return info;
	}
}
