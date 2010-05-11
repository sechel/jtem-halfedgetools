package de.jtem.halfedgetools.symmetry.calculators;

import java.util.List;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.calculator.EdgeAverageCalculator;
import de.jtem.halfedgetools.algorithm.calculator.FaceBarycenterCalculator;
import de.jtem.halfedgetools.algorithm.calculator.VertexPositionCalculator;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;
import de.jtem.halfedgetools.symmetry.node.SymmetricFace;
import de.jtem.halfedgetools.symmetry.node.SymmetricVertex;

public class SymmetricSubdivisionCalculator implements EdgeAverageCalculator , VertexPositionCalculator, FaceBarycenterCalculator {

	private double
		edgeAlpha = 0.5;
	private boolean 
		edgeIgnore = false;
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(E e) {
		SymmetricEdge<?, ?, ?> je = (SymmetricEdge<?, ?, ?>)e;
		return je.getEmbeddingOnEdge(edgeAlpha,edgeIgnore);
	}

	//TODO: Check method with getEmbeddingOnEdge
	@Override
	public  <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(V v) {
		SymmetricVertex<?, ?, ?> jv = (SymmetricVertex<?, ?, ?>)v;
		return jv.getEmbedding();
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> void set(V v, double[] c) {
		SymmetricVertex<?, ?, ?> jv = (SymmetricVertex<?, ?, ?>)v;
		jv.setEmbedding(c);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(F f) {
		double[] pos = new double[3];
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		
		SymmetricFace<?, ?, ?> sf = (SymmetricFace<?, ?, ?>)f;
		for(int i = 0; i < b.size(); i++){
			Rn.add(pos, pos, sf.getEmbeddingOnBoundary(i,false));
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>
	> double[] get(
			F f, E e) {
		
		boolean faceIgnore = false;
		
//		List<E> edgeBoundary = new LinkedList<E>();
//		edgeBoundary.addAll(HalfEdgeUtils.boundaryEdges(e.getLeftFace()));
//		edgeBoundary.addAll(HalfEdgeUtils.boundaryEdges(e.getRightFace()));
//		edgeBoundary.remove(e);
//		edgeBoundary.remove(e.getOppositeEdge());

//		int nrOnCycle = 0;
//		
//		boolean isOn = false;
//		for(E ee : edgeBoundary){
//			SymmetricEdge<?,?,?> se = (SymmetricEdge<?,?,?>)ee;
//			if(se.isRightIncomingOfSymmetryCycle() != null){
//				isOn = true;
//			}
//		}
//		
//		if(!isOn){
//			for(E ee : edgeBoundary){
//				SymmetricVertex<?,?,?> sv = (SymmetricVertex<?,?,?> )ee.getTargetVertex();
//				if(sv.isSymmetryVertex()) {
//					nrOnCycle++;
//				}
//			}
//		}
//		
//		if(nrOnCycle == 1){
//			System.err.println("on");
//			faceIgnore = true;
//		}
		
		SymmetricEdge<?,?,?> se = (SymmetricEdge<?,?,?>)e;
		if(se.isSymmetryHalfEdge()){
			faceIgnore = true;
		}
		
		double[] pos = new double[3];
		List<E> b = HalfEdgeUtils.boundaryEdges(f);
		
		SymmetricFace<?, ?, ?> sf = (SymmetricFace<?, ?, ?>)f;
		for(int i = 0; i < b.size(); i++){
			Rn.add(pos, pos, sf.getEmbeddingOnBoundary(i,faceIgnore));
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}

	@Override
	public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
		boolean result = false;
		result |= SymmetricVertex.class.isAssignableFrom(nodeClass);
		result |= SymmetricEdge.class.isAssignableFrom(nodeClass);
		result |= SymmetricFace.class.isAssignableFrom(nodeClass);
		return result;
	}

	@Override
	public void setEdgeAlpha(double alpha) {
		this.edgeAlpha = alpha;
	}

	@Override
	public void setEdgeIgnore(boolean ignore) {
		this.edgeIgnore = ignore;
	}
}
