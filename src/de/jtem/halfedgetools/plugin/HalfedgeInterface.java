package de.jtem.halfedgetools.plugin;

import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Dimension; 
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

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
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Calculator;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.io.HalfedgeIO;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.adapter.JRBaryCenterAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRColorAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRLabelAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRNormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRRadiusAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRSizeAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRTexCoordAdapter;
import de.jtem.halfedgetools.jreality.calculator.JRFaceAreaCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRFaceNormalCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRSubdivisionCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRVertexPositionCalculator;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;


public class HalfedgeInterface extends ShrinkPanelPlugin implements ListSelectionListener, ActionListener {

	private Scene
		scene = null;
	private Content
		content = null;
	private SceneGraphComponent
		contentParseRoot = null;
	private JList	
		selectionList = new JList(),
		geometryList = new JList();
	private JScrollPane
		selectionScroller = new JScrollPane(selectionList, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER),
		geometriesScroller = new JScrollPane(geometryList, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER);
	private HalfedgeContentListener
		contentChangedListener = new HalfedgeContentListener();
	private JButton
		saveHDSButton = new JButton(ImageHook.getIcon("disk.png")),
		loadHDSButton = new JButton(ImageHook.getIcon("folder.png")),
		clearSelectionButton = new JButton("Clean Selection"),
		rescanButton = new JButton("Rescan");
	private JLabel
		hdsLabel = new JLabel("No HDS cached");
	private JCheckBox
		viewSelectionChecker = new JCheckBox("View Selection");
	private JFileChooser 
		chooser = new JFileChooser();
	
	private boolean
		hdsIsDirty = true;
	private HalfEdgeDataStructure<?, ?, ?> 
		cachedHEDS = new DefaultJRHDS();
	private Map<? extends Edge<?,?,?>, Integer>
		edgeMap = new HashMap<Edge<?,?,?>, Integer>();
	private HalfedgeSelection
		selection = new HalfedgeSelection();
	private AdapterSet
		adapters = new AdapterSet(),
		cachedAdapters = new AdapterSet();
	private CalculatorSet
		calculators = new CalculatorSet();
	
	private List<HalfedgeListener>
		listeners = new LinkedList<HalfedgeListener>();
	
	private SceneGraphComponent	
		activeComponent = new SceneGraphComponent("HalfedgeInterface");
	private ConverterHeds2JR 
		converterHeds2JR = new ConverterHeds2JR();
	private ConverterJR2Heds
		converterJR2Heds = new ConverterJR2Heds();

	
	public HalfedgeInterface() {
		makeLayout();
		adapters.add(new JRBaryCenterAdapter());
		adapters.add(new JRColorAdapter());
		adapters.add(new JRLabelAdapter());
		adapters.add(new JRNormalAdapter());
		adapters.add(new JRPositionAdapter());
		adapters.add(new JRRadiusAdapter());
		adapters.add(new JRSizeAdapter());
		adapters.add(new JRTexCoordAdapter());
		adapters.add(new SelectionAdapter(this));
		calculators.add(new JRVertexPositionCalculator());
		calculators.add(new JRFaceAreaCalculator());
		calculators.add(new JRFaceNormalCalculator());
		calculators.add(new JRSubdivisionCalculator());
	}
	
	
	private void makeLayout() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
				
		geometriesScroller.setMinimumSize(new Dimension(30, 70));
		JPanel adaptersPanel = new JPanel();
		adaptersPanel.setLayout(new GridLayout());
		adaptersPanel.add(geometriesScroller);
		adaptersPanel.setBorder(BorderFactory.createTitledBorder("Available Geometries"));
		
		c.gridwidth = 1;
		c.weightx = 1.0;
		shrinkPanel.add(hdsLabel, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.0;
		shrinkPanel.add(loadHDSButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		shrinkPanel.add(saveHDSButton, c);
		c.weightx = 0.0;
		c.weighty = 1.0;
		shrinkPanel.add(adaptersPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(rescanButton, c);
		
		selectionScroller.setMinimumSize(new Dimension(30, 70));
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new GridLayout());
		selectionPanel.add(selectionScroller);
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Selection"));
		
		c.weighty = 1.0;
		shrinkPanel.add(selectionPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(viewSelectionChecker, c);
		shrinkPanel.add(clearSelectionButton, c);
		
		File userDir = new File(System.getProperty("user.dir"));
		chooser.setDialogTitle("Halfedge Files");
		chooser.setCurrentDirectory(userDir);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "Halfedge XML (*.heml)";
			}
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".heml");
			}
		});
		
		
		rescanButton.addActionListener(this);
		clearSelectionButton.addActionListener(this);
		viewSelectionChecker.addActionListener(this);
		saveHDSButton.addActionListener(this);
		loadHDSButton.addActionListener(this);
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		if (rescanButton == e.getSource()) {
			ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
			cce.node = contentParseRoot;
			contentChangedListener.contentChanged(cce);
		}
		if (clearSelectionButton == e.getSource()) {
			selection.clear();
		}
		if(loadHDSButton == e.getSource()) {
			File file = null;
			if (chooser.showOpenDialog(w) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			}
			if (file != null) {
				HalfEdgeDataStructure<?, ?, ?> hds = HalfedgeIO.readHDS(file.getAbsolutePath());
				set(hds, getAdapters());
			}
		}
		if(saveHDSButton == e.getSource()) {
			File file = null;
			if (chooser.showSaveDialog(w) == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
			}
			if (file != null) {
				HalfedgeIO.writeHDS(cachedHEDS, file.getAbsolutePath());
			}
		}
		updateStates();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (geometryList.getSelectedValue() == null) return;
		activeComponent = (SceneGraphComponent)geometryList.getSelectedValue();
		selection.clear();
		updateStates();
	}
	
	
	protected void updateStates() {
		DefaultListModel model = new DefaultListModel();
		for (Node<?,?,?> n : selection.getNodes()) {
			model.addElement(n);
		}
		selectionList.setModel(model);
		geometryList.setSelectedValue(activeComponent, true);
		if (cachedHEDS != null) {
			String text = cachedHEDS.getClass().getSimpleName() + ": ";
			text += "V" + cachedHEDS.numVertices() + " ";
			text += "E" + cachedHEDS.numEdges() + " ";
			text += "F" + cachedHEDS.numFaces() + " ";
			hdsLabel.setText(text);
		} else {
			hdsLabel.setText("No HDS cached");
		}
		hdsLabel.repaint();
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds, AdapterSet a) {
		AdapterSet all = new AdapterSet();
		if (a != null) all.addAll(a);
		all.addAll(adapters);
		Map<E, Integer> edgeMap = new HashMap<E, Integer>();
		IndexedFaceSet ifs = converterHeds2JR.heds2ifs(hds, all, edgeMap);
		this.edgeMap = edgeMap;
		updateCache(hds, a);
		if(ifs != null && ifs.getVertexAttributes(Attribute.COORDINATES) != null) {
			IndexedFaceSetUtility.calculateAndSetNormals(ifs);
			activeComponent.setGeometry(ifs);
		}
	}
	
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void set(HDS hds) {
		set(hds, null);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS hds, AdapterSet a) {
		if (!(activeComponent.getGeometry() instanceof IndexedFaceSet)) {
			return (HDS)cachedHEDS;
		}
		if (hds == null) {
			return (HDS)cachedHEDS;
		}
		if (cachedHEDS.getClass().isAssignableFrom(hds.getClass()) && !hdsIsDirty) {
			return (HDS)cachedHEDS;
		}
		AdapterSet all = new AdapterSet();
		if (a != null) all.addAll(a);
		all.addAll(adapters);
		hds.clear();
		IndexedFaceSet ifs = (IndexedFaceSet)activeComponent.getGeometry();
		boolean oriented = IndexedFaceSetUtility.makeConsistentOrientation(ifs);
		if (!oriented) {
			return hds;
		}
		Map<E, Integer> edgeMap = new HashMap<E, Integer>();
		converterJR2Heds.ifs2heds(ifs, hds, all, edgeMap);
		this.edgeMap = edgeMap;
		updateCache(hds, a);
		return hds;
	}
	
	
	public HalfEdgeDataStructure<?, ?, ?> get(AdapterSet a) {
		return get((HalfEdgeDataStructure<?, ?, ?>)null, a);
	}
	
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS get(HDS hds) {
		return get(hds, null);
	}
	
	
	public HalfEdgeDataStructure<?, ?, ?> get() {
		return get((HalfEdgeDataStructure<?, ?, ?>)null);
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> HDS createEmpty(HDS template) {
		try {
			return (HDS)template.getClass().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	
	public void update() {
		set(cachedHEDS, cachedAdapters);
	}
	
	
	/**
	 * Returns a collection of cached and normal adapters
	 * @param a
	 */
	public AdapterSet getAdapters() {
		AdapterSet result = new AdapterSet();
		result.addAll(cachedAdapters);
		result.addAll(adapters);
		return result;
	}
	
	public boolean addAdapter(Adapter<?> a) {
		return adapters.add(a);
	}
	
	public boolean removeAdapter(Adapter<?> a) {
		return adapters.remove(a);
	}
	
	
	public CalculatorSet getCalculators() {
		return calculators;
	}
	
	public boolean addCalculator(Calculator c) {
		return calculators.add(c);
	}
	
	public boolean removeCalculator(Calculator c) {
		return calculators.remove(c);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		content = JRViewerUtility.getContentPlugin(c);
		content.addContentChangedListener(contentChangedListener);
		scene = c.getPlugin(Scene.class);
		contentParseRoot = scene.getContentComponent();
		contentChangedListener.contentChanged(null);
		geometryList.addListSelectionListener(this);
		c.getPlugin(SelectionInterface.class);
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
		info.name = "Halfedge JReality Interface";
		info.vendorName = "Stefan Sechelmann";
		return info;
	}
	

	private void updateCache(HalfEdgeDataStructure<?, ?, ?> hds, AdapterSet adapters) {
		hdsIsDirty = false;
		cachedHEDS = hds;
		cachedAdapters.clear();
		if (adapters != null) {
			cachedAdapters.addAll(adapters);
		}
		fireHalfedgeChanged(this);
		updateStates();
	}
	
	
	/**
	 * @author josefsso
	 * If someone overwrites our content, immediately convert the new IFS to a HDS, then convert back with
	 * the adapters to a new IFS and rewrite. Unless skipNextUpdate is called before explicitly.
	 */
	public class HalfedgeContentListener implements ContentChangedListener {
		
		public void contentChanged(ContentChangedEvent cce) {
			final DefaultListModel model = new DefaultListModel();
			activeComponent = contentParseRoot;
			hdsIsDirty = true;
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
						model.addElement(c);
					}
					c.childrenAccept(this);
				}
			
			});
			geometryList.setModel(model);
			if (model.getSize() != 0) {
				geometryList.setSelectedIndex(0);
			}
			get(cachedHEDS, new AdapterSet());
			set(cachedHEDS, cachedAdapters);
			updateStates();
		}
		
	}
	
	protected SceneGraphComponent getActiveComponent() {
		return activeComponent;
	}
	
	protected Map<? extends Edge<?, ?, ?>, Integer> getEdgeMap() {
		return edgeMap;
	}
	
	public HalfedgeSelection getSelection() {
		return selection;
	}
	public void setSelection(HalfedgeSelection s) {
		selection = s;
		updateStates();
	}
	public void clearSelection() {
		selection.clear();
		updateStates();
	}
	
	
	protected HalfEdgeDataStructure<?, ?, ?> getCache() {
		return cachedHEDS;
	}
	
	
	public void addHalfedgeListener(HalfedgeListener l) {
		listeners.add(l);
	}
	
	public void removeHalfedgeListener(HalfedgeListener l) {
		listeners.remove(l);
	}
	
	
	protected void fireHalfedgeChanged(HalfedgeInterface hif) {
		for (HalfedgeListener l : listeners) {
			l.halfedgeChanged(hif);
		}
	}
	

}
