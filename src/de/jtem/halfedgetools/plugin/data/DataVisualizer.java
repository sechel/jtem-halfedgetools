package de.jtem.halfedgetools.plugin.data;

import javax.swing.JPanel;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class DataVisualizer extends Plugin {

	public JPanel getUserInterface() {
		return null;
	}
	
	public boolean canVisualize(Adapter<?> a) {
		return false;
	}
	
	public abstract void visualize(Adapter<?> a, HalfedgeInterface hi);
	
}
