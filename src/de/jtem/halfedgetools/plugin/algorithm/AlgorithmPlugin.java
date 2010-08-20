/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
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

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.ViewMenuBar;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.EditingCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.FileCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.GeneratorCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.GeometryCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.SelectionCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.SimplificationCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.SubdivisionCategoryToolbar;
import de.jtem.halfedgetools.plugin.AlgorithmToolbars.TopologyCategoryToolbar;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeToolBar;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.aggregators.ToolBarAggregator;

public abstract class AlgorithmPlugin extends Plugin implements Comparable<AlgorithmPlugin> {

	protected ViewMenuBar
		viewMenuBar = null;
	protected ToolBarAggregator
		toolbar = null;
	protected HalfedgeInterface
		hcp = null;
	private HalfedgeAction 
		action = new HalfedgeAction();
	
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
			try {
				execute(hcp.get(), hcp.getCalculators(), hcp);
			} catch (Exception e1) {
				e1.printStackTrace();
				Window w = SwingUtilities.getWindowAncestor(hcp.getShrinkPanel());
				String msg = e1.getClass().getSimpleName() + ": " + e1.getLocalizedMessage();
				JOptionPane.showMessageDialog(w, msg, "Error: " + getAlgorithmName(), ERROR_MESSAGE);
			}
		}
		
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hcp = c.getPlugin(HalfedgeInterface.class);
		viewMenuBar = c.getPlugin(ViewMenuBar.class);
		viewMenuBar.addMenuItem(getClass(), getPriority(), action, "Halfedge", getAlgorithmCategory().toString());
		switch (getAlgorithmCategory()) {
		case Editing:
			toolbar = c.getPlugin(EditingCategoryToolbar.class);
			break;
		case Geometry:
			toolbar = c.getPlugin(GeometryCategoryToolbar.class);
			break;
		case Simplification:
			toolbar = c.getPlugin(SimplificationCategoryToolbar.class);
			break;
		case Subdivision:
			toolbar = c.getPlugin(SubdivisionCategoryToolbar.class);
			break;
		case Topology:
			toolbar = c.getPlugin(TopologyCategoryToolbar.class);
			break;
		case File:
			toolbar = c.getPlugin(FileCategoryToolbar.class);
			break;
		case Generator:
			toolbar = c.getPlugin(GeneratorCategoryToolbar.class);
			break;
		case Selection:
			toolbar = c.getPlugin(SelectionCategoryToolbar.class);
			break;
		default:
			toolbar = c.getPlugin(HalfedgeToolBar.class); 
		}
		toolbar.addAction(getClass(), getPriority(), action);
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
	
	public abstract AlgorithmCategory getAlgorithmCategory();
	
	public abstract String getAlgorithmName();

	public double getPriority() {
		return 0;
	}
	
	public abstract < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException;
	
}
