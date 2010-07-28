package de.jtem.halfedgetools.plugin.algorithm.selection;

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
import de.jtem.halfedgetools.adapter.CalculatorException;
import de.jtem.halfedgetools.adapter.CalculatorSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.HalfedgeSelection;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;

public class ImportSelection extends AlgorithmPlugin implements UIFlavor {

	private View
		view = null;
	private JFileChooser
		selChooser = new JFileChooser();
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
		return -2;
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
		if (selChooser.showOpenDialog(w) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = selChooser.getSelectedFile();
		try {
			hcp.clearSelection();
			HalfedgeSelection sel = new HalfedgeSelection();
			FileReader fr = new FileReader(file);
			int[][] indices = (int[][])xstream.fromXML(fr);
			for (int vi : indices[0]) {
				if (vi < hds.numVertices()) {
					Vertex<?,?,?> vertex = hds.getVertex(vi);
					sel.setSelected(vertex, true);
				}
			}
			for (int ei : indices[1]) {
				if (ei < hds.numEdges()) {
					Edge<?,?,?> edge = hds.getEdge(ei);
					sel.setSelected(edge, true);
				}
			}
			for (int fi : indices[2]) {
				if (fi < hds.numFaces()) {
					Face<?,?,?> face = hds.getFace(fi);
					sel.setSelected(face, true);
				}
			}
			hcp.setSelection(sel);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
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
