package de.jtem.halfedgetools.plugin.data;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.data.DataVisualizer.NodeType;

public interface DataVisualization {

	public Adapter<?> getSource();

	public DataVisualizer getVisualizer();

	public NodeType getType();
	
	public void update();
	public void remove();

}