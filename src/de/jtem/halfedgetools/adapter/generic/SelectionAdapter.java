package de.jtem.halfedgetools.adapter.generic;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Selection;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;

@Selection
public class SelectionAdapter extends AbstractAdapter<Boolean> {

	private HalfedgeInterface
		hif = null;
	
	public SelectionAdapter(HalfedgeInterface hif) {
		super(Boolean.class, true, true);
		this.hif = hif;
	}
	
	@Override
	public double getPriority() {
		return 0;
	}
	
	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		return true;
	}

	@Override
	public boolean checkType(Class<?> typeClass) {
		return Boolean.class == typeClass;
	}

	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Boolean getV(V v, AdapterSet a) {
		return hif.isSelected(v);
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Boolean getE(E e, AdapterSet a) {
		return hif.isSelected(e);
	}	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Boolean getF(F f, AdapterSet a) {
		return hif.isSelected(f);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, Boolean value, AdapterSet a) {
		hif.setSelected(v, value);
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, Boolean value, AdapterSet a) {
		hif.setSelected(e, value);
	}
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F f, Boolean value, AdapterSet a) {
		hif.setSelected(f, value);
	}
	
	@Override
	public String toString() {
		return "Selection";
	}
	
}
