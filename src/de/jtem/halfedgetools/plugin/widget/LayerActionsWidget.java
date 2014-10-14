package de.jtem.halfedgetools.plugin.widget;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;

public class LayerActionsWidget extends JPanel implements ActionListener, ChangeListener {

	private static final long 
		serialVersionUID = 1L;
	private HalfedgeLayer
		layer = null;
	private JCheckBox
		clippingBox = new JCheckBox("Clipping");
	private SpinnerNumberModel
		xSizeModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1),
		ySizeModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1),
		zSizeModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1);
	private JSpinner
		xSizeSpinner = new JSpinner(xSizeModel),
		ySizeSpinner = new JSpinner(ySizeModel),
		zSizeSpinner = new JSpinner(zSizeModel);
	
	public LayerActionsWidget() {
		setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		add(clippingBox,lc);
		add(new JLabel("x"),lc);
		add(xSizeSpinner,lc);
		add(new JLabel("y"),lc);
		add(ySizeSpinner,lc);
		add(new JLabel("z"),lc);
		add(zSizeSpinner,rc);
		xSizeSpinner.setPreferredSize(new Dimension(50,20));
		ySizeSpinner.setPreferredSize(new Dimension(50,20));
		zSizeSpinner.setPreferredSize(new Dimension(50,20));
		clippingBox.addActionListener(this);
		xSizeSpinner.addChangeListener(this);
		ySizeSpinner.addChangeListener(this);
		zSizeSpinner.addChangeListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == clippingBox) {
			layer.setEnableClipping(clippingBox.isSelected());
		}
	}

	
	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == xSizeSpinner || e.getSource() == ySizeSpinner || e.getSource() == zSizeSpinner) {
			layer.setClippingScale(xSizeModel.getNumber().doubleValue(),ySizeModel.getNumber().doubleValue(),zSizeModel.getNumber().doubleValue());
		}
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
		clippingBox.setSelected(layer.isClippingEnabled());
		double[] scale = layer.getClippingScale();
		xSizeModel.setValue(scale[0]);
		ySizeModel.setValue(scale[1]);
		zSizeModel.setValue(scale[2]);
	}
	
	
}