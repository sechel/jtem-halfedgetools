package de.jtem.halfedgetools.plugin;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import de.jreality.plugin.basic.ViewMenuBar;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class HalfedgeAlgorithmPlugin <
V extends Vertex<V,E,F>,
E extends Edge<V,E,F> ,
F extends Face<V,E,F>,
HDS extends HalfEdgeDataStructure<V,E,F>
>  extends Plugin {

	protected ViewMenuBar
		viewMenuBar = null;
	protected HalfedgeToolBar
		halfedgeToolBar = null;
	protected HalfedgeInterfacePlugin<V,E,F,HDS>
		hcp = null;
	protected double
		actionPriority = 1.0;
	
	public HalfedgeAlgorithmPlugin() {
		this(1.0);
	}
	
	public HalfedgeAlgorithmPlugin(double priority) {
		this.actionPriority = priority;
	}
	
	
	private class HalfedgeAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public HalfedgeAction() {
			putValue(NAME, getAlgorithmName());
			putValue(SMALL_ICON, getPluginInfo().icon);
		}
		
		public void actionPerformed(ActionEvent e) {
			execute(hcp);
		}
		
	}
	
	public static enum AlgorithmType {
		Geometry,
		Utility,
		Diagnosis
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		HalfedgeAction action = new HalfedgeAction();
		hcp = c.getPlugin(HalfedgeInterfacePlugin.class);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		viewMenuBar.addMenuItem(getClass(), actionPriority, action, "Halfedge", getCategoryName());
		halfedgeToolBar = c.getPlugin(HalfedgeToolBar.class);
		halfedgeToolBar.addAction(getClass(), actionPriority, action);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewMenuBar.removeAll(getClass());
		halfedgeToolBar.removeAll(getClass());
	}
	
	public abstract AlgorithmType getAlgorithmType();
	
	public abstract String getCategoryName();
	
	public abstract String getAlgorithmName();

	public abstract void execute(HalfedgeInterfacePlugin<V,E,F,HDS> hcp);
	
}
