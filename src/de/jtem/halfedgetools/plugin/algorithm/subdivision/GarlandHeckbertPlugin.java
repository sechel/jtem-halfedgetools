package de.jtem.halfedgetools.plugin.algorithm.subdivision;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.calculator.FaceAreaCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceNormalCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.algorithm.simplification.GarlandHeckbert;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;
import de.jtem.halfedgetools.plugin.HalfedgeAlgorithmPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class GarlandHeckbertPlugin extends HalfedgeAlgorithmPlugin {

	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		VertexPositionCalculator vc = c.get(hds.getVertexClass(), VertexPositionCalculator.class);
		FaceNormalCalculator fnc = c.get(hds.getFaceClass(), FaceNormalCalculator.class);
		FaceAreaCalculator fac = c.get(hds.getFaceClass(), FaceAreaCalculator.class);
		if (vc == null || fnc == null || fac == null) {
			throw new CalculatorException("No Subdivision calculators found for " + hds);
		}
		Triangulator t = new Triangulator();
		t.triangulate(hds);
		GarlandHeckbert<V, E, F, HDS> gh = new GarlandHeckbert<V, E, F, HDS>(hds, vc, fnc, fac);
		gh.simplify(5);
		hcp.set(hds);
	}

	@Override
	public String getAlgorithmName() {
		return "Garland & Heckbert";
	}


	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Simplification;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Garland & Heckbert Algorithm", "Stefan Sechelmann, Kristoffer Josefsson");
		return info;
	}

}
