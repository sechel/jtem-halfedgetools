package de.jtem.halfedgetools.plugin.algorithm.selection;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class InvertFaceSelection extends AlgorithmPlugin {

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Invert Faces";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		HalfedgeSelection sel = hcp.getSelection();
		for (F f : hds.getFaces()){
			boolean selected = sel.isSelected(f);
			sel.setSelected(f, !selected);
		}
		hcp.setSelection(sel);
	}

}
