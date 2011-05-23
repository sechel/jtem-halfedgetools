package de.jtem.halfedgetools.plugin.data.source;

import static de.jreality.math.Rn.crossProduct;
import static de.jreality.math.Rn.determinant;
import static de.jreality.math.Rn.euclideanNorm;
import static de.jreality.math.Rn.subtract;
import static java.lang.Math.abs;

import java.util.Iterator;
import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;
import de.jtem.halfedgetools.plugin.data.DataSourceProvider;
import de.jtem.jrworkspace.plugin.Plugin;

public class FacePlanarityDataSource extends Plugin implements DataSourceProvider {

	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>
	> double getRelativeUnevenness(F f, AdapterSet ad) {
		List<V> boundary = HalfEdgeUtils.boundaryVertices(f);
		if (boundary.size() != 4) return 0.0;
		Iterator<V> vIt = boundary.iterator();
		double[] a = ad.getD(Position3d.class, vIt.next());
		double[] b = ad.getD(Position3d.class, vIt.next());
		double[] c = ad.getD(Position3d.class, vIt.next());
		double[] d = ad.getD(Position3d.class, vIt.next());
		double[] Mtetraeder = {c[0] - a[0], c[1] - a[1], c[2] - a[2], b[0] - a[0], b[1] - a[1], b[2] - a[2], d[0] - a[0], d[1] - a[1], d[2] - a[2]};
		double vol = determinant(Mtetraeder);
		double[][] point = {a,b,c,d};
		double maxHeight = 0.0;
		double meanLength = 0.0;
		int meanCount = 0;
		for (int i = 1; i <= 4; i++) {
			meanLength += Rn.euclideanDistance(point[i - 1], point[i % 4]);
			double[] v = crossProduct(null, subtract(null, point[i - 1], point[i % 4]), subtract(null, point[(i + 1) % 4], point[i % 4]));
			double area = euclideanNorm(v);
			if (area < 1E-5) continue;
			double offset = abs(vol / area);
			if (offset > maxHeight) {
				maxHeight = offset;
			}
			meanCount++;
		}
		meanLength /= meanCount;
		return maxHeight / meanLength;
	}
	
	
	private class FacePlanarityAdapter extends AbstractAdapter<Double> {
		
		public FacePlanarityAdapter() {
			super(Double.class, true, false);
		}

		@Override
		public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
			return Face.class.isAssignableFrom(nodeClass);
		}
		
		@Override
		public double getPriority() {
			return 0;
		}
		
		@Override
		public <
			V extends Vertex<V, E, F>,
			E extends Edge<V, E, F>,
			F extends Face<V, E, F>
		> Double getF(F f, AdapterSet a) {
			return getRelativeUnevenness(f, a) * 100;
		}
		
		@Override
		public String toString() {
			return "Planarity";
		}
		
	}
	
	@Override
	public AdapterSet getDataSources() {
		return new AdapterSet(new FacePlanarityAdapter());
	}

}
