/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2010, Technische Universität Berlin, jTEM
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

-	Redistributions of source code must retain the above copyright notice, 
	this list of conditions and the following disclaimer.

-	Redistributions in binary form must reproduce the above copyright notice, 
	this list of conditions and the following disclaimer in the documentation 
	and/or other materials provided with the distribution.
 
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS 
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT 
OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING 
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
OF SUCH DAMAGE.
**/

package de.jtem.halfedgetools.plugin;

import static java.awt.GridBagConstraints.BOTH;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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

/**
 * @author josefsso
 *
 * @param <V>
 * @param <E>
 * @param <F>
 * @param <HDS>
 */
public  class HalfedgeInterfacePlugin 
	< 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V,E,F>
	> extends ShrinkPanelPlugin implements StatusFlavor, ActionListener {

	private Scene
		scene = null;
	private Content
		content = null;
	private SceneGraphComponent
		contentParseRoot = null;
	private DefaultListModel
		geometryModel = new DefaultListModel(),
		adapterModel = new DefaultListModel();
	private JList
		adapterList = new JList(adapterModel);
	private JScrollPane
		adaptersScroller = new JScrollPane(adapterList, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	private JLabel
		displayedIFSLabel = new JLabel("Euler: "),
		selectedNodesLabel = new JLabel("Sel. V: E: F: "),
		vClassLabel = new JLabel("vClass: "),
		eClassLabel = new JLabel("eClass: "),
		fClassLabel = new JLabel("fClass: ");
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
	
	private JFileChooser 
		chooser = FileLoaderDialog.createFileChooser("heml", "HalfEdge Markup Language");
	
	private HDS 
		cachedHEDS = null;
	
	private DragEventTool
		det = new DragEventTool(InputSlot.LEFT_BUTTON);
	
	private int
		selectedFace, 
		selectedEdge, 
		selectedVertex;
	private Component 
		parent;
	
	
	private Set<Integer> 
		selV = new TreeSet<Integer>(),
		selE = new TreeSet<Integer>(),
		selF = new TreeSet<Integer>();
	private List<Adapter> 
		adapters = new LinkedList<Adapter>();

	private Class<HDS> 
		hdsClass = null;
	
	
	
	public static HalfedgeInterfacePlugin<StandardVertex,StandardEdge,StandardFace,StandardHDS> getStandardHalfedgeInterfacePlugin() {
		return new HalfedgeInterfacePlugin
		<StandardVertex,StandardEdge,StandardFace,StandardHDS>
		(StandardHDS.class, new StandardCoordinateAdapter(AdapterType.VERTEX_ADAPTER));
	}
	
	
	public HalfedgeInterfacePlugin() {
		this(null);
	}
	
	
	public HalfedgeInterfacePlugin(Class<HDS> hdsClass, Adapter... a) {
		
		if (hdsClass != null) {
			try {
				cachedHEDS = hdsClass.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		this.hdsClass = hdsClass;
		this.adapters.addAll(Arrays.asList(a));
		
		
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		shrinkPanel.add(vClassLabel, c);
		shrinkPanel.add(eClassLabel, c);
		shrinkPanel.add(fClassLabel, c);
		shrinkPanel.add(displayedIFSLabel, c);
		shrinkPanel.add(selectedNodesLabel, c);
				
		adaptersScroller.setMinimumSize(new Dimension(30, 70));
		JPanel adaptersPanel = new JPanel();
		adaptersPanel.setLayout(new GridLayout());
		adaptersPanel.add(adaptersScroller);
		adaptersPanel.setBorder(BorderFactory.createTitledBorder("Available Adapters"));
		
		c.gridwidth = 1;
		c.weightx = 1.0;
		shrinkPanel.add(loadButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(saveButton, c);
		
		c.weighty = 0.0;

		shrinkPanel.add(rescanButton, c);
		
		c.weighty = 1.0;
		shrinkPanel.add(adaptersPanel, c);
		
		rescanButton.addActionListener(this);
		loadButton.addActionListener(this);
		saveButton.addActionListener(this);
		
		setAdapters(a);
	}
	
	
	public void setHalfedgeClass(Class<HDS> hdsClass) {
		try {
			cachedHEDS = hdsClass.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		this.hdsClass = hdsClass;
	}
	
	
	public void addAdapter(Adapter a) {
		adapters.add(a);
	}
	
	public void removeAdapter(Adapter a) {
		adapters.remove(a);
	}
	
	public void setAdapters(Adapter... apts) {
		for(Adapter a : apts) {
			adapterModel.addElement(a);
		}
		adapters.clear();
		adapters.addAll(Arrays.asList(apts));
	}
	
	public List<Adapter> getAdapters() {
		return Collections.unmodifiableList(adapters);
	}
	
	
	@SuppressWarnings("unchecked")
	public 	void actionPerformed(ActionEvent e) {
		if (rescanButton == e.getSource()) {
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
			cce.node = contentParseRoot;
			contentChangedListener.contentChanged(cce);
		} else if(loadButton == e.getSource()) {
			File file = null;
			if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			}
			if (file != null) {
				HDS newHeds = (HDS)HalfedgeIO.readHDS(file.getAbsolutePath());

				updateHalfedgeContentAndActiveGeometry(newHeds);

			}
			
		} else if(saveButton == e.getSource()) {
			File file = null;
			if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			}
			if (file != null) {
				HalfedgeIO.writeHDS(cachedHEDS, file.getAbsolutePath());
			}
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

	
	public void convertActiveGeometryToHDS(HDS hds) {
		
		hds.clear();

		IndexedFaceSet ifs = null;
		
		if(activeGeometry != null) {
			ifs = (IndexedFaceSet)activeGeometry.cgc.getGeometry();
		
			ConverterJR2Heds<V, E, F> c = new ConverterJR2Heds<V, E, F>(hds.getVertexClass(), hds.getEdgeClass(), hds.getFaceClass());
	
			boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
			if (!oriented) {
				statusChangedListener.statusChanged("Surface is not orientable!");
				return;
			}
			
			c.ifs2heds(ifs, hds, adapters.toArray(new Adapter[] {}));
		} else {
			hds = getBlankHDS();
		}
		updateCache(hds);
	}
	
	
	public StandardHDS convertActiveGeometryToStandardHDS(Adapter... a) {
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
	
	
	public HDS getCachedHalfEdgeDataStructure() {
		return cachedHEDS;
	}
	
	public HDS getBlankHDS() {
		
		HDS newHds = null;
		
		try {
			newHds = hdsClass.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return newHds;
	}

	
	public void updateHalfedgeContentAndActiveGeometry(HDS hds, Adapter... a) {

		ConverterHeds2JR<V, E, F> c = new ConverterHeds2JR<V, E, F>();
		IndexedFaceSet ifs = c.heds2ifs(hds, a);

		updateCache(hds);
		
		if(ifs != null) {
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
			activeGeometry.cgc.setGeometry(ifs);
		}
		
//		contentChangedListener.skipNextUpdate(true);
//		contentChangedListener.contentChanged(new ContentChangedEvent(ChangeEventType.ContentChanged));
	}
	
	public void updateHalfedgeContentAndActiveGeometry(HDS hds) {

		updateHalfedgeContentAndActiveGeometry(hds, adapters.toArray(new Adapter[0]));
	}
	
	
	public String getHalfedgeContentName() {
		if (activeGeometry == null) {
			return "No Geometry Selected";
		} else {
			return activeGeometry.cgc.getGeometry().getName();
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
		SceneGraphComponent sc = scene.getContentComponent();
		setContentParseRoot(sc); // also calls contentChanged()
		
//		det.setDescription("Select node");
//	    det.addFaceDragListener(new FaceDragListener() {
//	    	public void faceDragStart(FaceDragEvent e) { 
//
//	    	}
//	    	public void faceDragged(FaceDragEvent e) {
//	      
//	    	};
//	    	public void faceDragEnd(FaceDragEvent e) {
//	    		selectedFace = e.getIndex();
//	    		updateSelectedLabel();
//	    		selF.clear();
//	    		selF.add(selectedFace);
////	    		updateIfsFromSelection();
//	    		statusChangedListener.statusChanged("Selected face: " + selectedFace);
//	    		
//	    		
//	    	} 
//	    
//	    });
//	    det.addLineDragListener(new LineDragListener() {
//	    	public void lineDragStart(LineDragEvent e) { 
//
//	    	}
//	    	public void lineDragged(LineDragEvent e) {
//	    		selectedEdge = e.getIndex();
//	    		IndexedFaceSet ifs = (IndexedFaceSet)((GeomObject)geometryModel.get(0)).cgc.getGeometry();
//	    		IntArrayArray iiData=null;
//	    		int[][][] indices=new int[3][][];
//	    		iiData = (IntArrayArray)ifs.getEdgeAttributes(Attribute.INDICES);
//	    		if (iiData!=null)
//	    			indices[1]= iiData.toIntArrayArray(null);
//
//	    		// FIXME for general case
//	    		int[] vs = indices[1][selectedEdge];
//	    		int v1 = vs[0];
//	    		int v2 = vs[1];
//	    		
//	    		E ee = HalfEdgeUtils.findEdgeBetweenVertices(cachedHEDS.getVertex(v1), cachedHEDS.getVertex(v2));
//	    		selectedEdge = ee.getIndex();
//		
//	    		updateSelectedLabel();
//	    		selE.clear();
//	    		selE.add(selectedEdge);
//	    		statusChangedListener.statusChanged("Selected edge: " + selectedEdge);
//	    	};
//	    	public void lineDragEnd(LineDragEvent e) {
//
//	    		
//	    	} 
//	    
//	    });
//	    det.addPointDragListener(new PointDragListener() {
//			public void pointDragStart(PointDragEvent e) { 
//
//	    	}
//	    	public void pointDragged(PointDragEvent e) {
//	      
//	    	};
//	    	public void pointDragEnd(PointDragEvent e) {
//	    		selectedVertex = e.getIndex();
//	    		updateSelectedLabel();
//	    		selV.clear();
//	    		selV.add(selectedVertex);
////	    		updateIfsFromSelection();
//	    		statusChangedListener.statusChanged("Selected vertex: " + selectedVertex);
//	    		
//	    		
//	    	} 
//	    
//	    });
//		    
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
		info.name = "Halfedge Interface";
		info.vendorName = "Kristoffer Josefsson";
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
		displayedIFSLabel.setText("Euler: " + "V: " + hds.numVertices() + " E: " + hds.numEdges() + " F: " + hds.numFaces());
		vClassLabel.setText("vClass: " + hds.getVertexClass().getSimpleName());
		eClassLabel.setText("eClass: " + hds.getEdgeClass().getSimpleName());
		fClassLabel.setText("fClass: " + hds.getFaceClass().getSimpleName());
		
	}
	
	
	
	/**
	 * @author josefsso
	 * If someone overwrites our content, immediately convert the new IFS to a HDS, then convert back with
	 * the adapters to a new IFS and rewrite. Unless skipNextUpdate is called before explicitly.
	 */
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
						activeGeometry = new GeomObject(c);
					}
					c.childrenAccept(this);
				}
			
			});
			if(!ownUpdate ) {
		
				convertActiveGeometryToHDS(cachedHEDS);
				updateHalfedgeContentAndActiveGeometry(cachedHEDS);

			} else {
				ownUpdate = false;
			}
		}
		
	}

	public SelfAwareContentChangedListener getContenChangedtListener() {
		return contentChangedListener;
	}
	
}
