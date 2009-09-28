package de.jtem.halfedgetools.plugin;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;

public abstract class AnnotationAdapter<N extends Node<?, ?, ?>> {

	public abstract String getText(N n);
	
	
	public static abstract class VertexAnnotation <
		V extends Vertex<V, ?, ?>
	> extends AnnotationAdapter<V> {
		
	}
	
	public static abstract class EdgeAnnotation <
		E extends Edge<?, E, ?>
	> extends AnnotationAdapter<E> {
		
	}
	
	public static abstract class FaceAnnotation <
		F extends Face<?, ?, F>
	> extends AnnotationAdapter<F> {
		
	}
	
	public static class VertexIndexAnnotation <
		V extends Vertex<V, ?, ?>
	> extends VertexAnnotation<V> {
		@Override
		public String getText(V n) {
			return "" + n.getIndex();
		}
	}
	
	public static class EdgeIndexAnnotation <
		E extends Edge<?, E, ?>
	> extends EdgeAnnotation<E> {
		@Override
		public String getText(E n) {
			return "" + n.getIndex();
		}
	}
	
	public static class FaceIndexAnnotation <
		F extends Face<?, ?, F>
	> extends FaceAnnotation<F> {
		@Override
		public String getText(F n) {
			return "" + n.getIndex();
		};
	}
	
}
