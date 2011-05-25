package de.jtem.halfedgetools.plugin.data.visualizer;

import static de.jreality.ui.LayoutFactory.createLeftConstraint;
import static de.jreality.ui.LayoutFactory.createRightConstraint;
import static java.lang.Math.abs;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
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
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleInsets;

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
	private DecimalFormat
		numberFormat = new DecimalFormat("0.000E0");
	
	private NumberAxis 
		domainAxis = new NumberAxis(),
		rangeAxis = new NumberAxis();
	private HistogramDataset
		dataSet = new HistogramDataset();
	private ColoredXYBarRenderer
		plotRenderer = new ColoredXYBarRenderer();
	private XYPlot
		plot = new XYPlot(dataSet, domainAxis, rangeAxis, plotRenderer);
	private JFreeChart
		chart = new JFreeChart(plot);
	private ChartPanel
		chartPanel = new ChartPanel(chart);

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
		domainAxis.setAutoRangeIncludesZero(false);
		domainAxis.setAutoRangeStickyZero(false);
		domainAxis.setNumberFormatOverride(numberFormat);
		rangeAxis.setAutoRange(true);
		
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
		plotRenderer = new ColoredXYBarRenderer();
		plotRenderer.setShadowVisible(false);
		plotRenderer.setBarPainter(new StandardXYBarPainter());
		plot = new XYPlot(dataSet, domainAxis, rangeAxis, plotRenderer);
		for (HistogrammVisualization vis : activeHistograms) {
			vis.addDataSeries(dataSet);
			vis.addMarkers(plot);
		}
		plot.configureDomainAxes();
		plot.configureRangeAxes();
		chart = new JFreeChart(plot);
		chartPanel.setChart(chart);
		chartPanel.restoreAutoBounds();
	}
	
	
	private HistogrammVisualization getHistogrammForRow(int row) {
		List<HistogrammVisualization> list = new ArrayList<HistogrammVisualization>();
		for (HistogrammVisualization vis : activeHistograms) {
			if (vis.isActive()) {
				list.add(vis);
			}
		}
		return list.get(row);
	}
	
	
	private class HistogrammVisualization extends AbstractDataVisualization {

		private int 
			numBins = 100,
			exp = 0;
		private ColorMap
			colorMap = ColorMap.Mono;
		private ColoredXYBarRenderer
			renderer = new ColoredXYBarRenderer();
		private double[]
		    dataSeries = null;
		private DecimalFormat
			formatNormal = new DecimalFormat("0.000"),
			formatExp = new DecimalFormat("0.000E0");
		
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
			if (!isActive()) {
				updateHistograms();
				return;
			}
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
			if (!isActive() || dataSeries.length == 0) return;
			String name = getSource().toString().replace("Adapter", "");
			dataSet.addSeries(name, dataSeries, numBins);
		}
		
		public void addMarkers(XYPlot plot) {
			if (!isActive()) return;
			String name = getSource().toString().replace("Adapter", "");
			double min = Double.MAX_VALUE;
			double max = -Double.MAX_VALUE;
			double mean = 0;
			for (double val : dataSeries) {
				if (val > max) max = val;
				if (val < min) min = val;
				mean += val;
			}
			mean /= dataSeries.length;
			String minStr = formatNormal.format(min);
			String maxStr = formatNormal.format(max);
			String meanStr = formatNormal.format(mean);
			if (abs(min) < 1.0 || 10 < abs(min)) {
				minStr = formatExp.format(min);
			}
			if (abs(max) < 1.0 || 10 < abs(max)) {
				maxStr = formatExp.format(max);
			}
			if (abs(mean) < 1.0 || 10 < abs(mean)) {
				meanStr = formatExp.format(mean);
			}
			ValueMarker minMarker = new ValueMarker(min);
			ValueMarker maxMarker = new ValueMarker(max);
			ValueMarker meanMarker = new ValueMarker(mean);
			minMarker.setLabel("Min " + name);
			minMarker.setLabelAnchor(RectangleAnchor.TOP);
			minMarker.setLabelOffset(new RectangleInsets(50, 0, 0, 50));
			maxMarker.setLabel("Max " + name);
			maxMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
			maxMarker.setLabelOffset(new RectangleInsets(10, 20, 0, 0));
			meanMarker.setLabel("Mean " + name);
			meanMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			meanMarker.setLabelOffset(new RectangleInsets(30, 0, 0, 25));
			plot.addDomainMarker(minMarker, Layer.FOREGROUND);
			plot.addDomainMarker(maxMarker, Layer.FOREGROUND);
			plot.addDomainMarker(meanMarker, Layer.FOREGROUND);
			minMarker = new ValueMarker(min);
			maxMarker = new ValueMarker(max);
			meanMarker = new ValueMarker(mean);
			minMarker.setLabel(minStr);
			minMarker.setLabelAnchor(RectangleAnchor.TOP);
			minMarker.setLabelOffset(new RectangleInsets(60, 0, 0, 50));
			maxMarker.setLabel(maxStr);
			maxMarker.setLabelAnchor(RectangleAnchor.TOP_LEFT);
			maxMarker.setLabelOffset(new RectangleInsets(20, 20, 0, 0));
			meanMarker.setLabel(meanStr);
			meanMarker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
			meanMarker.setLabelOffset(new RectangleInsets(40, 0, 0, 25));
			plot.addDomainMarker(minMarker, Layer.FOREGROUND);
			plot.addDomainMarker(maxMarker, Layer.FOREGROUND);
			plot.addDomainMarker(meanMarker, Layer.FOREGROUND);
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
