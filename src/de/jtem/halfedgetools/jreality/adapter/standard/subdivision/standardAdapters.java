package de.jtem.halfedgetools.jreality.adapter.standard.subdivision;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.Coord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionCoord3DAdapter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdge3DAdapter;
import de.jtem.halfedgetools.jreality.node.JREdge;
import de.jtem.halfedgetools.jreality.node.JRFace;
import de.jtem.halfedgetools.jreality.node.JRVertex;

public interface standardAdapters {
	final class StandardVAdapter <V extends JRVertex<V,?,?>> implements Coord3DAdapter<V> {
		public double[] getCoord(V v) {
			return v.position.clone();
		}
		public void setCoord(V v, double[] c) {
			v.position = c;
		}
	}
	
	final class StandardEAdapter  <E extends JREdge<?,E,?>> implements Coord3DAdapter<E> {
		public double[] getCoord(E e) {
			return e.getTargetVertex().position.clone();
		}
		public void setCoord(E e, double[] c) {
			e.getTargetVertex().position = c;
		}
	}
	
	final class StandardSubdivisionVAdapter <V extends JRVertex<V,?,?>> implements SubdivisionCoord3DAdapter<V> {
		public double[] getCoord(V v) {
			return v.position.clone();
		}
		public void setCoord(V v, double[] c) {
			v.position = c;
		}
	}
	
	final class StandardSubdivisionEAdapter <E extends JREdge<?,E,?>> implements SubdivisionEdge3DAdapter<E> {
		public double[] getCoord(E e, double a, boolean i) {
			return Rn.linearCombination(null, a, e.getTargetVertex().position.clone(), 1-a, e.getStartVertex().position.clone());
		}
	}
	
	final class StandardMedEAdapter <E extends JREdge<?,E,?>> implements Coord3DAdapter<E> {
		public double[] getCoord(E e) {
			return Rn.linearCombination(null, 0.5, e.getTargetVertex().position, 0.5, e.getStartVertex().position);
		}
		public void setCoord(E e, double[] c) {
			e.getTargetVertex().position = c;
		}
	}
	
	final class StandardFAdapter <F extends JRFace<?,E,F>, E extends JREdge<?,E,F>> implements Coord3DAdapter<F> {
		public double[] getCoord(F f) {
			double[] sum = {0, 0, 0};
			List<E> b = HalfEdgeUtils.boundaryEdges(f);
			int size = 0;
			for (E e : b) {
				Rn.add(sum, sum, e.getTargetVertex().position);
				size++;
			}
			Rn.times(sum, 1.0 / size, sum);
			return sum;
			
		}
		public void setCoord(F f, double[] c) {
		}
	}
}
