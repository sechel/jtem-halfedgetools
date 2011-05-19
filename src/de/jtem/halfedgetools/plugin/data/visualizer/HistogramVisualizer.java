package de.jtem.halfedgetools.plugin.data.visualizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;

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
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.data.color.ColorMap;
import de.jtem.halfedgetools.plugin.data.color.HueColorMap;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class HistogramVisualizer extends DataVisualizerPlugin implements ChangeListener {

	private JPanel
		panel = new JPanel();
	private SpinnerNumberModel
		numBinsModel = new SpinnerNumberModel(500, 1, 100000, 1),
		scaleExpModel = new SpinnerNumberModel(0, -20, 20, 1);
	private JSpinner
		numBinsSpinner = new JSpinner(numBinsModel),
		scaleExpSpinner = new JSpinner(scaleExpModel);
	private HistogrammVisualization
		activeVis = null;
	
	private HistogramDataset
		plotDataSet = new HistogramDataset();
	private NumberAxis 
		domainAxis = new NumberAxis(),
		rangeAxis = new NumberAxis();
	private XYBarRenderer
		barRenderer = new ColoredXYBarRenderer();
	private XYPlot
		plot = new XYPlot(plotDataSet, domainAxis, rangeAxis, barRenderer);
	private JFreeChart
		chart = new JFreeChart(plot);
	private ChartPanel
		chartPanel = new ChartPanel(chart);
		
	public HistogramVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		panel.add(new JLabel("Bins"), cl);
		panel.add(numBinsSpinner, cr);
		panel.add(new JLabel("Exp"), cl);
		panel.add(scaleExpSpinner, cr);
		
		numBinsSpinner.addChangeListener(this);
		scaleExpSpinner.addChangeListener(this);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (activeVis == null) return;
		activeVis.numBins = numBinsModel.getNumber().intValue();
		activeVis.exp = scaleExpModel.getNumber().intValue();
		activeVis.update();
	}
	
	
	private class ColoredXYBarRenderer extends XYBarRenderer {
		
		private static final long serialVersionUID = 1L;
		private ColorMap colorMap = new HueColorMap();
		
		@Override
		public Paint getItemPaint(int row, int column) {
			int maxIndex = plotDataSet.getItemCount(row) - 1;
			float min = plotDataSet.getX(row, 0).floatValue();
			float max = plotDataSet.getX(row, maxIndex).floatValue();
			float x = plotDataSet.getX(row, column).floatValue();
			return colorMap.getColor(x, min, max);
		}
		
	}
	
	private class HistogrammVisualization extends AbstractDataVisualization {

		private int 
			numBins = 200,
			exp = 0;
		
		public HistogrammVisualization(
			HalfedgeLayer layer,
			Adapter<?> source,
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
		}

		@Override
		public void update() {
			System.out
					.println("HistogramVisualizer.HistogrammVisualization.update()");
		}
		
		@Override
		public void remove() {
			System.out
					.println("FaceColorVisualizer.FaceColorVisualization.remove()");
		}
		
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("chart_bar.png");
		return info;
	}
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		activeVis = (HistogrammVisualization)visualization;
		numBinsModel.setValue(activeVis.numBins);
		scaleExpModel.setValue(activeVis.exp);
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
		return new HistogrammVisualization(layer, source, this, type);
	}

}
