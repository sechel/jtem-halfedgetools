package de.jtem.halfedgetools.plugin.visualizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.LabelAdapter2Ifs;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;

public class EdgeLengthVisualizer extends VisualizerPlugin implements ChangeListener {

	private DecimalFormat
		format = new DecimalFormat("0.000");
	private SpinnerNumberModel
		placesModel = new SpinnerNumberModel(3, 0, 20, 1);
	private JSpinner	
		placesSpinner = new JSpinner(placesModel);
	private JPanel
		panel = new JPanel();
	
	
	public EdgeLengthVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = 1;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		panel.add(new JLabel("Decimal Places"), gbc1);
		panel.add(placesSpinner, gbc2);
		
		placesSpinner.addChangeListener(this);
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		String fs = "0.";
		for (int i = 0; i < placesModel.getNumber().intValue(); i++) {
			fs += "0";
		}
		format = new DecimalFormat(fs);
		updateContent();
	}
	
	public class EdgeLengthAdapter <E extends JREdge<?, E, ?>> implements  LabelAdapter2Ifs<E> {

		@Override
		public AdapterType getAdapterType() {
			return AdapterType.EDGE_ADAPTER;
		}

		@Override
		public String getLabel(E e) {
			double[] p1 = e.getStartVertex().position;
			double[] p2 = e.getTargetVertex().position;
			double l = Rn.euclideanDistance(p1, p2);
			return format.format(l);
		}
		
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public Set<? extends Adapter> getAdapters() {
		return Collections.singleton(new EdgeLengthAdapter());
	}


	@Override
	public String getName() {
		return "Edge Lengths";
	}

}
