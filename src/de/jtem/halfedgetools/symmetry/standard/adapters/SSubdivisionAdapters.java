package de.jtem.halfedgetools.symmetry.standard.adapters;

import java.util.List;

import de.jreality.math.Rn;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionEdgeInterpolator;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionFaceBarycenter;
import de.jtem.halfedgetools.algorithm.subdivision.adapters.SubdivisionVertexAdapter;
import de.jtem.halfedgetools.symmetry.standard.SEdge;
import de.jtem.halfedgetools.symmetry.standard.SFace;
import de.jtem.halfedgetools.symmetry.standard.SVertex;

public class SSubdivisionAdapters {

	public static class SSubdivisionVA implements SubdivisionVertexAdapter<SVertex> {

		public double[] getData(SVertex v) {
			return v.getEmbedding();
		}

		public void setData(SVertex v, double[] c) {
			v.setEmbedding(c);
		}
		
	}
	
	public static class SSubdivisionEA implements SubdivisionEdgeInterpolator<SEdge> {

		public double[] getData(SEdge e, double a, boolean ignore) {

			return e.getEmbeddingOnEdge(a, ignore);
			
		}
	}
		
	public static class SSubdivisionFA implements SubdivisionFaceBarycenter<SFace> {

//		public double[] getData(SFace f) {
//			double[] sum = {0, 0, 0};
//			List<SEdge> b = HalfEdgeUtils.boundaryEdges(f);
//			int size = 0;
//			for (SEdge e : b) {
//				Rn.add(sum, sum, e.getEmbeddingOnEdge(1,true));
//				size++;
//			}
//			Rn.times(sum, 1.0 / size, sum);
//			return sum;
//		}
		
		public double[] getData(SFace f) {
			double[] sum = {0, 0, 0};
			Rn.add(sum, sum, f.getEmbeddingOnBoundary(0));
			Rn.add(sum, sum, f.getEmbeddingOnBoundary(1));
			Rn.add(sum, sum, f.getEmbeddingOnBoundary(2));
			Rn.times(sum, 1.0/3.0, sum);
			return sum;
			
		}
		
	}
}
