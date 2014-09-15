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
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
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

	private SpinnerNumberModel scaleModel = new SpinnerNumberModel(1.0, -100.0,
			100.0, 0.1), thicknessModel = new SpinnerNumberModel(1.0, 0.0, 100.0,
			0.1);
	private JSpinner scaleSpinner = new JSpinner(scaleModel),
			thicknessSpinner = new JSpinner(thicknessModel);
	private JCheckBox directedChecker = new JCheckBox("Directed"),
			tubesChecker = new JCheckBox("Tubes"),
			normalizedChecker = new JCheckBox("Normalized"),
			centeredChecker = new JCheckBox("Centered");
	private JPanel optionsPanel = new JPanel();

	private JComboBox colorChooser;
	private Color[] colors = { Color.RED, Color.GREEN, Color.BLUE, Color.CYAN,
			Color.MAGENTA, Color.YELLOW, Color.ORANGE, Color.PINK, Color.BLACK,
			Color.WHITE };

	private VectorFieldVisualization actVis = null;
	private boolean listenersDisabled = false;

	public VectorFieldVisualizer() {
		initOptionPanel();
	}

	private void initOptionPanel() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();

		optionsPanel.add(new JLabel("Scale"), cl);
		optionsPanel.add(scaleSpinner, cr);
		scaleSpinner.addChangeListener(this);
		
		optionsPanel.add(new JLabel("Thickness"), cl);
		optionsPanel.add(thicknessSpinner, cr);
		thicknessSpinner.addChangeListener(this);

		optionsPanel.add(tubesChecker, cl);
		tubesChecker.addActionListener(this);

		optionsPanel.add(directedChecker, cr);
		directedChecker.addActionListener(this);
		
		optionsPanel.add(normalizedChecker, cl);
		normalizedChecker.setSelected(true);
		normalizedChecker.addActionListener(this);
		
		optionsPanel.add(centeredChecker, cr);
		centeredChecker.setSelected(true);
		centeredChecker.addActionListener(this);

		String[] colornames = { "RED", "GREEN", "BLUE", "CYAN", "MAGENTA",
				"YELLOW", "ORANGE", "PINK", "BLACK", "WHITE" };
		colorChooser = new JComboBox(colornames);
		colorChooser.setSelectedIndex(8);
		cl.gridwidth = 2;
		optionsPanel.add(colorChooser, cl);
		colorChooser.addActionListener(this);

		checkTubesEnabled();
	}

	private void checkTubesEnabled() {
		thicknessSpinner.setEnabled(tubesChecker.isSelected());
		directedChecker.setEnabled(tubesChecker.isSelected());
	}

	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		boolean accept = false;
		accept |= a.checkType(double[][].class);
		accept |= a.checkType(double[].class);
		return accept;
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
		vis.normalized = normalizedChecker.isSelected();
		vis.centered = centeredChecker.isSelected();
		vis.color = colors[colorChooser.getSelectedIndex()];
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
		actVis.normalized = normalizedChecker.isSelected();
		actVis.centered = centeredChecker.isSelected();
		actVis.color = colors[colorChooser.getSelectedIndex()];
		actVis.update();
	}

	public class VectorFieldVisualization extends AbstractDataVisualization {

		private SceneGraphComponent vectorsComponent = new SceneGraphComponent(
				"Vectors");
		private Appearance vectorFieldApp = new Appearance(
				"Vector Field Appearance");

		private double scale = 1., thickness = 1.;
		private boolean tubesenabled = false, directed = false,
				normalized = true, centered = true;
		private Color color = Color.BLACK;

		public VectorFieldVisualization(HalfedgeLayer layer, Adapter<?> source,
				DataVisualizer visualizer, NodeType type) {
			super(layer, source, visualizer, type);
			initLineAppearance();
			vectorsComponent.setAppearance(vectorFieldApp);
		}

		private void initLineAppearance() {
			vectorFieldApp.setAttribute(LINE_SHADER + "." + DEPTH_FUDGE_FACTOR,
					0.88888);
			vectorFieldApp.setAttribute(EDGE_DRAW, true);
			vectorFieldApp.setAttribute(VERTEX_DRAW, false);
			vectorFieldApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW, false);
			vectorFieldApp.setAttribute(LINE_SHADER + "." + LINE_WIDTH, 1.0);
			vectorFieldApp.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, 0.1);
			vectorFieldApp.setAttribute(LINE_SHADER + "." + PICKABLE, false);
			vectorFieldApp.setAttribute(DEPTH_FUDGE_FACTOR, 0.9999);

			vectorFieldApp.setAttribute(LINE_SHADER + "." + POLYGON_SHADER
					+ "." + SMOOTH_SHADING, true);
			vectorFieldApp.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING,
					true);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
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

			List<? extends Node> nodes = null;
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

			IndexedLineSet ils = tubesenabled ? generateTubedVectorArrows(
					nodes, genericAdapter, aSet,
					meanEdgeLength, directed) : generateSimpleVectorLineSet(
					nodes, genericAdapter, aSet,
					meanEdgeLength);

			clearVectorsComponent();

			vectorsComponent.setGeometry(ils);

			vectorsComponent.setName(getName());
			vectorsComponent.setVisible(true);

			vectorFieldApp.setAttribute(LINE_SHADER + "." + TUBES_DRAW,
					tubesenabled);
			vectorFieldApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR,
					color);

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

		
		@SuppressWarnings("unchecked")
		private <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>, 
			N extends Node<V, E, F>
		> IndexedLineSet generateSimpleVectorLineSet(
				Collection<N> nodes, Adapter<?> vec, AdapterSet aSet,
				double meanEdgeLength) {

			IndexedLineSetFactory ilf = new IndexedLineSetFactory();
			if (nodes.size() == 0) {
				ilf.update();
				return ilf.getIndexedLineSet();
			}

			Object val = vec.get(nodes.iterator().next(), aSet);

			List<double[]> vData = new LinkedList<double[]>();
			List<int[]> iData = new LinkedList<int[]>();
			if (val instanceof double[])
				getVectors(nodes, (Adapter<double[]>)vec, aSet, meanEdgeLength, vData, iData);
			if (val instanceof double[][])
				getMultiVectors(nodes, (Adapter<double[][]>)vec, aSet, meanEdgeLength, vData, iData);

			if (vData.size() == 0) {
				return ilf.getIndexedLineSet();
			}
			ilf.setVertexCount(vData.size());
			ilf.setEdgeCount(vData.size() / 2);
			ilf.setVertexCoordinates(vData.toArray(new double[][] {}));
			ilf.setEdgeIndices(iData.toArray(new int[][] {}));
			ilf.update();
			return ilf.getIndexedLineSet();
		}

		@SuppressWarnings("unchecked")
		private <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>, 
			N extends Node<V, E, F>
		> IndexedLineSet generateTubedVectorArrows(
				Collection<N> nodes, Adapter<?> vec, AdapterSet aSet,
				double meanEdgeLength, boolean arrows) {

			IndexedLineSetFactory ilsf = new IndexedLineSetFactory();
			if (nodes.size() == 0) {
				ilsf.update();
				return ilsf.getIndexedLineSet();
			}
			
			Object val = null; 
			for (N node : nodes) {
				val = vec.get(node, aSet);
				if (val != null) break;
			}

			List<double[]> vData = new LinkedList<double[]>();
			List<int[]> iData = new LinkedList<int[]>();
			if (val instanceof double[])
				getVectors(nodes, (Adapter<double[]>)vec, aSet, meanEdgeLength, vData, iData);
			if (val instanceof double[][])
				getMultiVectors(nodes, (Adapter<double[][]>)vec, aSet, meanEdgeLength, vData, iData);

			int numOfVectors = vData.size() / 2;

			int numcoords = arrows ? 6 * numOfVectors : 4 * numOfVectors;
			int numedges = arrows ? 2 * numOfVectors : numOfVectors;

			double[][] coords = new double[numcoords][];
			int[][] edges = new int[numedges][];
			double[] radii = new double[numcoords];
			Color[] edgecolors = new Color[numedges];

			double[] startcoords, targetcoords, vector;
			int[] ids;
			int startid, targetid;

			for (int i = 0; i < numOfVectors; i++) {
				ids = iData.get(i);
				if (ids.length != 2)
					throw new RuntimeException(
							"Is not a single edge (= vector)!");
				startid = ids[0];
				targetid = ids[1];

				startcoords = vData.get(startid);
				targetcoords = vData.get(targetid);

				vector = Rn.subtract(null, targetcoords, startcoords);

				coords[i + 0 * numOfVectors] = Rn.subtract(null, startcoords,
						Rn.setEuclideanNorm(null, 0.001, vector));
				coords[i + 1 * numOfVectors] = startcoords.clone();
				coords[i + 2 * numOfVectors] = targetcoords.clone();

				radii[i + 0 * numOfVectors] = 0.001;
				radii[i + 1 * numOfVectors] = thickness;
				radii[i + 2 * numOfVectors] = thickness;

				edgecolors[i] = color;

				if (arrows) {
					edges[i] = new int[] { i + 0 * numOfVectors,
							i + 1 * numOfVectors, i + 2 * numOfVectors };

					coords[i + 3 * numOfVectors] = Rn.subtract(null,
							targetcoords, Rn.times(null, 0.001, vector));
					radii[i + 3 * numOfVectors] = 0.001;

					coords[i + 4 * numOfVectors] = targetcoords.clone();
					radii[i + 4 * numOfVectors] = 1.5 * thickness;

					coords[i + 5 * numOfVectors] = Rn.add(null, targetcoords,
							Rn.times(null, .2, vector));
					radii[i + 5 * numOfVectors] = 0.001;

					edges[i + numOfVectors] = new int[] { i + 3 * numOfVectors,
							i + 4 * numOfVectors, i + 5 * numOfVectors };

					edgecolors[i + numOfVectors] = Color.black;
				} else {
					coords[i + 3 * numOfVectors] = Rn.add(null, targetcoords,
							Rn.setEuclideanNorm(null, 0.001, vector));

					radii[i + 3 * numOfVectors] = 0.001;

					edges[i] = new int[] { i + 0 * numOfVectors,
							i + 1 * numOfVectors, i + 2 * numOfVectors,
							i + 3 * numOfVectors };
				}

			}

			if (numcoords == 0) {
				return ilsf.getIndexedLineSet();
			}
			ilsf.setVertexCount(numcoords);
			ilsf.setEdgeCount(numedges);

			ilsf.setVertexCoordinates(coords);
			ilsf.setVertexRelativeRadii(radii);
			ilsf.setEdgeIndices(edges);
			ilsf.setEdgeColors(edgecolors);

			ilsf.update();
			return ilsf.getIndexedLineSet();

		}

		private <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>, 
			N extends Node<V, E, F>
		> void getVectors(
				Collection<N> nodes, Adapter<double[]> vec, AdapterSet aSet,
				double meanEdgeLength, List<double[]> vData, List<int[]> iData) {
			aSet.setParameter("alpha", .5);
			int numNullValues = 0;
			for (N node : nodes) {
				double[] v = vec.get(node, aSet);
				if (v == null) {
					numNullValues++;
					continue;
				} else if (v.length == 4) {
					Pn.dehomogenize(v, v);
				} else if (v.length != 3) {
					throw new RuntimeException(
							"Adapter does not return vectors in 3-space or homogeneous 4-space.");
				}
				v = new double[]{v[0], v[1], v[2]};
				double[] p = aSet.getD(BaryCenter3d.class, node);
				if (normalized) {
					Rn.normalize(v, v);
					Rn.times(v, meanEdgeLength, v);
				}
				if(centered){
					Rn.times(v, scale / 2., v);
					vData.add(Rn.add(null, p, v));
					Rn.times(v, -1, v);	
					vData.add(Rn.add(null, p, v));
				}else{
					Rn.times(v, scale / 2., v);
					vData.add(Rn.add(null, p, v));
					vData.add(p);
				}
				iData.add(new int[] { vData.size() - 1, vData.size() - 2 });
			}
			if (numNullValues > 0) {
				System.err.println(numNullValues + " null values in adapter " + vec + " found.");
			}
		}

		private <
			V extends Vertex<V, E, F>, 
			E extends Edge<V, E, F>, 
			F extends Face<V, E, F>, 
			N extends Node<V, E, F>
		> void getMultiVectors(Collection<N> nodes, Adapter<double[][]> vec, 
				AdapterSet aSet, double meanEdgeLength, List<double[]> vData, 
				List<int[]> iData) {
			aSet.setParameter("alpha", .5);
			int numNullValues = 0;
			for (N node : nodes) {
				double[][] v = vec.get(node, aSet);
				if (v == null) {
					numNullValues++;
					continue;
				} else {
					for (int i = 0; i < v.length; i++) {
						if (v[i].length != 3)
							throw new RuntimeException(
									"Adapter does not return vectors in 3-space.");
						v[i] = v[i].clone();
						double[] p = aSet.getD(BaryCenter3d.class, node);
						if (normalized) {
							Rn.normalize(v[i], v[i]);
							Rn.times(v[i], meanEdgeLength, v[i]);
						}
						if (centered) {
							Rn.times(v[i], scale / 2., v[i]);
							vData.add(Rn.add(null, p, v[i]));
							Rn.times(v[i], -1, v[i]);
							vData.add(Rn.add(null, p, v[i]));
						} else {
							Rn.times(v[i], scale / 2., v[i]);
							vData.add(Rn.add(null, p, v[i]));
							vData.add(p);
						}
						iData.add(new int[] { vData.size() - 1,
								vData.size() - 2 });
					}
				}
			}
			if (numNullValues > 0) {
				System.err.println(numNullValues + " null values in adapter " + vec + " found.");
			}
		}

		private void updateVectorsComponent() {
			HalfedgeLayer layer = getLayer();
			layer.removeTemporaryGeometry(vectorsComponent);
			layer.addTemporaryGeometry(vectorsComponent);
		}

		public double getScale() {
			return scale;
		}

		public void setScale(double scale) {
			this.scale = scale;
		}

		public double getThickness() {
			return thickness;
		}

		public void setThickness(double thickness) {
			this.thickness = thickness;
		}

		public boolean isTubesenabled() {
			return tubesenabled;
		}

		public void setTubesenabled(boolean tubesenabled) {
			this.tubesenabled = tubesenabled;
		}

		public boolean isDirected() {
			return directed;
		}

		public void setDirected(boolean directed) {
			this.directed = directed;
		}

		public boolean isNormalized() {
			return normalized;
		}

		public void setNormalized(boolean normalize) {
			this.normalized = normalize;
		}
		
		public boolean isCentered() {
			return centered;
		}

		public void setCentered(boolean centered) {
			this.centered = centered;
		}

		public Color getColor() {
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

	}

	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (VectorFieldVisualization) visualization;

		listenersDisabled = true;
		scaleModel.setValue(actVis.scale);
		tubesChecker.setSelected(actVis.tubesenabled);
		directedChecker.setSelected(actVis.directed);
		directedChecker.setEnabled(actVis.tubesenabled);
		thicknessSpinner.setEnabled(actVis.tubesenabled);
		thicknessModel.setValue(actVis.thickness);
		normalizedChecker.setSelected(actVis.normalized);
		listenersDisabled = false;

		return optionsPanel;
	}

}
