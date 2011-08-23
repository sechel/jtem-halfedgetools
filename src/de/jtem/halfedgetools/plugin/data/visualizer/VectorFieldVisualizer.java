package de.jtem.halfedgetools.plugin.data.visualizer;

import static de.jreality.shader.CommonAttributes.DEPTH_FUDGE_FACTOR;
import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.LINE_WIDTH;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.data.Attribute;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.util.GeometryUtility;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class VectorFieldVisualizer extends DataVisualizerPlugin implements
		ActionListener, ChangeListener {

	private SpinnerNumberModel scaleModel = new SpinnerNumberModel(1.0, 0.0,
			10.0, 0.1), thicknessModel = new SpinnerNumberModel(1.0, 0.0, 10.0,
			0.1);
	private JSpinner scaleSpinner = new JSpinner(scaleModel),
			thicknessSpinner = new JSpinner(thicknessModel);
	private JCheckBox directedChecker = new JCheckBox("Directed"),
			tubesChecker = new JCheckBox("Tubes"),
			normalizeChecker = new JCheckBox("Normalize");
	private JPanel optionsPanel = new JPanel();

	private VectorFieldVisualization actVis = null;
	private boolean listenersDisabled = false;
	private Appearance vectorFieldApp = new Appearance(
			"Vector Field Appearance");

	public VectorFieldVisualizer() {
		initOptionPanel();
		initLineAppearance();
	}

	private void initLineAppearance() {
		vectorFieldApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR,
				Color.RED);
		vectorFieldApp.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR,
				0.88888);
		vectorFieldApp.setAttribute(EDGE_DRAW, true);
		vectorFieldApp.setAttribute(VERTEX_DRAW, false);
		vectorFieldApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
		vectorFieldApp.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 1.0);
		vectorFieldApp.setAttribute(LINE_SHADER + "." + PICKABLE, false);
		vectorFieldApp.setAttribute(DEPTH_FUDGE_FACTOR, 0.9999);

		vectorFieldApp.setAttribute(LINE_SHADER + "." + POLYGON_SHADER + "."
				+ SMOOTH_SHADING, true);
		vectorFieldApp.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING,
				true);
	}

	private void initOptionPanel() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();

		optionsPanel.add(new JLabel("Scale"), cl);
		optionsPanel.add(scaleSpinner, cr);
		scaleSpinner.addChangeListener(this);

		optionsPanel.add(normalizeChecker, cl);
		normalizeChecker.setSelected(true);
		normalizeChecker.addActionListener(this);

		optionsPanel.add(tubesChecker, cr);
		tubesChecker.addActionListener(this);

		optionsPanel.add(new JLabel("Thickness"), cl);
		optionsPanel.add(thicknessSpinner, cr);
		thicknessSpinner.addChangeListener(this);

		optionsPanel.add(directedChecker, cl);
		directedChecker.addActionListener(this);

		checkTubesEnabled();
	}

	private void checkTubesEnabled() {
		thicknessSpinner.setEnabled(tubesChecker.isSelected());
		directedChecker.setEnabled(tubesChecker.isSelected());
	}

	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(double[].class);
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("arrow_out.png");
		return info;
	}

	@Override
	public String getName() {
		return "Vector Field";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer,
			NodeType type, Adapter<?> source) {
		VectorFieldVisualization vis = new VectorFieldVisualization(layer,
				source, this, type);
		// copy last values
		vis.scale = scaleModel.getNumber().doubleValue();
		vis.thickness = thicknessModel.getNumber().doubleValue();
		vis.tubesenabled = tubesChecker.isSelected();
		vis.directed = directedChecker.isSelected();
		vis.normalize = normalizeChecker.isSelected();
		layer.addTemporaryGeometry(vis.vectorsComponent);

		return vis;
	}

	@Override
	public void disposeVisualization(DataVisualization vis) {
		VectorFieldVisualization vfVis = (VectorFieldVisualization) vis;
		vis.getLayer().removeTemporaryGeometry(vfVis.vectorsComponent);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateGeometry();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateGeometry();
	}

	private void updateGeometry() {
		checkTubesEnabled();
		if (actVis == null || listenersDisabled)
			return;
		actVis.scale = scaleModel.getNumber().doubleValue();
		actVis.thickness = thicknessModel.getNumber().doubleValue();
		actVis.tubesenabled = tubesChecker.isSelected();
		actVis.directed = directedChecker.isSelected();
		actVis.normalize = normalizeChecker.isSelected();
		actVis.update();
	}

	private class VectorFieldVisualization extends AbstractDataVisualization {

		private SceneGraphComponent vectorsComponent = new SceneGraphComponent(
				"Vectors");

		protected double scale = 1., thickness = 1.;
		protected boolean tubesenabled = false, directed = false,
				normalize = true;

		public VectorFieldVisualization(HalfedgeLayer layer, Adapter<?> source,
				DataVisualizer visualizer, NodeType type) {
			super(layer, source, visualizer, type);
			vectorsComponent.setAppearance(vectorFieldApp);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void update() {

			if (!isActive()) {
				vectorsComponent.setVisible(false);
				return;
			} else {
				vectorsComponent.setVisible(true);
			}

			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			AdapterSet aSet = getLayer().getEffectiveAdapters();

			Adapter<?> genericAdapter = getSource();

			List<? extends Node<?, ?, ?>> nodes = null;
			switch (getType()) {
			case Vertex:
				nodes = hds.getVertices();
				break;
			case Edge:
				nodes = hds.getEdges();
				break;
			default:
				nodes = hds.getFaces();
				break;
			}

			double meanEdgeLength = GeometryUtility
					.getMeanEdgeLength(hds, aSet);

			IndexedLineSet ils = generateVectorLineSet(nodes,
					(Adapter<double[]>) genericAdapter, aSet, meanEdgeLength);

			clearVectorsComponent();

			if (tubesenabled)
				vectorsComponent
						.setGeometry(generateVectorArrows(ils, directed));
			else
				vectorsComponent.setGeometry(ils);

			vectorsComponent.setName(getName());
			vectorsComponent.setVisible(true);

			vectorFieldApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW,
					tubesenabled);

			updateVectorsComponent();
		}

		private void clearVectorsComponent() {
			vectorsComponent.setGeometry(null);
			List<SceneGraphComponent> sgc = vectorsComponent
					.getChildComponents();
			int n = sgc.size();
			for (int i = 0; i < n; i++)
				vectorsComponent.removeChild(sgc.get(0));
		}

		private <V extends Vertex<V, E, F>, E extends Edge<V, E, F>, F extends Face<V, E, F>, N extends Node<V, E, F>> IndexedLineSet generateVectorLineSet(
				Collection<N> nodes, Adapter<double[]> vec, AdapterSet aSet,
				double meanEdgeLength) {
			IndexedLineSetFactory ilf = new IndexedLineSetFactory();
			if (nodes.size() == 0) {
				ilf.update();
				return ilf.getIndexedLineSet();
			}
			List<double[]> vData = new LinkedList<double[]>();
			List<int[]> iData = new LinkedList<int[]>();
			aSet.setParameter("alpha", .5);
			for (N node : nodes) {
				double[] v = vec.get(node, aSet);
				if (v == null) {
					System.err.println("Null value in adapter " + vec
							+ " for node " + node + " found.");
					continue;
				} else if (v.length != 3) {
					throw new RuntimeException(
							"Adapter does not return vectors in 3-space.");
				}
				v = v.clone();
				double[] p = aSet.getD(BaryCenter3d.class, node);
				if (normalize) {
					Rn.normalize(v, v);
					Rn.times(v, meanEdgeLength, v);
				}
				Rn.times(v, scale / 2., v);
				vData.add(Rn.add(null, p, v));
				Rn.times(v, -1, v);
				vData.add(Rn.add(null, p, v));
				iData.add(new int[] { vData.size() - 1, vData.size() - 2 });
			}
			ilf.setVertexCount(vData.size());
			ilf.setEdgeCount(vData.size() / 2);
			ilf.setVertexCoordinates(vData.toArray(new double[][] {}));
			ilf.setEdgeIndices(iData.toArray(new int[][] {}));
			ilf.update();
			return ilf.getIndexedLineSet();
		}

		private IndexedLineSet generateVectorArrows(IndexedLineSet ils,
				boolean arrows) {

			double[][] oldcoords = ils.getVertexAttributes(
					Attribute.COORDINATES).toDoubleArrayArray(null);
			int[][] oldedges = ils.getEdgeAttributes(Attribute.INDICES)
					.toIntArrayArray(null);

			int oldnumedges = oldedges.length;

			int numcoords = 5 * oldnumedges;
			int numedges = 2 * oldnumedges;

			double[][] coords = new double[numcoords][];
			int[][] edges = new int[numedges][];
			double[] radii = new double[numcoords];
			Color[] edgecolors = new Color[numedges];

			double[][] startVertexCoords = new double[numedges][];
			double[][] targetVertexCoords = new double[numedges][];
			double[][] edgevectors = new double[numedges][];

			for (int i = 0; i < oldnumedges; i++) {
				if (oldedges[i].length != 2)
					throw new RuntimeException("cannot be a vector!");
				int startid = oldedges[i][0];
				int targetid = oldedges[i][1];

				startVertexCoords[i] = oldcoords[startid].clone();
				targetVertexCoords[i] = oldcoords[targetid].clone();

				edgevectors[i] = Rn.subtract(null, targetVertexCoords[i],
						startVertexCoords[i]);
			}

			for (int i = 0; i < oldnumedges; i++) {
				coords[i + 0 * oldnumedges] = Rn.subtract(null,
						startVertexCoords[i],
						Rn.setEuclideanNorm(null, 0.001, edgevectors[i]));
				coords[i + 1 * oldnumedges] = startVertexCoords[i].clone();
				coords[i + 2 * oldnumedges] = targetVertexCoords[i].clone();

				radii[i + 0 * oldnumedges] = 0.001;
				radii[i + 1 * oldnumedges] = thickness;
				radii[i + 2 * oldnumedges] = thickness;

				edges[i] = new int[] { i + 0 * oldnumedges,
						i + 1 * oldnumedges, i + 2 * oldnumedges };

				edgecolors[i] = Color.yellow;

				coords[i + 3 * oldnumedges] = Rn.add(null,
						targetVertexCoords[i],
						Rn.setEuclideanNorm(null, 0.001, edgevectors[i]));
				if (arrows) {
					radii[i + 3 * oldnumedges] = 1.5 * thickness;
					coords[i + 4 * oldnumedges] = Rn.add(null,
							targetVertexCoords[i],
							Rn.times(null, .2, edgevectors[i]));
					radii[i + 4 * oldnumedges] = 0.001;

					edges[i + oldnumedges] = new int[] { i + 2 * oldnumedges,
							i + 3 * oldnumedges, i + 4 * oldnumedges };

					edgecolors[i + oldnumedges] = Color.red;
				} else {
					radii[i + 3 * oldnumedges] = 0.001;
					coords[i + 4 * oldnumedges] = Rn.subtract(null,
							targetVertexCoords[i],
							Rn.setEuclideanNorm(null, 0.001, edgevectors[i]));
					radii[i + 4 * oldnumedges] = 0.001;

					edges[i + oldnumedges] = new int[] { i + 4 * oldnumedges,
							i + 2 * oldnumedges, i + 3 * oldnumedges };

					edgecolors[i + oldnumedges] = Color.yellow;
				}

			}

			IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
			ilsf.setVertexCount(numcoords);
			ilsf.setEdgeCount(numedges);

			ilsf.setVertexCoordinates(coords);
			ilsf.setVertexRelativeRadii(radii);
			ilsf.setEdgeIndices(edges);
			ilsf.setEdgeColors(edgecolors);

			ilsf.update();
			return ilsf.getIndexedLineSet();
		}

		private void updateVectorsComponent() {
			HalfedgeLayer layer = getLayer();
			layer.removeTemporaryGeometry(vectorsComponent);
			layer.addTemporaryGeometry(vectorsComponent);
		}

	}

	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (VectorFieldVisualization) visualization;

		listenersDisabled = true;
		scaleModel.setValue(actVis.scale);
		thicknessModel.setValue(actVis.thickness);
		tubesChecker.setSelected(actVis.tubesenabled);
		directedChecker.setSelected(actVis.directed);
		normalizeChecker.setSelected(actVis.normalize);
		listenersDisabled = false;

		return optionsPanel;
	}

}
