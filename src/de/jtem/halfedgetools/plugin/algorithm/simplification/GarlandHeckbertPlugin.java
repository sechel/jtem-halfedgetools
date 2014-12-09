package de.jtem.halfedgetools.plugin.algorithm.simplification;

import java.awt.EventQueue;

import javax.swing.JOptionPane;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.algorithm.simplification.GarlandHeckbert;
import de.jtem.halfedgetools.algorithm.triangulation.Triangulator;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class GarlandHeckbertPlugin extends AlgorithmPlugin {

	private Integer numVertices = null;
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) throws Exception {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				String numString = JOptionPane.showInputDialog(getOptionParent(), "Retain Vertices", 20);
				if (numString == null) {
					numVertices = null;
				} else {
					numVertices = Integer.parseInt(numString);
				}
			}
		};
		EventQueue.invokeAndWait(r);
		if (numVertices == null) { // cancel
			return;
		}
		int numSteps = hds.numVertices() - numVertices;
		if (numSteps <= 0) return;
		Triangulator.triangulateSingleSource(hds);
		GarlandHeckbert<V, E, F, HDS> gh = new GarlandHeckbert<V, E, F, HDS>(hds, a);
		gh.simplify(numSteps);
		hcp.set(hds);
	}

	@Override
	public String getAlgorithmName() {
		return "Garland & Heckbert";
	}

	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Editing;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Garland & Heckbert Algorithm", "Stefan Sechelmann, Kristoffer Josefsson");
		info.icon = ImageHook.getIcon("emoticon_smile.png");
		return info;
	}

}
