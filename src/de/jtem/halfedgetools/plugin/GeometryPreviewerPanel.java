package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SMOOTH_SHADING;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Window;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.geometry.BoundingBoxUtility;
import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.content.ContentTools;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.Transformation;
import de.jreality.util.CameraUtility;
import de.jreality.util.Rectangle3D;
import de.jreality.util.SceneGraphUtility;

public class GeometryPreviewerPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 1L;
	
	private	JRViewer 
		viewer = new JRViewer(true);
	private Appearance
		contentApp = new Appearance(),
		rootApp = null;
	
	public GeometryPreviewerPanel() {
		setLayout(new GridLayout());
		setPreferredSize(new Dimension(300, 300));
		viewer.getController().setPropertyEngineEnabled(false);
		viewer.addContentSupport(ContentType.Raw);
		viewer.addContentUI();
		viewer.getController().setRegisterSPIPlugins(false);
		viewer.startupLocal();
		View view = viewer.getPlugin(View.class);
		Scene scene = viewer.getPlugin(Scene.class);
		rootApp = scene.getSceneRoot().getAppearance();
		add(view.getViewer().getViewingComponent());
		ContentTools tools = viewer.getPlugin(ContentTools.class);
		tools.setRotationEnabled(true);
		tools.setDragEnabled(false);
		tools.setEncompassEnabled(true);
		
		contentApp.setAttribute(VERTEX_DRAW, false);
		contentApp.setAttribute(EDGE_DRAW, false);
		contentApp.setAttribute(FACE_DRAW, true);
		contentApp.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, 0.07);
		contentApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, new Color(100, 200, 50));
		contentApp.setAttribute(POINT_SHADER + "." + POINT_RADIUS, 0.12);
		contentApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, new Color(200, 100, 50));
		contentApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, new Color(200, 200, 200));
		contentApp.setAttribute(POLYGON_SHADER + "." + SMOOTH_SHADING, false);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		rootApp.setAttribute("backgroundColor", getBackground());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		Window w = SwingUtilities.getWindowAncestor(this);
		String propertyName = evt.getPropertyName();
		if (propertyName.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
            File file = (File)evt.getNewValue();
            if (file == null || !file.exists()) {
                return;
            } 
			try {
				if (file.getName().toLowerCase().endsWith(".obj")) {
					ReaderOBJ reader = new ReaderOBJ();
					SceneGraphComponent c = reader.read(file);
					Geometry g = SceneGraphUtility.getFirstGeometry(c);
					if (g instanceof IndexedFaceSet) {
						IndexedFaceSetUtility.calculateAndSetFaceNormals((IndexedFaceSet)g);
					}
					Rectangle3D bbox = BoundingBoxUtility.calculateBoundingBox(c);
					MatrixBuilder mb = MatrixBuilder.euclidean();
					double maxExtend = bbox.getMaxExtent();		
					mb.scale(10 / maxExtend);
					Transformation normalizeTransform = new Transformation();
					normalizeTransform.setMatrix(mb.getArray());
					c.setTransformation(normalizeTransform);
					c.setAppearance(contentApp);
					
					Content content = JRViewerUtility.getContentPlugin(viewer.getController());
					content.setContent(c);
					View view = viewer.getPlugin(View.class);
					CameraUtility.encompass(view.getViewer());
				}
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(w, ex.getMessage(), ex.getClass().getSimpleName(), ERROR_MESSAGE);
				ex.printStackTrace();
			}
		}
	}
}
