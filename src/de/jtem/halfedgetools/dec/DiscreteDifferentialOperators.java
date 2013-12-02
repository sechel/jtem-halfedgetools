package de.jtem.halfedgetools.dec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.CompColMatrix;
import no.uib.cipr.matrix.sparse.CompDiagMatrix;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import de.jreality.math.Rn;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.generic.TriangleVolumeAdapter;
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
				int ei = (int) adapters.get(EdgeIndex.class, e, Integer.class);
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
				Matrix bd = getBoundaryOperator(heds,adapters,0);
				Matrix d0 = new CompRowMatrix(bd.numColumns(),bd.numRows(),getColumnNonZeros(bd)); 
				bd.transpose(d0);
				return d0;
			}
			case 1:
			{
				Matrix bd = getBoundaryOperator(heds,adapters,1);
				Matrix d1 = new CompRowMatrix(bd.numColumns(),bd.numRows(),getColumnNonZeros(bd));
				bd.transpose(d1);
				return d1;
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
		switch (dim) {
			case -1:
			{
				return new CompDiagMatrix(1,heds.numVertices());
			}
			case 0:
			{
				return calculateCodifferential(heds, adapters, dim);
			}
			case 1:
			{
				return calculateCodifferential(heds, adapters, dim);
			}
			case 2:
			{
				return new CompDiagMatrix(heds.numFaces(),1);
			}
		}
 		return null; //unreachable
	}

	private static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix calculateCodifferential(HDS heds, AdapterSet adapters, int dim) {
		Matrix hs0inv = invertDiagonalMatrix(getHodgeStar(heds, adapters, dim));
		Matrix hs1 = getHodgeStar(heds, adapters, dim+1);
		Matrix bd = getBoundaryOperator(heds, adapters, dim);
		
		int[][] nz = getColumnNonZeros(bd);
		Matrix M = new CompColMatrix(bd.numRows(),bd.numColumns(),nz);
		M.set(bd);
		for(MatrixEntry me : M) {
			double val = me.get();
			me.set(-1.0*hs0inv.get(me.row(), me.row())*hs1.get(me.column(), me.column())*val);
		}
		return M;
	}

	private static int[][] getColumnNonZeros(Matrix A) {
		ArrayList< List<Integer> > nzList = new ArrayList<List<Integer>>(A.numColumns());
		for(int i = 0; i < A.numColumns(); ++i) {
			nzList.add(new LinkedList<Integer>());
		}
		for(MatrixEntry me : A) {
			int r = me.row(), c = me.column();
			nzList.get(c).add(r);
		}
		int[][] nz = new int[A.numColumns()][];
		int i = 0;
		for(List<Integer> cl : nzList) {
			nz[i] = new int[cl.size()];
			int j = 0;
			for(Integer mi : cl) {
				nz[i][j] = mi;
				++j;
			}
			++i;
		}
		return nz;
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
			return calculateLaplaceOperator(heds, adapters, dim);		
		case 1:
			return calculateLaplaceOperator(heds, adapters, dim);
		case 2:
			return calculateLaplaceOperator(heds, adapters, dim);
		}
		return M;
	}

	private static <
		V extends Vertex<V,E,F>,
		E extends Edge<V,E,F>,
		F extends Face<V,E,F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Matrix calculateLaplaceOperator(HDS heds, AdapterSet adapters, int dim) {
		Matrix cdk = getCoDifferential(heds, adapters, dim);
		Matrix dk = getDifferential(heds, adapters, dim);
		Matrix dk_1 = getDifferential(heds, adapters, dim-1);
		Matrix cdk_1 = getCoDifferential(heds, adapters, dim-1);
		
		int size = cdk.numRows();
		int[][] nz = new int[size][];
		switch (dim) {
			case 0: {
				for(V v : heds.getVertices()) {
					List<E> inEdges = HalfEdgeUtils.incomingEdges(v);
					nz[v.getIndex()] = new int[inEdges.size()];
					int i = 0;
					for(E e : inEdges) {
						nz[v.getIndex()][i++] = e.getStartVertex().getIndex();
					}
				}
				break;
			}	
			case 1: {
				for(E e : heds.getEdges()) {
					List<E> 
						tInEdges = HalfEdgeUtils.incomingEdges(e.getTargetVertex()),
						sInEdges = HalfEdgeUtils.incomingEdges(e.getStartVertex());
					int eIndex = (int)adapters.get(EdgeIndex.class, e, Integer.class);
					nz[eIndex] = new int[tInEdges.size()+sInEdges.size()-1];
					
					nz[eIndex][0] = eIndex;
					int i = 1;
					for(E ie : tInEdges) {
						Integer index = adapters.get(EdgeIndex.class, ie, Integer.class);
						if(index == eIndex) {
							continue;
						}
						nz[eIndex][i++] = index;
					}
					for(E ie : sInEdges) {
						Integer index = adapters.get(EdgeIndex.class, ie, Integer.class);
						if(index == eIndex) {
							continue;
						}
						nz[eIndex][i++] = index;
					}
				}
				break;
			}
			case 2: {
				for(F f : heds.getFaces()) {
					List<E> bdEdges = HalfEdgeUtils.boundaryEdges(f);
					nz[f.getIndex()] = new int[bdEdges.size()];
					int i = 0;
					for(E be : bdEdges) {
						F rightFace = be.getRightFace();
						if(rightFace == null) {
							continue;
						}
						nz[f.getIndex()][i++] = rightFace.getIndex();
					}
				}
				break;
			}
		}	
		Matrix M = new CompColMatrix(size,size,nz);
		
		// cdk*dk + dk_1*cdk_1
		cdk.mult(dk, M);
		return dk_1.multAdd(cdk_1, M);
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
				int j = (int) adapters.get(EdgeIndex.class,e,Integer.class);
				nz[j][0] = e.getStartVertex().getIndex();
				nz[j][1] = e.getTargetVertex().getIndex();
			}
			CompColMatrix d0 = new CompColMatrix(heds.numVertices(), heds.numEdges()/2, nz);
			for(E e : heds.getPositiveEdges()) {
				int j = (int)adapters.get(EdgeIndex.class,e,Integer.class);
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
					int ei = (int)adapters.get(EdgeIndex.class,e,Integer.class);
					nz[j][i] = ei;
					i++;
				}
			}
			CompColMatrix d1 = new CompColMatrix(heds.numEdges()/2,heds.numFaces(),nz);
			for(F f : heds.getFaces()) {
				int j = f.getIndex();
				E e = f.getBoundaryEdge();
				do {
					double val = e.isPositive()?1.0:-1.0;
					int ei = (int)adapters.get(EdgeIndex.class,e,Integer.class);
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
