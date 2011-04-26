package de.jtem.halfedgetools.plugin.algorithm.generator;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Random;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import de.jreality.math.Rn;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.algorithm.computationalgeometry.ConvexHull;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;

public class RandomEllipsoidGenerator extends AlgorithmDialogPlugin {

	private Random 
		rnd = new Random();
	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		numPointsModel = new SpinnerNumberModel(4, 4, 100000, 1),
		aSpinnerModel = new SpinnerNumberModel(1.0, 0.01, 10000.0, 0.1),
		bSpinnerModel = new SpinnerNumberModel(1.0, 0.01, 10000.0, 0.1),
		cSpinnerModel = new SpinnerNumberModel(1.0, 0.01, 10000.0, 0.1);
	private JSpinner
		numPointsSpinner = new JSpinner(numPointsModel),
		aSpinner = new JSpinner(aSpinnerModel),
		bSpinner = new JSpinner(bSpinnerModel),
		cSpinner = new JSpinner(cSpinnerModel);
	
	public RandomEllipsoidGenerator() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c1 = LayoutFactory.createLeftConstraint();
		GridBagConstraints c2 = LayoutFactory.createRightConstraint();
		panel.add(new JLabel("Points"), c1);
		panel.add(numPointsSpinner, c2);
		panel.add(new JLabel("a"), c1);
		panel.add(aSpinner, c2);
		panel.add(new JLabel("b"), c1);
		panel.add(bSpinner, c2);
		panel.add(new JLabel("c"), c1);
		panel.add(cSpinner, c2);
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Generator;
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}

	
	@Override
	public String getAlgorithmName() {
		return "Random Ellipsoid";
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet ad, HalfedgeInterface hi) {
		double a = aSpinnerModel.getNumber().doubleValue();
		double b = bSpinnerModel.getNumber().doubleValue();
		double c = cSpinnerModel.getNumber().doubleValue();
		int numPoints = numPointsModel.getNumber().intValue();
		HDS r = hi.createEmpty(hds);
		for (int i = 0; i < numPoints; i++) {
			double[] pos = {rnd.nextGaussian(), rnd.nextGaussian(), rnd.nextGaussian()};
			Rn.normalize(pos, pos);
			pos[0] *= a;
			pos[1] *= b;
			pos[2] *= c;
			V v = r.addNewVertex();
			ad.set(Position.class, v, pos);
		}
		ConvexHull.convexHull(r, ad, 1E-8);
		hi.set(r);
	}

}
