/**
This file is part of a jTEM project.
All jTEM projects are licensed under the FreeBSD license 
or 2-clause BSD license (see http://www.opensource.org/licenses/bsd-license.php). 

Copyright (c) 2002-2009, Technische Universität Berlin, jTEM
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

import static de.jreality.plugin.basic.Content.ChangeEventType.ContentChanged;
import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
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
import de.jreality.plugin.basic.Content.ContentChangedEvent;
import de.jreality.plugin.basic.Content.ContentChangedListener;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphVisitor;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.halfedgetools.jreality.node.standard.StandardEdge;
import de.jtem.halfedgetools.jreality.node.standard.StandardFace;
import de.jtem.halfedgetools.jreality.node.standard.StandardHDS;
import de.jtem.halfedgetools.jreality.node.standard.StandardVertex;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.StatusFlavor;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;


public class HalfedgeConnectorPlugin extends ShrinkPanelPlugin implements ListSelectionListener, StatusFlavor {

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
	private GeomObject	
		activeGeometry = null;
	private StatusChangedListener
		statusChangedListener = null;
	private ContentChangedListener
		contentChangedListener = new MyContentChangedListener();
	
	public HalfedgeConnectorPlugin() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		
		geometryList.getSelectionModel().addListSelectionListener(this);
		geometriesScroller.setMinimumSize(new Dimension(30, 70));
		JPanel geometriesPanel = new JPanel();
		geometriesPanel.setLayout(new GridLayout());
		geometriesPanel.add(geometriesScroller);
		geometriesPanel.setBorder(BorderFactory.createTitledBorder("Available Face Sets"));
		shrinkPanel.add(geometriesPanel, c);
	}
	
	@Override
	public void setStatusListener(StatusChangedListener scl) {
		statusChangedListener = scl;
	}
	
	
	
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void toHalfedge(IndexedFaceSet ifs, HDS hds, Adapter... a) {
		ConverterJR2Heds<V, E, F> c = new ConverterJR2Heds<V, E, F>(hds.getVertexClass(), hds.getEdgeClass(), hds.getFaceClass());
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
		if (!oriented) {
			statusChangedListener.statusChanged("Surface is not orientable!");
			return;
		}
		c.ifs2heds(ifs, hds, a);
	}
	
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> StandardHDS toHalfedge(IndexedFaceSet ifs, Adapter... a) {
		ConverterJR2Heds<StandardVertex, StandardEdge, StandardFace> c = new ConverterJR2Heds<StandardVertex, StandardEdge, StandardFace>(StandardVertex.class, StandardEdge.class, StandardFace.class);
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
		if (!oriented) {
			statusChangedListener.statusChanged("Surface is not orientable!");
			return null;
		}
		StandardHDS result = new StandardHDS();
		c.ifs2heds(ifs, result, a);
		return result;
	}
	
	
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void getHalfedgeContent(HDS hds, Adapter... a) {
		if (activeGeometry == null) {
			return;
		}
		ConverterJR2Heds<V, E, F> c = new ConverterJR2Heds<V, E, F>(hds.getVertexClass(), hds.getEdgeClass(), hds.getFaceClass());
		IndexedFaceSet ifs = (IndexedFaceSet)activeGeometry.cgc.getGeometry();
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
		if (!oriented) {
			statusChangedListener.statusChanged("Surface is not orientable!");
			return;
		}
		c.ifs2heds(ifs, hds, a);
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
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void updateHalfedgeContent(HDS hds, Adapter... a) {
		if (activeGeometry == null) {
			return;
		}
		ConverterHeds2JR<V, E, F> c = new ConverterHeds2JR<V, E, F>();
		activeGeometry.cgc.setGeometry(c.heds2ifs(hds, a));
		contentChangedListener.contentChanged(new ContentChangedEvent(ContentChanged));
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void updateHalfedgeContent(HDS hds, boolean normals, Adapter... a) {
		if (activeGeometry == null) {
			return;
		}
		ConverterHeds2JR<V, E, F> c = new ConverterHeds2JR<V, E, F>();
		IndexedFaceSet ifs = c.heds2ifs(hds, a);
		if (normals) {
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
		}
		activeGeometry.cgc.setGeometry(ifs);
		contentChangedListener.contentChanged(new ContentChangedEvent(ContentChanged));
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void setHalfedgeContent(HDS hds, boolean normals, Adapter... a) {
		SceneGraphComponent root = new SceneGraphComponent();
		activeGeometry = new GeomObject(root);
		ConverterHeds2JR<V, E, F> c = new ConverterHeds2JR<V, E, F>();
		IndexedFaceSet ifs = c.heds2ifs(hds, a);
		if (normals) {
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
		}
		
		activeGeometry.cgc.setGeometry(ifs);
		content.setContent(root);
		contentChangedListener.contentChanged(new ContentChangedEvent(ContentChanged));
	}
	
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> IndexedFaceSet toIndexedFaceSet(HDS hds, boolean normals, Adapter... a) {
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
	
	
	@Override
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
		content = JRViewerUtility.getContentPlugin(c);
		content.addContentChangedListener(contentChangedListener);
		scene = c.getPlugin(Scene.class);
		setContentParseRoot(scene.getContentComponent());
		contentChangedListener.contentChanged(null);
	}
	
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
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
	
	
	private class MyContentChangedListener implements ContentChangedListener {

		@Override
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
			}
		}
		
	}
	
}
