package de.jtem.halfedgetools.plugin.widget;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.Rectangle3D;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;

public class LayerActionsWidget extends JPanel implements ActionListener, ChangeListener {

	public static enum ClippingMode {
		ABS,REL;
	}
	
	private static final long 
		serialVersionUID = 1L;
	private HalfedgeLayer
		layer = null;
	private JCheckBox
		clippingBox = new JCheckBox("Clipping");
	private JRadioButton
		relativeButton = new JRadioButton("Relative"),
		absoluteButton = new JRadioButton("Absolute");
	private ButtonGroup
		scaleGroup = new ButtonGroup();
	private SpinnerNumberModel
		xSizeModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1),
		ySizeModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1),
		zSizeModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1),
		xOriginModel = new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0),
		yOriginModel = new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0),
		zOriginModel = new SpinnerNumberModel(0.0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0);
	
	private JSpinner
		xSizeSpinner = new JSpinner(xSizeModel),
		ySizeSpinner = new JSpinner(ySizeModel),
		zSizeSpinner = new JSpinner(zSizeModel),
		xOriginSpinner = new JSpinner(xOriginModel),
		yOriginSpinner = new JSpinner(yOriginModel),
		zOriginSpinner = new JSpinner(zOriginModel);
	
	public LayerActionsWidget() {
		setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		add(clippingBox,lc);
		add(relativeButton,lc);
		add(absoluteButton,rc);
		add(new JLabel("Size"),lc);
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
		
		add(new JLabel("Origin"),lc);
		add(new JLabel("x"),lc);
		add(xOriginSpinner,lc);
		add(new JLabel("y"),lc);
		add(yOriginSpinner,lc);
		add(new JLabel("z"),lc);
		add(zOriginSpinner,rc);
		xOriginSpinner.setPreferredSize(new Dimension(50,20));
		yOriginSpinner.setPreferredSize(new Dimension(50,20));
		zOriginSpinner.setPreferredSize(new Dimension(50,20));
		clippingBox.addActionListener(this);
		xOriginSpinner.addChangeListener(this);
		yOriginSpinner.addChangeListener(this);
		zOriginSpinner.addChangeListener(this);
		xOriginSpinner.setEnabled(false);
		yOriginSpinner.setEnabled(false);
		zOriginSpinner.setEnabled(false);
		
		scaleGroup.add(relativeButton);
		scaleGroup.add(absoluteButton);
		relativeButton.setSelected(true);
		relativeButton.addActionListener(this);
		absoluteButton.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == clippingBox) {
			layer.setEnableClipping(clippingBox.isSelected());
		} else if(e.getSource() == absoluteButton || e.getSource() == relativeButton) {
			layer.setClippingMode(absoluteButton.isSelected()?ClippingMode.ABS:ClippingMode.REL);
			updateSpinners();
		}
	}

	
	private void updateSpinners() {
		if(relativeButton.isSelected()) {
			xSizeModel.setValue(.5);
			xSizeModel.setMaximum(1.0);
			xSizeModel.setStepSize(.05);
			ySizeModel.setValue(.5);
			ySizeModel.setMaximum(1.0);
			ySizeModel.setStepSize(.05);
			zSizeModel.setValue(.5);
			zSizeModel.setMaximum(1.0);
			zSizeModel.setStepSize(.05);
		} else if(absoluteButton.isSelected()) {
			xOriginSpinner.setEnabled(absoluteButton.isSelected());
			yOriginSpinner.setEnabled(absoluteButton.isSelected());
			zOriginSpinner.setEnabled(absoluteButton.isSelected());
			
			Rectangle3D bb = BoundingBoxUtility.calculateBoundingBox(layer.getGeometryRoot());
			Rectangle3D bbGeometry = BoundingBoxUtility.calculateBoundingBox(layer.getGeometry());
			double[] extent = bb.getExtent();
			double[] gExtent = bbGeometry.getExtent();
			xSizeModel.setValue(2.0*gExtent[0]);
			xSizeModel.setMaximum(Double.MAX_VALUE);
			xSizeModel.setStepSize(extent[0]*0.05);
			
			ySizeModel.setValue(2.0*gExtent[1]);
			ySizeModel.setMaximum(Double.MAX_VALUE);
			ySizeModel.setStepSize(extent[1]*0.05);
			
			zSizeModel.setValue(2.0*gExtent[1]);
			zSizeModel.setMaximum(Double.MAX_VALUE);
			zSizeModel.setStepSize(extent[1]*0.05);
			
			xOriginModel.setStepSize(extent[0]*0.05);
			yOriginModel.setStepSize(extent[1]*0.05);
			zOriginModel.setStepSize(extent[1]*0.05);
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if(e.getSource() == xSizeSpinner || e.getSource() == ySizeSpinner || e.getSource() == zSizeSpinner ||
		   e.getSource() == xOriginSpinner || e.getSource() == yOriginSpinner || e.getSource() == zOriginSpinner) {
			double x = xSizeModel.getNumber().doubleValue();
			double y = ySizeModel.getNumber().doubleValue();
			double z = zSizeModel.getNumber().doubleValue();
			double ox = xOriginModel.getNumber().doubleValue();
			double oy = yOriginModel.getNumber().doubleValue();
			double oz = zOriginModel.getNumber().doubleValue();
			layer.setClippingScale(x,y,z);
			layer.setClippingOrigin(ox,oy,oz);
		}
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
		clippingBox.setSelected(layer.isClippingEnabled());
		double[] scale = layer.getClippingScale();
		switch(layer.getClippingMode()){
			case ABS:
				absoluteButton.setSelected(true);
				updateSpinners();
				break;
			case REL:
				relativeButton.setSelected(true);
				updateSpinners();
				break;
		}	
		xSizeModel.setValue(scale[0]);
		ySizeModel.setValue(scale[1]);
		zSizeModel.setValue(scale[2]);
		double[] origin = layer.getClippingOrigin();
		xOriginModel.setValue(origin[0]);
		yOriginModel.setValue(origin[1]);
		zOriginModel.setValue(origin[2]);
	}
	
	
}