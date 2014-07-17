package de.jtem.halfedgetools.plugin.data.visualizer;

import javax.swing.SpinnerNumberModel;

public class ClampSpinnerModel extends SpinnerNumberModel {

	private static final long serialVersionUID = 1L;
	private Double stepsize = 1E-1;

	public ClampSpinnerModel() {
		super(0.0, null, null, 1.0);
	}

	@Override
	public Object getNextValue() {
		return getNumber().doubleValue() + stepsize;
	}

	@Override
	public Object getPreviousValue() {
		return getNumber().doubleValue() - stepsize;
	}

	@Override
	public void setStepSize(Number stepSize) {
		this.stepsize = stepSize.doubleValue();
	}

}