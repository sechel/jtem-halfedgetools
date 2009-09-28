package de.jtem.halfedgetools.functional.alexandrov;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;


public interface AAdapters {

	public static interface Variable <V extends Vertex<V, ?, ?>> {
		public boolean isVariable(V v);
		public int getVarIndex(V v);
	}
	
	public static interface Alpha <E extends Edge<?, E, ?>> {
		public double getAlpha(E e);
		public void setAlpha(E e, double alpha);
	}
	
	public static interface Lambda <E extends Edge<?, E, ?>> {
		public double getLambda(E e);
		public void setLambda(E e, double lambda);
	}
	
	public static interface Theta <V extends Vertex<V, ?, ?>> {
		public double getTheta(V v);
	}
	
	public static interface InitialEnergy <F extends Face<?, ?, F>> {
		public double getInitialEnergy(F f);
	}
	
}
