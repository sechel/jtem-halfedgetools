package de.jtem.halfedgetools.plugin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Selection;

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

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Boolean getV(V v, AdapterSet a) {
		return hif.getSelection().isSelected(v);
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Boolean getE(E e, AdapterSet a) {
		return hif.getSelection().isSelected(e);
	}	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> Boolean getF(F f, AdapterSet a) {
		return hif.getSelection().isSelected(f);
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, Boolean value, AdapterSet a) {
		hif.getSelection().setSelected(v, value);
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E e, Boolean value, AdapterSet a) {
		hif.getSelection().setSelected(e, value);
	}
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F f, Boolean value, AdapterSet a) {
		hif.getSelection().setSelected(f, value);
	}
	
}
