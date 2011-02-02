package de.jtem.halfedgetools.nurbs;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.VectorField;

@VectorField
public class IndexedVectorField extends AbstractAdapter<double[]> {

	private Map<Integer,double[]>
		vectorMap = null;
	private String name = "-";
	
	public IndexedVectorField(String name, Map<Integer,double[]> vectorMap) {
		super(double[].class, true, false);
		this.name = name;
		this.vectorMap = new HashMap<Integer,double[]>(vectorMap);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return Vertex.class.isAssignableFrom(nodeClass);
	}

	
	@Override
	public <
		V extends Vertex<V,E,F>, 
		E extends Edge<V,E,F>, 
		F extends Face<V,E,F>
	> double[] getV(V v, AdapterSet a) {
		return vectorMap.get(v.getIndex());
	}

	@Override
	public String toString() {
		return name;
	}
}
