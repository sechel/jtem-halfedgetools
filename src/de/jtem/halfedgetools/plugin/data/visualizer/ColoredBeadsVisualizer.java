package de.jtem.halfedgetools.plugin.data.visualizer;

import static de.jreality.scene.data.Attribute.POINT_SIZE;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

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
import de.jtem.halfedgetools.adapter.type.Length;
import de.jtem.halfedgetools.adapter.type.generic.BaryCenter3d;
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
		rangeModel = new SpinnerNumberModel(1.0, 0.001, 1.0, 0.001),
		scaleModel = new SpinnerNumberModel(1.0, 0.1, 1000.0, 0.1);
	private JSpinner
		rangeSpinner = new JSpinner(rangeModel),
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
		optionsPanel.add(new JLabel("Ramge"), cl);
		optionsPanel.add(rangeSpinner, cr);
		optionsPanel.add(new JLabel("Colors"), cl);
		optionsPanel.add(colorMapCombo, cr);

		scaleSpinner.addChangeListener(this);
		rangeSpinner.addChangeListener(this);
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
		actVis.update();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (actVis == null || listenersDisabled) return;
		actVis.scale = scaleModel.getNumber().doubleValue();
		actVis.range = rangeModel.getNumber().doubleValue();
		actVis.update();
	}
	
	private class ColoredBeadsVisualization extends AbstractDataVisualization {
		
		private double
			range = 1.0,
			scale = 1.0;
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

		@SuppressWarnings("unchecked")
		@Override
		public void update() {
			if (!isActive()) {
				beadsComponent.setVisible(false);
				return;
			} else {
				beadsComponent.setVisible(true);
			}
			Adapter<Number> numAdapter = (Adapter<Number>)getSource();
			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			AdapterSet aSet = getLayer().getEffectiveAdapters();
			List<? extends Node<?,?,?>> nodes = null;
			switch (getType()) {
			case Vertex:
				nodes = hds.getVertices();
				break;
			case Edge:
				nodes = hds.getEdges();
				break;
			case Face:
				nodes = hds.getFaces();
				break;
			}
			double meanEdgeLength = 0;
			for (Edge<?,?,?> e : hds.getPositiveEdges()) {
				meanEdgeLength += aSet.get(Length.class, e, Double.class);
			}
			meanEdgeLength /= hds.numEdges() / 2;
			double[][] vertexData = new double[nodes.size()][];
			int i = 0;
			double max = -Double.MAX_VALUE;
			double min = Double.MAX_VALUE;
			double mean = 0;
			for (Node<?,?,?> n : nodes) {
				vertexData[i++] = aSet.getD(BaryCenter3d.class, n);
				double v = numAdapter.get(n, aSet).doubleValue();
				if (v > max) max = v;
				if (v < min) min = v;
				mean += v;
			}
			mean /= nodes.size();
			Color[] colorData = new Color[nodes.size()];
			double[] sizeData = new double[nodes.size()];
			i = 0;
			for (Node<?,?,?> n : nodes) {
				double v = numAdapter.get(n, aSet).doubleValue();
				colorData[i] = colorMap.getColor(v, min, max);
				sizeData[i++] = mapScale(v, scale, range, min, max, mean, meanEdgeLength);
			}
			psf.setVertexCount(nodes.size());
			psf.setVertexCoordinates(vertexData);
			psf.setVertexColors(colorData);
			psf.setVertexRelativeRadii(sizeData);
			psf.setVertexAttribute(POINT_SIZE, sizeData);
			psf.update();
			beadsComponent.setGeometry(psf.getPointSet());
			beadsComponent.setName(getName());
		}
		
	}
	
	
	private double mapScale(double val, double scale, double range, double min, double max, double mean, double resultMean) {
		double dist = max - min;
		double result = resultMean * scale * (val - min) / dist;
		double rangeResult = result * range;
		return rangeResult + (result - rangeResult);
	}
	
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (ColoredBeadsVisualization)visualization;
		listenersDisabled = true;
		colorMapCombo.setSelectedItem(actVis.colorMap);
		scaleModel.setValue(actVis.scale);
		rangeModel.setValue(actVis.range);
		listenersDisabled = false;
		return optionsPanel;
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(Number.class);
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
		layer.addTemporaryGeometry(vis.beadsComponent);
		return vis;
	}
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
		ColoredBeadsVisualization cbVis = (ColoredBeadsVisualization)vis;
		vis.getLayer().removeTemporaryGeometry(cbVis.beadsComponent);
	}
}
