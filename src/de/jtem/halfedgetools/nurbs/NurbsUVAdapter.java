package de.jtem.halfedgetools.nurbs;

import java.util.HashMap;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;

@NurbsUVCoordinate
public class NurbsUVAdapter extends AbstractAdapter<double[]> {

	Map<Integer,double[]>
		indexUVMap = null;
		
	public NurbsUVAdapter(Map<Integer,double[]> indexMap) {
		super(double[].class,true,false);
		indexUVMap = new HashMap<Integer, double[]>(indexMap);
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
		return indexUVMap.get(v.getIndex());
	}
}
