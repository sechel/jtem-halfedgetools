package de.jtem.halfedgetools.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.SwingUtilities;

import de.jreality.plugin.JRViewerUtility;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.ToolContext;
import de.jreality.tools.ActionTool;
import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class SelectionInterface extends Plugin implements ActionListener, HalfedgeListener {

	private HalfedgeSelection
		selection = new HalfedgeSelection();
	private HalfedgeInterface
		hif = null;
	private ActionTool
		actionTool1 = new ActionTool("PrimaryAction"),
		actionTool2 = new ActionTool("SecondaryAction");
	private List<SelectionListener>
		listeners = new LinkedList<SelectionListener>();
	private VisualizersManager
		visManager = null;
	
	public SelectionInterface() {
		actionTool1.addActionListener(this);
		actionTool2.addActionListener(this);
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void halfedgeConverting(HDS hds, AdapterSet a, HalfedgeInterface hif) {
	}
	
	@Override
	public < 
		V extends Vertex<V, E, F>,
		E extends Edge<V, E, F>,
		F extends Face<V, E, F>,
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void halfedgeChanged(HDS hds, AdapterSet a, HalfedgeInterface hif) {
		selection.clear();
		fireSelectionChanged(selection);
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
		visManager = c.getPlugin(VisualizersManager.class);
		visManager.setActive(c.getPlugin(SelectionVisualizer.class), true);
		JRViewerUtility.getContentPlugin(c).addContentTool(actionTool1);
		JRViewerUtility.getContentPlugin(c).addContentTool(actionTool2);
	}

	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		hif.removeHalfedgeListener(this);
		JRViewerUtility.getContentPlugin(c).removeContentTool(actionTool1);
		JRViewerUtility.getContentPlugin(c).removeContentTool(actionTool2);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return super.getPluginInfo();
	}

	
	@Override
	public void actionPerformed(ActionEvent e) {
		ToolContext tc = (ToolContext)e.getSource();
		SelectionUpdater updater = new SelectionUpdater(tc);
		SwingUtilities.invokeLater(updater);
	}
	
	
	private class SelectionUpdater implements Runnable {
		
		private ToolContext
			toolContext = null;
		
		public SelectionUpdater(ToolContext toolContext) {
			this.toolContext = toolContext;
		}

		@Override
		public void run() {
			PickResult pr = null;
			try {
				pr = toolContext.getCurrentPick();
			} catch (Exception e) {
				return;
			}
			if (pr == null) return;
			HalfEdgeDataStructure<?, ?, ?> hds = hif.getCache();
			if (hds == null) return;
			int index = pr.getIndex();
			if (index < 0) return;
			
			switch (pr.getPickType()) {
			case PickResult.PICK_TYPE_POINT:
				if (hds.numVertices() <= index) return;
				Vertex<?,?,?> v = hds.getVertex(index);
				boolean selected = isSelected(v);
				setSelected(v, !selected);
				break;
			case PickResult.PICK_TYPE_LINE:
				Map<? extends Edge<?,?,?>, Integer> edgeMap = hif.getEdgeMap();
				TreeSet<Integer> indexSet = new TreeSet<Integer>();
				indexSet.addAll(edgeMap.values());
				if (!indexSet.contains(index)) {
					System.err.println("Edge index not found");
					return;
				}
				List<Edge<?,?,?>> eList = new LinkedList<Edge<?,?,?>>();
				for (Edge<?,?,?> edge : edgeMap.keySet()) {
					if (edgeMap.get(edge).equals(index)) {
						eList.add(edge);
					}
				}
				for (Edge<?,?,?> edge : eList) {
					selected = isSelected(edge);
					selection.setSelected(edge, !selected);
				}
				fireSelectionChanged(selection);
				break;
			case PickResult.PICK_TYPE_FACE:
				if (hds.numFaces() <= index) return;
				Face<?,?,?> f = hds.getFace(index);
				selected = isSelected(f);
				setSelected(f, !selected);
				break;
			default:
				return;
			}
			hif.updateStates();			
		}
	}
	
	
	public void addSelectionListener(SelectionListener l) {
		listeners.add(l);
	}
	
	public void removeSelectionListener(SelectionListener l) {
		listeners.remove(l);
	}
	
	protected void fireSelectionChanged(HalfedgeSelection s) {
		for (SelectionListener l : listeners) {
			l.selectionChanged(s, this);
		}
	}
	
	public HalfedgeSelection getSelection() {
		return new HalfedgeSelection(selection);
	}
	public void setSelection(HalfedgeSelection s) {
		selection = s;
		hif.updateStates();
		fireSelectionChanged(selection);
	}
	public void clearSelection() {
		selection.clear();
		hif.updateStates();
		fireSelectionChanged(selection);
	}
	
	public void setSelected(Node<?,?,?> n, boolean selected) {
		selection.setSelected(n, selected);
		hif.updateStates();
		fireSelectionChanged(selection);
	}
	
	public boolean isSelected(Node<?,?,?> n) {
		return selection.isSelected(n);
	}
	
}
