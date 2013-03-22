package de.jtem.halfedgetools.plugin.algorithm;

import static javax.swing.JOptionPane.OK_CANCEL_OPTION;
import static javax.swing.JOptionPane.OK_OPTION;
import static javax.swing.JOptionPane.PLAIN_MESSAGE;

import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jreality.plugin.job.AbstractJob;
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

	private View
		view = null;
	private static Icon
		defaultIcon = ImageHook.getIcon("cog_edit.png");

	
	private class AlgorithmBeforeJob extends AbstractJob {
		
		@Override
		public String getJobName() {
			return getAlgorithmName() + " Init";
		}
		
		@Override
		public void execute() throws Exception {
			executeBeforeDialog(hcp.get(), hcp.getAdapters(), hcp);
		}
		
	}
	
	private class AlgorithmShowDialogJob extends AbstractJob {
		
		private int dialogResult = OK_OPTION;
	
		@Override
		public String getJobName() {
			return "Dialog";
		}
		
		@Override
		public void execute() throws Exception {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
					Icon icon = getPluginInfo().icon != null ? getPluginInfo().icon : defaultIcon;
					dialogResult = JOptionPane.showConfirmDialog(
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
	
	
	private class AlgorithmExecuteDialogJob extends AbstractJob {
		
		@Override
		public String getJobName() {
			return "Dialog";
		}
		
		@Override
		public void execute() throws Exception {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					executeDialog(hcp.get(), hcp.getAdapters(), hcp);
				}
			};
			EventQueue.invokeAndWait(r);
		}
		
	}
	
	
	private class AlgorithmAfterJob extends AbstractJob {
		
		private AlgorithmShowDialogJob
			dialogJob = null;
		
		private AlgorithmAfterJob(AlgorithmShowDialogJob dialogJob) {
			super();
			this.dialogJob = dialogJob;
		}

		@Override
		public String getJobName() {
			return getAlgorithmName();
		}
		
		@Override
		public void execute() throws Exception {
			if (dialogJob.dialogResult == OK_OPTION) {
				executeAfterDialog(hcp.get(), hcp.getAdapters(), hcp);
			}
		}
		
	}
	
	
	@Override
	public void execute() {
		AlgorithmBeforeJob beforeJob = new AlgorithmBeforeJob();
		AlgorithmShowDialogJob dialogJob = new AlgorithmShowDialogJob();
		AlgorithmExecuteDialogJob execDialogJob = new AlgorithmExecuteDialogJob();
		AlgorithmAfterJob afterJob = new AlgorithmAfterJob(dialogJob);
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
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi) {
		execute();
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeBeforeDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog(HDS hds, AdapterSet a, HalfedgeInterface hi) {}
	
	
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
