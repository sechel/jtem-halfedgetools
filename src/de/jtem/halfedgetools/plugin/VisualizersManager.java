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

import static javax.swing.ListSelectionModel.SINGLE_SELECTION;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import de.jtem.halfedgetools.plugin.swing.IconCellRenderer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.flavor.UIFlavor;

public class VisualizersManager extends Plugin implements ListSelectionListener, UIFlavor {

	private HalfedgeInterface
		hif = null;
	private List<VisualizerPlugin>
		visualizers = new LinkedList<VisualizerPlugin>();
	private JPanel
		optionsPanel = new JPanel();
	private JTable
		pluginTable = new JTable();
	private JScrollPane
		pluginScroller = new JScrollPane(pluginTable),
		optionsScroller = new JScrollPane(optionsPanel);
	private JPanel
		panel = new JPanel();
	
	public VisualizersManager() {
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.fill = GridBagConstraints.BOTH;
		gbc2.weightx = 1.0;
		gbc2.gridwidth = GridBagConstraints.REMAINDER;
		gbc2.insets = new Insets(2, 2, 2, 2);
		
		optionsPanel.setLayout(new GridLayout());
		
		pluginScroller.setPreferredSize(new Dimension(10, 150));
		pluginTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		pluginTable.getDefaultEditor(Boolean.class).addCellEditorListener(new PluginActivationListener());
		pluginTable.setRowHeight(22);
		pluginTable.getSelectionModel().addListSelectionListener(this);
		pluginTable.getSelectionModel().setSelectionMode(SINGLE_SELECTION);
		pluginTable.setBorder(BorderFactory.createEtchedBorder());
		optionsPanel.setPreferredSize(new Dimension(10, 100));
		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
		
		gbc2.weighty = 1.0;
		panel.add(pluginScroller, gbc2);
		gbc2.weighty = 0.0;
		panel.add(optionsScroller, gbc2);
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
					return isActive(op);
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
			updateContent();
			pluginTable.revalidate();
		}
		
	}
	
	
	public void updateContent() {
		HalfedgeSelection sel = hif.getSelection();
		hif.updateNoUndo();
		hif.setSelection(sel);
	}
	
	
	public boolean isActive(VisualizerPlugin op) {
		HalfedgeLayer layer = hif.getActiveLayer();
		return layer.getVisualizers().contains(op);
	}
	
	public void setActive(VisualizerPlugin op, boolean active) {
		HalfedgeLayer layer = hif.getActiveLayer();
		if (active) {
			layer.addVisualizer(op);
		} else {
			layer.removeVisualizer(op);
		}
	}
	
	
	public void update() {
		updatePluginTable();
	}
	
	public JPanel getPanel() {
		return panel;
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
	}
	
	
	private void updatePluginTable() {
		pluginTable.setModel(new PluginTableModel());
		pluginTable.getColumnModel().getColumn(0).setMaxWidth(30);
		pluginTable.getColumnModel().getColumn(0).setCellRenderer(new IconCellRenderer());
		pluginTable.getColumnModel().getColumn(1).setMaxWidth(30);
	}
	
	
	protected void addVisualizerPlugin(VisualizerPlugin vp) {
		visualizers.add(vp);
		updatePluginTable();
	}
	
	protected void removeVisualizerPlugin(VisualizerPlugin vp) {
		visualizers.remove(vp);
		updatePluginTable();
	}

	@Override
	public void mainUIChanged(String uiClass) {
		SwingUtilities.updateComponentTreeUI(optionsPanel);
		SwingUtilities.updateComponentTreeUI(pluginScroller);
	}

}
