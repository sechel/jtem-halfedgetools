package de.jtem.halfedgetools.plugin.data;

import javax.swing.Icon;
import javax.swing.JPanel;

import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class DataVisualizerPlugin extends Plugin implements DataSourceProvider, DataVisualizer {

	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		return null;
	}

	@Override
	public abstract boolean canRead(Adapter<?> a, NodeType type);


	@Override
	public Icon getIcon() {
		return getPluginInfo().icon;
	}

	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet();
	}
	
	@Override
	public String getName() {
		return toString();
	}
	
}
