package de.jtem.halfedgetools.plugin.algorithm.selection;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.TypedSelection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class CornerVertexSelection extends AlgorithmDialogPlugin implements ChangeListener { 
	
	private JPanel 
		panel = new JPanel();
	
	private SpinnerNumberModel
		ratioPiModel = new SpinnerNumberModel(2, 1, 10000, 1);
	
	private JSpinner
		ratioPiSpinner = new JSpinner(ratioPiModel);
	
	private Selection oldSelection = null;
	
	public CornerVertexSelection() {
		panel .setLayout(new GridBagLayout());
		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.fill = GridBagConstraints.BOTH;
		gbc1.weightx = 1.0;
		gbc1.gridwidth = GridBagConstraints.REMAINDER;
		gbc1.insets = new Insets(2, 2, 2, 2);
		GridBagConstraints gbc2 = new GridBagConstraints();
		
		ratioPiSpinner.addChangeListener(this);
		panel.add(new JLabel("ratio of Pi"), gbc1);
		panel.add(ratioPiSpinner, gbc2);
		
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Corner boundary vertices";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hcp) {
		hcp.setSelection(selectCorners(hcp, hds, a));
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		if (getDialogPanel() != null) {
			SwingUtilities.updateComponentTreeUI(getDialogPanel());
		}
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.icon = ImageHook.getIcon("InvertPt.png",16,16);  //???
		return info;
	}
	
	@Override
	public void stateChanged(ChangeEvent e){
		if(oldSelection == null) {
			oldSelection = hcp.getSelection();
		}
		TypedSelection<? extends Node<?,?,?>> cornerSel = selectCorners(hcp, hcp.get(), hcp.getAdapters());
		hcp.setSelection(cornerSel);
	}

	private<
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> TypedSelection<V> selectCorners(HalfedgeInterface hi, HDS hds, AdapterSet a) {
		TypedSelection<V> selCorners = new TypedSelection<V>();
		double eps = Math.PI / ratioPiModel.getNumber().intValue();
		
		double [] n = new double[3];
		double [] u = new double[3];
		double [] w = new double[3];
		
		for(E e : HalfEdgeUtils.boundaryEdges(hds)){
			Rn.negate(n, a.get(Position.class, e.getTargetVertex(), double[].class));
			Rn.add(u, a.get(Position.class, e.getStartVertex(), double[].class), n);
			Rn.add(w, a.get(Position.class, e.getNextEdge().getTargetVertex(), double[].class), n);
			if(Math.abs(Rn.euclideanAngle(u, w)-Math.PI) > eps){
				selCorners.add(e.getTargetVertex());
			}
		}
		
		return null;
	}

}
