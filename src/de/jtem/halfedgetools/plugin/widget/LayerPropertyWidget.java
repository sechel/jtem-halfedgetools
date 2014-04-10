package de.jtem.halfedgetools.plugin.widget;

import static java.lang.Double.parseDouble;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
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
		removeTextureJumpsRadio = new JRadioButton("Remove Texture Jumps"),
		thickenChecker = new JRadioButton("Thicken"),
		implodeChecker = new JRadioButton("Implode");
	private JCheckBox
		makeHolesChecker = new JCheckBox("Holes");
	private SpinnerNumberModel
		jumpSizeModel = new SpinnerNumberModel(1.0, 0.0, 100.0, 0.1),
		stepsPerEdgeModel = new SpinnerNumberModel(1, 1, 100, 1),
		holeFactorModel = new SpinnerNumberModel(1.0, -100.0, 100.0, 0.01),
		implodeFactorModel = new SpinnerNumberModel(0.5, -1.0, 1.0, 0.01),
		thicknessModel = new SpinnerNumberModel(1.0, 0.0, 100.0, 0.01);
	private JSpinner
		jumpSizeSpinner = new JSpinner(jumpSizeModel),
		stepsPerEdgeSpinner = new JSpinner(stepsPerEdgeModel),
		holeFactorSpinner = new JSpinner(holeFactorModel),
		implodeFactorSpinner = new JSpinner(implodeFactorModel), 
		thicknessSpinner = new JSpinner(thicknessModel); 
	private JTextArea
		profileCurveArea = new JTextArea();
	private JScrollPane
		profileScroller = new JScrollPane(profileCurveArea);
	private JPanel
		profilePanel = new JPanel(new GridLayout());
	private JButton
		updateButton = new JButton("Update Geometry");
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

		add(removeTextureJumpsRadio, c1);
		add(jumpSizeSpinner, c2);
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
		add(profilePanel, c2);
		profilePanel.add(profileScroller);
		profilePanel.setBorder(BorderFactory.createTitledBorder("Profile Curve"));
		profilePanel.setMinimumSize(new Dimension(10, 150));
		profilePanel.setPreferredSize(new Dimension(10, 150));
		add(updateButton, c2);
		
		c2.weighty = 1.0;
		add(new JPanel(), c2);
		
		implodeChecker.addActionListener(this);
		thickenChecker.addActionListener(this);
		implodeFactorSpinner.addChangeListener(this);
		removeTextureJumpsRadio.addChangeListener(this);
		jumpSizeSpinner.addChangeListener(this);
		thicknessSpinner.addChangeListener(this);
		makeHolesChecker.addActionListener(this);
		holeFactorSpinner.addChangeListener(this);
		stepsPerEdgeSpinner.addChangeListener(this);
		noEffectChecker.addActionListener(this);
		updateButton.addActionListener(this);
		
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(noEffectChecker);
		modeGroup.add(removeTextureJumpsRadio);
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
		layer.setRemoveTextureJumps(removeTextureJumpsRadio.isSelected());
		layer.setTextureJumpSize(jumpSizeModel.getNumber().doubleValue());
		layer.setImplode(implodeChecker.isSelected());
		layer.setThickenSurface(thickenChecker.isSelected());
		layer.setImplodeFactor(implodeFactorModel.getNumber().doubleValue());
		layer.setThickness(thicknessModel.getNumber().doubleValue());
		layer.setMakeHoles(makeHolesChecker.isSelected());
		layer.setHoleFactor(holeFactorModel.getNumber().doubleValue());
		layer.setStepsPerEdge(stepsPerEdgeModel.getNumber().intValue());
		
		String profileCurveTxt = profileCurveArea.getText();
		StringTokenizer st = new StringTokenizer(profileCurveTxt);
		List<double[]> pointsList = new LinkedList<double[]>();
		try {
			while (st.hasMoreTokens()) {
				String xStr = st.nextToken();
				String yStr = st.nextToken();
				double[] p = {parseDouble(xStr), parseDouble(yStr)};
				pointsList.add(p);
			}
			double[][] profileCurve = pointsList.toArray(new double[pointsList.size()][]);
			layer.setProfileCurve(profileCurve);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Profile Curve Parsing Error", ERROR_MESSAGE);
			return;
		}
		layer.update();
	}
	
	public void setLayer(HalfedgeLayer layer) {
		this.layer = layer;
		disableListeners = true;
		jumpSizeModel.setValue(layer.getTextureJumpSize());
		removeTextureJumpsRadio.setSelected(layer.isRemoveTextureJumps());
		implodeChecker.setSelected(layer.isImplode());
		thickenChecker.setSelected(layer.isThickenSurface());
		implodeFactorModel.setValue(layer.getImplodeFactor());
		thicknessModel.setValue(layer.getThickness());
		makeHolesChecker.setSelected(layer.isMakeHoles());
		holeFactorModel.setValue(layer.getHoleFactor());
		stepsPerEdgeModel.setValue(layer.getStepsPerEdge());
		noEffectChecker.setSelected(
			!layer.isImplode() && 
			!layer.isThickenSurface() && 
			!layer.isRemoveTextureJumps()
		);
		StringBuffer sb = new StringBuffer();
		for (double[] p : layer.getProfileCurve()) {
			sb.append(p[0] + "\t" + p[1] + "\n");
		}
		profileCurveArea.setText(sb.toString());
		disableListeners = false;
	}
	
}
