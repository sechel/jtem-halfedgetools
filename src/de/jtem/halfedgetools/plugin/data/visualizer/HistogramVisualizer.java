package de.jtem.halfedgetools.plugin.data.visualizer;

import static de.jreality.ui.LayoutFactory.createLeftConstraint;
import static de.jreality.ui.LayoutFactory.createRightConstraint;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.data.color.ColorMap;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class HistogramVisualizer extends DataVisualizerPlugin implements ChangeListener, ActionListener {

	private JPanel
		panel = new JPanel();
	private JComboBox
		colorMapCombo = new JComboBox(ColorMap.values());
	private SpinnerNumberModel
		numBinsModel = new SpinnerNumberModel(500, 1, 100000, 1),
		scaleExpModel = new SpinnerNumberModel(0, -20, 20, 1);
	private JSpinner
		numBinsSpinner = new JSpinner(numBinsModel),
		scaleExpSpinner = new JSpinner(scaleExpModel);
	private Set<HistogrammVisualization>
		activeHistograms = new TreeSet<HistogrammVisualization>();
	private HistogrammVisualization 
		uiVis = null; 
	
	private NumberAxis 
		domainAxis = new NumberAxis(),
		rangeAxis = new NumberAxis();
	private HistogramDataset
		dataSet = new HistogramDataset();
	private XYPlot
		plot = new XYPlot(dataSet, domainAxis, rangeAxis, new XYBarRenderer());
	private JFreeChart
		chart = new JFreeChart(plot);
	private ChartPanel
		chartPanel = new ChartPanel(chart);
	private ColoredXYBarRenderer
		plotRenderer = new ColoredXYBarRenderer();
	private boolean
		listenersDisabled = false;
		
	public HistogramVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cl = createLeftConstraint();
		GridBagConstraints cr = createRightConstraint();
		panel.add(new JLabel("Bins"), cl);
		panel.add(numBinsSpinner, cr);
		panel.add(new JLabel("Exp"), cl);
		panel.add(scaleExpSpinner, cr);
		panel.add(new JLabel("Colors"), cl);
		panel.add(colorMapCombo, cr);
		
		domainAxis.setAutoRange(true);
		rangeAxis.setAutoRange(true);
		chartPanel.zoomOutBoth(2.0, 2.0);
		chartPanel.restoreAutoBounds();
		plotRenderer.setShadowVisible(false);
		plotRenderer.setBarPainter(new StandardXYBarPainter());
		plot.setRenderer(plotRenderer);
		
		numBinsSpinner.addChangeListener(this);
		scaleExpSpinner.addChangeListener(this);
		colorMapCombo.addActionListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (uiVis == null || listenersDisabled) return;
		uiVis.numBins = numBinsModel.getNumber().intValue();
		uiVis.exp = scaleExpModel.getNumber().intValue();
		uiVis.colorMap = (ColorMap)colorMapCombo.getSelectedItem();
		uiVis.update();
		updateHistograms();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		stateChanged(null);
	}
	
	private class ColoredXYBarRenderer extends XYBarRenderer {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public Paint getItemPaint(int row, int column) {
			HistogrammVisualization vis = getHistogrammForRow(row);
			ColorMap colorMap = vis.colorMap;
			if (colorMap == ColorMap.Mono) {
				return super.getItemPaint(row, column);
			}
			int maxIndex = dataSet.getItemCount(row) - 1;
			float min = dataSet.getX(row, 0).floatValue();
			float max = dataSet.getX(row, maxIndex).floatValue();
			float x = dataSet.getX(row, column).floatValue();
			return colorMap.getColor(x, min, max);
		}
		
	}
	
	private void updateHistograms() {
		dataSet = new HistogramDataset();
		for (HistogrammVisualization vis : activeHistograms) {
			vis.addDataSeries(dataSet);
		}
		plot.setDataset(dataSet);
		chartPanel.restoreAutoBounds();
	}
	
	
	private HistogrammVisualization getHistogrammForRow(int row) {
		List<HistogrammVisualization> list = new ArrayList<HistogrammVisualization>(activeHistograms);
		return list.get(row);
	}
	
	
	private class HistogrammVisualization extends AbstractDataVisualization {

		private int 
			numBins = 200,
			exp = 0;
		private ColorMap
			colorMap = ColorMap.Hue;
		private ColoredXYBarRenderer
			renderer = new ColoredXYBarRenderer();
		private double[]
		    dataSeries = null;
		
		public HistogrammVisualization(
			HalfedgeLayer layer,
			Adapter<?> source,
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
			renderer.setShadowVisible(false);
			renderer.setBarPainter(new StandardXYBarPainter());
		}

		@SuppressWarnings("unchecked")
		@Override
		public void update() {
			Adapter<Number> numSource = (Adapter<Number>)getSource();
			dataSeries = createDataSeries(
				getType(), 
				numSource, 
				getLayer().get(), 
				getLayer().getEffectiveAdapters(),
				exp
			);
			updateHistograms();
		}
		
		
		public void addDataSeries(HistogramDataset dataSet) {
			String name = getSource().toString().replace("Adapter", "");
			dataSet.addSeries(name, dataSeries, numBins);
		}
		
	}
	
	
	
	private <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> double[] createDataSeries(
		NodeType type, 
		Adapter<Number> source, 
		HDS hds, 
		AdapterSet aSet,
		int scaleExp
	) {
		switch (type) {
		case Vertex:
			return getData(hds.getVertices(), source, aSet, scaleExp);
		case Edge:
			return getData(hds.getEdges(), source, aSet, scaleExp);
		default:
			return getData(hds.getFaces(), source, aSet, scaleExp);
		}
	}
	
	
		
	private <
		N extends Node<V, E, F>,
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getData(
		Collection<N> nodes, 
		Adapter<Number> source, 
		AdapterSet aSet, 
		int scaleExp
	) {
		double scale = Math.pow(10, scaleExp); 
		double[] data = new double[nodes.size()];
		int i = 0;
		for (N n : nodes) {
			Number val = source.get(n, aSet);
			if (val == null) continue;
			data[i++] = val.doubleValue() * scale;
		}
		return Arrays.copyOf(data, i);
	}
		
		
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("chart_bar.png");
		return info;
	}
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		uiVis = (HistogrammVisualization)visualization;
		listenersDisabled = true;
		numBinsModel.setValue(uiVis.numBins);
		scaleExpModel.setValue(uiVis.exp);
		colorMapCombo.setSelectedItem(uiVis.colorMap);
		listenersDisabled = false;
		return panel;
	}
	@Override
	public JPanel getDataDisplay() {
		return chartPanel;
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(Number.class);
	}
	
	@Override
	public String getName() {
		return "Histogram";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		HistogrammVisualization v = new HistogrammVisualization(layer, source, this, type);
		activeHistograms.add(v);
		return v;
	}
	
	@Override
	public void disposeVisualization(DataVisualization vis) {
		activeHistograms.remove(vis);
		updateHistograms();
	}

}
