package de.jtem.halfedgetools.plugin.data.visualizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;

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
	
	private class HistogrammVisualization extends AbstractDataVisualization {

		private int 
			numBins = 200,
			exp = 0;
		
		public HistogrammVisualization(
			Adapter<?> source,
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(source, visualizer, type);
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
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		activeVis = (HistogrammVisualization)visualization;
		numBinsModel.setValue(activeVis.numBins);
		scaleExpModel.setValue(activeVis.exp);
		return panel;
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
	public DataVisualization createVisualization(NodeType type, Adapter<?> source) {
		return new HistogrammVisualization(source, this, type);
	}

}
