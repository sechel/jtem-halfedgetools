package de.jtem.halfedgetools.plugin.algorithm.selection;

import java.awt.Window;
import java.io.File;
import java.io.FileWriter;

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

public class ExportSelection extends AlgorithmPlugin implements UIFlavor {

	private View
		view = null;
	private JFileChooser
		selChooser = new JFileChooser();
	private XStream 
		xstream = new XStream(new PureJavaReflectionProvider());
	
	public ExportSelection() {
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
		return "Export";
	}

	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void execute(HDS hds, CalculatorSet c, HalfedgeInterface hcp) throws CalculatorException {
		Window w = SwingUtilities.getWindowAncestor(view.getCenterComponent());
		if (selChooser.showSaveDialog(w) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File file = selChooser.getSelectedFile();
		HalfedgeSelection sel = hcp.getSelection();
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
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
	} 
	

	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = new PluginInfo("Halfedge Selection Export");
		info.icon = ImageHook.getIcon("disk.png");
		return info;
	}
	
	
	@Override
	public double getPriority() {
		return -2;
	}
	
	@Override
	public void mainUIChanged(String uiClass) {
		SwingUtilities.updateComponentTreeUI(selChooser);
	}
	
}
