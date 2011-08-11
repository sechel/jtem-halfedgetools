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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.data.color.ColorMap;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class ColoredBeadsVisualizer extends DataVisualizerPlugin implements ActionListener, ChangeListener {
	
	private JComboBox
		colorMapCombo = new JComboBox(ColorMap.values());
	private SpinnerNumberModel
		spanModel = new SpinnerNumberModel(1.0, 0.0, 100.0, 0.1),
		scaleModel = new SpinnerNumberModel(1.0, 0.1, 100.0, 0.1);
	private JCheckBox
		invertChecker = new JCheckBox("Invert Scale");
	private JSpinner
		spanSpinner = new JSpinner(spanModel),
		scaleSpinner = new JSpinner(scaleModel);
	private JPanel
		optionsPanel = new JPanel();
	private ColoredBeadsVisualization
		actVis = null;
	private boolean
		listenersDisabled = false;
	private Appearance
		appBeads = new Appearance("Beads Appearance");
	
	public ColoredBeadsVisualizer() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		optionsPanel.add(new JLabel("Scale"), cl);
		optionsPanel.add(scaleSpinner, cr);
		optionsPanel.add(new JLabel("Span"), cl);
		optionsPanel.add(spanSpinner, cr);
		optionsPanel.add(invertChecker, cr);
		optionsPanel.add(new JLabel("Colors"), cl);
		optionsPanel.add(colorMapCombo, cr);

		scaleSpinner.addChangeListener(this);
		spanSpinner.addChangeListener(this);
		invertChecker.addActionListener(this);
		colorMapCombo.addActionListener(this);
		
		appBeads.setAttribute(VERTEX_DRAW, true);
		appBeads.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, true);
		appBeads.setAttribute(POINT_SHADER + "." + POLYGON_SHADER + "." + SMOOTH_SHADING, true);
		appBeads.setAttribute(POINT_SHADER + "." + POINT_RADIUS, 1.0);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (actVis == null || listenersDisabled) return;
		actVis.colorMap = (ColorMap)colorMapCombo.getSelectedItem();
		actVis.invert = invertChecker.isSelected();
		actVis.update();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (actVis == null || listenersDisabled) return;
		actVis.scale = scaleModel.getNumber().doubleValue();
		actVis.span = spanModel.getNumber().doubleValue();
		actVis.update();
	}
	
	private class ColoredBeadsVisualization extends AbstractDataVisualization {
		
		private double
			span = 1.0,
			scale = 1.0;
		private boolean
			invert = false;
		private ColorMap
			colorMap = ColorMap.Hue;
		private SceneGraphComponent
			beadsComponent = new SceneGraphComponent("Beads");
		private PointSetFactory 
			psf = new PointSetFactory();
		
		public ColoredBeadsVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source, 
			DataVisualizer visualizer, 
			NodeType type
		) {
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
			Adapter<?> genericAdapter = getSource();
			
			List<? extends Node<?,?,?>> nodes = null;
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
			for (Edge<?,?,?> e : hds.getPositiveEdges()) {
				meanEdgeLength += aSet.get(Length.class, e, Double.class);
			}
			meanEdgeLength /= hds.numEdges() / 2;
			List<double[]> vertexData = new LinkedList<double[]>();
			int i = 0;
			double max = -Double.MAX_VALUE;
			double min = Double.MAX_VALUE;
			double mean = 0;
			for (Node<?,?,?> n : nodes) {
				Object val = genericAdapter.get(n, aSet);
				if (val == null) {
					System.err.println("Null value in adapter " + genericAdapter + " for node " + n + " found.");
					continue;
				}
				double[] numbers = convertValue(val);
				for (int j = 0; j < numbers.length; j++) {
					aSet.setParameter("beadsPerNode", numbers.length);
					aSet.setParameter("beadIndex", j);
					vertexData.add(aSet.getD(BeadPosition.class, n));
					double num = numbers[j];
					if (num > max) max = num;
					if (num < min) min = num;
					mean += num;
				}
			}
			mean /= nodes.size();
			
			double[][] vertexDataArr = vertexData.toArray(new double[0][0]);
			Color[] colorData = new Color[vertexDataArr.length];
			double[] sizeData = new double[vertexDataArr.length];
			i = 0;
			for (Node<?,?,?> n : nodes) {
				Object val = genericAdapter.get(n, aSet);
				double[] numbers = convertValue(val);
				for (int j = 0; j < numbers.length; j++) {
					double v = numbers[j];
					colorData[i] = colorMap.getColor(v, min, max);
					sizeData[i++] = mapScale(v, scale, span, invert, min, max, mean, meanEdgeLength / 4);
				}
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
		
		
		private double[] convertValue(Object val) {
			double[] numbers = new double[0];
			if (val instanceof Number) {
				Number num = (Number)val;
				numbers = new double[] {num.doubleValue()};
			} else if (val instanceof double[]) {
				numbers = (double[])val;
			} else if (val instanceof float[]) {
				float[] fVal = (float[])val;
				numbers = new double[fVal.length];
				System.arraycopy(fVal, 0, numbers, 0, fVal.length);
			} else if (val instanceof long[]) {
				long[] lVal = (long[])val;
				numbers = new double[lVal.length];
				System.arraycopy(lVal, 0, numbers, 0, lVal.length);
			} else if (val instanceof int[]) {
				int[] iVal = (int[])val;
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
		
	}
	
	
	private double mapScale(double val, double scale, double span, boolean invert, double min, double max, double mean, double resultMean) {
		double dist = max - min;
		double offset = (1 - span) * resultMean * scale;
		double nomaleVal = (val - min) / dist;
		double result = offset + span * resultMean * scale * (invert ? 1 - nomaleVal : nomaleVal);
		return max(result, 0);
	}
	
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (ColoredBeadsVisualization)visualization;
		listenersDisabled = true;
		colorMapCombo.setSelectedItem(actVis.colorMap);
		scaleModel.setValue(actVis.scale);
		spanModel.setValue(actVis.span);
		invertChecker.setSelected(actVis.invert);
		listenersDisabled = false;
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
	

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		ColoredBeadsVisualization vis = new ColoredBeadsVisualization(layer, source, this, type);
		// copy last values
		vis.colorMap = (ColorMap)colorMapCombo.getSelectedItem();
		vis.scale = scaleModel.getNumber().doubleValue();
		vis.span = spanModel.getNumber().doubleValue();
		vis.invert = invertChecker.isSelected();
		layer.addTemporaryGeometry(vis.beadsComponent);
		return vis;
	}
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
		ColoredBeadsVisualization cbVis = (ColoredBeadsVisualization)vis;
		vis.getLayer().removeTemporaryGeometry(cbVis.beadsComponent);
	}
}