/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universitaet Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.plugin.algorithm;

import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.EventQueue;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.job.AbstractJob;
import de.jreality.plugin.job.Job;
import de.jreality.plugin.job.JobListener;
import de.jreality.plugin.job.JobQueuePlugin;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.ProgressNotifier;
import de.jtem.halfedgetools.plugin.AlgorithmDropdownToolbar;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;

public abstract class AlgorithmPlugin extends Plugin implements Comparable<AlgorithmPlugin>, JobListener {

	private Logger
		log = Logger.getLogger(getClass().getName());
	protected JobQueuePlugin
		jobQueue = null;
	protected View
		view = null;
	protected ViewMenuBar
		viewMenuBar = null;
	protected ToolBarAggregator
		toolbar = null;
	protected AlgorithmDropdownToolbar
		dropdownToolbar = null;
	protected HalfedgeInterface
		hcp = null;
	private HalfedgeAction 
		action = new HalfedgeAction();
	protected AlgorithmJob
		currentJob = new AlgorithmJob();
	
	private static AlgorithmPlugin
		lastAlgorithm = null;
	
	public AlgorithmPlugin() {
	}
	
	@Override
	public int compareTo(AlgorithmPlugin o) {
		double p0 = getPriority();
		double p1 = o.getPriority();
		if (p0 == p1) {
			return getAlgorithmName().compareTo(o.getAlgorithmName());
		} else {
			return p0 < p1 ? -1 : 1;
		}
	}
	
	public Window getOptionParent() {
		return SwingUtilities.getWindowAncestor(view.getCenterComponent());
	}
	
	
	public class HalfedgeAction extends AbstractAction {
		
		private static final long 
			serialVersionUID = 1L;

		public HalfedgeAction() {
			putValue(NAME, getAlgorithmName());
			putValue(SHORT_DESCRIPTION, getAlgorithmName());
			putValue(SMALL_ICON, getPluginInfo().icon);
			if (getKeyboardShortcut() != null) {
				putValue(ACCELERATOR_KEY, getKeyboardShortcut());
			}
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			execute();
		}
		
	}
	
	
	protected class AlgorithmJob extends AbstractJob implements ProgressNotifier {
		
		@Override
		public String getJobName() {
			return getAlgorithmName();
		}
		
		@Override
		public void executeJob() throws Exception {
			try {
				AlgorithmPlugin.this.execute(hcp.get(), hcp.getAdapters(), hcp);
			} catch (final Exception e1) {
				e1.printStackTrace();
				Runnable r = new Runnable() {
					@Override
					public void run() {
						Window w = SwingUtilities.getWindowAncestor(hcp.getShrinkPanel());
						String msg = e1.getClass().getSimpleName() + ": " + e1.getLocalizedMessage();
						JOptionPane.showMessageDialog(w, msg, "Error: " + getAlgorithmName(), ERROR_MESSAGE);						
					}
				};
				EventQueue.invokeLater(r);
				throw e1;
			}
		}
		
		@Override
		public void fireJobProgress(double arg0) {
			super.fireJobProgress(arg0);
		}
		@Override
		public void fireJobFailed(Exception arg0) {
			super.fireJobFailed(arg0);
		}
		
	}
	
	public void execute() {
		lastAlgorithm = this;
		currentJob = new AlgorithmJob();
		currentJob.addJobListener(this);
		jobQueue.queueJob(currentJob);
	}
	
	@Override
	public void jobStarted(Job job) {
	}
	@Override
	public void jobCancelled(Job job) {
	}
	@Override
	public void jobFinished(Job job) {
	}
	@Override
	public void jobFailed(Job job, Exception e) {
		log.log(Level.WARNING, e.getMessage(), e);
	}
	@Override
	public void jobProgress(Job job, double progress) {
	}
	
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		jobQueue = c.getPlugin(JobQueuePlugin.class);
		view = c.getPlugin(View.class);
		hcp = c.getPlugin(HalfedgeInterface.class);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		String catName = getCategory();
		viewMenuBar.addMenuItem(getClass(), getPriority(), action, "Halfedge", catName);
		dropdownToolbar = c.getPlugin(AlgorithmDropdownToolbar.class);
		dropdownToolbar.addAlgorithm(this);
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		viewMenuBar.removeAll(getClass());
		toolbar.removeAll(getClass());
	}
	
	public HalfedgeAction getHalfedgeAction() {
		return action;
	}
	
	
	public KeyStroke getKeyboardShortcut() {
		return null;
	}
	
	public String getCategory() {
		return getAlgorithmCategory().toString();
	}
	
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Custom;
	}
	
	public abstract String getAlgorithmName();

	public double getPriority() {
		return 0;
	}
	
	public abstract<
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hi);
		
	public AlgorithmJob getCurrentJob() {
		return currentJob;
	}
	
	public static AlgorithmPlugin getLastAlgorithm() {
		return lastAlgorithm;
	}
	
}
