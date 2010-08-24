package de.jtem.halfedgetools.plugin.widget;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jtem.halfedgetools.plugin.HalfedgeLayer;

public class LayerPropertyWidget extends JPanel implements ActionListener, ChangeListener {

	private static final long 
		serialVersionUID = 1L;
	private HalfedgeLayer
		layer = null;
	
	private JRadioButton
		noEffectChecker = new JRadioButton("No Geometry Effect"),
		thickenChecker = new JRadioButton("Thicken"),
		implodeChecker = new JRadioButton("Implode");
	private JCheckBox
		makeHolesChecker = new JCheckBox("Holes");
	private SpinnerNumberModel
		stepsPerEdgeModel = new SpinnerNumberModel(1, 1, 100, 1),
		holeFactorModel = new SpinnerNumberModel(1.0, 0.0, 100.0, 0.1),
		implodeFactorModel = new SpinnerNumberModel(0.5, 0.0, 1.0, 0.1),
		thicknessModel = new SpinnerNumberModel(1.0, 0.0, 100.0, 0.1);
	private JSpinner
		stepsPerEdgeSpinner = new JSpinner(stepsPerEdgeModel),
		holeFactorSpinner = new JSpinner(holeFactorModel),
		implodeFactorSpinner = new JSpinner(implodeFactorModel), 
		thicknessSpinner = new JSpinner(thicknessModel); 
	
	private boolean
		disableListeners = false;
	
	public LayerPropertyWidget() {
		setLayout(new GridBagLayout());
		GridBagConstraints c1 = new GridBagConstraints();
		c1.insets = new Insets(2, 2, 2, 2);
		c1.weightx = 1.0;
		c1.fill = GridBagConstraints.BOTH;
		c1.gridwidth = GridBagConstraints.RELATIVE;
		GridBagConstraints c2 = (GridBagConstraints)c1.clone();
		c2.gridwidth = GridBagConstraints.REMAINDER;

		add(noEffectChecker, c2);
		add(new JSeparator(), c2);
		
		add(implodeChecker, c1);
		add(implodeFactorSpinner, c2);
		add(new JSeparator(), c2);
		
		add(thickenChecker, c1);
		add(thicknessSpinner, c2);
		add(makeHolesChecker, c2);
		add(new JLabel("Hole Factor"), c1);
		add(holeFactorSpinner, c2);
		add(new JLabel("Steps Per Edge"), c1);
		add(stepsPerEdgeSpinner, c2);
		
		c2.weighty = 1.0;
		add(new JPanel(), c2);
		
		implodeChecker.addActionListener(this);
		thickenChecker.addActionListener(this);
		implodeFactorSpinner.addChangeListener(this);
		thicknessSpinner.addChangeListener(this);
		makeHolesChecker.addActionListener(this);
		holeFactorSpinner.addChangeListener(this);
		stepsPerEdgeSpinner.addChangeListener(this);
		noEffectChecker.addActionListener(this);
		
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(noEffectChecker);
		modeGroup.add(thickenChecker);
		modeGroup.add(implodeChecker);
	}
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (disableListeners) return;
		updateLayer();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (disableListeners) return;
		updateLayer();
	}
	
	
	private void updateLayer() {
		if (layer == null) return;
		layer.setImplode(implodeChecker.isSelected());
		layer.setThickenSurface(thickenChecker.isSelected());
		layer.setImplodeFactor(implodeFactorModel.getNumber().doubleValue());
		layer.setThickness(thicknessModel.getNumber().doubleValue());
		layer.setMakeHoles(makeHolesChecker.isSelected());
		layer.setHoleFactor(holeFactorModel.getNumber().doubleValue());
		layer.setStepsPerEdge(stepsPerEdgeModel.getNumber().intValue());
		layer.update();
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
		disableListeners = true;
		implodeChecker.setSelected(layer.isImplode());
		thickenChecker.setSelected(layer.isThickenSurface());
		implodeFactorModel.setValue(layer.getImplodeFactor());
		thicknessModel.setValue(layer.getThickness());
		makeHolesChecker.setSelected(layer.isMakeHoles());
		holeFactorModel.setValue(layer.getHoleFactor());
		stepsPerEdgeModel.setValue(layer.getStepsPerEdge());
		noEffectChecker.setSelected(!layer.isImplode() && !layer.isThickenSurface());
		disableListeners = false;
	}
	
}
