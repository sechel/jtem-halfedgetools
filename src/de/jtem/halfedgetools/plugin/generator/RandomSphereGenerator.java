package de.jtem.halfedgetools.plugin.generator;

import java.util.Random;

import javax.swing.JOptionPane;

import de.jreality.math.Rn;
import de.jreality.plugin.basic.Content;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.jreality.calculator.JRVertexPositionCalculator;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.jreality.node.DefaultJRVertex;
import de.jtem.halfedgetools.plugin.GeneratorPlugin;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;

public class RandomSphereGenerator extends GeneratorPlugin {

	private Random 
		rnd = new Random();
	
	@Override
	protected void generate(Content content, HalfedgeInterface hif) {
		String numString = JOptionPane.showInputDialog("Number of points", 20);
		if (numString == null) return;
		int extraPoints = Integer.parseInt(numString);
		DefaultJRHDS hds = new DefaultJRHDS();
		for (int i = 0; i < extraPoints; i++) {
			double[] pos = {rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian()};
			Rn.normalize(pos, pos);
			DefaultJRVertex v = hds.addNewVertex();
			v.position = pos;
		}
		ConvexHull.convexHull(hds, new JRVertexPositionCalculator(), 1E-8);
		hif.set(hds);
	}
	
	@Override
	protected String[] getMenuPath() {
		return new String[] {};
	}

}
