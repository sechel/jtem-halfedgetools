package de.jtem.halfedgetools.plugin;

import static de.jreality.shader.CommonAttributes.DIFFUSE_COLOR;
import static de.jreality.shader.CommonAttributes.EDGE_DRAW;
import static de.jreality.shader.CommonAttributes.FACE_DRAW;
import static de.jreality.shader.CommonAttributes.LINE_SHADER;
import static de.jreality.shader.CommonAttributes.POINT_RADIUS;
import static de.jreality.shader.CommonAttributes.POINT_SHADER;
import static de.jreality.shader.CommonAttributes.POLYGON_SHADER;
import static de.jreality.shader.CommonAttributes.SPHERES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBES_DRAW;
import static de.jreality.shader.CommonAttributes.TUBE_RADIUS;
import static de.jreality.shader.CommonAttributes.VERTEX_DRAW;
import static de.jreality.util.SceneGraphUtility.getPathsBetween;

import java.awt.Color;
import java.util.List;

import de.jreality.geometry.Primitives;
import de.jreality.math.FactoredMatrix;
import de.jreality.math.Matrix;
import de.jreality.math.MatrixBuilder;
import de.jreality.plugin.basic.Scene;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedLineSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.Transformation;
import de.jreality.scene.event.TransformationEvent;
import de.jreality.scene.event.TransformationListener;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class CoordinatesPivot extends Plugin implements TransformationListener {

	private HalfedgeInterface
		hif = null;
	private Scene
		scene = null;
//	private View
//		view = null;
	private Transformation
		pivotTransfom = new Transformation("Pivot Tranformation");
	private SceneGraphComponent
		pivotRoot = new SceneGraphComponent("Pivot"),
		pivot = Primitives.wireframeSphere();
	
	public CoordinatesPivot() {
		pivotRoot.addChild(pivot);
		pivotRoot.setTransformation(pivotTransfom);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
//		hif.getHalfedgeRoot().addChild(pivotRoot);
		scene = c.getPlugin(Scene.class);
//		scene.getContentComponent().getTransformation().addTransformationListener(this);
//		view = c.getPlugin(View.class);
	}
	
	@Override
	public void transformationMatrixChanged(TransformationEvent ev) {
		SceneGraphComponent root = scene.getSceneRoot();
//		SceneGraphPath camPath = scene.getCameraPath();
		List<SceneGraphPath> paths = getPathsBetween(root, pivotRoot);
		assert !paths.isEmpty();
		SceneGraphPath pivotPath = paths.get(0);
		pivotPath.pop();
		Matrix world = new Matrix(pivotPath.getInverseMatrix(null));
		FactoredMatrix fac = new FactoredMatrix(pivotPath.getMatrix(null));
		
//		Matrix P = new Matrix(getCameraToNDC(view.getViewer()));
//		Matrix C = new Matrix(camPath.getMatrix(null));
		Matrix T = new Matrix(world);
		T.multiplyOnRight(fac.getRotation());
//		T.multiplyOnLeft(C);
		pivotTransfom.setMatrix(T.getArray());
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		hif.getHalfedgeRoot().removeChild(pivotRoot);
		scene.getContentComponent().getTransformation().removeTransformationListener(this);
	}
	
	
	public static SceneGraphComponent createPivot() {
		SceneGraphComponent root = new SceneGraphComponent();
		IndexedLineSet arrow = Primitives.arrow(0, 0, 1, 0, 0.2);
		SceneGraphComponent xArrow = new SceneGraphComponent("xArrow");
		SceneGraphComponent yArrow = new SceneGraphComponent("yArrow");
		SceneGraphComponent zArrow = new SceneGraphComponent("zArrow");
		SceneGraphComponent box = new SceneGraphComponent("Box");
		xArrow.setGeometry(arrow);
		yArrow.setGeometry(arrow);
		zArrow.setGeometry(arrow);
		box.setGeometry(Primitives.cube());
		MatrixBuilder mb = MatrixBuilder.euclidean();
		mb.rotateZ(Math.PI / 2);
		mb.assignTo(yArrow);
		mb.rotateY(Math.PI / 2);
		mb.assignTo(zArrow);
		mb.scale(0.1);
		mb.assignTo(box);
		root.addChild(xArrow);
		root.addChild(yArrow);
		root.addChild(zArrow);
		root.addChild(box);
		Appearance app = new Appearance("Pivot Appearance");
		app.setAttribute(EDGE_DRAW, true);
		app.setAttribute(VERTEX_DRAW, true);
		app.setAttribute(LINE_SHADER + "." + TUBES_DRAW, true);
		app.setAttribute(LINE_SHADER + "." + TUBE_RADIUS, 0.02);
		app.setAttribute(POINT_SHADER + "." + SPHERES_DRAW, true);
		app.setAttribute(POINT_SHADER + "." + POINT_RADIUS, 0.02);
		root.setAppearance(app);
		Appearance xApp = new Appearance("X Appearance");
		xApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		xApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.RED);
		xArrow.setAppearance(xApp);
		Appearance yApp = new Appearance("Y Appearance");
		yApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.GREEN);
		yApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.GREEN);
		yArrow.setAppearance(yApp);
		Appearance zApp = new Appearance("Z Appearance");
		zApp.setAttribute(LINE_SHADER + "." + DIFFUSE_COLOR, Color.BLUE);
		zApp.setAttribute(POINT_SHADER + "." + DIFFUSE_COLOR, Color.BLUE);
		zArrow.setAppearance(zApp);
		Appearance boxApp = new Appearance("Box Appearance");
		boxApp.setAttribute(EDGE_DRAW, false);
		boxApp.setAttribute(VERTEX_DRAW, false);
		boxApp.setAttribute(FACE_DRAW, true);
		boxApp.setAttribute(POLYGON_SHADER + "." + DIFFUSE_COLOR, Color.YELLOW);
		box.setAppearance(boxApp);
		return root;
	}
	
	
	
	
}
