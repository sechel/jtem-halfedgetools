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

package de.jtem.halfedgetools.plugin;

import static javax.swing.SwingUtilities.isEventDispatchThread;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.IndexLabelAdapter;
import de.jtem.java2dx.beans.Viewer2DWithInspector;
import de.jtem.java2dx.modelling.GraphicsModeller2D;
import de.jtem.java2dx.modelling.SimpleModeller2D;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class HalfedgeDebuggerPlugin extends ShrinkPanelPlugin implements ActionListener, ChangeListener {

	private HalfedgeInterface
		hcp = null;
	private SimpleModeller2D
		moddeller = new GraphicsModeller2D();
	private Viewer2DWithInspector
		viewer = moddeller.getViewer();
	private boolean
		showVertices = true,
		showEdges = true,
		showFaces = true;
	private SpinnerNumberModel
		vertexNumberModel = new SpinnerNumberModel(0, 0, 0, 1),
		edgeNumberModel = new SpinnerNumberModel(0, 0, 0, 1),
		faceNumberModel = new SpinnerNumberModel(0, 0, 0, 1);
	private JLabel
		dataLabel = new JLabel("No Data Loaded");
	private JSpinner
		vertexSpinner = new JSpinner(vertexNumberModel),
		edgeSpinner = new JSpinner(edgeNumberModel),
		faceSpinner = new JSpinner(faceNumberModel);
	private JButton
		getGeometryButton = new JButton("Retrieve Geometry"),
		continueButton = new JButton("Continue Algorithm"),
		makeTutte = new JButton("Tutte");
	private JCheckBox
		displayProgress = new JCheckBox("Display Progress"),
		skipBreakpointsChecker = new JCheckBox("Skip", true);
	private JPanel
		navigationPanel = new JPanel(),
		debugPanel = new JPanel();
	
	private HalfEdgeDataStructure<?, ?, ?>
		hds = null;
	private AdapterSet
		adapters = new AdapterSet(new IndexLabelAdapter());

	public HalfedgeDebuggerPlugin() {
		makeLayout();
		makeTutte.addActionListener(this);
		vertexSpinner.addChangeListener(this);
		edgeSpinner.addChangeListener(this);
		faceSpinner.addChangeListener(this);
		getGeometryButton.addActionListener(this);
		continueButton.addActionListener(this);
	}

	
	private void makeLayout() {
		viewer.setPreferredSize(new Dimension(200, 200));
		viewer.setMinimumSize(viewer.getPreferredSize());
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(0, 0, 2, 0);
		c.gridwidth = GridBagConstraints.REMAINDER;
		viewer.setBorder(BorderFactory.createEtchedBorder());
		c.weighty = 0.0;
		shrinkPanel.add(getGeometryButton, c);
		shrinkPanel.add(dataLabel, c);
		c.weighty = 1.0;
		shrinkPanel.add(viewer, c);
		c.weighty = 0.0;
		shrinkPanel.add(navigationPanel, c);
		debugPanel.setBorder(BorderFactory.createTitledBorder("Debugging"));
		debugPanel.setLayout(new GridBagLayout());
		shrinkPanel.add(debugPanel, c);
		
		navigationPanel.setBorder(BorderFactory.createTitledBorder("Navigation"));
		navigationPanel.setLayout(new GridBagLayout());
		c.insets = new Insets(1, 1, 1, 1);
		c.weightx = 0.0;
		c.gridwidth = 1;
		navigationPanel.add(new JLabel("Vertex"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		navigationPanel.add(vertexSpinner, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		navigationPanel.add(new JPanel(), c);
		c.gridwidth = 1;
		navigationPanel.add(new JLabel("Edge"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		navigationPanel.add(edgeSpinner, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		navigationPanel.add(new JPanel(), c);
		c.gridwidth = 1;
		navigationPanel.add(new JLabel("Face"), c);
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		navigationPanel.add(faceSpinner, c);
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		navigationPanel.add(makeTutte, c);
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		
		c.gridwidth = GridBagConstraints.RELATIVE;
		debugPanel.add(skipBreakpointsChecker, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		debugPanel.add(displayProgress, c);
		debugPanel.add(continueButton, c);
		continueButton.setEnabled(false);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (getGeometryButton == s) {
			hds = hcp.get(hds);
			setData(hds);
		}
		if (continueButton == s) {
			synchronized (this) {
				notify();				
			}
		}
		if (hds == null) {
			return;
		}
		if (makeTutte == s) {
			makeTutte(0, false, adapters);
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();
		if (vertexSpinner == s) {
			makeVertexCloseUp(vertexNumberModel.getNumber().intValue(), false, adapters);
		}
		if (edgeSpinner == s) {
			makeEdgeCloseUp(edgeNumberModel.getNumber().intValue(), false, adapters);
		}
		if (faceSpinner == s) {
			makeFaceCloseUp(faceNumberModel.getNumber().intValue(), false, adapters);
		}
	}
	
	
	private synchronized void parkInvokeThread() {
		if (skipBreakpointsChecker.isSelected()) {
			return;
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				continueButton.setEnabled(true);
			}
		});
		try {
			if (!isEventDispatchThread()) {
				wait();
			} else {
				System.out.println("Halfedge algorithm runs on the event thread -> cannot debug");
			}
		} catch (InterruptedException e) {}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				continueButton.setEnabled(false);
			}
		});
	}
	
	
	public void makeNeighborhood(int rootVertexIndex, int neighborhood, boolean wait, AdapterSet a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numVertices() <= rootVertexIndex) {
			throw new IllegalArgumentException("Vertex index out of range in makeNeighborhood()");
		}
		if (!displayProgress.isSelected() && skipBreakpointsChecker.isSelected()) {
			return;
		}
		setData(hds);
		DebugFactory.makeNeighborhood(
			hds.getVertex(rootVertexIndex), 
			neighborhood, 
			showVertices, 
			showEdges, 
			showFaces, 
			moddeller, 
			a
		);
		updateSceneOnEventThread();
		adapters = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	public void makeTutte(int boundFaceIndex, boolean wait, AdapterSet a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numFaces() <= boundFaceIndex) {
			throw new IllegalArgumentException("Face index out of range in makeTutte()");
		}
		setData(hds);
//		DebugFactory.makeTutte(
//			hds, 
//			hds.getFace(boundFaceIndex), 			
//			showVertices, 
//			showEdges, 
//			showFaces, 
//			moddeller,
//			a
//		);
		updateSceneOnEventThread();
		adapters = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	
	public void makeVertexCloseUp(int vertexIndex, boolean wait, AdapterSet a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numVertices() <= vertexIndex) {
			throw new IllegalArgumentException("Vertex index out of range in makeVertexCloseUp()");
		}
		setData(hds);
		DebugFactory.makeVertexCloseUp(hds.getVertex(vertexIndex), moddeller, a);
		updateSceneOnEventThread();
		adapters = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	public void makeEdgeCloseUp(int edgeIndex, boolean wait, AdapterSet a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numEdges() <= edgeIndex) {
			throw new IllegalArgumentException("Edge index out of range in makeEdgeCloseUp()");
		}
		setData(hds);
		DebugFactory.makeEdgeCloseUp(hds.getEdge(edgeIndex), moddeller, a);
		updateSceneOnEventThread();
		adapters = a; 
		if (wait) {
			parkInvokeThread();
		}
	}
	
	public void makeFaceCloseUp(int faceIndex, boolean wait, AdapterSet a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numFaces() <= faceIndex) {
			throw new IllegalArgumentException("Face index out of range in makeFaceCloseUp()");
		}
		setData(hds);
		DebugFactory.makeFaceCloseUp(hds.getFace(faceIndex), moddeller, a);
		updateSceneOnEventThread();
		adapters = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setData(HDS hds) {
		this.hds = hds;
		vertexSpinner.removeChangeListener(this);
		edgeSpinner.removeChangeListener(this);
		faceSpinner.removeChangeListener(this);
		vertexNumberModel.setMaximum(hds.numVertices() - 1);
		edgeNumberModel.setMaximum(hds.numEdges() - 1);
		faceNumberModel.setMaximum(hds.numFaces() - 1);
		vertexSpinner.addChangeListener(this);
		edgeSpinner.addChangeListener(this);
		faceSpinner.addChangeListener(this);
		dataLabel.setText("HDS: V" + hds.numVertices() + " E" + hds.numEdges() + " F" + hds.numFaces());
		dataLabel.repaint();
		moddeller.getViewer().getRoot().removeAllChildren();
		moddeller.getViewer().repaint();
	}
	
	
	private void updateSceneOnEventThread() {
		Runnable updater = new Runnable() {
			public void run() {
				viewer.encompass(viewer.getBounds2D());
			}
		};
		if (SwingUtilities.isEventDispatchThread()) {
			updater.run();
		} else {
			SwingUtilities.invokeLater(updater);
		}
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("Halfedge Debugger", "Stefan Sechelmann");
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hcp = c.getPlugin(HalfedgeInterface.class);
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "showVertices", showVertices);
		c.storeProperty(getClass(), "showEdges", showEdges);
		c.storeProperty(getClass(), "showFaces", showFaces);
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		showVertices = c.getProperty(getClass(), "showVertices", showVertices);
		showEdges = c.getProperty(getClass(), "showEdges", showEdges);
		showFaces = c.getProperty(getClass(), "showFaces", showFaces);
	}

	public void setShowVertices(boolean showVertices) {
		this.showVertices = showVertices;
	}
	
	public void setShowEdges(boolean showEdges) {
		this.showEdges = showEdges;
	}
	
	public void setShowFaces(boolean showFaces) {
		this.showFaces = showFaces;
	}
	
}
