package de.jtem.halfedgetools.jreality.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Color;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public class GeometryUtility {

	public static <
		V extends Vertex<?,?,?>
	> PointSet createVertices(Collection<V> vList, AdapterSet a, boolean useColors) {
		if (vList.size() == 0) return new PointSet();
		int index = 0;
		double[][] vertexVerts = new double[vList.size()][];
		double[][] vertexColors = null;
		if (useColors) {
			vertexColors = new double[vList.size()][];
		}
		for (Vertex<?,?,?> v : vList) {
			double[] pos = a.getD(Position3d.class, v);
			vertexVerts[index++] = pos;
			if (vertexColors != null) {
				double[] color = a.getD(Color.class, v);
				vertexColors[index - 1] = color;
			}
		}
		PointSetFactory psf = new PointSetFactory();
		psf.setVertexCount(vertexVerts.length);
		psf.setVertexCoordinates(vertexVerts);
		if (vertexColors != null) {
			psf.setVertexColors(vertexColors);
		}
		psf.update();
		return psf.getPointSet();
	}
	

	public static <
		E extends Edge<?,?,?>
	> IndexedLineSet createEdges(Collection<E> eList, AdapterSet a, boolean useColors) {
		Set<E> drawSet = new HashSet<E>();
		for (E e : eList) {
			if (drawSet.contains(e.getOppositeEdge())) {
				continue;
			} else {
				drawSet.add(e);
			}
		}
		double[][] edgeVerts = new double[drawSet.size() * 2][];
		int[][] edgeIndices = new int[drawSet.size()][2];
		double[][] edgeColors = null;
		if (useColors) {
			edgeColors = new double[drawSet.size()][];
		}
		int index = 0;
		for (Edge<?,?,?> e : drawSet) {
			Vertex<?,?,?> s = e.getStartVertex();
			Vertex<?,?,?> t = e.getTargetVertex();
			double[] sp = a.getD(Position3d.class, s);
			double[] tp = a.getD(Position3d.class, t);
			edgeVerts[index++] = sp;
			edgeVerts[index++] = tp;
			edgeIndices[index/2 - 1][0] = index - 2;
			edgeIndices[index/2 - 1][1] = index - 1;
			if (edgeColors != null) {
				double[] color = a.getD(Color.class, e);
				edgeColors[index/2 - 1] = color; 
			}
		}
		IndexedLineSetFactory lsf = new IndexedLineSetFactory();
		lsf.setVertexCount(index);
		lsf.setEdgeCount(index / 2);
		lsf.setVertexCoordinates(edgeVerts);
		lsf.setEdgeIndices(edgeIndices);
		if (edgeColors != null) {
			lsf.setEdgeColors(edgeColors);
		}
		lsf.update();
		return lsf.getIndexedLineSet();
	}
	
	public static <
		F extends Face<?,?,?>
	> IndexedFaceSet createOffsetFaces(Collection<F> fList, AdapterSet a, double offset, boolean useColors) {
		double[][] faceVerts = null;
		int[][] faceIndices = new int[fList.size() * 2][];
		double[][] faceColors = null;
		if (useColors) {
			faceColors = new double[fList.size() * 2][];
		}
		int index = 0;
		int vIndex = 0;
		List<double[]> vList = new LinkedList<double[]>();
		for (Face<?,?,?> f : fList) {
			Edge<?,?,?> b0 = f.getBoundaryEdge();
			double[] v1 = a.getD(Position3d.class, b0.getStartVertex());
			double[] v2 = a.getD(Position3d.class, b0.getTargetVertex());
			double dist = Rn.euclideanDistance(v1, v2);
			Edge<?,?,?> b = b0;
			List<double[]> fvList = new LinkedList<double[]>();
			do {
				double[] s1 = a.getD(Position3d.class, b.getStartVertex());
				double[] s2 = a.getD(Position3d.class, b.getNextEdge().getTargetVertex());
				double[] t = a.getD(Position3d.class, b.getTargetVertex());
				if (s1.length > 3) {
					Pn.dehomogenize(s1, s1);
					Pn.dehomogenize(s2, s2);
					Pn.dehomogenize(t, t);
				}
				double[] vec1 = Rn.subtract(null, s1, t);
				double[] vec2 = Rn.subtract(null, s2, t);
				double[] n = Rn.crossProduct(null, vec1, vec2);
				Rn.normalize(n, n);
				double[] offset1 = Rn.times(null, offset * dist, n);
				double[] offset2 = Rn.times(null, -1, offset1);
				double[] vert1 = Rn.add(null, t, offset1);
				double[] vert2 = Rn.add(null, t, offset2);
				if (vert1.length > 3) {
					vert1[3] = 1.0;
					vert2[3] = 1.0;
				}
				fvList.add(vert1);
				fvList.add(vert2);
				b = b.getNextEdge();
			} while (b != b0);
			int[] indices1 = new int[fvList.size() / 2];
			int[] indices2 = new int[fvList.size() / 2];
			faceIndices[index++] = indices1;
			faceIndices[index++] = indices2;
			for (int i = 0; i < fvList.size() / 2; i++) {
				indices1[i] = vIndex + i * 2;
				indices2[i] = vIndex + i * 2 + 1;
			}
			if (faceColors != null) {
				double[] color = a.getD(Color.class, f);
				faceColors[index - 1] = color;
				faceColors[index - 2] = color;
			}
			vList.addAll(fvList);
			vIndex += fvList.size();
		}
		faceVerts = vList.toArray(new double[][] {});
		IndexedFaceSetFactory fsf = new IndexedFaceSetFactory();
		fsf.setVertexCount(faceVerts.length);
		fsf.setFaceCount(faceIndices.length);
		fsf.setVertexCoordinates(faceVerts);
		fsf.setFaceIndices(faceIndices);
		if (faceColors != null) {
			fsf.setFaceColors(faceColors);
		}
		fsf.setGenerateFaceNormals(true);
		fsf.update();
		return fsf.getIndexedFaceSet();
	}
	
	
}
