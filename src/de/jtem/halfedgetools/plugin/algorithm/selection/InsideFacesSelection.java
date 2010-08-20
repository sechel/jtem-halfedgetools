package de.jtem.halfedgetools.plugin.algorithm.selection;

import java.util.List;

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
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class InsideFacesSelection extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Inside Faces";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		HalfedgeSelection sel = hcp.getSelection();
		
		
		for (V v : sel.getVertices(hds)){
			for(F f : HalfEdgeUtilsExtra.getFaceStar(v) ){
				boolean s = true;
				for(V vf : HalfEdgeUtils.boundaryVertices(f)){
					s &= sel.isSelected(vf);//&= ist isSelcted einmal false wird auch s false gesetzt
				}
				if(s==true) sel.setSelected(f,true);
			}
		}
		hcp.setSelection(sel);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("InvertPt.png",16,16);  //???
		return info;
	}
}