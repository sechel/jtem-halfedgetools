package de.jtem.halfedgetools.plugin;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Paint;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.View;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TypedAdapterSet;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class Histogram extends ShrinkPanelPlugin implements HalfedgeListener {

	private HalfedgeInterface
		hif = null;
	private int
		numBins = 500;
	
	private HistogramDataset
		dataSet = new HistogramDataset();
	private NumberAxis 
		domainAxis = new NumberAxis(),
		rangeAxis = new NumberAxis();
	private XYBarRenderer
		barRenderer = new ColoredXYBarRenderer();
	private XYPlot
		plot = new XYPlot(dataSet, domainAxis, rangeAxis, barRenderer);
	private JFreeChart
		chart = new JFreeChart(plot);
	private ChartPanel
		chartPanel = new ChartPanel(chart);
	
	public Histogram() {
		shrinkPanel.setTitle("Histogram");
		chartPanel.setMinimumSize(new Dimension(10, 200));
		setInitialPosition(SHRINKER_TOP);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		shrinkPanel.add(chartPanel, c);
		
		domainAxis.setAutoRange(true);
		rangeAxis.setAutoRange(true);
		barRenderer.setShadowVisible(false);
		barRenderer.setBarPainter(new StandardXYBarPainter());
		chartPanel.zoomOutBoth(2.0, 2.0);
		chartPanel.restoreAutoBounds();
	}

	
	private class ColoredXYBarRenderer extends XYBarRenderer {
		
		private static final long serialVersionUID = 1L;

		@Override
		public Paint getItemPaint(int row, int column) {
			// TODO: use nodes color here
			return super.getItemPaint(row, column);
		}
		
	}
	
	private void updateHistograms(HalfedgeLayer l) {
		HalfEdgeDataStructure<?, ?, ?> hds = l.get();
		AdapterSet aSet = l.getEffectiveAdapters();
		TypedAdapterSet<Number> numSet = aSet.querySet(Number.class);
		dataSet = new HistogramDataset();
		if (numSet.isEmpty()) {
			plot.setDataset(dataSet);
			return;
		}
		for (Adapter<?> a : numSet) {
			String name = a.toString().replace("Adapter", "");
			if (a.canAccept(hds.getVertexClass())) { // create vertex histogram
				double[] data = getData(hds.getVertices(), (Adapter<?>)a, aSet);
				if (data.length > 0) {
					dataSet.addSeries(name, data, numBins);
				}
			}
			if (a.canAccept(hds.getEdgeClass())) { // create edge histogram
				double[] data = getData(hds.getEdges(), (Adapter<?>)a, aSet);
				if (data.length > 0) {
					dataSet.addSeries(name, data, numBins);
				}
			}
			if (a.canAccept(hds.getFaceClass())) { // create face histogram
				double[] data = getData(hds.getFaces(), (Adapter<?>)a, aSet);
				if (data.length > 0) {
					dataSet.addSeries(name, data, numBins);
				}
			}
		}
		plot.setDataset(dataSet);
		chartPanel.restoreAutoBounds();
	}
	
	
	private double[] getData(Collection<?> nodes, Adapter<?> a, AdapterSet aSet) {
		double[] data = new double[nodes.size()];
		int i = 0;
		for (Object n : nodes) {
			data[i++] = ((Number)a.get((Node<?,?,?>)n, aSet)).doubleValue();
		}
		return data;
	}
	
	
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		updateHistograms(active);
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateHistograms(layer);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.name = "Histogram View";
		info.vendorName = "Stefan Sechelmann";
		info.email = "sechel@math.tu-berlin.de";
		try {
			info.documentationURL = new URL("http://www.jfree.org/jfreechart/");
		} catch (MalformedURLException e) {}
		return info;
	}

	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.registerPlugin(new Histogram());
		v.startup();
	}
	
	
}
