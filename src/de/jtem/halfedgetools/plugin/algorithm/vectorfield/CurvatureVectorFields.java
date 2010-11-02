package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import static de.jtem.halfedgetools.util.CurvatureUtility.getCurvatureTensor;
import static de.jtem.halfedgetools.util.CurvatureUtility.getSortedEigenVectors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import no.uib.cipr.matrix.EVD;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.util.GeometryUtility;

public class CurvatureVectorFields extends AlgorithmDialogPlugin {

	private JCheckBox
		k1Radio = new JCheckBox("K1", true),
		k2Radio = new JCheckBox("K2"),
		nRadio = new JCheckBox("N"),
		onBoundaryChecker = new JCheckBox("On Boundary");
	private JComboBox
		nodeTypeCombo = new JComboBox(new String[] {"Vertices", "Edges", "Faces"});
	private SpinnerNumberModel
		radiusModel = new SpinnerNumberModel(6.0, 0.1, 10.0, 0.1);
	private JSpinner
		radiusSpinner = new JSpinner(radiusModel);
	private JPanel
		panel = new JPanel(),
		vecPanel = new JPanel();
	
	public CurvatureVectorFields() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Radius"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(radiusSpinner, c);
		panel.add(vecPanel, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("On"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(nodeTypeCombo, c);
		panel.add(onBoundaryChecker, c);
		nodeTypeCombo.setSelectedIndex(0);
		
		vecPanel.setLayout(new GridLayout(1, 3));
		vecPanel.add(k1Radio);
		vecPanel.add(k2Radio);
		vecPanel.add(nRadio);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(
		HDS hds,
		CalculatorSet c,
		HalfedgeInterface hcp
	) throws CalculatorException {
		AdapterSet a = hcp.getAdapters();
		KdTree<V, E, F> kd = new KdTree<V, E, F>(hds, a, 10, false);
		boolean boundaryOnly = onBoundaryChecker.isSelected();
		double scale = GeometryUtility.getMeanEdgeLength(hds, a);
		double radius = radiusModel.getNumber().doubleValue();
		EVD evd = null;
		Map<Node<V, E, F>, double[]> k1Map = new HashMap<Node<V, E, F>, double[]>();
		Map<Node<V, E, F>, double[]> k2Map = new HashMap<Node<V, E, F>, double[]>();
		Map<Node<V, E, F>, double[]> nMap = new HashMap<Node<V, E, F>, double[]>();
		
		Collection<? extends Node<V, E, F>> nodes = null;
		switch (nodeTypeCombo.getSelectedIndex()) {
			case 0: default: 
				if (boundaryOnly) {
					nodes = HalfEdgeUtils.boundaryVertices(hds);
				} else {
					nodes = hds.getVertices();
				}
				break;
			case 1: 
				if (boundaryOnly) {
					nodes = HalfEdgeUtils.boundaryEdges(hds);
				} else {
					nodes = hds.getEdges();
				}
				break;
			case 2: 
				nodes = hds.getFaces(); 
				break;
		}
		
		for (Node<V, E, F> node : nodes) {
			double[] p = a.get(BaryCenter.class, node, double[].class);
			try {
				evd = getCurvatureTensor(p, scale * radius, kd, a);
				double[] n = a.get(Normal.class, node, double[].class);
				double[][] vecs = getSortedEigenVectors(evd);
				Rn.projectOntoComplement(vecs[0], vecs[0], n);
				Rn.projectOntoComplement(vecs[1], vecs[1], n);
				Rn.normalize(vecs[0], vecs[0]);
				Rn.normalize(vecs[1], vecs[1]);
				k1Map.put(node, vecs[0]);
				k2Map.put(node, vecs[1]);
				nMap.put(node, n);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		AbstractVectorFielAdapter k1Adapter = null;
		AbstractVectorFielAdapter k2Adapter = null;
		AbstractVectorFielAdapter nAdapter = null;
		
		switch (nodeTypeCombo.getSelectedIndex()) {
			case 0: default: 
				k1Adapter = new VertexVectorFielAdapter(k1Map, "K1 Curvature Field"); 
				k2Adapter = new VertexVectorFielAdapter(k2Map, "K2 Curvature Field"); 
				nAdapter = new VertexVectorFielAdapter(nMap, "Normals"); 
				break;
			case 1: 
				k1Adapter = new EdgeVectorFielAdapter(k1Map, "K1 Curvature Field"); 
				k2Adapter = new EdgeVectorFielAdapter(k2Map, "K2 Curvature Field"); 
				nAdapter = new EdgeVectorFielAdapter(nMap, "Normals"); 
				break;
			case 2: 
				k1Adapter = new FaceVectorFielAdapter(k1Map, "K1 Curvature Field"); 
				k2Adapter = new FaceVectorFielAdapter(k2Map, "K2 Curvature Field"); 
				nAdapter = new FaceVectorFielAdapter(nMap, "Normals"); 
				break;
		}
		
		if (k1Radio.isSelected()) {
			hcp.addLayerAdapter(k1Adapter, false);
		}
		if (k2Radio.isSelected()) {
			hcp.addLayerAdapter(k2Adapter, false);
		}
		if (nRadio.isSelected()) {
			hcp.addLayerAdapter(nAdapter, false);
		}
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.VectorField;
	}

	@Override
	public String getAlgorithmName() {
		return "Curvature Vector Fields";
	}

}
