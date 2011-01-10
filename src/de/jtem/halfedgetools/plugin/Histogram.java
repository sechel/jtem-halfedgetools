package de.jtem.halfedgetools.plugin;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

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

public class Histogram extends ShrinkPanelPlugin implements HalfedgeListener, ChangeListener {

	private HalfedgeInterface
		hif = null;
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
	private JTable
		adapterTable = new JTable();
	private JScrollPane
		adapterScrollPane = new JScrollPane(adapterTable);
	private SpinnerNumberModel
		numBinsModel = new SpinnerNumberModel(500, 1, 100000, 1),
		scaleExpModel = new SpinnerNumberModel(0, -20, 20, 1);
	private JSpinner
		numBinsSpinner = new JSpinner(numBinsModel),
		scaleExpSpinner = new JSpinner(scaleExpModel);
	private JPanel
		selectionPanel = new JPanel();
	
	private List<Adapter<Number>>
		availableSet = new ArrayList<Adapter<Number>>(),
		activeSet = new ArrayList<Adapter<Number>>();
	
	public Histogram() {
		shrinkPanel.setTitle("Histogram");
		selectionPanel.setLayout(new GridBagLayout());
		chartPanel.setMinimumSize(new Dimension(300, 250));
		
		
		setInitialPosition(SHRINKER_TOP);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 1, 0, 1);
		c.weightx = 1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		selectionPanel.add(new JLabel("Bins"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		selectionPanel.add(numBinsSpinner, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		selectionPanel.add(new JLabel("Exp"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		selectionPanel.add(scaleExpSpinner, c);
		c.weighty = 1;
		selectionPanel.add(adapterScrollPane, c);
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0;
		shrinkPanel.add(selectionPanel, c);
		c.weightx = 1;
		shrinkPanel.add(chartPanel, c);
		
		adapterScrollPane.setMinimumSize(new Dimension(150, 250));
		adapterTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		TableCellEditor boolEditor = adapterTable.getDefaultEditor(Boolean.class);
		boolEditor.addCellEditorListener(new DataActivationListener());	
		adapterTable.setRowHeight(22);
		adapterTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		adapterTable.setBorder(BorderFactory.createEtchedBorder());
		
		domainAxis.setAutoRange(true);
		rangeAxis.setAutoRange(true);
		barRenderer.setShadowVisible(false);
		barRenderer.setBarPainter(new StandardXYBarPainter());
		chartPanel.zoomOutBoth(2.0, 2.0);
		chartPanel.restoreAutoBounds();
		
		numBinsSpinner.addChangeListener(this);
		scaleExpSpinner.addChangeListener(this);
	}
	
	
	private class DataTableModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return availableSet.size();
		}
		
		@Override
		public int getColumnCount() {
			return 2;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Boolean.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= availableSet.size()) {
				return "-";
			}
			Adapter<Number> op = availableSet.get(row);
			Object value = null;
			switch (column) {
				case 0: 
					return activeSet.contains(op);
				case 1:
					return op.toString().replace("Adapter", "");
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 0:
					return true;
				default: 
					return false;
			}
		}
		
		
	}
	
	private class DataActivationListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			int row = adapterTable.getSelectedRow();
			if (adapterTable.getRowSorter() != null) {
				row = adapterTable.getRowSorter().convertRowIndexToModel(row);
			}
			Adapter<Number> op = availableSet.get(row);
			if (activeSet.contains(op)) {
				activeSet.remove(op);
			} else {
				activeSet.add(op);
			}
			updateActive(hif.getActiveLayer());
			adapterTable.revalidate();
		}
		
	}
	
	private class ColoredXYBarRenderer extends XYBarRenderer {
		
		private static final long serialVersionUID = 1L;

		@Override
		public Paint getItemPaint(int row, int column) {
			// TODO: use nodes color here
			return super.getItemPaint(row, column);
		}
		
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (hif != null) {
			updateActive(hif.getActiveLayer());
		}
	}
	
	private void updateAdapterTable() {
		adapterTable.setModel(new DataTableModel());
		adapterTable.getColumnModel().getColumn(0).setMaxWidth(30);
	}
	
	private void updateActive(HalfedgeLayer l) {
		int numBins = numBinsModel.getNumber().intValue();
		HalfEdgeDataStructure<?, ?, ?> hds = l.get();
		AdapterSet aSet = l.getEffectiveAdapters();
		dataSet = new HistogramDataset();
		if (activeSet.size() == 0 ||
			hds.numVertices() == 0 ||
			hds.numEdges() == 0 ||
			hds.numFaces() == 0
		) {
			plot.setDataset(dataSet);
			chartPanel.restoreAutoBounds();
			updateAdapterTable();
			return;
		}
		for (Adapter<? extends Number> a : activeSet) {
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
		updateAdapterTable();
	}

	
	@SuppressWarnings("unchecked")
	private void updateAvailable(HalfedgeLayer l) {
		availableSet.clear();
		activeSet.clear();
		HalfEdgeDataStructure<?, ?, ?> hds = l.get();
		AdapterSet aSet = l.getEffectiveAdapters();
		TypedAdapterSet<Number> numSet = aSet.querySet(Number.class);
		if (numSet.isEmpty() || 
			hds.numVertices() == 0 ||
			hds.numEdges() == 0 ||
			hds.numFaces() == 0
		) return;
		for (Adapter<?> a : numSet) {
			if (a.canAccept(hds.getVertexClass())) { // create vertex histogram
				availableSet.add((Adapter<Number>)a);
			}
			if (a.canAccept(hds.getEdgeClass())) { // create edge histogram
				availableSet.add((Adapter<Number>)a);
			}
			if (a.canAccept(hds.getFaceClass())) { // create face histogram
				availableSet.add((Adapter<Number>)a);
			}
		}
		updateActive(l);
	}
	
	
	private double[] getData(Collection<?> nodes, Adapter<?> a, AdapterSet aSet) {
		int scaleExp = scaleExpModel.getNumber().intValue();
		double scale = Math.pow(10, scaleExp); 
		double[] data = new double[nodes.size()];
		int i = 0;
		for (Object n : nodes) {
			double value = ((Number)a.get((Node<?,?,?>)n, aSet)).doubleValue();
			data[i++] = value * scale;
		}
		return data;
	}
	
	
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		updateAvailable(active);
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateAvailable(layer);
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "numBins", numBinsModel.getNumber().intValue());
		c.storeProperty(getClass(), "scaleExp", scaleExpModel.getNumber().intValue());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		numBinsModel.setValue(c.getProperty(getClass(), "numBins", 500));
		scaleExpModel.setValue(c.getProperty(getClass(), "scaleExp", 0));
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
