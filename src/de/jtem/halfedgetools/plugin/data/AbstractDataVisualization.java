package de.jtem.halfedgetools.plugin.data;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.data.DataVisualizer.NodeType;

public abstract class AbstractDataVisualization implements DataVisualization, Comparable<AbstractDataVisualization> {

	private Adapter<?> source = null;
	private DataVisualizer visualizer = null;
	private NodeType type = NodeType.Vertex;
	
	public AbstractDataVisualization(
		Adapter<?> source, 
		DataVisualizer visualizer,
		NodeType type
	) {
		super();
		this.source = source;
		this.visualizer = visualizer;
		this.type = type;
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
	public String toString() {
		return type + " " + source.toString() + " " + visualizer.getName();
	}
	
	@Override
	public int compareTo(AbstractDataVisualization o) {
		return toString().compareTo(o.toString());
	}
	
}
