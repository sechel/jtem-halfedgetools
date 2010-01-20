package de.jtem.halfedgetools.functional.edgelength;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.functional.DomainValue;
import de.jtem.halfedgetools.functional.Energy;
import de.jtem.halfedgetools.functional.Functional;
import de.jtem.halfedgetools.functional.Gradient;
import de.jtem.halfedgetools.functional.Hessian;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.Length;
import de.jtem.halfedgetools.functional.edgelength.EdgeLengthAdapters.WeightFunction;

public class EdgeLengthFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private Length<E>
		l0 = null;
	private WeightFunction<E> 
		w = null;
	
	public EdgeLengthFunctional(Length<E> l0, WeightFunction<E> w) {
		this.l0 = l0;
		this.w = w;
	}
	
	
	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> void evaluate(HDS hds,
			DomainValue x, Energy E, Gradient G, Hessian H) {
		if (E != null) {
			E.set(evaluate(hds, x));
		}
		if (G != null) {
			evaluateGradient(hds, x, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
	}

	@Override
	public <HDS extends HalfEdgeDataStructure<V, E, F>> int getDimension(HDS hds) {
		return hds.numVertices() * 3;
	}

	
	public double evaluate(
		HalfEdgeDataStructure<V, E, F> G,
		DomainValue x
	) {
		double[] s = new double[3];
		double[] t = new double[3];
		double[] smt = new double[3];
		double result = 0.0;
		for (E e : G.getPositiveEdges()) {
			double lsq = l0.getTargetLength(e) * l0.getTargetLength(e);
			getPosition(e.getStartVertex(), x, s);
			getPosition(e.getTargetVertex(), x, t);
			subtract(smt, s, t);
			double dot = innerProduct(smt, smt);
			double dif = dot - lsq;
			result += dif * dif * w.getWeight(e);
		}
		return result;
	}
	
	
	public void evaluateGradient(
		// input	
			HalfEdgeDataStructure<V, E, F> G,
			DomainValue x,
		// output
			Gradient grad
	) {
		grad.setZero();
		double[] vk = new double[3];
		double[] vj = new double[3];
		double[] smt = new double[3];
		for (V v : G.getVertices()) {
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				double lsq = l0.getTargetLength(e) * l0.getTargetLength(e);
				getPosition(v, x, vk);
				getPosition(e.getStartVertex(), x, vj);
				subtract(smt, vk, vj);
				double factor = innerProduct(smt, smt) - lsq;
				int off = v.getIndex() * 3;
				for (int d = 0; d < 3; d++) {
					grad.add(off + d, 4 * (vk[d] - vj[d]) * factor * w.getWeight(e));
				}
			}
		}
	}
	
	
	
	
	public void evaluateHessian(
		// input
			HalfEdgeDataStructure<V, E, F> G,
			DomainValue x,
		// output	
			Hessian H
	) {
		H.setZero();
		double[] vkPos = new double[3];
		double[] vjPos = new double[3];
		double[] smt = new double[3];
		for (V vk : G.getVertices()) {
			int koff = vk.getIndex() * 3;
			List<E> star = HalfEdgeUtils.incomingEdges(vk);
			for (E e : star) {
				double lsq = l0.getTargetLength(e) * l0.getTargetLength(e);
				V vj = e.getStartVertex();
				int joff = vj.getIndex() * 3;
				getPosition(vk, x, vkPos);
				getPosition(vj, x, vjPos);
				subtract(smt, vkPos, vjPos);
				double edgeContrib = innerProduct(smt, smt) - lsq;
				for (int d = 0; d < 3; d++) {
					double vContrib = vkPos[d] - vjPos[d];
					double vContribSq = 2 * vContrib * vContrib;
					double diag = 4 * (vContribSq + edgeContrib);
					H.add(koff + d, koff + d, diag * w.getWeight(e));
					H.add(koff + d, joff + d, -diag * w.getWeight(e));
					for (int d2 = 0; d2 < 3; d2++) {
						if (d2 == d) {
							continue;
						}
						double d2Contrib = vkPos[d2] - vjPos[d2];
						H.add(koff + d, koff + d2, 8 * d2Contrib * vContrib * w.getWeight(e));
						H.add(koff + d, joff + d2, -8 * d2Contrib * vContrib * w.getWeight(e));
					}
				}
			}
		}
	}
	
	
	private double innerProduct(double[] u, double[] v) {
		if (u.length != v.length) {
			if (Math.abs(u.length - v.length) != 1) {
				throw new IllegalArgumentException(
						"Vectors must have same length");
			}
		}
		double norm = 0.0;
		int n = u.length < v.length ? u.length : v.length;
		for (int i = 0; i < n; ++i) {
			norm += u[i] * v[i];
		}
		return norm;
	}

	private double[] subtract(double[] dst, double[] src1, double[] src2) {
		int n = Math.min(src1.length, src2.length);
		if (dst == null)
			dst = new double[n];
		if (dst.length > n) {
			throw new IllegalArgumentException("Invalid dimension for target");
		}
		for (int i = 0; i < dst.length; ++i) {
			dst[i] = src1[i] - src2[i];
		}
		return dst;
	}
	
	
	
	public <
		HDS extends HalfEdgeDataStructure<V, E, F>
	> int[][] getNonZeroPattern(HDS hds){
		int dim = hds.numVertices() * 3;
		Map<Integer, List<Integer>> nzMap = new HashMap<Integer, List<Integer>>();
		for (V v : hds.getVertices()) {
			for (E e : HalfEdgeUtils.incomingEdges(v)) {
				int off = v.getIndex() * 3;
				int off2 = e.getStartVertex().getIndex() * 3;
				for (int d = 0; d < 3; d++) {
					if (!nzMap.containsKey(off + d)) {
						nzMap.put(off + d, new LinkedList<Integer>());
					}
					List<Integer> nzList = nzMap.get(off + d);
					nzList.add(off + d);
					nzList.add(off2 + d);
					for (int d2 = 0; d2 < 3; d2++) {
						if (d2 == d) {
							continue;
						}
						nzList.add(off + d2);
						nzList.add(off2 + d2);
					}
				}
			}
		}
		int[][] nzPattern = new int[dim][];
		for (int i = 0; i < nzPattern.length; i++) {
			List<Integer> nzList = nzMap.get(i); 
			if (nzList == null) {
				nzPattern[i] = new int[0];
				continue;
			}
			nzPattern[i] = new int[nzList.size()];
			int j = 0;
			for (Integer nz : nzList) {
				nzPattern[i][j++] = nz;
			}
		}
		return nzPattern;
	}

	
	public void getPosition(V v, DomainValue x, double[] pos) {
		pos[0] = x.get(v.getIndex() * 3 + 0);
		pos[1] = x.get(v.getIndex() * 3 + 1);
		pos[2] = x.get(v.getIndex() * 3 + 2);
	}
	
	
    
    @Override
    public boolean hasHessian() {
    	return true;
    }
    
	
}
