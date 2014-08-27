package de.jtem.halfedgetools.adapter.generic;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Parameter;
import de.jtem.halfedgetools.adapter.type.TextureBaryCenter;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition3d;

@TextureBaryCenter
public class TextureBaryCenterAdapter extends AbstractAdapter<double[]> {

	private double
		edgeAlpha = 0.5;
	
	public TextureBaryCenterAdapter() {
		super(double[].class, true, false);
	}
	
	@Override
	public <T extends Node<?, ?, ?>> boolean canAccept(Class<T> nodeClass) {
		return true;
	}
	
	@Override
	public double getPriority() {
		return -1;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getV(V v, AdapterSet a) {
		return a.getDefault(TexturePosition3d.class, v, new double[] {0, 0, 0});
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getE(E e, AdapterSet a) {
		double[] s = getV(e.getStartVertex(), a);
		double[] t = getV(e.getTargetVertex(), a);
		return Rn.linearCombination(null, edgeAlpha, t, 1 - edgeAlpha, s);
	}	
	
	@Override
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double[] getF(F f, AdapterSet a) {
		double[] pos = new double[3];
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		for (E e : b) {
			double[] tmp = getV(e.getTargetVertex(), a);
			Rn.add(pos, pos, tmp);
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}
	
	@Parameter(name="alpha")
	public void setEdgeAlpha(double edgeAlpha) {
		this.edgeAlpha = edgeAlpha;
	}
	
	@Override
	public String toString() {
		return "Texture Barycenter";
	}
	
}
