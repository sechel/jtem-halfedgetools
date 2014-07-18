package de.jtem.halfedgetools.plugin.texturespace;

import static de.jtem.jrworkspace.plugin.simplecontroller.SimpleController.PropertiesMode.StaticPropertiesFile;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.basic.ViewShrinkPanelPlugin;
import de.jreality.reader.ReaderOBJ;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.util.Input;
import de.jreality.util.SceneGraphUtility;
import de.jtem.beans.Inspector;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.HalfedgeListener;
import de.jtem.halfedgetools.plugin.PresetContentLoader;
import de.jtem.java2d.Viewer2D;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerLayout;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkPanel;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkSlot;
import de.jtem.jrworkspace.plugin.sidecontainer.widget.ShrinkSlotVertical;

public class TextureSpaceInterface extends ViewShrinkPanelPlugin implements HalfedgeListener {

	private ShrinkSlot
		leftSlot = new ShrinkSlotVertical(250),
		rightSlot = new ShrinkSlotVertical(250);
	private HalfedgeInterface
		hif = null;
	private Viewer2D
		viewer = new Viewer2D();
	private LayoutManager
		mainLayout = new SideContainerLayout(leftSlot, rightSlot, viewer);
	private LayerComponent
		layerComponent = new LayerComponent(); 
	
	final private static String[] excludedPropertiesForViewer = {
		"UIClassID","alignmentX","alignmentY","autoscrolls",
		"componentCount","debugGraphicsOptions","enabled","focusCycleRoot",
		"focusTraversalPolicyProvider","focusTraversalPolicySet","focusable",
		"font","foreground","height","inheritsPopupMenu","managingFocus",
		"name","opaque","optimizedDrawingEnabled","paintingTile",
		"requestFocusEnabled","toolTipText","validateRoot","doubleBuffered",
		"verifyInputWhenFocusTarget","visible","width","x","y","encompassMargin",
		"keepingAspectRatio","translateToolEnabled","scaleToolEnabled",
		"menuToolEnabled","baselineResizeBehavior","lastPopupLocationX",
		"lastPopupLocationY","paintingForPrint"
	};
	
	public TextureSpaceInterface() {
		shrinkPanel.setTitle("Texture Space Viewer");
		shrinkPanel.setPreferredPosition(SHRINKER_TOP);
		shrinkPanel.setLayout(mainLayout);
		shrinkPanel.add(viewer);
		shrinkPanel.add(leftSlot);
		shrinkPanel.add(rightSlot);
		viewer.setPreferredSize(new Dimension(300, 450));
		viewer.getRoot().addChild(layerComponent);
		try {
			createInpectors();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void createInpectors() throws Exception {
		Inspector faceInspector = new Inspector(layerComponent.getFaceComponent(), new LinkedList<String>());
		ShrinkPanel faceShrinker = new ShrinkPanel("Faces");
		faceShrinker.setLayout(new GridLayout());
		faceShrinker.add(faceInspector);
		leftSlot.addShrinkPanel(faceShrinker);
		
		Inspector edgeInspector = new Inspector(layerComponent.getEdgeComponent(), new LinkedList<String>());
		ShrinkPanel edgeShrinker = new ShrinkPanel("Edges");
		edgeShrinker.setLayout(new GridLayout());
		edgeShrinker.add(edgeInspector);
		leftSlot.addShrinkPanel(edgeShrinker);
		
		Inspector vertexInspector = new Inspector(layerComponent.getVertexComponent(), new LinkedList<String>());
		ShrinkPanel vertexShrinker = new ShrinkPanel("Vertices");
		vertexShrinker.setLayout(new GridLayout());
		vertexShrinker.add(vertexInspector);
		leftSlot.addShrinkPanel(vertexShrinker);
		
		Inspector viewInspector = new Inspector(viewer, Arrays.asList(excludedPropertiesForViewer));
		ShrinkPanel viewShrinker = new ShrinkPanel("Viewer");
		viewShrinker.setLayout(new GridLayout());
		viewShrinker.add(viewInspector);
		leftSlot.addShrinkPanel(viewShrinker);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		activeLayerChanged(null, hif.getActiveLayer());
		hif.addHalfedgeListener(this);
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
		v.startup();
		
		// load cat head
		HalfedgeInterface hif = v.getPlugin(HalfedgeInterface.class);
		ReaderOBJ objReader = new ReaderOBJ();
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
	
}
