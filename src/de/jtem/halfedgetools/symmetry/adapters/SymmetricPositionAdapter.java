/**
 * 
 */
package de.jtem.halfedgetools.symmetry.adapters;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;

@Position
public class SymmetricPositionAdapter extends AbstractAdapter<double[]> {

	public SymmetricPositionAdapter() {
		super(double[].class, true, true);
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return SymmetricVertex.class.isAssignableFrom(nodeClass);
	}
	
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		SymmetricVertex<?,?,?> sv = (SymmetricVertex<?,?,?>)v;
		return sv.getEmbedding();
	}

	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, double[] value, AdapterSet a) {
		SymmetricVertex<?,?,?> sv = (SymmetricVertex<?,?,?>)v;
		sv.setEmbedding(value);
	}
	
}