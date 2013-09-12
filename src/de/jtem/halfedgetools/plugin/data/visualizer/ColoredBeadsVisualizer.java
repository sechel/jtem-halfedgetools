package de.jtem.halfedgetools.plugin.data.visualizer;

import static de.jreality.scene.data.Attribute.POINT_SIZE;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static java.lang.Math.max;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.PointSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.BeadPosition;
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.data.color.ColorMap;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class ColoredBeadsVisualizer extends DataVisualizerPlugin implements
		ActionListener, ChangeListener {

	private JComboBox beadsPosCombo = new JComboBox(),
			colorMapCombo = new JComboBox(ColorMap.values());
	private MyClampModel clampLowModel = new MyClampModel(),
			clampHighModel = new MyClampModel();
	private SpinnerNumberModel spanModel = new SpinnerNumberModel(1.0, 0.0,
			100.0, 0.1), scaleModel = new SpinnerNumberModel(1.0, 0.1, 100.0,
			0.1);
	private JCheckBox clampChecker = new JCheckBox("C"),
			invertChecker = new JCheckBox("Invert Scale"),
			absoluteChecker = new JCheckBox("Absolute Size");
	private JSpinner clampLowSpinner = new JSpinner(clampLowModel),
			clampHighSpinner = new JSpinner(clampHighModel),
			spanSpinner = new JSpinner(spanModel), scaleSpinner = new JSpinner(
					scaleModel);
	private JPanel optionsPanel = new JPanel();
	private ColoredBeadsVisualization actVis = null;
	private boolean listenersDisabled = false;
	private Appearance appBeads = new Appearance("Beads Appearance");
	private SimpleBeadsPositionAdapter simpleBeadsPositionAdapter = new SimpleBeadsPositionAdapter();
	private HalfedgeInterface hif = null;

	private class MyClampModel extends SpinnerNumberModel {

		private static final long serialVersionUID = 1L;
		private Double stepsize = 1E-1;

		public MyClampModel() {
			super(0.0, null, null, 1.0);
		}

		@Override
		public Object getNextValue() {
			return getNumber().doubleValue() + stepsize;
		}

		@Override
		public Object getPreviousValue() {
			return getNumber().doubleValue() - stepsize;
		}

		@Override
		public void setStepSize(Number stepSize) {
			this.stepsize = stepSize.doubleValue();
		}

	}

	public ColoredBeadsVisualizer() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.gridwidth = 1;
		c.insets = new Insets(1, 1, 1, 1);
		c.fill = GridBagConstraints.BOTH;
		optionsPanel.add(new JLabel("Scale/Span"), c);
		optionsPanel.add(scaleSpinner, c);
		optionsPanel.add(spanSpinner, cr);
		optionsPanel.add(clampChecker, c);
		optionsPanel.add(clampLowSpinner, c);
		optionsPanel.add(clampHighSpinner, cr);
		optionsPanel.add(invertChecker, cl);
		optionsPanel.add(absoluteChecker, cr);
		optionsPanel.add(new JLabel("Colors"), cl);
		optionsPanel.add(colorMapCombo, cr);
		optionsPanel.add(new JLabel("Position"), cl);
		optionsPanel.add(beadsPosCombo, cr);

		JComponent highEditor = new JSpinner.NumberEditor(clampHighSpinner,
				"0.00E0");
		clampHighSpinner.setEditor(highEditor);
		JComponent lowEditor = new JSpinner.NumberEditor(clampLowSpinner,
				"0.00E0");
		clampLowSpinner.setEditor(lowEditor);

		scaleSpinner.addChangeListener(this);
		spanSpinner.addChangeListener(this);
		clampHighSpinner.addChangeListener(this);
		clampLowSpinner.addChangeListener(this);
		invertChecker.addActionListener(this);
		absoluteChecker.addActionListener(this);
		colorMapCombo.addActionListener(this);
		beadsPosCombo.addActionListener(this);
		clampChecker.addActionListener(this);

		colorMapCombo.setSelectedItem(ColorMap.Hue);

		appBeads.setAttribute(VERTEX_DRAW, true);
		appBeads.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, true);
		appBeads.setAttribute(POINT_SHADER + "." + POLYGON_SHADER + "."
				+ SMOOTH_SHADING, true);
		appBeads.setAttribute(POINT_SHADER + "." + POINT_RADIUS, 1.0);
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent e) {
		if (actVis == null || listenersDisabled)
			return;
		actVis.colorMap = (ColorMap) colorMapCombo.getSelectedItem();
		actVis.invert = invertChecker.isSelected();
		actVis.absolute = absoluteChecker.isSelected();
		actVis.beadPosAdapter = (Adapter<double[]>) beadsPosCombo
				.getSelectedItem();
		actVis.clamp = clampChecker.isSelected();
		actVis.update();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (actVis == null || listenersDisabled)
			return;
		actVis.scale = scaleModel.getNumber().doubleValue();
		actVis.span = spanModel.getNumber().doubleValue();
		actVis.clampHigh = clampHighModel.getNumber().doubleValue();
		actVis.clampLow = clampLowModel.getNumber().doubleValue();
		actVis.update();
	}

	public class ColoredBeadsVisualization extends AbstractDataVisualization {

		private double clampLow = 0.0, clampHigh = 0.0, span = 1.0,
				scale = 1.0;
		private boolean clamp = false, clampInited = false, invert = false,
				absolute = false;
		private ColorMap colorMap = ColorMap.Hue;
		private SceneGraphComponent beadsComponent = new SceneGraphComponent(
				"Beads");
		private PointSetFactory psf = new PointSetFactory();
		private Adapter<double[]> beadPosAdapter = null;
		private boolean cutSmallValues = false;
		private double smallestAbsolutValue = 1E-10;
		private double minValue = 0.0, maxValue = 0.0;

		public ColoredBeadsVisualization(HalfedgeLayer layer,
				Adapter<?> source, DataVisualizer visualizer, NodeType type) {
			super(layer, source, visualizer, type);
			beadsComponent.setAppearance(appBeads);
		}

		@Override
		public void update() {
			if (!isActive()) {
				beadsComponent.setVisible(false);
				return;
			} else {
				beadsComponent.setVisible(true);
			}

			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			AdapterSet aSet = getLayer().getEffectiveAdapters();
			aSet.addAll(hif.getActiveVolatileAdapters());
			aSet.add(simpleBeadsPositionAdapter);
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
			double meanEdgeLength = 0;
			for (Edge<?, ?, ?> e : hds.getPositiveEdges()) {
				meanEdgeLength += aSet.get(Length.class, e, Double.class);
			}
			meanEdgeLength /= hds.numEdges() / 2;
			List<double[]> vertexData = new LinkedList<double[]>();
			int i = 0;
			maxValue = -Double.MAX_VALUE;
			minValue = Double.MAX_VALUE;
			double mean = 0;
			aSet.setParameter("beadScale", scale);
			int foundNullValues = 0;
			for (Node<?, ?, ?> n : nodes) {
				Object val = genericAdapter.get(n, aSet);
				if (val == null) {
					foundNullValues++;
					continue;
				}
				double[] numbers = convertValue(val);
				for (int j = 0; j < numbers.length; j++) {
					aSet.setParameter("beadsPerNode", numbers.length);
					aSet.setParameter("beadIndex", j);
					// TODO: find the right beadScale
					aSet.setParameter("beadScale", meanEdgeLength);
					vertexData.add(beadPosAdapter.get(n, aSet));
					double num = numbers[j];
					if (num > maxValue)
						maxValue = num;
					if (num < minValue)
						minValue = num;
					mean += num;
				}
			}
			if (!clampInited) {
				clampHigh = maxValue;
				clampLow = minValue;
				clampInited = true;
			}
			mean /= nodes.size();
			if (foundNullValues > 0) {
				System.err.println("Null value in adapter " + genericAdapter
						+ " for " + foundNullValues + " nodes found.");
			}

			double[][] vertexDataArr = vertexData.toArray(new double[0][0]);
			Color[] colorData = new Color[vertexDataArr.length];
			double[] sizeData = new double[vertexDataArr.length];
			i = 0;
			for (Node<?, ?, ?> n : nodes) {
				Object val = genericAdapter.get(n, aSet);
				double[] numbers = convertValue(val);
				for (int j = 0; j < numbers.length; j++) {
					double v = numbers[j];
					if (Double.isNaN(v) || Double.isInfinite(v)) {
						colorData[i] = Color.BLACK;
						sizeData[i++] = 0.0;
						continue;
					}
					if (clamp) {
						v = ColorMap.clamp(v, clampLow, clampHigh);
						colorData[i] = colorMap
								.getColor(v, clampLow, clampHigh);
					} else {
						colorData[i] = colorMap.getColor(v, minValue, maxValue);
					}
					sizeData[i++] = mapScale(v, scale, span, invert, absolute,
							minValue, maxValue, mean, meanEdgeLength / 4);
				}
			}
			if (vertexDataArr.length == 0) {
				beadsComponent.setGeometry(null);
				beadsComponent.setName(getName());
				updateBeadsComponent();
				return;
			}
			psf.setVertexCount(vertexDataArr.length);
			psf.setVertexCoordinates(vertexDataArr);
			psf.setVertexColors(colorData);
			psf.setVertexRelativeRadii(sizeData);
			psf.setVertexAttribute(POINT_SIZE, sizeData);
			psf.update();
			beadsComponent.setGeometry(psf.getPointSet());
			beadsComponent.setName(getName());
			updateBeadsComponent();
		}

		private double mapScale(double val, double scale, double span,
				boolean invert, boolean absolute, double min, double max,
				double mean, double resultMean) {
			if (Math.abs(val) < smallestAbsolutValue)
				return 0;
			double dist = max - min;
			double offset = (1 - span) * resultMean * scale;
			double nomaleVal = ((absolute ? Math.abs(val) : val) - min) / dist;
			double result = offset + span * resultMean * scale
					* (invert ? 1 - nomaleVal : nomaleVal);
			return max(result, 0);
		}

		private double[] convertValue(Object val) {
			double[] numbers = new double[0];
			if (val instanceof Number) {
				Number num = (Number) val;
				numbers = new double[] { num.doubleValue() };
			} else if (val instanceof double[]) {
				numbers = (double[]) val;
			} else if (val instanceof float[]) {
				float[] fVal = (float[]) val;
				numbers = new double[fVal.length];
				System.arraycopy(fVal, 0, numbers, 0, fVal.length);
			} else if (val instanceof long[]) {
				long[] lVal = (long[]) val;
				numbers = new double[lVal.length];
				System.arraycopy(lVal, 0, numbers, 0, lVal.length);
			} else if (val instanceof int[]) {
				int[] iVal = (int[]) val;
				numbers = new double[iVal.length];
				System.arraycopy(iVal, 0, numbers, 0, iVal.length);
			}
			return numbers;
		}

		private void updateBeadsComponent() {
			HalfedgeLayer layer = getLayer();
			layer.removeTemporaryGeometry(beadsComponent);
			layer.addTemporaryGeometry(beadsComponent);
		}

		public double getSpan() {
			return span;
		}

		public void setSpan(double span) {
			this.span = span;
		}

		public double getScale() {
			return scale;
		}

		public void setScale(double scale) {
			this.scale = scale;
		}

		public boolean isInvert() {
			return invert;
		}

		public void setInvert(boolean invert) {
			this.invert = invert;
		}

		public ColorMap getColorMap() {
			return colorMap;
		}

		public void setColorMap(ColorMap colorMap) {
			this.colorMap = colorMap;
		}

		public boolean isCutSmallValues() {
			return cutSmallValues;
		}

		public void setCutSmallValues(boolean dontShowZeros) {
			this.cutSmallValues = dontShowZeros;
		}

		public double getSmallestAbsolutValue() {
			return smallestAbsolutValue;
		}

		public void setSmallestAbsolutValue(double smallestValue) {
			this.smallestAbsolutValue = smallestValue;
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (ColoredBeadsVisualization) visualization;
		listenersDisabled = true;
		colorMapCombo.setSelectedItem(actVis.colorMap);
		scaleModel.setValue(actVis.scale);
		spanModel.setValue(actVis.span);
		invertChecker.setSelected(actVis.invert);
		absoluteChecker.setSelected(actVis.absolute);
		clampChecker.setSelected(actVis.clamp);
		if (actVis.clampHigh < actVis.minValue) {
			actVis.clampHigh = actVis.minValue;
		}
		if (actVis.clampLow < actVis.minValue) {
			actVis.clampLow = actVis.minValue;
		}
		if (actVis.clampHigh > actVis.maxValue) {
			actVis.clampHigh = actVis.maxValue;
		}
		if (actVis.clampLow > actVis.maxValue) {
			actVis.clampLow = actVis.maxValue;
		}
		double stepSize = Math.abs(actVis.maxValue - actVis.minValue) / 100.0;
		clampHighModel.setStepSize(stepSize);
		clampLowModel.setStepSize(stepSize);
		clampHighModel.setValue(actVis.clampHigh);
		clampLowModel.setValue(actVis.clampLow);
		listenersDisabled = false;

		AdapterSet aSet = visualization.getLayer().getEffectiveAdapters();
		aSet.add(simpleBeadsPositionAdapter);
		List beadPosList = aSet.queryAll(BeadPosition.class);
		Collections.sort(beadPosList);
		Vector<Adapter<?>> beadPosVec = new Vector<Adapter<?>>(beadPosList);
		ComboBoxModel beadPosModel = new DefaultComboBoxModel(beadPosVec);
		beadsPosCombo.setModel(beadPosModel);
		beadsPosCombo.setSelectedItem(actVis.beadPosAdapter);
		return optionsPanel;
	}

	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		boolean accept = false;
		accept |= a.checkType(Number.class);
		accept |= a.checkType(double[].class);
		accept |= a.checkType(float[].class);
		accept |= a.checkType(long[].class);
		accept |= a.checkType(int[].class);
		return accept;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("bullets.png");
		return info;
	}

	@Override
	public String getName() {
		return "Colored Beads";
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer,
			NodeType type, Adapter<?> source) {
		ColoredBeadsVisualization vis = new ColoredBeadsVisualization(layer,
				source, this, type);
		// copy last values
		vis.colorMap = (ColorMap) colorMapCombo.getSelectedItem();
		vis.scale = scaleModel.getNumber().doubleValue();
		vis.span = spanModel.getNumber().doubleValue();
		vis.invert = invertChecker.isSelected();
		vis.absolute = absoluteChecker.isSelected();
		layer.addTemporaryGeometry(vis.beadsComponent);

		AdapterSet aSet = layer.getEffectiveAdapters();
		aSet.add(simpleBeadsPositionAdapter);
		List beadPosList = aSet.queryAll(BeadPosition.class);
		Collections.sort(beadPosList);
		vis.beadPosAdapter = (Adapter<double[]>) beadPosList.get(0);

		return vis;
	}

	@Override
	public void disposeVisualization(DataVisualization vis) {
		ColoredBeadsVisualization cbVis = (ColoredBeadsVisualization) vis;
		vis.getLayer().removeTemporaryGeometry(cbVis.beadsComponent);
	}
}
