package de.jtem.halfedgetools.plugin;

import static de.jreality.scene.Appearance.DEFAULT;
import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TEXTURE_2D;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.RELATIVE;
import static java.awt.GridBagConstraints.REMAINDER;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.View;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.VectorField;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.util.GeometryUtility;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class VectorFieldManager extends ShrinkPanelPlugin implements ActionListener, ChangeListener, HalfedgeListener {
	
	private SpinnerNumberModel
		lengthModel = new SpinnerNumberModel(1.0, 0.0, 10.0, 0.1),
		thicknessModel = new SpinnerNumberModel(1.0, 0.0, 10.0, 0.1);
	private JSpinner
		lengthSpinner = new JSpinner(lengthModel),
		thicknessSpinner = new JSpinner(thicknessModel);
	private JCheckBox
		directedChecker = new JCheckBox("Directed"),
		tubesChecker = new JCheckBox("Tubes");
	private JPanel
		panel = new JPanel();
	private JTable
		fieldTable = new JTable();
	private JScrollPane
		fieldScrollPane = new JScrollPane(fieldTable);
	private List<Adapter<double[]>> 
		fields = new LinkedList<Adapter<double[]>>();
	private Map<Adapter<double[]>, SceneGraphComponent>
		activeFields = new HashMap<Adapter<double[]>, SceneGraphComponent>();
	private JButton
		updateButton = new JButton("Update");
	private HalfedgeInterface 
		hif = null;
	
	public VectorFieldManager() {
		shrinkPanel.setTitle("Vector Field Manager");
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = BOTH;
		c.insets = new Insets(2, 2, 2, 2);
	
		c.gridwidth = RELATIVE;
		shrinkPanel.add(new JLabel("Length"), c);
		c.gridwidth = REMAINDER;
		shrinkPanel.add(lengthSpinner, c);
		
		c.gridwidth = RELATIVE;
		shrinkPanel.add(new JLabel("Thickness"), c);
		c.gridwidth = REMAINDER;
		shrinkPanel.add(thicknessSpinner, c);
		
		c.gridwidth = RELATIVE;
		shrinkPanel.add(directedChecker, c);
		c.gridwidth = REMAINDER;
		shrinkPanel.add(tubesChecker, c);
		shrinkPanel.add(updateButton, c);
		
		fieldTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		fieldTable.getDefaultEditor(Boolean.class).addCellEditorListener(new VectorFieldActivationListener());
		fieldTable.setRowHeight(22);
		fieldTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		fieldTable.setBorder(BorderFactory.createEtchedBorder());
		fieldTable.setPreferredSize(null);
		fieldTable.setPreferredScrollableViewportSize(null);
		c.weighty = 1.0;
		fieldScrollPane.setPreferredSize(new Dimension(10, 150));
		shrinkPanel.add(fieldScrollPane,c);
		updateButton.addActionListener(this);
	}
	
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		updateStates();
	}
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateStates();
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateStates();
	}
	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}
	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
	}
	
	
	private void updateStates() {
		AdapterSet all = hif.getAdapters();
		all.addAll(hif.getVolatileAdapters());
		all.addAll(hif.getActiveLayer().getVisualizerAdapters());
		fields = all.queryAll(VectorField.class, double[].class);
		Collections.sort(fields);
		Map<Adapter<double[]>, SceneGraphComponent> active = new HashMap<Adapter<double[]>, SceneGraphComponent>();
		for (Adapter<double[]> a : activeFields.keySet()) {
			SceneGraphComponent c = activeFields.get(a);
			if (fields.contains(a)) {
				active.put(a, c);
			}
		}
		activeFields = active; 
		fieldTable.setModel(new VectorFieldTableModel());
		fieldTable.getColumnModel().getColumn(0).setMaxWidth(30);
		panel.updateUI();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Set<Adapter<double[]>> active = new HashSet<Adapter<double[]>>(activeFields.keySet());
		for (Adapter<double[]> a : active) {
			setActive(a, false);
			setActive(a, true);
		}
	}
	
	private < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> IndexedLineSet generateVectorLineSet(
		Collection<N> nodes,
		Adapter<double[]> vec, 
		AdapterSet aSet,
		double posScale,
		double negScale
	) {
		IndexedLineSetFactory ilf = new IndexedLineSetFactory();
		if (nodes.size() == 0) {
			ilf.update();
			return ilf.getIndexedLineSet();
		}
		List<double[]> vData = new LinkedList<double[]>();
		List<int[]> iData = new LinkedList<int[]>();
		for (N node : nodes) {
			double[] v = vec.get(node, aSet);
			if (v == null) continue;
			v = v.clone();
			double[] p = aSet.getD(BaryCenter3d.class, node);
			Rn.normalize(v, v);
			Rn.times(v, posScale, v);
			vData.add(Rn.add(null, p, v));
			Rn.times(v, -1, v);
			vData.add(Rn.add(null, p, v));
			iData.add(new int[] {vData.size() - 1, vData.size() - 2});
		}
		ilf.setVertexCount(vData.size());
		ilf.setEdgeCount(vData.size() / 2);
		ilf.setVertexCoordinates(vData.toArray(new double[][] {}));
		ilf.setEdgeIndices(iData.toArray(new int[][] {}));
		ilf.update();
		return ilf.getIndexedLineSet();
	}
	
	
	private SceneGraphComponent generateVectorArrows(IndexedLineSet ils, double scale, boolean arrows) {
		BallAndStickFactory bsf = new BallAndStickFactory(ils);
		bsf.setShowBalls(false);

		bsf.setShowArrows(arrows);
	    bsf.setArrowScale(scale * 2);
	    bsf.setArrowSlope(1.5);
	    bsf.setArrowPosition(1);
	    
	    bsf.setShowSticks(true);
	    bsf.setStickRadius(scale);
		bsf.update();
		
		SceneGraphComponent c = bsf.getSceneGraphComponent();
		Appearance app = c.getAppearance();
		app.setAttribute(POLYGON_SHADER + "." + TEXTURE_2D, DEFAULT); 
		return c;
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "length", lengthModel.getNumber());
		c.storeProperty(getClass(), "thickness", thicknessModel.getNumber());
		c.storeProperty(getClass(), "directed", directedChecker.isSelected());
		c.storeProperty(getClass(), "tubes", tubesChecker.isSelected());
	}
	
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		lengthModel.setValue(c.getProperty(getClass(), "normalLength", lengthModel.getNumber()));
		thicknessModel.setValue(c.getProperty(getClass(), "thickness", thicknessModel.getNumber()));
		directedChecker.setSelected(c.getProperty(getClass(), "directed", directedChecker.isSelected()));
		tubesChecker.setSelected(c.getProperty(getClass(), "tubes", tubesChecker.isSelected()));
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		lengthSpinner.addChangeListener(this);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
		updateStates();
	}

	private class VectorFieldTableModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return fields.size();
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Boolean.class;
				case 1: return Adapter.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= fields.size()) {
				return "-";
			}
			Adapter<double[]> a = fields.get(row);
			Object value = null;
			switch (column) {
				case 0: 
					return isActive(a);
				case 1:
					value = a;
					break;
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 0:
					return true;
				default: 
					return false;
			}
		}
		
		
	}
	
	private class VectorFieldActivationListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			int row = fieldTable.getSelectedRow();
			Adapter<double[]> a = fields.get(row);
			setActive(a, !isActive(a));
			fieldTable.revalidate();
		}

	}
	
	
	private void setActive(Adapter<double[]> vec, boolean active) {
		if (active) {
			HalfEdgeDataStructure<?, ?, ?> hds = hif.get();
			AdapterSet aSet = hif.getAdapters();
			double meanEdgeLength = GeometryUtility.getMeanEdgeLength(hds, aSet);
			double lengthScale = meanEdgeLength * lengthModel.getNumber().doubleValue() * 0.7;
			double thicknessScale =  meanEdgeLength * thicknessModel.getNumber().doubleValue() * 0.07;
			boolean arrows = directedChecker.isSelected();
			boolean tubes = tubesChecker.isSelected();
			IndexedLineSet ils = null;
			if (vec.canAccept(hds.getVertexClass())) {
				ils = generateVectorLineSet(hds.getVertices(), vec, aSet, lengthScale, lengthScale);
			}
			if (vec.canAccept(hds.getEdgeClass())) {
				ils = generateVectorLineSet(hds.getEdges(), vec, aSet, lengthScale, lengthScale);
			}
			if (vec.canAccept(hds.getFaceClass())) {
				ils = generateVectorLineSet(hds.getFaces(), vec, aSet, lengthScale, lengthScale);
			}
			SceneGraphComponent c = null;
			if (tubes) {
				c = generateVectorArrows(ils, thicknessScale, arrows);
			} else {
				Appearance vecApp = new Appearance();
				vecApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
				vecApp.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR, 0.88888);
				vecApp.setAttribute(EDGE_DRAW, true);
				vecApp.setAttribute(VERTEX_DRAW, false);
				vecApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
				vecApp.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 1.0);
				vecApp.setAttribute(LINE_SHADER + "." + PICKABLE, false);
				vecApp.setAttribute(DEPTH_FUDGE_FACTOR, 0.9999);
				c = new SceneGraphComponent();
				c.setAppearance(vecApp);
				c.setGeometry(ils);
			}
			c.setName(vec.toString());
			hif.getActiveLayer().addTemporaryGeometry(c);
			activeFields.put(vec, c);
		} else {
			SceneGraphComponent c = activeFields.get(vec);
			activeFields.remove(vec);
			if (c != null) {
				hif.getActiveLayer().removeTemporaryGeometry(c);
			}
		}
		System.gc();
	}

	private boolean isActive(Adapter<double[]> a) {
		return activeFields.containsKey(a);
	}

	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
