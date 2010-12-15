package de.jtem.halfedgetools.dec;

import java.util.Iterator;
import java.util.List;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.TriangleVolumeAdapter;
import de.jtem.halfedgetools.adapter.type.CircumCenter;
import de.jtem.halfedgetools.adapter.type.EdgeIndex;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.halfedgetools.adapter.type.Volume;

public class DiscreteDifferentialOperators 
{

	private final static double EPS = 1E-10;
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix getHodgeStar(HDS heds, AdapterSet adapters, int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No hodge star operators for dimension "+dim);
		}
		if(!adapters.contains(Position.class, heds.getVertexClass(), double[].class)) {
			throw new RuntimeException("Need adapter for position of vertices to calculate hodge star operator.");
		}
		
		switch (dim) {
		case -1:
			return new CompDiagMatrix(1,1);
		case 0: {
			Matrix s0 = new CompDiagMatrix(heds.numVertices(), heds.numVertices());
			for(V v : heds.getVertices()) {
				double[] vc = adapters.get(Position.class, v, double[].class);
				E e = v.getIncomingEdge();
				double volume = 0.0;
				do {
					double[]
					       ovc = adapters.get(Position.class, e.getStartVertex(), double[].class),
					       emc = Rn.linearCombination(null, 0.5, vc, 0.5, ovc);
					F face = e.getLeftFace();
					if(face != null) {
						double[] ccc = adapters.get(CircumCenter.class, face, double[].class);
						double 
							b = Rn.euclideanDistance(vc, emc),
							h = Rn.euclideanDistance(ccc, emc);
						volume += b*h;
					}
					face = e.getRightFace();
					if(face != null) {
						double[] ccc = adapters.get(CircumCenter.class, e.getRightFace(), double[].class);
						double
							b = Rn.euclideanDistance(vc, emc),
							h = Rn.euclideanDistance(ccc, emc);
						volume += b*h;
					}
					e = e.getNextEdge().getOppositeEdge();
				} while(e != v.getIncomingEdge());
				volume += EPS;
				s0.set(v.getIndex(),v.getIndex(),volume/2.0);
			}
			return s0;
		}
		case 1: {
			Matrix s1 = new CompDiagMatrix(heds.numEdges()/2,heds.numEdges()/2);
			for(E e : heds.getPositiveEdges()) {
				double dualVolume = 0.0;
				double[]
				       svc = adapters.get(Position.class, e.getStartVertex(), double[].class),
				       tvc = adapters.get(Position.class, e.getTargetVertex(), double[].class),
				       emc = Rn.linearCombination(null, 0.5, svc, 0.5, tvc);
				double primalVolume = Rn.euclideanDistance(svc, tvc) + EPS;;
				
				F face = e.getLeftFace();
				if(face != null) {
					double[] ccc = adapters.get(CircumCenter.class, face, double[].class);
					double h = Rn.euclideanDistance(ccc, emc);
					dualVolume += h;
				}
				face = e.getRightFace();
				if(face != null) {
					double[] ccc = adapters.get(CircumCenter.class, e.getRightFace(), double[].class);
					double h = Rn.euclideanDistance(ccc, emc);
					dualVolume += h;
				}
				dualVolume += EPS;
				int ei = adapters.get(EdgeIndex.class, e, Integer.class);
				s1.set(ei,ei,dualVolume/primalVolume);
			}
			return s1;
		}
		case 2: {
			Matrix s2 = new CompDiagMatrix(heds.numFaces(),heds.numFaces());
			for(F f : heds.getFaces()) {
				adapters.add(new TriangleVolumeAdapter());
				double volume = adapters.get(Volume.class, f, Double.class) + EPS;
				s2.set(f.getIndex(),f.getIndex(),1.0/volume);
			}
			return s2;
		}
		case 3: {
			return new CompDiagMatrix(1,1);
		}
		default: {
			return null;
		}
		}
	}
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix getDifferential(HDS heds, AdapterSet adapters, int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No differential for dimension "+dim);
		}
		switch (dim) {
			case -1:
				return new CompDiagMatrix(heds.numVertices(),1); 			
			case 0:
			{
				
				return getBoundaryOperator(heds,adapters,0).transpose();
			}
			case 1:
			{
				return getBoundaryOperator(heds,adapters,1).transpose();
			}
			case 2:
				return new CompDiagMatrix(1,heds.numFaces()); 
		}
		return null;
	}
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix getCoDifferential(HDS heds, AdapterSet adapters, int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No differential for dimension "+dim);
		}
		Matrix M = null;
		switch (dim) {
			case -1:
			{
				return new CompDiagMatrix(1,heds.numVertices());
			}
			case 0:
			{
				Matrix cD0 = new FlexCompColMatrix(heds.numVertices(),heds.numEdges()/2);
				calculateCodifferential(heds, adapters, dim, cD0);
				M = cD0;
				break;
			}
			case 1:
			{
				Matrix cD1 = new FlexCompColMatrix(heds.numEdges()/2,heds.numFaces());
				calculateCodifferential(heds, adapters, dim, cD1);
				M = cD1;
				break;
			}
			case 2:
			{
				return new CompDiagMatrix(heds.numFaces(),1);
			}
		}
 		return M;
	}

	private static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void calculateCodifferential(HDS heds, AdapterSet adapters, int dim, Matrix M) {
		Matrix hs0inv = invertDiagonalMatrix(getHodgeStar(heds, adapters, dim));
		Matrix hs1 = getHodgeStar(heds, adapters, dim+1);
		Matrix tmp = new FlexCompColMatrix(M);
		getBoundaryOperator(heds, adapters, dim).mult(-1.0, hs1, tmp);
		hs0inv.mult(tmp,M);
	}
	
	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix getLaplaceOperator(HDS heds, AdapterSet adapters, int dim) {
		if(dim < 0 || dim > 2) {
			throw new IllegalArgumentException("No laplacian for dimension "+dim);
		}
		Matrix M = null;
		switch (dim) {
		case 0:
			Matrix L0 = new FlexCompColMatrix(heds.numVertices(),heds.numVertices());
				calculateLaplaceOperator(heds, adapters, dim, L0);		
			M = L0;
			break;
		case 1:
			Matrix L1 = new FlexCompColMatrix(heds.numEdges()/2,heds.numEdges()/2);
			calculateLaplaceOperator(heds, adapters, dim, L1);
			M = L1;
			break;
		case 2:
			Matrix L2 = new FlexCompColMatrix(heds.numFaces(),heds.numFaces());
			calculateLaplaceOperator(heds, adapters, dim, L2);
			M = L2;
			break;
		}
		return M;
	}

	private static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void calculateLaplaceOperator(HDS heds, AdapterSet adapters, int dim, Matrix M) {
		getCoDifferential(heds, adapters, dim).mult(getDifferential(heds, adapters, dim), M);
		getDifferential(heds, adapters, dim-1).multAdd(getCoDifferential(heds, adapters, dim-1), M);
	}
	
	private static CompDiagMatrix invertDiagonalMatrix(Matrix m) {
		CompDiagMatrix minv = new CompDiagMatrix(m.numRows(),m.numColumns());
		Iterator<MatrixEntry> it = m.iterator();
		while(it.hasNext()) {
			MatrixEntry me = it.next();
			minv.set(me.row(),me.column(),1.0/me.get());
		}
		return minv;
	}

	public static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix getBoundaryOperator(HDS heds, AdapterSet adapters, int dim) {
		if(dim < -1 || dim > 2) {
			throw new IllegalArgumentException("No boundary operators for dimension "+dim);
		}
		switch (dim) {
		case -1: 
		{
			return new CompDiagMatrix(1,heds.numVertices());
		}
		case 0:
		{
			int[][] nz = new int[heds.numEdges()/2][2];
			for(E e : heds.getPositiveEdges()) {
				int j = adapters.get(EdgeIndex.class,e,Integer.class);
				nz[j][0] = e.getStartVertex().getIndex();
				nz[j][1] = e.getTargetVertex().getIndex();
			}
			Matrix d0 = new CompColMatrix(heds.numVertices(), heds.numEdges()/2, nz);
			for(E e : heds.getPositiveEdges()) {
				int j = adapters.get(EdgeIndex.class,e,Integer.class);
				d0.set(e.getStartVertex().getIndex(),j,-1.0);
				d0.set(e.getTargetVertex().getIndex(),j,1.0);
			}
			return d0;
		}
		case 1:
		{
			int[][] nz = new int[heds.numFaces()][];  
			for(F f : heds.getFaces()) {
				int j = f.getIndex();
				List<E> edges = HalfEdgeUtils.boundaryEdges(f);
				nz[j] = new int[edges.size()];
				int i = 0;
				for(E e : edges) {
					int ei = adapters.get(EdgeIndex.class,e,Integer.class);
					nz[j][i] = ei;
					i++;
				}
			}
			Matrix d1 = new CompColMatrix(heds.numEdges()/2,heds.numFaces(),nz);
			for(F f : heds.getFaces()) {
				int j = f.getIndex();
				E e = f.getBoundaryEdge();
				do {
					double val = e.isPositive()?1.0:-1.0;
					int ei = adapters.get(EdgeIndex.class,e,Integer.class);
					d1.set(ei,j,val);
					e = e.getNextEdge();
				} while(e != f.getBoundaryEdge());
			}
			return d1;
		}
		case 2:
			{
				return new CompDiagMatrix(heds.numFaces(),1); 
			}
		default:
			return null;
		}
	}
	
}
