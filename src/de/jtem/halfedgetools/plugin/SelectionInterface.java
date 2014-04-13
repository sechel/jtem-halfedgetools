package de.jtem.halfedgetools.plugin;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.ui.ButtonCellEditor;
import de.jtem.halfedgetools.ui.ButtonCellRenderer;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class SelectionInterface extends ShrinkPanelPlugin implements ActionListener {

	private ButtonGroup
		activationButtonGroup = new ButtonGroup();	
	private List<Channel>
		channels = new ArrayList<Channel>();
	private Channel
		defaultChannel = new Channel(0, "Default Channel");
	private Channel
		activeChannel = defaultChannel;

	private Icon
		removeIcon = ImageHook.getIcon("remove.png");
	private JButton
		addChannelButton = new JButton("New Channel");
	private ChannelModel
		channelModel = new ChannelModel();
	private JTable
		channelTable = new JTable(new ChannelModel());
	private JScrollPane
		channelScroller = new JScrollPane(channelTable);
	
	public SelectionInterface() {
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		channelScroller.setMinimumSize(new Dimension(30, 100));
		shrinkPanel.add(channelScroller, c);
		c.weighty = 0.0;
		shrinkPanel.add(addChannelButton, c);
		
		addChannelButton.addActionListener(this);
		
		updateChannelTable();
		activeChannel.activationButton.setSelected(true);
	}

	private class Channel {
		
		public Integer 
			channel = -1;
		public String
			name = "";
		public DeleteChannelButton
			deleteButton = new DeleteChannelButton(this);
		public ActivationRadioButton
			activationButton = new ActivationRadioButton(this);

		private Channel(Integer channel, String name) {
			this.channel = channel;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
	}
	
	private class ActivationRadioButton extends JRadioButton implements ActionListener {
		
		private static final long 
			serialVersionUID = 1L;
		private Channel
			channel = null;

		private ActivationRadioButton(Channel channel) {
			this.channel = channel;
			addActionListener(this);
			activationButtonGroup.add(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (isSelected()) {
				activeChannel = channel;
			}
			for (int i = 0; i < channelModel.getRowCount(); i++) {
				channelModel.fireTableCellUpdated(i, 1);
			}
		}
		
	}
	
	private class DeleteChannelButton extends JButton implements ActionListener {
		
		private static final long 
			serialVersionUID = 1L;
		private Channel
			channel = null;
		
		public DeleteChannelButton(Channel c) {
			super(removeIcon);
			this.channel = c;
			addActionListener(this);
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			channels.remove(channel);
			activeChannel = defaultChannel;
			activationButtonGroup.remove(channel.activationButton);
			channelModel.fireTableDataChanged();
		}
		
	}
	
	private class ChannelModel extends AbstractTableModel {

		private static final long 
			serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return channels.size() + 1;
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 1:
				return JRadioButton.class;
			case 2:
				return JButton.class;
			}
			return String.class;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Channel c = defaultChannel;
			if (row > 0) {
				c = channels.get(row - 1);
			}
			switch (col) {
			case 0:
				return c;
			case 1:
				return c.activationButton;
			case 2:
				return c.deleteButton;
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0 && rowIndex > 0 && aValue instanceof String) {
				Channel c = channels.get(rowIndex - 1);
				c.name = (String)aValue;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
		
	}
	
	private void updateChannelTable() {
		channelTable.setModel(channelModel);
		channelTable.setRowHeight(22);
		channelTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		channelTable.getColumnModel().getColumn(1).setMaxWidth(30);
		channelTable.getColumnModel().getColumn(2).setMaxWidth(30);
		channelTable.setDefaultRenderer(AbstractButton.class, new ButtonCellRenderer());
		channelTable.setDefaultEditor(AbstractButton.class, new ButtonCellEditor());
	}
	
	private Integer generateChannelId() {
		Integer id = 1;
		boolean taken = true;
		while (taken) {
			taken = false;
			for (Channel c : channels) {
				if (c.channel.equals(id)) {
					taken = true;
					break;
				}
			}
			if (!taken) {
				break;
			} else {
				id++;
			}
		}
		return id;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (addChannelButton == s) {
			Integer channel = generateChannelId();
			Channel newChannel = new Channel(channel, "Channel " + channel);
			channels.add(newChannel);
			channelModel.fireTableDataChanged();
		}
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
