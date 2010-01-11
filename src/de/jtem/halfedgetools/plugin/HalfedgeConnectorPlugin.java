package de.jtem.halfedgetools.plugin;

import static de.jreality.plugin.basic.Content.ChangeEventType.ContentChanged;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.REMAINDER;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.jreality.geometry.IndexedFaceSetUtility;
import de.jreality.plugin.JRViewerUtility;
import de.jreality.plugin.basic.Content;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.basic.View;
import de.jreality.plugin.basic.Content.ChangeEventType;
import de.jreality.plugin.basic.Content.ContentChangedEvent;
import de.jreality.plugin.basic.Content.ContentChangedListener;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jreality.scene.data.Attribute;
import de.jreality.scene.data.IntArrayArray;
import de.jreality.scene.tool.InputSlot;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.FaceDragEvent;
import de.jreality.tools.FaceDragListener;
import de.jreality.tools.LineDragEvent;
import de.jreality.tools.LineDragListener;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.ui.viewerapp.FileLoaderDialog;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.adapter.Adapter.AdapterType;
import de.jtem.halfedgetools.jreality.adapter.standard.StandardCoordinateAdapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.StatusFlavor;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;


// TODO also store adapters
public class HalfedgeConnectorPlugin < 
V extends Vertex<V, E, F>,
E extends Edge<V, E, F>, 
F extends Face<V, E, F>,
HDS extends HalfEdgeDataStructure<V,E,F>
>  extends ShrinkPanelPlugin implements ListSelectionListener, StatusFlavor, ActionListener {

	private Scene
		scene = null;
	private Content
		content = null;
	private SceneGraphComponent
		contentParseRoot = null;
	private DefaultListModel
		geometryModel = new DefaultListModel();
	private JList	
		geometryList = new JList(geometryModel);
	private JScrollPane
		geometriesScroller = new JScrollPane(geometryList, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	private JLabel
		selectedNodesLabel = new JLabel("Sel. V: E: F: "),
		hedsClassLabel = new JLabel("Class: ");
	private GeomObject	
		activeGeometry = null;
	private StatusChangedListener
		statusChangedListener = null;
	private SelfAwareContentChangedListener
		contentChangedListener = new SelfAwareContentChangedListener();
	private JButton
		rescanButton = new JButton("Rescan"),
		loadButton = new JButton("Load"),
		saveButton = new JButton("Save");
		
	private Adapter[] lastAdapters = null;
	
	private JFileChooser 
		chooser = FileLoaderDialog.createFileChooser("heml", "HalfEdge Markup Language");
	
	private HDS cachedHEDS = null;
	
	private DragEventTool
		det = new DragEventTool(InputSlot.LEFT_BUTTON);
	
	private int
		selectedFace, selectedEdge, selectedVertex;
	private Component parent;
	
	
	private Set<Integer> selV = new TreeSet<Integer>();
	private Set<Integer> selE = new TreeSet<Integer>();
	private Set<Integer> selF = new TreeSet<Integer>();
	private Adapter[] adapters = null;
	
	
	public HalfedgeConnectorPlugin() {
		this(new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
	}
	
	
	public HalfedgeConnectorPlugin(Adapter... a) {
		this.adapters  = a;
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		shrinkPanel.add(hedsClassLabel, c);
		shrinkPanel.add(selectedNodesLabel, c);
		
		geometryList.getSelectionModel().addListSelectionListener(this);
		geometriesScroller.setMinimumSize(new Dimension(30, 70));
		JPanel geometriesPanel = new JPanel();
		geometriesPanel.setLayout(new GridLayout());
		geometriesPanel.add(geometriesScroller);
		geometriesPanel.setBorder(BorderFactory.createTitledBorder("Available Face Sets"));
		
		
		
		c.gridwidth = 1;
		c.weightx = 1.0;
		shrinkPanel.add(loadButton, c);
		c.gridwidth = REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(saveButton, c);
		
		c.weighty = 0.0;

		shrinkPanel.add(rescanButton, c);
		
		c.weighty = 1.0;
		shrinkPanel.add(geometriesPanel, c);
		
		rescanButton.addActionListener(this);
		loadButton.addActionListener(this);
		saveButton.addActionListener(this);
	}
	
	public 	void actionPerformed(ActionEvent e) {
		if (rescanButton == e.getSource()) {
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
			cce.node = contentParseRoot;
			contentChangedListener.contentChanged(cce);
		} 
	}
	
	
	public void setStatusListener(StatusChangedListener scl) {
		statusChangedListener = scl;
	}
	
	public void setSelectedVertexIndex(int i) {
		selectedVertex = i;
	}
	
	public void setSelectedEdgeIndex(int i) {
		selectedEdge = i;
	}
	
	public void setSelectedFaceIndex(int i) {
		selectedFace = i;
	}
	
	public int getSelectedVertexIndex() {
		return selectedVertex;
	}
	
	public int getSelectedEdgeIndex() {
		return selectedEdge;
	}
	
	public int getSelectedFaceIndex() {
		return selectedFace;
	}

	
	public HDS getHalfedgeContent(HDS hds, Adapter... a) {
		if (activeGeometry == null) {
			// is there a better way to get this?
			IndexedFaceSet ifs = (IndexedFaceSet)((GeomObject)geometryModel.get(0)).cgc.getGeometry();
		}
		ConverterJR2Heds<V, E, F> c = new ConverterJR2Heds<V, E, F>(hds.getVertexClass(), hds.getEdgeClass(), hds.getFaceClass());
		IndexedFaceSet ifs = (IndexedFaceSet)activeGeometry.cgc.getGeometry();
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
		if (!oriented) {
			statusChangedListener.statusChanged("Surface is not orientable!");
			return null;
		}
		c.ifs2heds(ifs, hds, a);
		return hds;
	}
	
	
	public StandardHDS getHalfedgeContent(Adapter... a) {
		if (activeGeometry == null) {
			return null;
		}
		ConverterJR2Heds<StandardVertex, StandardEdge, StandardFace> c = new ConverterJR2Heds<StandardVertex, StandardEdge, StandardFace>(StandardVertex.class, StandardEdge.class, StandardFace.class);
		IndexedFaceSet ifs = (IndexedFaceSet)activeGeometry.cgc.getGeometry();
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
		if (!oriented) {
			statusChangedListener.statusChanged("Surface is not orientable!");
			return null;
		}
		StandardHDS result = new StandardHDS();
		c.ifs2heds(ifs, result, a);
		return result;
	}
	
	public HDS getCachedHalfEdgeDataStructure(HDS hds, Adapter... a) {

		if(cachedHEDS.getVertexClass() == hds.getVertexClass() &&
				cachedHEDS.getEdgeClass() == hds.getEdgeClass() &&
				cachedHEDS.getFaceClass() == hds.getFaceClass()) {
			
			return cachedHEDS;
		} else {
			System.err.println("cache didnt match class so returning as default hds");
			hds = getHalfedgeContent(hds, a);
			return hds;
		}
	}

	public  void updateHalfedgeContent(HDS hds, boolean normals, Adapter... a) {
		if (activeGeometry == null) {
			SceneGraphComponent root = new SceneGraphComponent();
			activeGeometry = new GeomObject(root);
		}
		ConverterHeds2JR<V, E, F> c = new ConverterHeds2JR<V, E, F>();
		IndexedFaceSet ifs = c.heds2ifs(hds, a);
		if (normals) {
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
		}
		lastAdapters = a;
		updateCache(hds);
		activeGeometry.cgc.setGeometry(ifs);
		contentChangedListener.skipNextUpdate(true);
		contentChangedListener.contentChanged(new ContentChangedEvent(ContentChanged));
	}
	
	
	public IndexedFaceSet toIndexedFaceSet(HDS hds, boolean normals, Adapter... a) {
		ConverterHeds2JR<V, E, F> c = new ConverterHeds2JR<V, E, F>();
		IndexedFaceSet ifs = c.heds2ifs(hds, a);
		if (normals) {
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
		}
		return ifs;
	}
	
	
	public String getHalfedgeContentName() {
		if (activeGeometry == null) {
			return "No Geometry Selected";
		} else {
			return activeGeometry.cgc.getGeometry().getName();
		}
	}
	
	
	public void valueChanged(ListSelectionEvent e) {
		Object cgcObject = geometryList.getSelectedValue();
		if (cgcObject != null) {
			activeGeometry = (GeomObject)cgcObject;
		} else {
			activeGeometry = null;
		}
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		View viewPlugin = c.getPlugin(View.class);
		parent = viewPlugin.getViewer().getViewingComponent();
		content = JRViewerUtility.getContentPlugin(c);
		content.addContentChangedListener(contentChangedListener);
		scene = c.getPlugin(Scene.class);
		setContentParseRoot(scene.getContentComponent());
		contentChangedListener.contentChanged(null);
		
		det.setDescription("Select node");
	    det.addFaceDragListener(new FaceDragListener() {
	    	public void faceDragStart(FaceDragEvent e) { 

	    	}
	    	public void faceDragged(FaceDragEvent e) {
	      
	    	};
	    	public void faceDragEnd(FaceDragEvent e) {
	    		selectedFace = e.getIndex();
	    		updateSelectedLabel();
	    		selF.clear();
	    		selF.add(selectedFace);
//	    		updateIfsFromSelection();
	    		statusChangedListener.statusChanged("Selected face: " + selectedFace);
	    		
	    		
	    	} 
	    
	    });
	    det.addLineDragListener(new LineDragListener() {
	    	public void lineDragStart(LineDragEvent e) { 

	    	}
	    	public void lineDragged(LineDragEvent e) {
	      
	    	};
	    	public void lineDragEnd(LineDragEvent e) {
	    		selectedEdge = e.getIndex();
	    		IndexedFaceSet ifs = (IndexedFaceSet)((GeomObject)geometryModel.get(0)).cgc.getGeometry();
	    		IntArrayArray iiData=null;
	    		int[][][] indices=new int[3][][];
	    		iiData = (IntArrayArray)ifs.getEdgeAttributes(Attribute.INDICES);
	    		if (iiData!=null)
	    			indices[1]= iiData.toIntArrayArray(null);

	    		// FIXME for general case
	    		int[] vs = indices[1][selectedEdge];
	    		int v1 = vs[0];
	    		int v2 = vs[1];
	    		
	    		E ee = HalfEdgeUtils.findEdgeBetweenVertices(cachedHEDS.getVertex(v1), cachedHEDS.getVertex(v2));
	    		selectedEdge = ee.getIndex();
		
	    		updateSelectedLabel();
	    		selE.clear();
	    		selE.add(selectedEdge);
//	    		updateIfsFromSelection();
	    		statusChangedListener.statusChanged("Selected edge: " + selectedEdge);
	    		
	    	} 
	    
	    });
	    det.addPointDragListener(new PointDragListener() {
			public void pointDragStart(PointDragEvent e) { 

	    	}
	    	public void pointDragged(PointDragEvent e) {
	      
	    	};
	    	public void pointDragEnd(PointDragEvent e) {
	    		selectedVertex = e.getIndex();
	    		updateSelectedLabel();
	    		selV.clear();
	    		selV.add(selectedVertex);
//	    		updateIfsFromSelection();
	    		statusChangedListener.statusChanged("Selected vertex: " + selectedVertex);
	    		
	    		
	    	} 
	    
	    });
		    
		content.addContentTool(det);
	}
	
	
	protected void updateSelectedLabel() {
		selectedNodesLabel.setText("Sel. V: " + selectedVertex + " E: " + selectedEdge + " F: " + selectedFace);
		
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		content.removeContentTool(det);
		content.removeContentChangedListener(contentChangedListener);
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo();
		info.name = "Halfedge Connector";
		info.vendorName = "Stefan Sechelmann";
		return info;
	}
	
	/**
	 * Sets the scene graph component, that will be the root
	 * when searching for geometries to convert
	 * @param contentParseRoot the search root node
	 */
	public void setContentParseRoot(SceneGraphComponent contentParseRoot) {
		this.contentParseRoot = contentParseRoot;
		contentChangedListener.contentChanged(null);
	}
	
	void updateCache(HDS hds) {
		cachedHEDS = hds;
		hedsClassLabel.setText("V: " + hds.getVertexClass().getSimpleName() + " E: " + hds.getEdgeClass().getSimpleName() + " F: " +hds.getFaceClass().getSimpleName());
		
	}
	
	public class SelfAwareContentChangedListener implements ContentChangedListener {
		
		private boolean ownUpdate = false;
		
		public void skipNextUpdate(boolean a) {
			ownUpdate = a;
		}

		public void contentChanged(ContentChangedEvent cce) {
			geometryModel.clear(); 
			activeGeometry = null;
			if (scene.getContentComponent() == null) {
				return;
			}
			SceneGraphComponent root = contentParseRoot;
			if (root == null) {
				root = scene.getContentComponent();
			}
			root.accept(new SceneGraphVisitor() {
				@Override
				public void visit(SceneGraphComponent c) {
					if (!c.isVisible()) {
						return;
					}
					if (c.getGeometry() instanceof IndexedFaceSet) {
						GeomObject go = new GeomObject(c);
						geometryModel.add(geometryModel.size(), go);
					}
					c.childrenAccept(this);
				}
			
			});
			if (geometryModel.getSize() != 0) {
				geometryList.setSelectedIndex(0);
				// content updated, create heds from JRNode

				HDS hds = (HDS)new StandardHDS();
				hds = getHalfedgeContent(hds, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));

				
				if(!ownUpdate ) {
					System.err.println("Heds connector updating");
					updateCache(hds);
				} else {
					System.err.println("Heds not updating because self-caused update");
					ownUpdate = false;
				}
			}
			
		}
		
	}

	public SelfAwareContentChangedListener getContenChangedtListener() {
		return contentChangedListener;
	}
	
}
