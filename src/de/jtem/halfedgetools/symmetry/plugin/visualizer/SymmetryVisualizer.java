/**
 * 
 */
package de.jtem.halfedgetools.symmetry.plugin.visualizer;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.halfedgetools.symmetry.node.SHDS;
import de.jtem.jrworkspace.plugin.Controller;

/**
 * @author josefsso
 *
 */
public class SymmetryVisualizer extends VisualizerPlugin implements ChangeListener{

	private final SceneGraphComponent body = SceneGraphUtility.createFullSceneGraphComponent("body");
	

	public SymmetryVisualizer() {
		// TODO Auto-generated constructor stub
	}

	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void initVisualization(HDS hds, AdapterSet a, HalfedgeInterface hif) {
	
		if(hds.getClass().isAssignableFrom(SHDS.class)) {
			
		}
	}

	@Override
	public String getName() {
		return "Symmetry visualizer";
	}
	
	public SceneGraphComponent getComponent() {
		return body;
	}
	
	
	@Override
	public void stateChanged(ChangeEvent e) {
		manager.update();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
	}

}
