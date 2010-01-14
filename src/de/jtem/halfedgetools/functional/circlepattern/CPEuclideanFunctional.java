package de.jtem.halfedgetools.functional.circlepattern;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.cosh;
import static java.lang.Math.sin;

import java.util.LinkedList;
import java.util.List;

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
import de.jtem.halfedgetools.functional.circlepattern.CPAdapters.Phi;
import de.jtem.halfedgetools.functional.circlepattern.CPAdapters.Theta;


/**
 * The functional to be minimized for koebes polyhedron
 * <p>
 * Copyright 2005 <a href="http://www.sechel.de">Stefan Sechelmann</a>
 * <a href="http://www.math.tu-berlin.de/geometrie">TU-Berlin</a> 
 * @author Stefan Sechelmann
 * @see koebe.KoebePolyhedron
 * @see <br><a href="http://opus.kobv.de/tuberlin/volltexte/2003/668/">Variational principles for circle patterns</a>
 */
public class CPEuclideanFunctional <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>
> implements Functional<V, E, F> {

	private Theta<E> 
		theta = null; 
	private Phi<F>
		phi = null;
	
	
	public CPEuclideanFunctional(Theta<E> theta, Phi<F> phi) {
		this.theta = theta;
		this.phi = phi;
	}
		
		
	public <
		HDS extends HalfEdgeDataStructure<V,E,F>
	> void evaluate(
		// input	
			HDS hds, 
			DomainValue x,
		// output	
			Energy E, 
			Gradient G, 
			Hessian H
	) {
		if (G != null || E != null) {
			evaluateEnergyAndGradient(hds, x, E, G);
		}
		if (H != null) {
			evaluateHessian(hds, x, H);
		}
	};
	
	
	public <
		HDS extends HalfEdgeDataStructure<V,E,F>
	> int getDimension(HDS hds) {
		return hds.numFaces();
	};
	
	
	public <
		HDS extends HalfEdgeDataStructure<V,E,F>
	> int[][] getNonZeroPattern(HDS hds) {
		int n = getDimension(hds);
		int[][] nz = new int[n][];
		for (F f : hds.getFaces()) {
			int i = f.getIndex();
			List<Integer> nzList = new LinkedList<Integer>();
			nzList.add(i);
			for (E e : HalfEdgeUtils.boundaryEdges(f)) {
				F neighbourFace = e.getRightFace();
				if (neighbourFace != null) {
					nzList.add(neighbourFace.getIndex());	
				}
			}
			nz[i] = new int[nzList.size()];
			int j = 0;
			for (Integer index : nzList) {
				nz[i][j++] = index;
			}
		}
		return nz;
	}
	

	
	private void evaluateHessian(
		// input	
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		// output
			Hessian H
	) {
		H.setZero();
	    for (E edge : hds.getPositiveEdges()) {
			if (!HalfEdgeUtils.isInteriorEdge(edge)) {
				continue;
			}
			F jFace = edge.getLeftFace();
			F kFace = edge.getOppositeEdge().getLeftFace();
			Integer j = jFace.getIndex();
			Integer k = kFace.getIndex();
			
			double rhok = x.get(kFace.getIndex());
			double rhoj = x.get(jFace.getIndex());
			if (k == 0) {
				rhok = 0.0;
			}
			if (j == 0) {
				rhoj = 0.0;
			}
			Double th = theta.getTheta(edge);
			Double hjk = sin(th) / (cosh(rhok - rhoj) - cos(th));

			if (j != 0) {
				H.add(j, j, hjk);
			}
			if (k != 0) {
				H.add(k, k, hjk);
			}
			if (j != 0 && k != 0) {
				H.add(j, k, -hjk);
				H.add(k, j, -hjk);
			}
		}
	}
	
	
	
	private void evaluateEnergyAndGradient(
		// input	
			HalfEdgeDataStructure<V, E, F> hds,
			DomainValue x,
		// output
			Energy E,
			Gradient G
	) {
		if (E != null) {
			E.setZero();
		}
		int n = getDimension(hds);
		double[] grad = new double[n];
		for (F face : hds.getFaces()) {
			if (face.getIndex() == 0) {
				continue;
			}
			double facePhi = phi.getPhi(face);
			if (E != null) {
				E.add(facePhi * x.get(face.getIndex()));
			}
			if (G != null) {
				grad[face.getIndex()] = facePhi;
			}
		}

		for (E edge : hds.getEdges()) {
			F leftFace = edge.getLeftFace();
			if (leftFace == null) {
				continue;
			}
			Double th = theta.getTheta(edge); 
			Double thStar = PI - th;
			Double leftRho = x.get(leftFace.getIndex());
			if (leftFace.getIndex() == 0) {
				leftRho = 0.0;
			}
			F rightFace = edge.getOppositeEdge().getLeftFace();
			if (rightFace == null) {
				// boundary face
				if (E != null) {
					E.add(-2 * thStar * leftRho);
				}
				if (G != null) {
					grad[leftFace.getIndex()] -= 2 * thStar;
				}
			} else {
				// interior face
				Double rightRho = x.get(rightFace.getIndex());
				if (rightFace.getIndex() == 0) {
					rightRho = 0.0;
				}
				Double diffRho = rightRho - leftRho;
				Double p = p(thStar, diffRho);
				if (E != null) {
					E.add(0.5 * p * diffRho);
					E.add(Clausen.clausen(thStar + p));
					E.add(-thStar * leftRho);
				}
				if (G != null) {
					grad[leftFace.getIndex()] -= p + thStar;
				}
			}
		}
		if (G != null) {
			for (int i = 0; i < n; i++) {
				G.set(i, grad[i]);
			}
			G.set(0, 0.0); // skip the first variable
		}
	}

	
    private Double p(Double thStar, Double diffRho) {
        Double exp = Math.exp(diffRho);
        Double tanhDiffRhoHalf = (exp - 1.0) / (exp + 1.0);
        return  2.0 * Math.atan(Math.tan(0.5 * thStar) * tanhDiffRhoHalf);
    }
    
    @Override
    public boolean hasHessian() {
    	return true;
    }
    
	
}
