package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.TRANSPARENCY;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.Position3d;

/**
 * @author Stefan Sechelmann, Andre Heydt 
 */

public class HalfedgeSelection {
	
	private Color defaultColor = Color.RED;

	private Map<Vertex<?, ?, ?>,Color>
		vMap = new HashMap<Vertex<?,?,?>,Color>();
	private Map<Edge<?, ?, ?>,Color>
		eMap = new HashMap<Edge<?,?,?>,Color>();
	private Map<Face<?, ?, ?>,Color>
		fMap = new HashMap<Face<?,?,?>,Color>();
	
	
	public HalfedgeSelection() {
	}
	
	public HalfedgeSelection(Collection<? extends Node<?,?,?>> nodes) {
		addAll(nodes);
	}
	
	public HalfedgeSelection(Node<?,?,?>... nodes) {
		this(Arrays.asList(nodes));
	}
	
	public HalfedgeSelection(HalfedgeSelection sel) {
		vMap = new HashMap<Vertex<?,?,?>,Color>(sel.vMap);
		eMap = new HashMap<Edge<?,?,?>,Color>(sel.eMap);
		fMap = new HashMap<Face<?,?,?>,Color>(sel.fMap);
	}	
	
	public Set<Vertex<?,?,?>> getVertices() {
		return Collections.unmodifiableSet(vMap.keySet());
	}
	public Set<Edge<?,?,?>> getEdges() {
		return Collections.unmodifiableSet(eMap.keySet());
	}
	public Set<Face<?,?,?>> getFaces() {
		return Collections.unmodifiableSet(fMap.keySet());
	}
	
	public Map<Vertex<?,?,?>,Color> getVertexMap() {
		return Collections.unmodifiableMap(vMap);
	}
	public Map<Edge<?,?,?>,Color> getEdgeMap() {
		return Collections.unmodifiableMap(eMap);
	}
	public Map<Face<?,?,?>,Color> getFaceMap() {
		return Collections.unmodifiableMap(fMap);
	}
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> Set<V> getVertices(HDS hds) {
		Set<V> result = new HashSet<V>();
		for (Vertex<?, ?, ?> v : vMap.keySet()) {
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
		for (Edge<?, ?, ?> e : eMap.keySet()) {
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
		for (Face<?, ?, ?> f : fMap.keySet()) {
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
		vMap.clear();
		eMap.clear();
		fMap.clear();
	}
	
	public boolean isSelected(Node<?,?,?> n) {
		return vMap.containsKey(n) | eMap.containsKey(n) | fMap.containsKey(n);
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected) {
		setSelected(n, selected, defaultColor);
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected, Color color) {
		if (selected == isSelected(n)) return;
		if (n instanceof Vertex<?,?,?>) {
			if (selected) {
				vMap.put((Vertex<?,?,?>)n, color);
			} else {
				vMap.remove(n);
			}
		}
		if (n instanceof Edge<?,?,?>) {
			if (selected) {
				eMap.put((Edge<?,?,?>)n, color);
			} else {
				eMap.remove(n);
			}
		}
		if (n instanceof Face<?,?,?>) {
			if (selected) {
				fMap.put((Face<?,?,?>)n, color);
			} else {
				fMap.remove(n);
			}
		}
	}
	
	
	public void add(Node<?,?,?> n) {
		add(n, defaultColor);
	}
	
	public void add(Node<?,?,?> n, Color color) {
		if (n instanceof Vertex<?,?,?>) {
			vMap.put((Vertex<?,?,?>)n, color);
		}
		if (n instanceof Edge<?,?,?>) {
			eMap.put((Edge<?,?,?>)n, color);
		}
		if (n instanceof Face<?,?,?>) {
			fMap.put((Face<?,?,?>)n, color);
		}
	}
	
	public void remove(Node<?,?,?> n) {
		if (n instanceof Vertex<?,?,?>) {
			vMap.remove(n);
		}
		if (n instanceof Edge<?,?,?>) {
			eMap.remove(n);
		}
		if (n instanceof Face<?,?,?>) {
			fMap.remove(n);
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
//		selectionAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
//		selectionAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
//		selectionAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		selectionAppearance.setAttribute(POLYGON_SHADER + "." + TRANSPARENCY, 0.1);
		edgeAppearance.setAttribute(VERTEX_DRAW, false);
		faceAppearance.setAttribute(VERTEX_DRAW, false);
		
		Map<Vertex<?, ?, ?>,Color> vMap = getVertexMap();
		Map<Edge<?, ?, ?>,Color> eMap = getEdgeMap();
		Map<Face<?, ?, ?>,Color> fMap = getFaceMap();
		
		SceneGraphComponent root = new SceneGraphComponent("Selection");
		if (vMap.size() > 0) {
			PointSet ps = createVertices(vMap, a);
			SceneGraphComponent pc = new SceneGraphComponent("Point Selection");
			pc.setGeometry(ps);
			root.addChild(pc);
		}
		if (eMap.size() > 0) {
			IndexedLineSet ils = createEdges(eMap, a);
			SceneGraphComponent ec = new SceneGraphComponent("Edge Selection");
			ec.setGeometry(ils);
			ec.setAppearance(edgeAppearance);
			root.addChild(ec);
		}
		if (fMap.size() > 0) {
			IndexedFaceSet ifs = createOffsetFaces(fMap, a, 0.01);
			SceneGraphComponent fc = new SceneGraphComponent("Face Selection");
			fc.setGeometry(ifs);
			fc.setAppearance(faceAppearance);
			root.addChild(fc);
		}		
		root.setAppearance(selectionAppearance);
		return root;
	}
	
	public static <
		V extends Vertex<?,?,?>
	> PointSet createVertices(Map<V,Color> vMap, AdapterSet a) {
		if (vMap.size() == 0) return new PointSet();
		int index = 0;
		double[][] vertexPos = new double[vMap.size()][];
		float[][] vColors = new float[vMap.size()][];
		double[][] vertexColors = new double[vMap.size()][];

		for (Vertex<?,?,?> v : vMap.keySet()) {
			double[] pos = a.getD(Position3d.class, v);
			vertexPos[index] = pos;
			Color vc = vMap.get(v);
			vColors[index] = vc.getRGBColorComponents(null);
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
	
	public static <
		E extends Edge<?,?,?>
	> IndexedLineSet createEdges(Map<E,Color> eMap, AdapterSet a) {
		Set<E> drawSet = new HashSet<E>();
		for (E e : eMap.keySet()) {
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
			Color ec = eMap.get(e);
			eColors[i] = ec.getRGBColorComponents(null);
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
	
	public static <
		F extends Face<?,?,?>
	> IndexedFaceSet createOffsetFaces(Map<F,Color> fMap, AdapterSet a, double offset) {
		double[][] faceVerts = null;
		int[][] faceIndices = new int[fMap.size() * 2][];
		float[][] fColors = new float[fMap.size()][3];
		double[][] faceColors = new double[fMap.size()*2][3];
		int index = 0;
		int vIndex = 0;
		int k=0;
		List<double[]> vList = new LinkedList<double[]>();
		for (Face<?,?,?> f : fMap.keySet()) {
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
			Color fc = fMap.get(f);
			fColors[k] = fc.getRGBColorComponents(null);
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
	
	public Color getColor(Node<?, ?, ?> n) {
		if (!this.isSelected(n)) throw new IllegalArgumentException(
				"Node: " + n.toString() + " not selected");
		if (n instanceof Vertex<?,?,?>) {
			return vMap.get((Vertex<?,?,?>)n);
		}
		if (n instanceof Edge<?,?,?>) {
			return eMap.get((Edge<?,?,?>)n);
		}
		if (n instanceof Face<?,?,?>) {
			return fMap.get((Face<?,?,?>)n);
		}
		System.out.println("Node: " + n.toString() + " not selected. Null returned!" );
		return null;
	}
	
	@Override
	public boolean equals(Object s) {
		if (!(s instanceof HalfedgeSelection)) return false;
		HalfedgeSelection s2 = (HalfedgeSelection)s;
		return vMap.equals(s2.vMap) && eMap.equals(s2.eMap) && fMap.equals(s2.fMap);
	}

	public Color getDefaultColor() {
		return defaultColor;
	}
}
