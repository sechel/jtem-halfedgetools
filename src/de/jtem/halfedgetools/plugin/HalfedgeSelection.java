package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY_ENABLED;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import de.jreality.scene.SceneGraphComponent;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;

public class HalfedgeSelection {

	private Set<Vertex<?, ?, ?>>
		vSet = new HashSet<Vertex<?,?,?>>();
	private Set<Edge<?, ?, ?>>
		eSet = new HashSet<Edge<?,?,?>>();
	private Set<Face<?, ?, ?>>
		fSet = new HashSet<Face<?,?,?>>();
	
	
	public HalfedgeSelection() {
	}
	
	public HalfedgeSelection(Collection<? extends Node<?,?,?>> nodes) {
		addAll(nodes);
	}
	
	public HalfedgeSelection(Node<?,?,?>... nodes) {
		this(Arrays.asList(nodes));
	}
	
	public HalfedgeSelection(HalfedgeSelection sel) {
		vSet = new HashSet<Vertex<?,?,?>>(sel.vSet);
		eSet = new HashSet<Edge<?,?,?>>(sel.eSet);
		fSet = new HashSet<Face<?,?,?>>(sel.fSet);
	}
	
	
	public Set<Vertex<?,?,?>> getVertices() {
		return Collections.unmodifiableSet(vSet);
	}
	public Set<Edge<?,?,?>> getEdges() {
		return Collections.unmodifiableSet(eSet);
	}
	public Set<Face<?,?,?>> getFaces() {
		return Collections.unmodifiableSet(fSet);
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> getVertices(HDS hds) {
		Set<V> result = new HashSet<V>();
		for (Vertex<?, ?, ?> v : vSet) {
			if (v.getHalfEdgeDataStructure() == hds) {
				result.add((V)v);
			}
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<E> getEdges(HDS hds) {
		Set<E> result = new HashSet<E>();
		for (Edge<?, ?, ?> e : eSet) {
			if (e.getHalfEdgeDataStructure() == hds) {
				result.add((E)e);
			}
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<F> getFaces(HDS hds) {
		Set<F> result = new HashSet<F>();
		for (Face<?, ?, ?> f : fSet) {
			if (f.getHalfEdgeDataStructure() == hds) {
				result.add((F)f);
			}
		}
		return result;
	}
	
	public List<Node<?,?,?>> getNodes() {
		List<Node<?,?,?>> result = new LinkedList<Node<?,?,?>>();
		for (Vertex<?,?,?> v : getVertices()) {
			result.add(v);
		}
		for (Edge<?,?,?> e : getEdges()) {
			result.add(e);
		}
		for (Face<?,?,?> f : getFaces()) {
			result.add(f);
		}
		return result;
	}
	
	public void clear() {
		vSet.clear();
		eSet.clear();
		fSet.clear();
	}
	
	public boolean isSelected(Node<?,?,?> n) {
		return vSet.contains(n) | eSet.contains(n) | fSet.contains(n);
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected) {
		if (selected == isSelected(n)) return;
		if (n instanceof Vertex<?,?,?>) {
			if (selected) {
				vSet.add((Vertex<?,?,?>)n);
			} else {
				vSet.remove(n);
			}
		}
		if (n instanceof Edge<?,?,?>) {
			if (selected) {
				eSet.add((Edge<?,?,?>)n);
			} else {
				eSet.remove(n);
			}
		}
		if (n instanceof Face<?,?,?>) {
			if (selected) {
				fSet.add((Face<?,?,?>)n);
			} else {
				fSet.remove(n);
			}
		}
	}
	
	
	public void add(Node<?,?,?> n) {
		if (n instanceof Vertex<?,?,?>) {
			vSet.add((Vertex<?,?,?>)n);
		}
		if (n instanceof Edge<?,?,?>) {
			eSet.add((Edge<?,?,?>)n);
		}
		if (n instanceof Face<?,?,?>) {
			fSet.add((Face<?,?,?>)n);
		}
	}
	
	public void remove(Node<?,?,?> n) {
		if (n instanceof Vertex<?,?,?>) {
			vSet.remove(n);
		}
		if (n instanceof Edge<?,?,?>) {
			eSet.remove(n);
		}
		if (n instanceof Face<?,?,?>) {
			fSet.remove(n);
		}
	}
	
	public void addAll(Collection<? extends Node<?,?,?>> nodes) {
		for (Node<?,?,?> n : nodes) {
			add(n);
		}
	}
	
	public void removeAll(Collection<? extends Node<?,?,?>> nodes) {
		for (Node<?,?,?> n : nodes) {
			remove(n);
		}
	}
	
	
	public SceneGraphComponent createSelectionGeometry(AdapterSet a) {
		Appearance edgeAppearance = new Appearance("Edge Appearance");
		Appearance faceAppearance = new Appearance("Face Appearance");
		Appearance selectionAppearance = new Appearance("Selection Appearance");
		selectionAppearance.setAttribute(VERTEX_DRAW, true);
		selectionAppearance.setAttribute(EDGE_DRAW, true);
		selectionAppearance.setAttribute(FACE_DRAW, true);
		selectionAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		selectionAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		selectionAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		selectionAppearance.setAttribute(TRANSPARENCY_ENABLED, false);
		edgeAppearance.setAttribute(VERTEX_DRAW, false);
		faceAppearance.setAttribute(VERTEX_DRAW, false);
		
		
		SceneGraphComponent root = new SceneGraphComponent("Selection");
		Set<Vertex<?, ?, ?>> vSet = getVertices();
		Set<Edge<?, ?, ?>> eSet = getEdges();
		Set<Face<?, ?, ?>> fSet = getFaces();
		if (vSet.size() > 0) {
			int index = 0;
			double[][] vertexVerts = new double[vSet.size()][];
			for (Vertex<?,?,?> v : vSet) {
				double[] pos = a.get(Position.class, v, double[].class);
				vertexVerts[index++] = pos;
			}
			PointSetFactory psf = new PointSetFactory();
			psf.setVertexCount(vertexVerts.length);
			psf.setVertexCoordinates(vertexVerts);
			psf.update();
			SceneGraphComponent pc = new SceneGraphComponent("Point Selection");
			pc.setGeometry(psf.getGeometry());
			root.addChild(pc);
		}
		if (eSet.size() > 0) {
			double[][] edgeVerts = new double[eSet.size()][];
			int[][] edgeIndices = new int[eSet.size() / 2][2];
			int index = 0;
			for (Edge<?, ?, ?> e : eSet) {
				if (e.isPositive()) continue;
				Vertex<?,?,?> s = e.getStartVertex();
				Vertex<?,?,?> t = e.getTargetVertex();
				double[] sp = a.get(Position.class, s, double[].class);
				double[] tp = a.get(Position.class, t, double[].class);
				edgeVerts[index++] = sp;
				edgeVerts[index++] = tp;
				edgeIndices[index/2 - 1][0] = index - 2;
				edgeIndices[index/2 - 1][1] = index - 1;
			}
			IndexedLineSetFactory lsf = new IndexedLineSetFactory();
			lsf.setVertexCount(edgeVerts.length);
			lsf.setEdgeCount(edgeIndices.length);
			lsf.setVertexCoordinates(edgeVerts);
			lsf.setEdgeIndices(edgeIndices);
			lsf.update();
			SceneGraphComponent ec = new SceneGraphComponent("Edge Selection");
			ec.setGeometry(lsf.getGeometry());
			ec.setAppearance(edgeAppearance);
			root.addChild(ec);
		}
		if (fSet.size() > 0) {
			double[][] faceVerts = null;
			int[][] faceIndices = new int[fSet.size() * 2][];
			int index = 0;
			int vIndex = 0;
			List<double[]> vList = new LinkedList<double[]>();
			for (Face<?,?,?> f : fSet) {
				Edge<?, ?, ?> b0 = f.getBoundaryEdge();
				double[] v1 = a.get(Position.class, b0.getStartVertex(), double[].class);
				double[] v2 = a.get(Position.class, b0.getTargetVertex(), double[].class);
				double dist = Rn.euclideanDistance(v1, v2);
				Edge<?, ?, ?> b = b0;
				List<double[]> fvList = new LinkedList<double[]>();
				do {
					double[] s1 = a.get(Position.class, b.getStartVertex(), double[].class);
					double[] s2 = a.get(Position.class, b.getNextEdge().getTargetVertex(), double[].class);
					double[] t = a.get(Position.class, b.getTargetVertex(), double[].class);
					if (s1.length > 3) {
						Pn.dehomogenize(s1, s1);
						Pn.dehomogenize(s2, s2);
						Pn.dehomogenize(t, t);
					}
					double[] vec1 = Rn.subtract(null, s1, t);
					double[] vec2 = Rn.subtract(null, s2, t);
					double[] n = Rn.crossProduct(null, vec1, vec2);
					Rn.normalize(n, n);
					double[] offset1 = Rn.times(null, dist / 100, n);
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
			}
			faceVerts = vList.toArray(new double[][] {});
			IndexedFaceSetFactory fsf = new IndexedFaceSetFactory();
			fsf.setVertexCount(faceVerts.length);
			fsf.setFaceCount(faceIndices.length);
			fsf.setVertexCoordinates(faceVerts);
			fsf.setFaceIndices(faceIndices);
			fsf.setGenerateFaceNormals(true);
			fsf.update();
			SceneGraphComponent fc = new SceneGraphComponent("Face Selection");
			fc.setGeometry(fsf.getGeometry());
			fc.setAppearance(faceAppearance);
			root.addChild(fc);
		}		
		root.setAppearance(selectionAppearance);
		return root;
	}
	
}
