package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import static de.jtem.halfedgetools.util.CurvatureUtility.getCurvatureTensor;
import static de.jtem.halfedgetools.util.CurvatureUtility.getSortedEigenValues;
import static de.jtem.halfedgetools.util.CurvatureUtility.getSortedEigenVectors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.bsp.KdTree;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.util.GeometryUtility;
import de.jtem.jrworkspace.plugin.Controller;

public class CurvatureVectorFields extends AlgorithmDialogPlugin {

	private JCheckBox
		k1Radio = new JCheckBox("K1", true),
		k2Radio = new JCheckBox("K2"),
		nRadio = new JCheckBox("N"),
		onBoundaryChecker = new JCheckBox("On Boundary");
	private JComboBox<String>
		nodeTypeCombo = new JComboBox<>(new String[] {"Vertices", "Edges", "Faces"});
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
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		Class<CurvatureVectorFields> context = CurvatureVectorFields.class;
		c.storeProperty(context, "k1Checked", k1Radio.isSelected());
		c.storeProperty(context, "k2Checked", k2Radio.isSelected());
		c.storeProperty(context, "nChecked", nRadio.isSelected());
		c.storeProperty(context, "onBoundaryChecked", onBoundaryChecker.isSelected());
		c.storeProperty(context, "nodeType", nodeTypeCombo.getSelectedIndex());
		c.storeProperty(context, "radius", radiusModel.getNumber().doubleValue());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		Class<CurvatureVectorFields> context = CurvatureVectorFields.class;
		k1Radio.setSelected(c.getProperty(context, "k1Checked", true));
		k2Radio.setSelected(c.getProperty(context, "k2Checked", true));
		nRadio.setSelected(c.getProperty(context, "k2Cheked", false));
		onBoundaryChecker.setSelected(c.getProperty(context, "onBoundaryChecked", false));
		nodeTypeCombo.setSelectedIndex(c.getProperty(context, "nodeType", 0));
		radiusModel.setValue(c.getProperty(context, "radius", 4.0));
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog (
		HDS hds,
		AdapterSet a,
		HalfedgeInterface hi
	){
		if (hds.numVertices() == 0) {
			return;
		}
		KdTree<V, E, F> kd = new KdTree<V, E, F>(hds, a, 10, false);
		boolean boundaryOnly = onBoundaryChecker.isSelected();
		double scale = GeometryUtility.getMeanEdgeLength(hds, a);
		double radius = radiusModel.getNumber().doubleValue();
		EVD evd = null;
		Map<Node<V, E, F>, double[]> k1Map = new HashMap<Node<V, E, F>, double[]>();
		Map<Node<V, E, F>, double[]> k2Map = new HashMap<Node<V, E, F>, double[]>();
		Map<Node<V, E, F>, Double> k1AbsMap = new HashMap<Node<V, E, F>, Double>();
		Map<Node<V, E, F>, Double> k2AbsMap = new HashMap<Node<V, E, F>, Double>();
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
					List<E> edgeList = new LinkedList<E>();
					for (E e : hds.getPositiveEdges()) {
						edgeList.add(e);
					}
					nodes = new LinkedList<Node<V,E,F>>(edgeList);
				}
				break;
			case 2: 
				nodes = hds.getFaces(); 
				break;
		}
		
		for (Node<V, E, F> node : nodes) {
			a.setParameter("alpha", 0.5);
			double[] p = a.getD(BaryCenter3d.class, node);
			try {
				evd = getCurvatureTensor(p, scale * radius, kd, a);
				double[] n = a.getD(Normal.class, node);
				double[][] vecs = getSortedEigenVectors(evd);
				double[] values = getSortedEigenValues(evd);
				Rn.projectOntoComplement(vecs[0], vecs[0], n);
				Rn.projectOntoComplement(vecs[1], vecs[1], n);
				Rn.normalize(vecs[0], vecs[0]);
				Rn.normalize(vecs[1], vecs[1]);
				k1Map.put(node, vecs[0]);
				k2Map.put(node, vecs[1]);
				nMap.put(node, n);
				k1AbsMap.put(node, values[0]);
				k2AbsMap.put(node, values[1]);
				if (node instanceof Edge) {
					Edge<V,E,F> e = (Edge<V,E,F>)node;
					Edge<V,E,F> eOpp = e.getOppositeEdge();
					k1Map.put(eOpp, vecs[0]);
					k2Map.put(eOpp, vecs[1]);
					nMap.put(eOpp, n);
					k1AbsMap.put(eOpp, values[0]);
					k2AbsMap.put(eOpp, values[1]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		AbstractVectorFieldMapAdapter k1Adapter = null;
		AbstractVectorFieldMapAdapter k2Adapter = null;
		AbstractVectorFieldMapAdapter nAdapter = null;
		AbstractDoubleMapAdapter k1AbsAdapter = null;
		AbstractDoubleMapAdapter k2AbsAdapter = null;
		
		switch (nodeTypeCombo.getSelectedIndex()) {
			case 0: default:
				k2Adapter = new VertexVectorFieldMaxAdapter(k1Map, "Kmin Vec V");
				k1Adapter = new VertexVectorFieldMinAdapter(k2Map, "Kmax Vec V");
				nAdapter = new VertexVectorFieldAdapter(nMap, "Vertex Normals");
				k2AbsAdapter = new VertexPrincipalCurvaturesMinAdapter(k1AbsMap, "Kmin V");
				k1AbsAdapter = new VertexPrincipalCurvaturesMaxAdapter(k2AbsMap, "Kmax V");
				break;
			case 1: 
				k2Adapter = new EdgeVectorFieldMaxAdapter(k1Map, "Kmin Vec E"); 
				k1Adapter = new EdgeVectorFieldMinAdapter(k2Map, "Kmax Vec E"); 
				nAdapter = new EdgeVectorFieldAdapter(nMap, "Edge Normals"); 
				k2AbsAdapter = new EdgePrincipalCurvaturesMinAdapter(k1AbsMap, "Kmin E");
				k1AbsAdapter = new EdgePrincipalCurvaturesMaxAdapter(k2AbsMap, "Kmax E");
				break;
			case 2: 
				k2Adapter = new FaceVectorFieldMaxAdapter(k1Map, "Kmin Vec F"); 
				k1Adapter = new FaceVectorFieldMinAdapter(k2Map, "Kmax Vec F"); 
				nAdapter = new FaceVectorFieldAdapter(nMap, "Face Normals"); 
				k2AbsAdapter = new FacePrincipalCurvaturesMinAdapter(k1AbsMap, "Kmin F");
				k1AbsAdapter = new FacePrincipalCurvaturesMaxAdapter(k2AbsMap, "Kmax F");
				break;
		}
		
		if (k1Radio.isSelected()) {
			hcp.addLayerAdapter(k1Adapter, false);
			hcp.addLayerAdapter(k1AbsAdapter, false);
		}
		if (k2Radio.isSelected()) {
			hcp.addLayerAdapter(k2Adapter, false);
			hcp.addLayerAdapter(k2AbsAdapter, false);
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
