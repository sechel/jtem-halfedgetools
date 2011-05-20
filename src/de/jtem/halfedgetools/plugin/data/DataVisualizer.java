package de.jtem.halfedgetools.plugin.data;

import javax.swing.Icon;
import javax.swing.JPanel;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;

public interface DataVisualizer {

	public static enum NodeType {Vertex, Edge, Face}
	
	public String getName();
	public Icon getIcon();
	
	public boolean canRead(Adapter<?> a, NodeType type);
	
	public JPanel connectUserInterfaceFor(DataVisualization vis);
	public JPanel getDataDisplay();
	
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source);
	public void disposeVisualization(DataVisualization vis);
	
}
