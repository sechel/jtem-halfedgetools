package de.jtem.halfedgetools.selection;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Pn;
import de.jreality.math.Rn;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.PointSet;
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

public final class SelectionUtility {

	private SelectionUtility() {
	}

	public static IndexedFaceSet createOffsetFaces(FaceSelection fSel, AdapterSet a, double offset) {
			double[][] faceVerts = null;
			int[][] faceIndices = new int[fSel.size() * 2][];
			float[][] fColors = new float[fSel.size()][3];
			double[][] faceColors = new double[fSel.size()*2][3];
			int index = 0;
			int vIndex = 0;
			int k=0;
			List<double[]> vList = new LinkedList<double[]>();
			for (Face<?,?,?> f : fSel) {
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
				vList.addAll(fvList);
				vIndex += fvList.size();
	//			Color fc = fMap.get(f);
				fColors[k] = new float[]{1,0,0};//fc.getRGBColorComponents(null);
				double[] color = new double[]{(double)fColors[k][0],
						(double)fColors[k][1],(double)fColors[k][2]};
				faceColors[2*k] = color;
				faceColors[2*k+1] = color;
				k++;
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

	public static IndexedLineSet createEdges(EdgeSelection eSel, AdapterSet a) {
			Set<Edge<?,?,?>> drawSet = new HashSet<Edge<?,?,?>>();
			for (Edge<?,?,?> e : eSel) {
				if (drawSet.contains(e.getOppositeEdge())) {
					continue;
				} else {
					drawSet.add(e);
				}
			}
			double[][] edgeVerts = new double[drawSet.size() * 2][];
			int[][] edgeIndices = new int[drawSet.size()][2];
			float[][] eColors = new float[drawSet.size()][3];
			double[][] edgeColors = new double[drawSet.size()][3];
			int index = 0;
			int i=0;
			for (Edge<?,?,?> e : drawSet) {
				Vertex<?,?,?> s = e.getStartVertex();
				Vertex<?,?,?> t = e.getTargetVertex();
				double[] sp = a.getD(Position3d.class, s);
				double[] tp = a.getD(Position3d.class, t);
				edgeVerts[index++] = sp;
				edgeVerts[index++] = tp;
				edgeIndices[index/2 - 1][0] = index - 2;
				edgeIndices[index/2 - 1][1] = index - 1;
	//			Color ec = eMap.get(e);
				eColors[i] = new float[]{1,0,0};//ec.getRGBColorComponents(null);
				edgeColors[i] = new double[]{(double)eColors[i][0],
						(double)eColors[i][1],(double)eColors[i][2]};
				i++;
			}
			IndexedLineSetFactory lsf = new IndexedLineSetFactory();
			lsf.setVertexCount(index);
			lsf.setEdgeCount(index / 2);
			lsf.setVertexCoordinates(edgeVerts);
			lsf.setEdgeIndices(edgeIndices);
			lsf.setEdgeColors(edgeColors);
			lsf.update();
			return lsf.getIndexedLineSet();
		}

	public static PointSet createVertices(VertexSelection vSel, AdapterSet a) {
			if (vSel.size() == 0) return new PointSet();
			int index = 0;
			double[][] vertexPos = new double[vSel.size()][];
			float[][] vColors = new float[vSel.size()][];
			double[][] vertexColors = new double[vSel.size()][];
	
			for (Vertex<?,?,?> v : vSel) {
				double[] pos = a.getD(Position3d.class, v);
				vertexPos[index] = pos;
	//			Integer vc = vSel.get(v);
				vColors[index] = new float[]{1,0,0};//vc.getRGBColorComponents(null);
				vertexColors[index] = new double[]{(double)vColors[index][0],
						(double)vColors[index][1],(double)vColors[index][2]};
				index++;
			}
			PointSetFactory psf = new PointSetFactory();
			psf.setVertexCount(vertexPos.length);
			psf.setVertexCoordinates(vertexPos);
			psf.setVertexColors(vertexColors);
			psf.update();
			return psf.getPointSet();
		}

	public static SceneGraphComponent createSelectionGeometry(TypedSelection<Node<?,?,?>> sel, AdapterSet a) {
			Appearance edgeAppearance = new Appearance("Edge Appearance");
			Appearance faceAppearance = new Appearance("Face Appearance");
			Appearance selectionAppearance = new Appearance("Selection Appearance");
			selectionAppearance.setAttribute(VERTEX_DRAW, true);
			selectionAppearance.setAttribute(EDGE_DRAW, true);
			selectionAppearance.setAttribute(FACE_DRAW, true);
	//		selectionAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
	//		selectionAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
	//		selectionAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, Color.RED);
			selectionAppearance.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, 0.1);
			edgeAppearance.setAttribute(VERTEX_DRAW, false);
			faceAppearance.setAttribute(VERTEX_DRAW, false);
			
			VertexSelection vSel = sel.getVertices();
			EdgeSelection eSel = sel.getEdges();
			FaceSelection fSel = sel.getFaces();
			
			SceneGraphComponent root = new SceneGraphComponent("Selection");
			if (vSel.size() > 0) {
				PointSet ps = createVertices(vSel, a);
				SceneGraphComponent pc = new SceneGraphComponent("Point Selection");
				pc.setGeometry(ps);
				root.addChild(pc);
			}
			if (eSel.size() > 0) {
				IndexedLineSet ils = createEdges(eSel, a);
				SceneGraphComponent ec = new SceneGraphComponent("Edge Selection");
				ec.setGeometry(ils);
				ec.setAppearance(edgeAppearance);
				root.addChild(ec);
			}
			if (fSel.size() > 0) {
				IndexedFaceSet ifs = createOffsetFaces(fSel, a, 0.01);
				SceneGraphComponent fc = new SceneGraphComponent("Face Selection");
				fc.setGeometry(ifs);
				fc.setAppearance(faceAppearance);
				root.addChild(fc);
			}		
			root.setAppearance(selectionAppearance);
			return root;
		}
	
}
