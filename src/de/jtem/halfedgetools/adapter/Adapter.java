package de.jtem.halfedgetools.adapter;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;


public interface Adapter<VAL> {
	  
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass);
	
	public boolean canInput(Class<?> typeClass); 
	
	public boolean canOutput(Class<?> typeClass);
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getV(V v, AdapterSet a);
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getE(E e, AdapterSet a);	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> VAL getF(F f, AdapterSet a);
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setV(V v, VAL value, AdapterSet a);
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setE(E v, VAL value, AdapterSet a);
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> void setF(F v, VAL value, AdapterSet a);
	
	
}
