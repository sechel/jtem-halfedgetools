package de.jtem.halfedgetools.symmetry.adapters;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AbstractTypedAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Parameter;
import de.jtem.halfedgetools.adapter.type.BaryCenter;
import de.jtem.halfedgetools.symmetry.node.SEdge;
import de.jtem.halfedgetools.symmetry.node.SFace;
import de.jtem.halfedgetools.symmetry.node.SVertex;
import de.jtem.halfedgetools.symmetry.node.SymmetricEdge;

@BaryCenter
public class SymmetricBaryCenterAdapter extends AbstractTypedAdapter<SVertex, SEdge, SFace, double[]> {

	private double
		edgeAlpha = 0.5;
	private boolean 
		edgeIgnore = false;
	private SEdge
		faceRefEdge = null;
	
	public SymmetricBaryCenterAdapter() {
		super(SVertex.class, SEdge.class, SFace.class, double[].class, true, true);
	}
	
	@Override
	public double getPriority() {
		return 1;
	}
	
	@Override
	public double[] getEdgeValue(SEdge e, AdapterSet a) {
		return e.getEmbeddingOnEdge(edgeAlpha,edgeIgnore);
	}

	//TODO: Check method with getEmbeddingOnEdge
	@Override
	public double[] getVertexValue(SVertex v, AdapterSet a) {
		return v.getEmbedding();
	}

	@Override
	public void setVertexValue(SVertex v, double[] c, AdapterSet a) {
		v.setEmbedding(c);
	}
	
	@Override
	public double[] getFaceValue(SFace f, AdapterSet a) {
		if (faceRefEdge != null) {
			return get(f, faceRefEdge);
		}
		double[] pos = new double[3];
		List<SEdge> b = HalfEdgeUtils.boundaryEdges(f);
		for(int i = 0; i < b.size(); i++){
			Rn.add(pos, pos, f.getEmbeddingOnBoundary(i,false));
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}
	
	public double[] get(SFace f, SEdge e) {
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
		
		SymmetricEdge<?,?,?> se = e;
		if(se.isSymmetryHalfEdge()){
			faceIgnore = true;
		}
		
		double[] pos = new double[3];
		List<SEdge> b = HalfEdgeUtils.boundaryEdges(f);
		
		for(int i = 0; i < b.size(); i++){
			Rn.add(pos, pos, f.getEmbeddingOnBoundary(i,faceIgnore));
		}
		return Rn.times(pos, 1.0 / b.size(), pos);
	}

	@Parameter(name="alpha")
	public void setEdgeAlpha(double edgeAlpha) {
		this.edgeAlpha = edgeAlpha;
	}
	
	@Parameter(name="ignore")
	public void setEdgeIgnore(boolean ignore) {
		this.edgeIgnore = ignore;
	}
	
	@Parameter(name="refEdge")
	public void setFaceReferenceEdge(SEdge e) {
		this.faceRefEdge = e;
	}
	
}
