package de.jtem.halfedgetools.plugin;


public interface HalfedgeListener {

	public void dataChanged(HalfedgeLayer layer);
	
	public void adaptersChanged(HalfedgeLayer layer);
	
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active);
	
}
