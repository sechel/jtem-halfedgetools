package de.jtem.halfedgetools.plugin.visualizers;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.BallAndStickFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Rn;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Normal;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class NormalVisualizer extends VisualizerPlugin implements ChangeListener {

	private SceneGraphComponent 
		normalComponent = null;
	private SpinnerNumberModel
		lengthModel = new SpinnerNumberModel(1.0, -100.0, 100.0, 0.01);
	private JSpinner
		lengthSpinner = new JSpinner(lengthModel);
	private JPanel
		panel = new JPanel();
	

	public NormalVisualizer() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.RELATIVE;
		panel.add(new JLabel("Length"), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(lengthSpinner, c);
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		manager.update();
	}
	
	@Override
	public String getName() {
		return "Normal Visualizer";
	}
	
	@Override
	public JPanel getOptionPanel() {
		return panel;
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		IndexedLineSetFactory ilf = new IndexedLineSetFactory();
		int numPoints = hds.numVertices();
		if(numPoints == 0) {
			normalComponent = null;
			return;
		}
		ilf.setVertexCount(2*numPoints);
		ilf.setEdgeCount(numPoints);
		int[][] edges = new int[numPoints][2];
		double[][] vertices = new double[2*numPoints][];
		int i = 0;
		for (V v : hds.getVertices()) {
			edges[i] = new int[]{i,numPoints+i};
			double[] n = a.get(Normal.class, v, double[].class); 
			vertices[i] = a.get(Position.class, v, double[].class);
			double[] ln = Rn.times(null, lengthModel.getNumber().doubleValue(), n);
			vertices[i + numPoints] = Rn.add(null, vertices[i], ln);
			i++;
		}
		ilf.setEdgeIndices(edges);
		ilf.setVertexCoordinates(vertices);
		ilf.update();
		
		BallAndStickFactory bsf = new BallAndStickFactory(ilf.getIndexedLineSet());
		// bsf.setBallRadius(.04);
		bsf.setShowBalls(false);
        bsf.setStickRadius(.005);
        bsf.setShowArrows(true);
        bsf.setArrowScale(.02);
        bsf.setArrowSlope(1.5);
        bsf.setArrowPosition(1);
		bsf.update();
		normalComponent = bsf.getSceneGraphComponent(); 
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "normalLength", lengthModel.getNumber());
	}
	
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		lengthModel.setValue(c.getProperty(getClass(), "normalLength", lengthModel.getNumber()));
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		lengthSpinner.addChangeListener(this);
	}
	
	@Override
	public SceneGraphComponent getComponent() {
		return normalComponent;
	}

}
