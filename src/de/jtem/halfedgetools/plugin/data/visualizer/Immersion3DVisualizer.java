package de.jtem.halfedgetools.plugin.data.visualizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.ui.LayoutFactory;
import de.jreality.ui.SimpleAppearanceInspector;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.generic.TexturePosition4d;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.util.HalfEdgeUtilsExtra;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class Immersion3DVisualizer <
	V extends Vertex<V, E, F>,
	E extends Edge<V, E, F>,
	F extends Face<V, E, F>,
	HDS extends HalfEdgeDataStructure<V, E, F>
> extends DataVisualizerPlugin implements ChangeListener {

	private JPanel 
		optionsPanel = new JPanel();
	private JCheckBox 
		showVertices = new JCheckBox("Show vertices"),
		showEdges = new JCheckBox("Show edges"),
		showFaces = new JCheckBox("Show faces");
	private Immersion3DVisualization 
		actVis = null;
	private boolean 
		listenersDisabled = false;
	private SimpleAppearanceInspector 
		appearanceInspector = new SimpleAppearanceInspector();
	
	public Immersion3DVisualizer() {
		initOptionPanel();
	}

	private void initOptionPanel() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();

		cl.gridx = 0;
		cl.gridy = 0;
		optionsPanel.add(showVertices, cl);
		cl.gridy = 1;
		optionsPanel.add(showEdges, cl);
		cl.gridy = 2;
		optionsPanel.add(showFaces, cl);

		showVertices.addChangeListener(this);
		showEdges.addChangeListener(this);
		showFaces.addChangeListener(this);
	}

	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		boolean accept = false;
		accept |= a.checkType(double[].class);
		return accept;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("immersion3D.png");
		return info;
	}

	@Override
	public String getName() {
		return "Immersion 3D";
	}

	@Override
	public DataVisualization createVisualization(
		HalfedgeLayer layer,
		NodeType type, 
		Adapter<?> source
	) {
		Immersion3DVisualization vis = new Immersion3DVisualization(layer, source, this, type);
		// copy last values
		layer.addTemporaryGeometry(vis.immersionComponent);
		return vis;
	}

	@Override
	public void disposeVisualization(DataVisualization vis) {
		@SuppressWarnings("unchecked")
		Immersion3DVisualization vfVis = (Immersion3DVisualization) vis;
		vis.getLayer().removeTemporaryGeometry(vfVis.immersionComponent);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		updateGeometry();
	}

	private void updateGeometry() {
		if (actVis == null || listenersDisabled) {
			return;
		}
		actVis.setShowVertices(showVertices.isSelected());
		actVis.setShowLines(showEdges.isSelected());
		actVis.setShowFaces(showFaces.isSelected());
		actVis.update();
	}

	public class Immersion3DVisualization extends AbstractDataVisualization {

		private SceneGraphComponent 
			immersionComponent = new SceneGraphComponent("Immersion 3D");
		private Appearance 
			immersionAppearance = new Appearance();

		private boolean 
			showVertices = true,
			showEdges = true,
			showFaces = false;

		public boolean isShowVertices() {
			return showVertices;
		}

		public void setShowVertices(boolean showVertices) {
			this.showVertices = showVertices;
		}

		public boolean isShowLines() {
			return showEdges;
		}

		public void setShowLines(boolean showLines) {
			this.showEdges = showLines;
		}

		public boolean isShowFaces() {
			return showFaces;
		}

		public void setShowFaces(boolean showFaces) {
			this.showFaces = showFaces;
		}

		public Immersion3DVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source, 
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
			appearanceInspector.setAppearance(immersionAppearance);
			immersionComponent.setAppearance(immersionAppearance);
		}

		@Override
		public void update() {
			if (!isActive()) {
				immersionComponent.setVisible(false);
				return;
			} else {
				immersionComponent.setVisible(true);
			}
			updateAppearance();

			@SuppressWarnings("unchecked")
			HDS hds = (HDS)getLayer().get();
			AdapterSet adapters = getLayer().getEffectiveAdapters();
			Adapter<?> imm = getSource();
			IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();

			Object val;
			double[][] p = null;
			double[][] t = null;
			int[][] ids = null;

			switch (getType()) {
			case Vertex:
				val = imm.get(hds.getVertex(0), adapters);
				if (!(val instanceof double[])) {
					throw new RuntimeException("No immersion!");
				}
				double[] tmp = (double[]) val;
				if (tmp.length > 4) {
					throw new RuntimeException("Value must be of dimension at most 4!");
				}
				// build up geometry
				ifsf.setVertexCount(hds.numVertices());
				ifsf.setFaceCount(hds.numFaces());

				p = new double[hds.numVertices()][];
				t = new double[hds.numVertices()][];
				ids = new int[hds.numFaces()][];

				for (V v : hds.getVertices()) {
					p[v.getIndex()] = (double[]) imm.get(v, adapters);
					t[v.getIndex()] = adapters.getD(TexturePosition4d.class, v);
				}
				for (F f : hds.getFaces()) {
					List<E> bd = HalfEdgeUtils.boundaryEdges(f);
					ids[f.getIndex()] = new int[bd.size()];
					for (int i = 0; i < bd.size(); i++) {
						ids[f.getIndex()][i] = bd.get(i).getStartVertex()
								.getIndex();
					}
				}
				break;
			case Edge:
				val = imm.get(hds.getEdge(0), adapters);
				if (!(val instanceof double[])) {
					throw new RuntimeException("Not an immersion!");
				}
				// build up geometry

				ifsf.setVertexCount(hds.numEdges());
				ifsf.setFaceCount(hds.numFaces() + hds.numVertices());

				p = new double[hds.numEdges()][];
				ids = new int[hds.numFaces() + hds.numVertices()][];

				for (E e : hds.getEdges()) {
					p[e.getIndex()] = (double[]) imm.get(e, adapters);
				}
				int nV = hds.numVertices();
				for (V v : hds.getVertices()) {
					List<E> star = HalfEdgeUtilsExtra.getEdgeStar(v);
					ids[v.getIndex()] = new int[star.size()];
					for (int i = 0; i < star.size(); i++) {
						ids[v.getIndex()][i] = star.get(i).getIndex();
					}
				}
				for (F f : hds.getFaces()) {
					List<E> bd = HalfEdgeUtils.boundaryEdges(f);
					ids[f.getIndex() + nV] = new int[bd.size()];
					for (int i = 0; i < bd.size(); i++) {
						ids[f.getIndex() + nV][i] = bd.get(i).getIndex();
					}
				}
				break;
			case Face:
				val = imm.get(hds.getFace(0), adapters);
				if (!(val instanceof double[])) {
					throw new RuntimeException("Not an immersion!");
				}
				// build up geometry
				int numBdVert = HalfEdgeUtils.boundaryVertices(hds).size();

				ifsf.setVertexCount(hds.numFaces());
				ifsf.setFaceCount(hds.numVertices() - numBdVert);

				p = new double[hds.numFaces()][];
				ids = new int[hds.numVertices() - numBdVert][];

				for (F f : hds.getFaces()) {
					p[f.getIndex()] = (double[]) imm.get(f, adapters);
				}
				int count = 0;
				for (int i = 0; i < hds.numVertices(); i++) {
					if (HalfEdgeUtils.isBoundaryVertex(hds.getVertex(i))) {
						count++;
					} else {
						List<E> star = HalfEdgeUtilsExtra.getEdgeStar(hds.getVertex(i));
						ids[i - count] = new int[star.size()];
						for (int j = 0; j < star.size(); j++) {
							ids[i - count][j] = (star.get(j)).getRightFace().getIndex();
						}
					}
				}
				break;
			default:
				ifsf = null;
				break;
			}
			if (ifsf != null) {
				ifsf.setVertexCoordinates(p);
				if(t != null) {
					ifsf.setVertexTextureCoordinates(t);
				}
				ifsf.setFaceIndices(ids);
				ifsf.setGenerateFaceNormals(true);
				ifsf.setGenerateEdgesFromFaces(true);
				ifsf.setGenerateVertexNormals(true);
				ifsf.update();
				immersionComponent.setGeometry(ifsf.getIndexedFaceSet());
			} else {
				immersionComponent.setGeometry(null);
			}
			immersionComponent.setName(getName());
			immersionComponent.setVisible(true);
			updateImmersionComponent();
		}

		private void updateImmersionComponent() {
			HalfedgeLayer layer = getLayer();
			layer.removeTemporaryGeometry(immersionComponent);
			layer.addTemporaryGeometry(immersionComponent);
		}

		private void updateAppearance() {
			immersionAppearance.setAttribute(CommonAttributes.VERTEX_DRAW, showVertices);
			immersionAppearance.setAttribute(CommonAttributes.EDGE_DRAW, showEdges);
			immersionAppearance.setAttribute(CommonAttributes.FACE_DRAW, showFaces);
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (Immersion3DVisualization) visualization;
		listenersDisabled = true;
		showVertices.setSelected(actVis.showVertices);
		showEdges.setSelected(actVis.showEdges);
		showFaces.setSelected(actVis.showFaces);
		actVis.updateAppearance();
		listenersDisabled = false;
		return appearanceInspector;
	}

}
