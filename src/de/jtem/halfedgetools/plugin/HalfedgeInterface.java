package de.jtem.halfedgetools.plugin;

import static java.awt.GridBagConstraints.BOTH;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import de.jreality.geometry.GeometryMergeFactory;
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
import de.jtem.discretegroup.core.DiscreteGroup;
import de.jtem.discretegroup.core.DiscreteGroupSceneGraphRepresentation;
import de.jtem.discretegroup.core.DiscreteGroupSimpleConstraint;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedge.util.HalfEdgeUtils;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.Calculator;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.adapter.generic.NormalAdapter;
import de.jtem.halfedgetools.io.HalfedgeIO;
import de.jtem.halfedgetools.jreality.ConverterHeds2JR;
import de.jtem.halfedgetools.jreality.ConverterJR2Heds;
import de.jtem.halfedgetools.jreality.adapter.JRNormalAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRPositionAdapter;
import de.jtem.halfedgetools.jreality.adapter.JRTexCoordAdapter;
import de.jtem.halfedgetools.jreality.calculator.JRFaceAreaCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRFaceNormalCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRSubdivisionCalculator;
import de.jtem.halfedgetools.jreality.calculator.JRVertexPositionCalculator;
import de.jtem.halfedgetools.jreality.node.DefaultJRHDS;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.symmetry.node.SHDS;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;


public class HalfedgeInterface extends ShrinkPanelPlugin implements ListSelectionListener, ActionListener {

	private Scene
		scene = null;
	private Content
		content = null;
	private SelectionInterface
		selectionInterface = null;
	private AuxSceneGraphComponent
		auxComponent = new AuxSceneGraphComponent();
	private SceneGraphComponent
		contentParseRoot = null;
	private SpinnerNumberModel
		selectIndexModel = new SpinnerNumberModel(0, 0, 10000000, 1);
	private JSpinner
		selectSpinner = new JSpinner(selectIndexModel);
	private JButton
		selectVertexButton = new JButton("V"),
		selectEdgeButton = new JButton("E"),
		selectFaceButton = new JButton("F"),
		selectBoundaryButton = new JButton("Boundary Vertices");
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
		saveSelectionButton = new JButton(ImageHook.getIcon("disk.png")),
		loadSelectionButton = new JButton(ImageHook.getIcon("folder.png")),
		clearSelectionButton = new JButton("Clean Selection"),
		rescanButton = new JButton("Rescan");
	private JLabel
		hdsLabel = new JLabel("No HDS cached");
	private JCheckBox
		viewSelectionChecker = new JCheckBox("View Selection", true);
	private JFileChooser 
		chooser = new JFileChooser(),
		selChooser = new JFileChooser();
	
	private boolean
		automaticConversion = true,
		hdsIsDirty = true;
	private HalfEdgeDataStructure<?, ?, ?> 
		cachedHEDS = new DefaultJRHDS();
	private Map<? extends Edge<?,?,?>, Integer>
		edgeMap = new HashMap<Edge<?,?,?>, Integer>();
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
	private XStream 
		xstream = new XStream(new PureJavaReflectionProvider());
	
	public HalfedgeInterface() {
		makeLayout();
		adapters.add(new JRNormalAdapter());
		adapters.add(new JRPositionAdapter());
		adapters.add(new JRTexCoordAdapter());
		adapters.add(new NormalAdapter());
		calculators.add(new JRVertexPositionCalculator());
		calculators.add(new JRFaceAreaCalculator());
		calculators.add(new JRFaceNormalCalculator());
		calculators.add(new JRSubdivisionCalculator());
		auxComponent.setPickable(false);
	}
	
	
	public class AuxSceneGraphComponent extends SceneGraphComponent {
		
		public AuxSceneGraphComponent() {
			super("Halfedge Aux");
		}
		
		public void startWriting() {
			startWriter();
		}
		
		public void finishWriting() {
			finishWriter();
		}
		
	}
	
	
	private void makeLayout() {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = BOTH;
		c.weightx = 1.0;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
				
		geometryList.setCellRenderer(new GeometryListRenderer());
		geometriesScroller.setMinimumSize(new Dimension(30, 70));
		JPanel adaptersPanel = new JPanel();
		adaptersPanel.setLayout(new GridLayout());
		adaptersPanel.add(geometriesScroller);
		adaptersPanel.setBorder(BorderFactory.createTitledBorder("Available Geometries"));
		
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(hdsLabel, c);
		c.gridwidth = 1;
		c.weightx = 0.0;
		shrinkPanel.add(loadHDSButton, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		shrinkPanel.add(saveHDSButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		shrinkPanel.add(new JPanel(), c);
		c.weightx = 0.0;
		c.weighty = 1.0;
		shrinkPanel.add(adaptersPanel, c);
		c.weighty = 0.0;
		shrinkPanel.add(rescanButton, c);
		
		selectionScroller.setMinimumSize(new Dimension(30, 70));
		JPanel selectionPanel = new JPanel();
		selectionPanel.setLayout(new GridBagLayout());
		c.weighty = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		selectionPanel.add(selectionScroller, c);
		selectionPanel.setBorder(BorderFactory.createTitledBorder("Selection"));
		c.weightx = 0.0;
		c.gridwidth = 1;
		selectSpinner.setPreferredSize(new Dimension(100,20));
		selectionPanel.add(selectSpinner, c);
		c.weightx = 0.0;
		selectionPanel.add(selectVertexButton, c);
		selectionPanel.add(selectEdgeButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		selectionPanel.add(selectFaceButton, c);
		selectionPanel.add(selectBoundaryButton, c);
		c.gridwidth = GridBagConstraints.RELATIVE;
		selectionPanel.add(saveSelectionButton, c);
		selectionPanel.add(loadSelectionButton, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		selectionPanel.add(new JPanel(), c);
		
		c.weighty = 1.0;
		shrinkPanel.add(selectionPanel, c);
		c.weightx = 1.0;
		shrinkPanel.add(clearSelectionButton, c);
		
		File userDir = new File(System.getProperty("user.dir"));
		chooser.setDialogTitle("Halfedge Export");
		chooser.setCurrentDirectory(userDir);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.addChoosableFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".obj");
			}

			@Override
			public String getDescription() {
				return "Wavefront OBJ (*.obj)";
			}
		});
		
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
		
		selChooser.setDialogTitle("Halfedge Selection");
		selChooser.setCurrentDirectory(userDir);
		selChooser.setAcceptAllFileFilterUsed(false);
		selChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		selChooser.setMultiSelectionEnabled(false);
		selChooser.setFileFilter(new FileFilter(){
			@Override
			public boolean accept(File f) {
				return f.isDirectory() || f.getName().toLowerCase().endsWith(".sml");
			}

			@Override
			public String getDescription() {
				return "Selection XML (*.sml)";
			}
		});
		
		selectVertexButton.addActionListener(this);
		selectEdgeButton.addActionListener(this);
		selectFaceButton.addActionListener(this);
		selectBoundaryButton.addActionListener(this);
		rescanButton.addActionListener(this);
		clearSelectionButton.addActionListener(this);
		viewSelectionChecker.addActionListener(this);
		saveHDSButton.addActionListener(this);
		loadHDSButton.addActionListener(this);
		saveSelectionButton.addActionListener(this);
		loadSelectionButton.addActionListener(this);
	}
	
	
	private class GeometryListRenderer extends DefaultListCellRenderer {
		
		private static final long 
			serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(
			JList list, 
			Object value, 
			int index, 
			boolean 
			isSelected, 
			boolean cellHasFocus
		) {
			DefaultListCellRenderer c = (DefaultListCellRenderer)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof SceneGraphComponent) {
				SceneGraphComponent sgc = (SceneGraphComponent)value;
				if (sgc.getGeometry() != null) {
					setText(sgc.getGeometry().getName());
				} else {
					sgc.getName();
				}
			}
			return c;
		}
		
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Window w = SwingUtilities.getWindowAncestor(shrinkPanel);
		if (rescanButton == e.getSource()) {
			rescan();
		}
		if (clearSelectionButton == e.getSource()) {
			selectionInterface.clearSelection();
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
				if(file.getName().toLowerCase().endsWith(".heml")) {
					HalfedgeIO.writeHDS(cachedHEDS, file.getAbsolutePath());
				} else if(file.getName().toLowerCase().endsWith(".obj")) {
					HalfedgeIO.writeOBJ(cachedHEDS,adapters,file.getAbsolutePath());
				}
			}
		}
		if (selectVertexButton == e.getSource()) {
			int index = selectIndexModel.getNumber().intValue();
			if (cachedHEDS == null || index >= cachedHEDS.numVertices()) return;
			Vertex<?,?,?> n = cachedHEDS.getVertex(index);
			boolean selected = selectionInterface.isSelected(n);
			selectionInterface.setSelected(n, !selected);
		}
		if (selectEdgeButton == e.getSource()) {
			int index = selectIndexModel.getNumber().intValue();
			if (cachedHEDS == null || index >= cachedHEDS.numEdges()) return;
			Edge<?,?,?> n = cachedHEDS.getEdge(index);
			boolean selected = selectionInterface.isSelected(n);
			selectionInterface.setSelected(n, !selected);
		}
		if (selectFaceButton == e.getSource()) {
			int index = selectIndexModel.getNumber().intValue();
			if (cachedHEDS == null || index >= cachedHEDS.numFaces()) return;
			Face<?,?,?> n = cachedHEDS.getFace(index);
			boolean selected = selectionInterface.isSelected(n);
			selectionInterface.setSelected(n, !selected);
		}
		if (selectBoundaryButton == e.getSource()) {
			int index = selectIndexModel.getNumber().intValue();
			if (cachedHEDS == null || index >= cachedHEDS.numVertices()) return;
			for(Vertex<?,?,?> v : HalfEdgeUtils.boundaryVertices(cachedHEDS)){
				boolean sel = selectionInterface.isSelected(v);
				selectionInterface.setSelected(v, !sel);
			}
		}
		if (saveSelectionButton == e.getSource()) {
			if (selChooser.showSaveDialog(w) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = selChooser.getSelectedFile();
			HalfedgeSelection sel = getSelection();
			int[] vIndices = new int[sel.getVertices().size()];
			int[] eIndices = new int[sel.getEdges().size()];
			int[] fIndices = new int[sel.getFaces().size()];
			int i = 0;
			for (Vertex<?,?,?> vertex : sel.getVertices()) {
				vIndices[i++] = vertex.getIndex();
			}
			i = 0;
			for (Edge<?,?,?> edge : sel.getEdges()) {
				eIndices[i++] = edge.getIndex();
			}
			i = 0;
			for (Face<?,?,?> face : sel.getFaces()) {
				fIndices[i++] = face.getIndex();
			}
			int[][] indices = {vIndices, eIndices, fIndices};
			String selXML = xstream.toXML(indices);
			try {
				FileWriter fw = new FileWriter(file);
				fw.write(selXML);
				fw.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (loadSelectionButton == e.getSource()) {
			if (selChooser.showOpenDialog(w) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			File file = selChooser.getSelectedFile();
			try {
				selectionInterface.clearSelection();
				HalfedgeSelection sel = new HalfedgeSelection();
				FileReader fr = new FileReader(file);
				int[][] indices = (int[][])xstream.fromXML(fr);
				for (int vi : indices[0]) {
					if (vi < cachedHEDS.numVertices()) {
						Vertex<?,?,?> vertex = cachedHEDS.getVertex(vi);
						sel.setSelected(vertex, true);
					}
				}
				for (int ei : indices[1]) {
					if (ei < cachedHEDS.numEdges()) {
						Edge<?,?,?> edge = cachedHEDS.getEdge(ei);
						sel.setSelected(edge, true);
					}
				}
				for (int fi : indices[2]) {
					if (fi < cachedHEDS.numFaces()) {
						Face<?,?,?> face = cachedHEDS.getFace(fi);
						sel.setSelected(face, true);
					}
				}
				selectionInterface.setSelection(sel);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		updateStates();
	}

	
	public void rescan() {
		ContentChangedEvent cce = new ContentChangedEvent(ChangeEventType.ContentChanged);
		cce.node = contentParseRoot;
		contentChangedListener.contentChanged(cce);
	}
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (geometryList.getSelectedValue() == null) return;
		activeComponent = (SceneGraphComponent)geometryList.getSelectedValue();
		selectionInterface.clearSelection();
		updateStates();
	}
	
	
	protected void updateStates() {
		if (selectionInterface != null) {
			DefaultListModel model = new DefaultListModel();
			for (Node<?,?,?> n : selectionInterface.getSelection().getNodes()) {
				model.addElement(n);
			}
			selectionList.setModel(model);
		}
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
	> void set(final HDS hds, final AdapterSet a) {
		fireHalfedgeConverting(hds);
		AdapterSet all = new AdapterSet();
		if (a != null) all.addAll(a);
		all.addAll(adapters);
		Map<E, Integer> edgeMap = new HashMap<E, Integer>();
		final IndexedFaceSet ifs = converterHeds2JR.heds2ifs(hds, all, edgeMap);
		HalfedgeInterface.this.edgeMap = edgeMap;
		try {
			Runnable r = new Runnable() {
				@Override
				public void run() {
					de.jreality.scene.Scene.executeWriter(activeComponent, new Runnable() {
						public void run() {
							updateCache(hds, a);
							if (ifs != null) {
								
								if(SHDS.class.isAssignableFrom(hds.getClass())) {
									SHDS shds = (SHDS)hds;
									DiscreteGroup dg = shds.getGroup();

									DiscreteGroupSimpleConstraint dgsc = new DiscreteGroupSimpleConstraint(-1,-1,3+(int)Math.round(Math.pow(dg.getGenerators().length/2,2.0)));
									dgsc.setManhattan(true);
									dg.setConstraint(dgsc);
									DiscreteGroupSceneGraphRepresentation repn = new DiscreteGroupSceneGraphRepresentation(dg, true);

									activeComponent.setGeometry(ifs);
									repn.setWorldNode(activeComponent);

									repn.update();
									
									SceneGraphComponent ss = repn.getRepresentationRoot(); 

									GeometryMergeFactory gmf2 = new GeometryMergeFactory();
									activeComponent.setGeometry(gmf2.mergeIndexedFaceSets(ss));
								} else {
									activeComponent.setGeometry(ifs);
								}
							}
							if (selectionInterface != null) {
								selectionInterface.clearSelection();
							}
						}
					});
				}
			};
			if (SwingUtilities.isEventDispatchThread()) {
				r.run();
			} else {
				SwingUtilities.invokeAndWait(r);
			}
		} catch (Exception e) { }
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
		if (a == null) a = new AdapterSet();
		if (!(activeComponent.getGeometry() instanceof IndexedFaceSet)) {
			return hds;
		}
		if (hds == null) {
			return (HDS)cachedHEDS;
		}
		if (hds.getClass().isAssignableFrom(cachedHEDS.getClass()) && 
			!hdsIsDirty && 
			a.isEmpty()
		) {
			return (HDS)cachedHEDS;
		}
		AdapterSet all = new AdapterSet();
		all.addAll(a);
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
		selectionInterface = c.getPlugin(SelectionInterface.class);
		adapters.add(new SelectionAdapter(selectionInterface));
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
		fireHalfedgeChanged(hds);
		updateStates();
	}
	

	public class HalfedgeContentListener implements ContentChangedListener {
		
		public void contentChanged(ContentChangedEvent cce) {
			final DefaultListModel model = new DefaultListModel();
			activeComponent.removeChild(auxComponent);
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
					if (!c.isVisible() || c == auxComponent) {
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
			activeComponent.addChild(auxComponent);
			get(createEmpty(cachedHEDS), new AdapterSet());
			if (automaticConversion) {
				set(cachedHEDS, cachedAdapters);
			}
			updateStates();
		}
		
	}
	
	public SceneGraphComponent getActiveComponent() {
		return activeComponent;
	}
	
	public SceneGraphComponent getAuxComponent() {
		return auxComponent;
	}
	
	protected Map<? extends Edge<?, ?, ?>, Integer> getEdgeMap() {
		return edgeMap;
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
	
	
	protected < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void fireHalfedgeConverting(HDS hds) {
		for (HalfedgeListener l : listeners) {
			l.halfedgeConverting(hds, getAdapters(), this);
		}
	}
	
	protected < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void fireHalfedgeChanged(HDS hds) {
		for (HalfedgeListener l : listeners) {
			l.halfedgeChanged(hds, getAdapters(), this);
		}
	}
	
	
	public HalfedgeSelection getSelection() {
		return selectionInterface.getSelection();
	}
	public void setSelection(HalfedgeSelection s) {
		selectionInterface.setSelection(s);
	}
	public void clearSelection() {
		selectionInterface.clearSelection();
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected) {
		selectionInterface.setSelected(n, selected);
	}
	
	public boolean isSelected(Node<?,?,?> n) {
		return selectionInterface.isSelected(n);
	}
	
	
	public boolean isAutomaticConversion() {
		return automaticConversion;
	}
	public void setAutomaticConversion(boolean automaticConversion) {
		this.automaticConversion = automaticConversion;
	}

}
