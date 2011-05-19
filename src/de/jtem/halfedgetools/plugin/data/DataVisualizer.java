package de.jtem.halfedgetools.plugin.data;

import javax.swing.Icon;
import javax.swing.JPanel;

import de.jtem.halfedgetools.adapter.Adapter;

public interface DataVisualizer {

	public static enum NodeType {Vertex, Edge, Face}
	
	public String getName();
	public Icon getIcon();
	
	public boolean canRead(Adapter<?> a, NodeType type);
	
	public JPanel connectUserInterfaceFor(DataVisualization visualization);
	public JPanel getDataDisplay();
	
	public DataVisualization createVisualization(NodeType type, Adapter<?> source);
	
}
