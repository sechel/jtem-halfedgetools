package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.RADII_WORLD_COORDINATES;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.geometry.PointSetFactory;
import de.jreality.math.Rn;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ShaderUtility;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Position;
import de.jtem.jrworkspace.plugin.Controller;

public class SelectionVisualizer extends VisualizerPlugin implements SelectionListener {

	private SceneGraphComponent 
		root = new SceneGraphComponent("Selection");
	private SelectionInterface
		sif = null;
	private HalfedgeInterface
		hif = null;
	private Scene
		scene = null;
	private Appearance
		edgeAppearance = new Appearance("Edge Appearance"),
		faceAppearance = new Appearance("Face Appearance"),
		selectionAppearance = new Appearance("Selection Appearance");

	
	public SelectionVisualizer() {
		selectionAppearance.setAttribute(VERTEX_DRAW, true);
		selectionAppearance.setAttribute(EDGE_DRAW, true);
		selectionAppearance.setAttribute(FACE_DRAW, true);
		selectionAppearance.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		selectionAppearance.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		selectionAppearance.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		edgeAppearance.setAttribute(VERTEX_DRAW, false);
		faceAppearance.setAttribute(VERTEX_DRAW, false);
	}
	
	@Override
	public SceneGraphComponent getComponent() {
		return root;
	}
	
	
	@Override
	public String getName() {
		return "Halfedge Selection";
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		sif = c.getPlugin(SelectionInterface.class);
		hif = c.getPlugin(HalfedgeInterface.class);
		sif.addSelectionListener(this);
		scene = c.getPlugin(Scene.class);
	}
	
	public void update(HalfedgeSelection sel) {
		root.setAppearance(null);
		root = new SceneGraphComponent("Selection");
		AdapterSet a = hif.getAdapters();
		Set<Vertex<?, ?, ?>> vSet = sel.getVertices();
		Set<Edge<?, ?, ?>> eSet = sel.getEdges();
		Set<Face<?, ?, ?>> fSet = sel.getFaces();
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
					double[] vec1 = Rn.subtract(null, s1, t);
					double[] vec2 = Rn.subtract(null, s2, t);
					double[] n = Rn.crossProduct(null, vec1, vec2);
					Rn.normalize(n, n);
					double[] offset1 = Rn.times(null, dist / 100, n);
					double[] offset2 = Rn.times(null, -1, offset1);
					fvList.add(Rn.add(null, t, offset1));
					fvList.add(Rn.add(null, t, offset2));
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
			faceVerts = (double[][])vList.toArray(new double[][] {});
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
		updateAppearance();
		manager.update();
	}
	
	private void updateAppearance() {
		SceneGraphComponent sceneRoot = scene.getSceneRoot();
		SceneGraphComponent auxRoot = hif.getAuxComponent();
		List<SceneGraphPath> pathList = SceneGraphUtility.getPathsBetween(sceneRoot, auxRoot);
		if (pathList.size() == 0) return;
		EffectiveAppearance ea = EffectiveAppearance.create(pathList.get(0));
		DefaultGeometryShader dgs1 = ShaderUtility.createDefaultGeometryShader(ea);
		DefaultPointShader dps1 = (DefaultPointShader) dgs1.getPointShader();
		DefaultLineShader dls1 = (DefaultLineShader) dgs1.getLineShader();
		selectionAppearance.setAttribute(POINT_SHADER + "." + RADII_WORLD_COORDINATES, dps1.getRadiiWorldCoordinates());
		selectionAppearance.setAttribute(POINT_SHADER + "." + POINT_RADIUS, dps1.getPointRadius() * 1.1);
		selectionAppearance.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, dls1.getTubeRadius() * 1.1);
		selectionAppearance.setAttribute(LINE_SHADER + "." + RADII_WORLD_COORDINATES, dls1.getRadiiWorldCoordinates());
	}


	@Override
	public void selectionChanged(HalfedgeSelection sel, SelectionInterface sif) {
		update(sel);
	}
	
	public Appearance getSelectionAppearance() {
		return selectionAppearance;
	}

}
