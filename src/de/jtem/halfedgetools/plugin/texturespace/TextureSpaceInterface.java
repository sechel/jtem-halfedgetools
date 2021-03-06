package de.jtem.halfedgetools.plugin.texturespace;

import static de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode.StaticPropertiesFile;
import static java.lang.Math.toRadians;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.event.AppearanceEvent;
import de.jreality.scene.event.AppearanceListener;
import de.jreality.shader.EffectiveAppearance;
import de.jreality.shader.ImageData;
import de.jreality.ui.ColorChooseJButton;
import de.jreality.ui.ColorChooseJButton.ColorChangedEvent;
import de.jreality.ui.ColorChooseJButton.ColorChangedListener;
import de.jreality.ui.LayoutFactory;
import de.jreality.util.Input;
import de.jreality.util.LoggingSystem;
import de.jreality.util.SceneGraphUtility;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.MarqueeSelectionPlugin;
import de.jtem.halfedgetools.plugin.PresetContentLoader;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.SelectionListener;
import de.jtem.java2d.AppearanceChangeEvent;
import de.jtem.java2d.AppearanceChangeListener;
import de.jtem.java2d.SceneComponent;
import de.jtem.java2d.Viewer2D;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerLayout;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkSlot;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkSlotVertical;

public class TextureSpaceInterface extends ViewShrinkPanelPlugin 
implements HalfedgeListener, ColorChangedListener, ActionListener, ChangeListener, AppearanceChangeListener, SelectionListener, AppearanceListener {

	private static Logger
		log = LoggingSystem.getLogger(TextureSpaceInterface.class);
	private HalfedgeInterface
		hif = null;
	private Scene
		scenePlugin = null;
	
	private ShrinkSlot
		leftSlot = new ShrinkSlotVertical(250),
		rightSlot = new ShrinkSlotVertical(250);
	private Viewer2D
		viewer = new Viewer2D();
	private LayoutManager
		mainLayout = new SideContainerLayout(leftSlot, rightSlot, viewer);
	private LayerComponent
		layerComponent = new LayerComponent(); 
	private ShrinkPanel
		optionsShrinker = new ShrinkPanel("Texture Space Options");
	private JLabel
		edgeWidthLabel = new JLabel("Edge Width"),
		faceAlphaLabel = new JLabel("Face Opacity"),
		rotationLabel = new JLabel("Rotation °");
	private SpinnerNumberModel
		rotationModel = new SpinnerNumberModel(0.0, -360.0, 360.0, 0.1),
		edgeWidthModel = new SpinnerNumberModel(1.0, 0.01, 20.0, 0.1),
		faceAlphaModel = new SpinnerNumberModel(0.2, 0.0, 1.0, 0.1);
	private JSpinner
		edgeWidthSpinner = new JSpinner(edgeWidthModel),
		rotationSpinner = new JSpinner(rotationModel),
		faceAlphaSpinner = new JSpinner(faceAlphaModel);
	private JCheckBox
		reflectChecker = new JCheckBox("Reflect", false),
		antiAliasChecker = new JCheckBox("Anti-Aliasing", true),
		gridChecker = new JCheckBox("Grid", true),
		verticesChecker = new JCheckBox("Vertices", false),
		vertexFillChecker = new JCheckBox("Vertex Color", true),
		vertexOutlineChecker = new JCheckBox("Vertex Outline", true),
		edgesChecker = new JCheckBox("Edges", true),
		facesChecker = new JCheckBox("Faces", true),
		selectionChecker = new JCheckBox("Selection", true),
		vertexIndexChecker = new JCheckBox("Indices"),
		edgeIndexChecker = new JCheckBox("Indices"),
		faceIndexChecker = new JCheckBox("Indices"),
		showTextureChecker = new JCheckBox("Texture");
	private ColorChooseJButton
		backgroundColorButton = new ColorChooseJButton(new Color(232, 232, 232), true),
		vertexColorButton = new ColorChooseJButton(Color.WHITE, true),
		vertexOutlineColorButton = new ColorChooseJButton(new Color(153, 0, 0), true),
		edgeColorButton = new ColorChooseJButton(new Color(102, 102, 102), true),
		faceColorButton = new ColorChooseJButton(new Color(0, 102, 204), true);
	
	private AffineTexturePaint
		texturePaint = null;
	
	public TextureSpaceInterface() {
		shrinkPanel.setTitle("Texture Space Viewer 2D");
		shrinkPanel.setPreferredPosition(SHRINKER_TOP);
		shrinkPanel.setLayout(mainLayout);
		shrinkPanel.add(viewer);
		shrinkPanel.add(leftSlot);
		shrinkPanel.add(rightSlot);
		viewer.setPreferredSize(new Dimension(300, 450));
		viewer.getRoot().addChild(layerComponent);
		leftSlot.setBorder(BorderFactory.createEtchedBorder());
		rightSlot.setBorder(BorderFactory.createEtchedBorder());
		try {
			createInspectors();
		} catch (Exception e) {
			e.printStackTrace();
		}
		updateAppearances();
	}
	
	private void createInspectors() throws Exception {
		optionsShrinker.setLayout(new GridBagLayout());
		GridBagConstraints lc = LayoutFactory.createLeftConstraint();
		GridBagConstraints rc = LayoutFactory.createRightConstraint();
		optionsShrinker.add(gridChecker, lc);
		optionsShrinker.add(backgroundColorButton, rc);
		optionsShrinker.add(antiAliasChecker, rc);
		optionsShrinker.add(rotationLabel, lc);
		optionsShrinker.add(rotationSpinner, rc);
		optionsShrinker.add(reflectChecker, rc);
		optionsShrinker.add(new JSeparator(JSeparator.HORIZONTAL), rc);
		optionsShrinker.add(verticesChecker, rc);
		optionsShrinker.add(vertexIndexChecker, rc);
		optionsShrinker.add(vertexFillChecker, lc);
		optionsShrinker.add(vertexColorButton, rc);
		optionsShrinker.add(vertexOutlineChecker, lc);
		optionsShrinker.add(vertexOutlineColorButton, rc);
		optionsShrinker.add(new JSeparator(JSeparator.HORIZONTAL), rc);
		optionsShrinker.add(edgesChecker, lc);
		optionsShrinker.add(edgeColorButton, rc);
		optionsShrinker.add(edgeIndexChecker, rc);
		optionsShrinker.add(edgeWidthLabel, lc);
		optionsShrinker.add(edgeWidthSpinner, rc);
		optionsShrinker.add(new JSeparator(JSeparator.HORIZONTAL), rc);
		optionsShrinker.add(facesChecker, lc);
		optionsShrinker.add(faceColorButton, rc);
		optionsShrinker.add(showTextureChecker, rc);
		optionsShrinker.add(faceIndexChecker, rc);
		optionsShrinker.add(faceAlphaLabel, lc);
		optionsShrinker.add(faceAlphaSpinner, rc);
		optionsShrinker.add(new JSeparator(JSeparator.HORIZONTAL), rc);
		optionsShrinker.add(selectionChecker, lc);
		
		antiAliasChecker.addActionListener(this);
		backgroundColorButton.addColorChangedListener(this);
		gridChecker.addActionListener(this);
		rotationSpinner.addChangeListener(this);
		verticesChecker.addActionListener(this);
		edgesChecker.addActionListener(this);
		facesChecker.addActionListener(this);
		vertexIndexChecker.addActionListener(this);
		edgeIndexChecker.addActionListener(this);
		faceIndexChecker.addActionListener(this);
		vertexFillChecker.addActionListener(this);
		vertexColorButton.addColorChangedListener(this);
		vertexOutlineChecker.addActionListener(this);
		vertexOutlineColorButton.addColorChangedListener(this);
		edgeColorButton.addColorChangedListener(this);
		edgeWidthSpinner.addChangeListener(this);
		faceColorButton.addColorChangedListener(this);
		faceAlphaSpinner.addChangeListener(this);
		selectionChecker.addActionListener(this);
		reflectChecker.addActionListener(this);
		showTextureChecker.addActionListener(this);
	}
	
	@Override
	public void appearanceChanged(AppearanceEvent ev) {
		updateTexturePaint();
		viewer.repaint();
	}
	
	private void updateTexturePaint() {
		SceneGraphPath path = scenePlugin.getContentPath();
		scenePlugin.getContentAppearance().removeAppearanceListener(this);
		scenePlugin.getContentAppearance().addAppearanceListener(this);
		EffectiveAppearance app = EffectiveAppearance.create(path);
		Object imageObject = app.getAttribute("polygonShader.texture2d:image", new Object());
		if (imageObject instanceof ImageData) {
			ImageData image = (ImageData)imageObject;
			BufferedImage bi = (BufferedImage)image.getOriginalImage();
			Rectangle anchor = new Rectangle(1, 1);
			texturePaint = new AffineTexturePaint(bi, anchor);
			texturePaint.setTransformationPath(path);
		} else {
			if (imageObject != null) {
				log.warning("cannot load texture of type: " + imageObject.getClass());
			} else {
				log.warning("no texture found");
			}
		}
		// workaround, awt otherwise does not notice texture changes
		if (showTextureChecker.isSelected()) {
			SceneComponent faces = layerComponent.getFaceComponent();
			faces.setPaint(texturePaint);
		}
	}
	
	private void updateAppearances() {
		viewer.setAntialias(antiAliasChecker.isSelected());
		viewer.setBackground(backgroundColorButton.getColor());
		viewer.setGridEnabled(gridChecker.isSelected());
		SceneComponent root = viewer.getRoot();
		AffineTransform T = new AffineTransform();
		T.rotate(toRadians(rotationModel.getNumber().doubleValue()));
		if (reflectChecker.isSelected()) T.scale(-1, 1);
		root.setTransform(T);
		
		SceneComponent vertices = layerComponent.getVertexComponent();
		SceneComponent edges = layerComponent.getEdgeComponent();
		SceneComponent faces = layerComponent.getFaceComponent();
		SceneComponent vertexSelection = layerComponent.getVertexSelectionComponent();
		SceneComponent edgeSelection = layerComponent.getEdgeSelectionComponent();
		SceneComponent faceSelection = layerComponent.getFaceSelectionComponent();
		vertices.setVisible(verticesChecker.isSelected());
		edges.setVisible(edgesChecker.isSelected());
		faces.setVisible(facesChecker.isSelected());
		vertices.setPointFilled(vertexFillChecker.isSelected());
		vertices.setPointPaint(vertexColorButton.getColor());
		vertices.setAnnotationPaint(vertexOutlineColorButton.getColor().darker());
		vertices.setPointOutlined(vertexOutlineChecker.isSelected());
		vertices.setOutlinePaint(vertexOutlineColorButton.getColor());
		vertices.setAnnotated(vertexIndexChecker.isSelected());
		edges.setAnnotationPaint(edgeColorButton.getColor().darker());
		edges.setOutlinePaint(edgeColorButton.getColor());
		edges.setStroke(new BasicStroke(edgeWidthModel.getNumber().floatValue()));
		edges.setAnnotated(edgeIndexChecker.isSelected());
		Color fc = faceColorButton.getColor();
		int faceAlpha = (int)(faceAlphaModel.getNumber().doubleValue() * 255);
		Color faceColorAlpha = new Color(fc.getRed(), fc.getGreen(), fc.getBlue(), faceAlpha);
		if (showTextureChecker.isSelected()) {
			updateTexturePaint();
			faces.setPaint(texturePaint);
		} else {
			faces.setPaint(faceColorAlpha);
		}
		faces.setAnnotationPaint(fc.darker());
		faces.setAnnotated(faceIndexChecker.isSelected());
		edgeSelection.setStroke(new BasicStroke(edgeWidthModel.getNumber().floatValue() * 2));
		vertexSelection.setVisible(selectionChecker.isSelected());
		edgeSelection.setVisible(selectionChecker.isSelected());
		faceSelection.setVisible(selectionChecker.isSelected());
		
		// global appearance
		root.setStroke(edges.getStroke());
		root.setOutlinePaint(edges.getOutlinePaint());
		root.setPaint(faceColorAlpha);
		root.setPointPaint(vertices.getPointPaint());
		viewer.repaint();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateAppearances();
	}
	
	@Override
	public void colorChanged(ColorChangedEvent cce) {
		updateAppearances();
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		updateAppearances();
	}
	
	@Override
	public void appearanceChange(AppearanceChangeEvent arg0) {
		updateAppearances();
	}
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(TextureSpaceInterface.class, "uiPosition", shrinkPanel.getParentSlot() == leftSlot);
	}

	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		boolean uiPos = c.getProperty(TextureSpaceInterface.class, "uiPosition", true);
		if (uiPos) {
			leftSlot.addShrinkPanel(optionsShrinker);
		} else {
			rightSlot.addShrinkPanel(optionsShrinker);
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		scenePlugin = c.getPlugin(Scene.class);
		activeLayerChanged(null, hif.getActiveLayer());
		hif.addHalfedgeListener(this);
		for (TextureSpacePlugin tp : c.getPlugins(TextureSpacePlugin.class)) {
			if (tp.getOptionPanel() != null) {
				leftSlot.addShrinkPanel(tp.getOptionPanel());
			}
			SceneComponent back = layerComponent.getAddonComponentBack();
			SceneComponent front = layerComponent.getAddonComponentFront();
			if (tp.getSceneComponent() != null) {
				if (tp.getRenderOnTop()) {
					front.addChild(tp.getSceneComponent());
				} else {
					back.addChild(0, tp.getSceneComponent());
				}
				tp.getSceneComponent().addAppearanceChangeListener(this);
			}
		}
		hif.addSelectionListener(this);
	}
	
	public static void main(String[] args) throws Exception {
		// start test viewer
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addContentUI();
		v.getController().setPropertiesMode(StaticPropertiesFile);
		v.getController().setStaticPropertiesFile(new File("TextureSpace.xml"));
		v.registerPlugin(TextureSpaceInterface.class);
		v.registerPlugin(PresetContentLoader.class);
		v.registerPlugin(MarqueeSelectionPlugin.class);
		v.startup();
		
		// load cat head
		HalfedgeInterface hif = v.getPlugin(HalfedgeInterface.class);
		ReaderOBJ objReader = new ReaderOBJ();
		objReader.setUseMultipleTexAndNormalCoords(false);
		Input in = new Input(TextureSpaceInterface.class.getResource("cathead.obj"));
		SceneGraphComponent c = objReader.read(in);
		IndexedFaceSet g = (IndexedFaceSet)SceneGraphUtility.getFirstGeometry(c);
		IndexedFaceSetUtility.calculateAndSetFaceNormals(g);
		hif.set(g);
		hif.encompassContent();
	}

	private void updateLayer(HalfedgeLayer layer) {
		layerComponent.setLayer(layer);
		layerComponent.update();
		viewer.encompass(viewer.getBounds2D());
		viewer.repaint();
	}
	
	private void updateLayerSelection(HalfedgeLayer layer) {
		layerComponent.setLayer(layer);
		layerComponent.updateSelection();
		viewer.repaint();
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("paintbrush.png");
		return info;
	}
	
	@Override
	public void dataChanged(HalfedgeLayer layer) {
		updateLayer(layer);
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
		updateLayer(layer);
	}
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		updateLayer(active);
	}
	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}
	@Override
	public void layerRemoved(HalfedgeLayer layer) {
	}
	
	@Override
	public void selectionChanged(Selection s, HalfedgeInterface hif) {
		updateLayerSelection(hif.getActiveLayer());
	}
	
}
