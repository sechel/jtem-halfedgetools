package de.jtem.halfedgetools.plugin.data.visualizer;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.PICKABLE;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jtem.halfedgetools.jreality.util.GeometryUtility.createEdges;
import static de.jtem.halfedgetools.jreality.util.GeometryUtility.createOffsetFaces;
import static de.jtem.halfedgetools.jreality.util.GeometryUtility.createVertices;
import static java.lang.Integer.MAX_VALUE;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.ui.LayoutFactory;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
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

public class NodeColorVisualizer extends DataVisualizerPlugin implements ActionListener, ChangeListener {

	private JComboBox
		colorMapCombo = new JComboBox(ColorMap.values());
	private SpinnerNumberModel
		offsetModel = new SpinnerNumberModel(0.005, 0.001, 1.0, 0.001);
	private JSpinner
		offsetSpinner = new JSpinner(offsetModel);
	private JPanel
		optionsPanel = new JPanel();
	private NodeColorVisualization
		actVis = null;
	private boolean
		listenersDisabled = false;
	private HalfedgeInterface
		hif = null;

	public NodeColorVisualizer() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		optionsPanel.add(new JLabel("Colors"), cl);
		optionsPanel.add(colorMapCombo, cr);
		optionsPanel.add(new JLabel("Offset"), cl);
		optionsPanel.add(offsetSpinner, cr);
		
		colorMapCombo.setSelectedItem(ColorMap.RedGreen);
		
		colorMapCombo.addActionListener(this);
		offsetSpinner.addChangeListener(this);
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
		actVis.offset = offsetModel.getNumber().doubleValue();
		actVis.update();
	}
	
	private class NodeColorVisualization extends AbstractDataVisualization {
		
		private ColorMap
			colorMap = ColorMap.RedGreen;
		private SceneGraphComponent
			geomComponent = new SceneGraphComponent("Node Colors");
		private Appearance
			nodeAppearance = new Appearance("Node Colors Appearance");
		private double
			offset = 0.005;
		private double[]
			minmax = {0, 0};
		
		public NodeColorVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source, 
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
			nodeAppearance.setAttribute(VERTEX_DRAW, true);
			nodeAppearance.setAttribute(EDGE_DRAW, true);
			nodeAppearance.setAttribute(FACE_DRAW, true);
			nodeAppearance.setAttribute(PICKABLE, false);
			geomComponent.setAppearance(nodeAppearance);
		}
	
		@Override
		public void update() {
			if (!isActive()) {
				geomComponent.setVisible(false);
				return;
			} else {
				geomComponent.setVisible(true);
			}
			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			AdapterSet a = getLayer().getEffectiveAdapters();
			a.addAll(hif.getActiveVolatileAdapters());
			switch (getType()) {
				case Vertex:
					List<? extends Vertex<?,?,?>> vList = getColoredNodes(hds.getVertices(), a, minmax);
					a.add(new ColorAdapter(minmax[0], minmax[1]));
					PointSet ps = createVertices(vList, a, true);
					ps.setName("Vertex Colors Geometry");
					geomComponent.setGeometry(ps);
					break;
				case Edge:
					List<? extends Edge<?,?,?>> eList = getColoredNodes(hds.getEdges(), a, minmax);
					a.add(new ColorAdapter(minmax[0], minmax[1]));
					IndexedLineSet ils = createEdges(eList, a, true);
					ils.setName("Edge Colors Geometry");
					geomComponent.setGeometry(ils);
					break;
				case Face:
					List<? extends Face<?,?,?>> fList = getColoredNodes(hds.getFaces(), a, minmax);
					a.add(new ColorAdapter(minmax[0], minmax[1]));
					IndexedFaceSet ifs = createOffsetFaces(fList, a, offset, true);
					ifs.setName("Face Colors Geometry");
					geomComponent.setGeometry(ifs);
					break;
			}
			HalfedgeLayer layer = getLayer();
			layer.removeTemporaryGeometry(geomComponent);
			layer.addTemporaryGeometry(geomComponent);
			nodeAppearance.setAttribute(VERTEX_DRAW, getType() == NodeType.Vertex);
			nodeAppearance.setAttribute(EDGE_DRAW, getType() == NodeType.Edge);
			nodeAppearance.setAttribute(FACE_DRAW, getType() == NodeType.Face);
			hif.createSelectionAppearance(nodeAppearance, layer, offset);
		}
		
		
		public <	
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>,
			T extends Node<V, E, F>
		> List<T> getColoredNodes(List<T> nList, AdapterSet a, double[] minmax) {
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			List<T> r = new LinkedList<T>();
			for (T n : nList) {
				Object val = getSource().get(n, a);
				if (val instanceof Number) {
					r.add(n);
					double dval = ((Number)val).doubleValue();
					if (dval > max) max = dval;
					if (dval < min) min = dval;
				}
			}
			minmax[0] = min;
			minmax[1] = max;
			return r;
		}
		
		@Color
		private class ColorAdapter extends AbstractAdapter<double[]> {
			
			private double
				min = 0,
				max = 1;
			private float[]
				colorValues = new float[3];
			
			public ColorAdapter(double min, double max) {
				super(double[].class, true, false);
				this.min = min;
				this.max = max;
			}
			
			@Override
			public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
				return true;
			}
			
			@Override
			public <
				V extends Vertex<V, E, F>,
				E extends Edge<V, E, F>,
				F extends Face<V, E, F>,
				N extends Node<V, E, F>
			> double[] get(N n, AdapterSet a) {
				double[] color = {0,0,0};
				Object val = getSource().get(n, a);
				if (val instanceof Number) {
					Number num = (Number)val;
					double dval = num.doubleValue();
					colorMap.getColor(dval, min, max).getRGBColorComponents(colorValues);
					color[0] = colorValues[0];
					color[1] = colorValues[1];
					color[2] = colorValues[2];
				}
				return color;
			}
			
			@Override
			public double getPriority() {
				return MAX_VALUE;
			}
			
		}
		
	}
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (NodeColorVisualization)visualization;
		listenersDisabled = true;
		colorMapCombo.setSelectedItem(actVis.colorMap);
		offsetModel.setValue(actVis.offset);
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
		info.icon = ImageHook.getIcon("color_swatch.png");
		return info;
	}
	
	@Override
	public String getName() {
		return "Node Colors";
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}

	@Override
	public NodeColorVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		NodeColorVisualization vis = new NodeColorVisualization(layer, source, this, type);
		vis.colorMap = (ColorMap)colorMapCombo.getSelectedItem();
		vis.offset = offsetModel.getNumber().doubleValue();
		return vis;
	}
	
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
		NodeColorVisualization cbVis = (NodeColorVisualization)vis;
		vis.getLayer().removeTemporaryGeometry(cbVis.geomComponent);
	}

}
