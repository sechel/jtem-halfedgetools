package de.jtem.halfedgetools.plugin;

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import de.jreality.plugin.basic.View;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedgetools.jreality.adapter.Adapter;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class VisualizersManager extends ShrinkPanelPlugin implements ListSelectionListener {

	@SuppressWarnings("unchecked")
	private HalfedgeInterfacePlugin
		hif = null;
	private List<VisualizerPlugin>
		visualizers = new LinkedList<VisualizerPlugin>();
	private Set<String>
		activateSet = new HashSet<String>();
	private JPanel
		optionsPanel = new JPanel();
	private JTable
		pluginTable = new JTable();
	private Map<VisualizerPlugin, Set<? extends Adapter>>
		adapterMap = new HashMap<VisualizerPlugin, Set<? extends Adapter>>();
	
	
	public VisualizersManager() {
		shrinkPanel.setTitle("Visualizers");
		
		shrinkPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		optionsPanel.setLayout(new GridLayout());
		
		pluginTable.setPreferredSize(new Dimension(10, 150));
		pluginTable.getDefaultEditor(Boolean.class).addCellEditorListener(new PluginActivationListener());
		pluginTable.setRowHeight(22);
		pluginTable.getSelectionModel().addListSelectionListener(this);
		pluginTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		pluginTable.setBorder(BorderFactory.createEtchedBorder());
		optionsPanel.setPreferredSize(new Dimension(10, 100));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		shrinkPanel.add(pluginTable, gbc2);
		shrinkPanel.add(optionsPanel, gbc2);
	}
	
	
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = pluginTable.getSelectedRow();
		if (pluginTable.getRowSorter() != null) {
			row = pluginTable.getRowSorter().convertRowIndexToModel(row);
		}
		if (row < 0 || row >= visualizers.size()) return;
		optionsPanel.removeAll();
		VisualizerPlugin p = visualizers.get(row);
		if (p.getOptionPanel() == null) {
			optionsPanel.add(new JLabel("No Options"));
			optionsPanel.updateUI();
			return;
		}
		optionsPanel.add(p.getOptionPanel());
		optionsPanel.updateUI();
	}
	
	
	private class PluginTableModel extends DefaultTableModel {

		private static final long 
			serialVersionUID = 1L;
		
		@Override
		public int getRowCount() {
			return visualizers.size();
		}
		
		@Override
		public int getColumnCount() {
			return 3;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Icon.class;
				case 1: return Boolean.class;
				case 2: return VisualizerPlugin.class;
				default: return String.class;
			}
		}
		
		@Override
		public Object getValueAt(int row, int column) {
			if (row < 0 || row >= visualizers.size()) {
				return "-";
			}
			VisualizerPlugin op = visualizers.get(row);
			Object value = null;
			switch (column) {
				case 0:
					return op.getPluginInfo().icon;
				case 1: 
					return activateSet.contains(op.getName());
				case 2:
					value = op;
					break;
				default: 
					value = "-";
					break;
			}
			return value;
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			switch (column) {
				case 1:
				case 3:
					return true;
				default: 
					return false;
			}
		}
		
		
	}
	
	
	private class PluginActivationListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			int row = pluginTable.getSelectedRow();
			if (pluginTable.getRowSorter() != null) {
				row = pluginTable.getRowSorter().convertRowIndexToModel(row);
			}
			VisualizerPlugin op = visualizers.get(row);
			setActive(op, !isActive(op));
			if (isActive(op)) {
				for (Adapter a : adapterMap.get(op)) {
					hif.addAdapter(a);
				}
			} else {
				for (Adapter a : adapterMap.get(op)) {
					hif.removeAdapter(a);
				}
			}
			updateContent();
		}
		
	}
	
	
	@SuppressWarnings("unchecked")
	public void updateContent() {
		HalfEdgeDataStructure hds = hif.getCachedHalfEdgeDataStructure();
		hif.updateHalfedgeContentAndActiveGeometry(hds);
	}
	
	
	private boolean isActive(VisualizerPlugin op) {
		return activateSet.contains(op.getName());
	}
	
	private void setActive(VisualizerPlugin op, boolean active) {
		if (!active) {
			activateSet.remove(op.getName());
		} else {
			activateSet.add(op.getName());
		}
	}
	
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterfacePlugin.class);
	}
	
	
	private void updatePluginTable() {
		pluginTable.setModel(new PluginTableModel());
		pluginTable.getColumnModel().getColumn(0).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(1).setMaxWidth(30);
	}
	
	
	public void addVisualizerPlugin(VisualizerPlugin vp) {
		visualizers.add(vp);
		updatePluginTable();
		adapterMap.put(vp, vp.getAdapters());
	}
	
	public void removeVisualizerPlugin(VisualizerPlugin vp) {
		visualizers.remove(vp);
		updatePluginTable();
		adapterMap.remove(vp);
	}
	
	
}
