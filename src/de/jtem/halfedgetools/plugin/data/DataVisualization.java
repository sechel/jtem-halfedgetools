package de.jtem.halfedgetools.plugin.data;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.DataVisualizer.NodeType;

public interface DataVisualization {

	public Adapter<?> getSource();

	public DataVisualizer getVisualizer();
	
	public HalfedgeLayer getLayer();

	public NodeType getType();
	
	public void update();
	public boolean isActive();
	public void setActive(boolean active);

}