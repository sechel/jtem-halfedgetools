package de.jtem.halfedgetools.plugin.algorithm.selection;

import static javax.swing.JFileChooser.APPROVE_OPTION;

import java.awt.EventQueue;
import java.awt.Window;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.SelectionInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;

public class ImportSelection extends AlgorithmPlugin implements UIFlavor {

	private final int
		CHANNEL_IMPORTED_SELECTION = 234234;
	private View
		view = null;
	private JFileChooser
		selChooser = new JFileChooser();
	private volatile int
		fileChooserResult = -1;
	private XStream 
		xstream = new XStream(new PureJavaReflectionProvider());
	
	public ImportSelection() {
		File userDir = new File(System.getProperty("user.dir"));
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
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}

	@Override
	public String getAlgorithmName() {
		return "Import";
	}
	
	@Override
	public double getPriority() {
		return -3;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, AdapterSet a, HalfedgeInterface hcp) throws Exception {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());	
				fileChooserResult = selChooser.showOpenDialog(w);
			}
		};
		try {
			EventQueue.invokeAndWait(r);
		} catch (Exception e) {}
		if (fileChooserResult != APPROVE_OPTION) return;
		File file = selChooser.getSelectedFile();
		Selection sel = new Selection();
		FileReader fr = new FileReader(file);
		int[][] indices = (int[][])xstream.fromXML(fr);
		for (int vi : indices[0]) {
			if (vi < hds.numVertices()) {
				sel.add(hds.getVertex(vi));
			}
		}
		for (int ei : indices[1]) {
			if (ei < hds.numEdges()) {
				sel.add(hds.getEdge(ei));
			}
		}
		for (int fi : indices[2]) {
			if (fi < hds.numFaces()) {
				sel.add(hds.getFace(fi));
			}
		}
		hcp.addSelection(sel);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		SelectionInterface sif = c.getPlugin(SelectionInterface.class);
		sif.registerChannelName(CHANNEL_IMPORTED_SELECTION, "Imported Selection");
	} 
	
	@Override
	public void storeStates(Controller c) throws Exception {
		super.storeStates(c);
		c.storeProperty(getClass(), "fileChooserLocation", selChooser.getCurrentDirectory().toString());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		super.restoreStates(c);
		selChooser.setCurrentDirectory(new File(c.getProperty(getClass(), "fileChooserLocation", selChooser.getCurrentDirectory().toString())));
	}
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Halfedge Selection Import");
		info.icon = ImageHook.getIcon("folder.png");
		return info;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		SwingUtilities.updateComponentTreeUI(selChooser);
	}
	
	
}
