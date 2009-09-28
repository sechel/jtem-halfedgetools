package de.jtem.halfedgetools.plugin;

import static de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType.VERTEX_ADAPTER;
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
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.halfedgetools.plugin.AnnotationAdapter.EdgeIndexAnnotation;
import de.jtem.halfedgetools.plugin.AnnotationAdapter.FaceIndexAnnotation;
import de.jtem.halfedgetools.plugin.AnnotationAdapter.VertexIndexAnnotation;
import de.jtem.java2dx.beans.Viewer2DWithInspector;
import de.jtem.java2dx.modelling.GraphicsModeller2D;
import de.jtem.java2dx.modelling.SimpleModeller2D;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class HalfedgeDebuggerPlugin <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> extends ShrinkPanelPlugin implements ActionListener, ChangeListener {

	private HalfedgeConnectorPlugin
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
	
	private HalfEdgeDataStructure<V, E, F>
		hds = null;
	private AnnotationAdapter<?>[]
	    defaultAnnotators = {
			new VertexIndexAnnotation<V>(), 
			new EdgeIndexAnnotation<E>(), 
			new FaceIndexAnnotation<F>()
		},
		lastAnnotators = defaultAnnotators;

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
	
	
	@SuppressWarnings("unchecked")
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (getGeometryButton == s) {
			hds = (HalfEdgeDataStructure<V, E, F>) new HalfEdgeDataStructure<StandardVertex, StandardEdge, StandardFace>(StandardVertex.class, StandardEdge.class, StandardFace.class);
			hcp.getHalfedgeContent(hds, new StandardCoordinateAdapter(VERTEX_ADAPTER));
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
			makeTutte(0, false, lastAnnotators);
		}
	}
	
	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();
		if (vertexSpinner == s) {
			makeVertexCloseUp(vertexNumberModel.getNumber().intValue(), false, lastAnnotators);
		}
		if (edgeSpinner == s) {
			makeEdgeCloseUp(edgeNumberModel.getNumber().intValue(), false, lastAnnotators);
		}
		if (faceSpinner == s) {
			makeFaceCloseUp(faceNumberModel.getNumber().intValue(), false, lastAnnotators);
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
	
	
	public void makeNeighborhood(int rootVertexIndex, int neighborhood, boolean wait, AnnotationAdapter<?>... a) {
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
		lastAnnotators = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	public void makeTutte(int boundFaceIndex, boolean wait, AnnotationAdapter<?>... a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numFaces() <= boundFaceIndex) {
			throw new IllegalArgumentException("Face index out of range in makeTutte()");
		}
		setData(hds);
		DebugFactory.makeTutte(
			hds, 
			hds.getFace(boundFaceIndex), 			
			showVertices, 
			showEdges, 
			showFaces, 
			moddeller,
			a
		);
		updateSceneOnEventThread();
		lastAnnotators = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	
	public void makeVertexCloseUp(int vertexIndex, boolean wait, AnnotationAdapter<?>... a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numVertices() <= vertexIndex) {
			throw new IllegalArgumentException("Vertex index out of range in makeVertexCloseUp()");
		}
		setData(hds);
		DebugFactory.makeVertexCloseUp(hds, hds.getVertex(vertexIndex), moddeller, a);
		updateSceneOnEventThread();
		lastAnnotators = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	public void makeEdgeCloseUp(int edgeIndex, boolean wait, AnnotationAdapter<?>... a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numEdges() <= edgeIndex) {
			throw new IllegalArgumentException("Edge index out of range in makeEdgeCloseUp()");
		}
		setData(hds);
		DebugFactory.makeEdgeCloseUp(hds, hds.getEdge(edgeIndex), moddeller, a);
		updateSceneOnEventThread();
		lastAnnotators = a; 
		if (wait) {
			parkInvokeThread();
		}
	}
	
	public void makeFaceCloseUp(int faceIndex, boolean wait, AnnotationAdapter<?>... a) {
		if (hds == null) {
			throw new IllegalArgumentException("No data structure set in HalfedgeDebuggetPlugin");
		}
		if (hds.numFaces() <= faceIndex) {
			throw new IllegalArgumentException("Face index out of range in makeFaceCloseUp()");
		}
		setData(hds);
		DebugFactory.makeFaceCloseUp(hds, hds.getFace(faceIndex), moddeller, a);
		updateSceneOnEventThread();
		lastAnnotators = a;
		if (wait) {
			parkInvokeThread();
		}
	}
	
	
	public void setData(HalfEdgeDataStructure<V, E, F> hds) {
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
		lastAnnotators = defaultAnnotators;
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
		hcp = c.getPlugin(HalfedgeConnectorPlugin.class);
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
