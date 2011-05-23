package de.jtem.halfedgetools.plugin.data;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.DataVisualizer.NodeType;

public abstract class AbstractDataVisualization implements DataVisualization, Comparable<AbstractDataVisualization> {

	private Adapter<?> source = null;
	private DataVisualizer visualizer = null;
	private NodeType type = NodeType.Vertex;
	private HalfedgeLayer layer = null;
	private boolean active = true;
	
	public AbstractDataVisualization(
		HalfedgeLayer layer,
		Adapter<?> source, 
		DataVisualizer visualizer,
		NodeType type
	) {
		super();
		this.source = source;
		this.visualizer = visualizer;
		this.type = type;
		this.layer = layer;
	}

	@Override
	public Adapter<?> getSource() {
		return source;
	}

	@Override
	public DataVisualizer getVisualizer() {
		return visualizer;
	}

	@Override
	public NodeType getType() {
		return type;
	}
	
	@Override
	public HalfedgeLayer getLayer() {
		return layer;
	}
	
	@Override
	public String toString() {
		return type + " " + source.toString() + " " + visualizer.getName();
	}
	
	@Override
	public int compareTo(AbstractDataVisualization o) {
		return toString().compareTo(o.toString());
	}
	
	@Override
	public boolean isActive() {
		return active;
	}
	@Override
	public void setActive(boolean active) {
		this.active = active;
	}
	
}
