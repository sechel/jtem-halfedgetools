package de.jtem.halfedgetools.plugin.algorithm.vectorfield;

import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
public abstract class AbstractVectorFielAdapter extends AbstractAdapter<double[]> {

	private Map<? extends Node<?, ?, ?>, double[]> 
		vecMap = null;
	private String
		name = "";
	
	public AbstractVectorFielAdapter(
		Map<? extends Node<?, ?, ?>, double[]> vecMap, 
		String name
	) {
		super(double[].class, true, false);
		this.vecMap = vecMap;
		this.name = name;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		N extends Node<V, E, F>
	> double[] get(N n, AdapterSet a) {
		return vecMap.get(n);
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
