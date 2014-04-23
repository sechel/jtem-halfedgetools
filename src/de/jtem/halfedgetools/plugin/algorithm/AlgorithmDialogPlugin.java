package de.jtem.halfedgetools.plugin.algorithm;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

import java.awt.EventQueue;
import java.awt.Window;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;

public abstract class AlgorithmDialogPlugin extends AlgorithmPlugin implements UIFlavor {

	private Logger 
		log = Logger.getLogger(AlgorithmExecuteDialogJob.class.getName());
	private View
		view = null;
	private static Icon
		defaultIcon = ImageHook.getIcon("cog_edit.png");
	private int 
		dialogResult = OK_OPTION;
	
	private class AlgorithmBeforeJob extends AlgorithmJob {
		
		@Override
		public String getJobName() {
			return getAlgorithmName() + " Init";
		}
		
		@Override
		public void executeJob() throws Exception {
			currentJob = this;
			executeBeforeDialog(hcp.get(), hcp.getAdapters(), hcp);
		}
		
	}
	
	private class AlgorithmShowDialogJob extends AlgorithmJob {
		
		@Override
		public String getJobName() {
			return "Dialog";
		}
		
		@Override
		public void executeJob() throws Exception {
			currentJob = this;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
					Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : defaultIcon;
					dialogResult = JOptionPane.showConfirmDialog (
						w, getDialogPanel(), 
						getPluginInfo().name, 
						OK_CANCEL_OPTION, 
						PLAIN_MESSAGE, 
						icon
					);		
				}
			};
			EventQueue.invokeAndWait(r);
		}
		
	}
	
	
	private class AlgorithmExecuteDialogJob extends AlgorithmJob {
		
		@Override
		public String getJobName() {
			return "Dialog";
		}
		
		@Override
		public void executeJob() throws Exception {
			currentJob = this;
			Runnable r = new Runnable() {
				@Override
				public void run() {
					try {
						executeDialog(hcp.get(), hcp.getAdapters(), hcp);
					} catch (Exception e) {
						log.warning(e.toString());
					}
				}
			};
			EventQueue.invokeAndWait(r);
		}
		
	}
	
	
	private class AlgorithmAfterJob extends AlgorithmJob {
		
		@Override
		public String getJobName() {
			return getAlgorithmName();
		}
		
		@Override
		public void executeJob() throws Exception {
			currentJob = this;
			if (dialogResult == OK_OPTION) {
				executeAfterDialog(hcp.get(), hcp.getAdapters(), hcp);
			} else {
				executeAfterDialogCancel(hcp.get(), hcp.getAdapters(), hcp);
			}
		}
		
	}
	
	public void executeWithoutDialog() {
		AlgorithmBeforeJob beforeJob = new AlgorithmBeforeJob();
		AlgorithmAfterJob afterJob = new AlgorithmAfterJob();
		jobQueue.queueJob(beforeJob);
		jobQueue.queueJob(afterJob);
	}
	
	@Override
	public void execute() {
		AlgorithmBeforeJob beforeJob = new AlgorithmBeforeJob();
		AlgorithmShowDialogJob dialogJob = new AlgorithmShowDialogJob();
		AlgorithmExecuteDialogJob execDialogJob = new AlgorithmExecuteDialogJob();
		AlgorithmAfterJob afterJob = new AlgorithmAfterJob();
		jobQueue.queueJob(beforeJob);
		if (getDialogPanel() != null) {
			jobQueue.queueJob(dialogJob);
		} else { 
			jobQueue.queueJob(execDialogJob);
		}
		jobQueue.queueJob(afterJob);
	}
	
	@Override
	public final <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {
		execute();
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialogCancel(HDS hds, AdapterSet a, HalfedgeInterface hi) throws Exception {}	
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
	}
	
	protected JPanel getDialogPanel() {
		return null;
	}
	
	protected int getDialogResult() {
		return dialogResult;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		if (getDialogPanel() != null) {
			SwingUtilities.updateComponentTreeUI(getDialogPanel());
		}
	}

}
