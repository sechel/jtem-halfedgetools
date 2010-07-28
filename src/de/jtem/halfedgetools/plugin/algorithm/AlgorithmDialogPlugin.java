package de.jtem.halfedgetools.plugin.algorithm;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;

public abstract class AlgorithmDialogPlugin extends AlgorithmPlugin implements UIFlavor {

	private View
		view = null;
	private static Icon
		defaultIcon = ImageHook.getIcon("cog_edit.png");

	public final < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
		Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : defaultIcon;
		int result = OK_OPTION;
		if (getDialogPanel() != null) {
			result = JOptionPane.showOptionDialog(
				w, getDialogPanel(), 
				getPluginInfo().name, 
				OK_CANCEL_OPTION, 
				PLAIN_MESSAGE, 
				icon, 
				new String[] {"Ok", "Cancel"}, 
				"Edit"
			);
		}
		if (result == OK_OPTION) {
			executeAfterDialog(hds, c, hcp);
		}
	}
	
	public abstract < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException;
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
	}
	
	protected JPanel getDialogPanel() {
		return null;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		if (getDialogPanel() != null) {
			SwingUtilities.updateComponentTreeUI(getDialogPanel());
		}
	}
}
