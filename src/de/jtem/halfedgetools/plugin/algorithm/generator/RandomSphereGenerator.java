package de.jtem.halfedgetools.plugin.algorithm.generator;

import java.util.Random;

import javax.swing.JOptionPane;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.jreality.calculator.JRVertexPositionCalculator;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;

public class RandomSphereGenerator extends AlgorithmPlugin {

	private Random 
		rnd = new Random();
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}

	@Override
	public String getAlgorithmName() {
		return "Random Sphere";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(
		HDS hds, CalculatorSet c, HalfedgeInterface hif) throws CalculatorException 
	{
		String numString = JOptionPane.showInputDialog("Number of points", 20);
		if (numString == null) return;
		int extraPoints = Integer.parseInt(numString);
		DefaultJRHDS r = new DefaultJRHDS();
		for (int i = 0; i < extraPoints; i++) {
			double[] pos = {rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian()};
			Rn.normalize(pos, pos);
			DefaultJRVertex v = r.addNewVertex();
			v.position = pos;
		}
		ConvexHull.convexHull(r, new JRVertexPositionCalculator(), 1E-8);
		hif.set(r);
	}

}
