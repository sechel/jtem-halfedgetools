package de.jtem.halfedgetools.plugin;

import static de.jtem.halfedgetools.selection.TypedSelection.CHANNEL_DEFAULT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import com.bric.swing.ColorPicker;

import de.jreality.plugin.basic.View;
import de.jreality.ui.ColorChooseJButton;
import de.jtem.halfedge.Node;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.halfedgetools.selection.Selection;
import de.jtem.halfedgetools.selection.SelectionListener;
import de.jtem.halfedgetools.ui.ButtonCellEditor;
import de.jtem.halfedgetools.ui.ButtonCellRenderer;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.PluginInfo;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class SelectionInterface extends ShrinkPanelPlugin implements ActionListener, HalfedgeListener, SelectionListener {

	private static Random
		rnd = new Random(0);
	public static final Color
		DEFAULT_COLOR = new Color(1,0.4f,0.2f); 
	private Icon
		removeIcon = ImageHook.getIcon("remove.png");
	private ButtonGroup
		activationButtonGroup = new ButtonGroup();	
	private LayerChannels
		channels = null;
	private Map<HalfedgeLayer, LayerChannels>
		layerMap = new HashMap<HalfedgeLayer, LayerChannels>();
	private HalfedgeInterface
		hif = null;
	private Map<Integer, String>
		channelNames = new HashMap<Integer, String>();

	private JPanel
		mainPanel = new JPanel();
	private JButton
		addChannelButton = new JButton("New Channel");
	private ChannelModel
		channelModel = new ChannelModel();
	private JTable
		channelTable = new JTable(new ChannelModel());
	private JScrollPane
		channelScroller = new JScrollPane(channelTable);
	
	public SelectionInterface() {
		mainPanel.setPreferredSize(new Dimension(100, 150));
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2);
		c.gridwidth = GridBagConstraints.REMAINDER;
		mainPanel.add(channelScroller, c);
		c.weighty = 0.0;
		mainPanel.add(addChannelButton, c);
		
		shrinkPanel.setLayout(new GridLayout(1,1));
		shrinkPanel.add(mainPanel);
		
		addChannelButton.addActionListener(this);
		
		updateChannelTable();
	}
	
	public Selection getFilteredSelection(HalfedgeLayer layer) {
		LayerChannels channels = layerMap.get(layer);
		if (channels == null) {
			return layer.getSelection();
		}
		Set<Integer> activeChannels = getAlgorithmChannels(layer);
		Selection input = layer.getSelection();
		Selection result = new Selection();
		for (Node<?,?,?> n : input) {
			Integer id = input.getChannel(n);
			if (activeChannels.contains(id)) {
				result.add(n, id);
			}
		}
		return result;
	}
	
	public void updateChannels(HalfedgeLayer layer) {
		if (!layerMap.containsKey(layer)) {
			layerMap.put(layer, new LayerChannels(layer));
		}
		LayerChannels channels = layerMap.get(layer);
		Selection s = layer.getSelection();
		for (Integer id : s.getChannels()) {
			Channel cc = null;
			for (Channel c : channels) {
				if (c.channel.equals(id)) {
					cc = c;
					break;
				}
			}
			if (cc == null) {
				String name = "Channel " + id;
				if (channelNames.containsKey(id)) {
					name = channelNames.get(id);
				}
				Channel c = new Channel(layer, id, name);
				channels.add(c);
			}
		}
		this.channels = channels;
		updateChannelTable();
	}
	
	public Set<Integer> getAlgorithmChannels(HalfedgeLayer layer) {
		List<Channel> channels = layerMap.get(layer);
		Set<Integer> result = new TreeSet<Integer>();
		for (Channel c : channels) {
			if (c.includeChannel) {
				result.add(c.channel);
			}
		}
		return result;
	}
	
	public Integer getActiveInputChannel(HalfedgeLayer layer) {
		LayerChannels channels = layerMap.get(layer);
		if (channels == null) {
			return CHANNEL_DEFAULT;
		} else {
			return channels.activeChannel.channel;
		}
	}
	
	public Map<Integer, Color> getChannelColors(HalfedgeLayer layer) {
		LayerChannels channels = layerMap.get(layer);
		Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
		for (Channel c : channels) {
			colorMap.put(c.channel, c.color);
		}
		return colorMap;
	}
	
	
	private void switchTo(JComponent content) {
		shrinkPanel.removeAll();
		shrinkPanel.setLayout(new GridLayout());
		shrinkPanel.add(content);
		shrinkPanel.updateUI();
		shrinkPanel.revalidate();
	}
	
	
	private class LayerChannels extends ArrayList<Channel> {

		private static final long 
			serialVersionUID = 1L;
		public Channel
			defaultChannel = null,
			activeChannel = null;
		public HalfedgeLayer
			layer = null;
		
		public LayerChannels(HalfedgeLayer layer) {
			this.layer = layer;
			this.defaultChannel = new Channel(layer, CHANNEL_DEFAULT, "Default Channel", DEFAULT_COLOR);
			this.activeChannel = this.defaultChannel;
			add(defaultChannel);
			activeChannel.activationButton.setSelected(true);
		}
		
	}

	private class Channel {
		
		public HalfedgeLayer
			layer = null;
		public Integer 
			channel = -1;
		public String
			name = "";
		public Color
			color = null;
		public boolean
			includeChannel = true;
		public DeleteChannelButton
			deleteButton = new DeleteChannelButton(this);
		public ActivationRadioButton
			activationButton = new ActivationRadioButton(this);
		public AlgorithmIncludeCheckBox
			algorithmIncludeCheckBox = new AlgorithmIncludeCheckBox(this);
		public ColorChooserButton
			colorChooseButton = new ColorChooserButton(this);

		private Channel(HalfedgeLayer layer, Integer channel, String name) {
			this.layer = layer;
			this.channel = channel;
			this.name = name;
			rnd.setSeed(channel);
			this.color = new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
		}
		
		private Channel(HalfedgeLayer layer, Integer channel, String name, Color color) {
			this.layer = layer;
			this.channel = channel;
			this.name = name;
			this.color = color;
		}
		
		@Override
		public String toString() {
			Selection s = layer.getSelection().getChannel(channel);
			int v = s.getVertices().size();
			int e = s.getEdges().size();
			int f = s.getFaces().size();
			return name + " - " + v + "/" + e + "/" + f;
		}
		
	}
	
	private class ColorChooserButton extends ColorChooseJButton implements ChangeListener, ActionListener {

		private static final long 
			serialVersionUID = 1L;
		private Channel
			channel = null;
		private JPanel
			colorChooserPanel = new JPanel();
		private ColorPicker
			colorPicker = new ColorPicker(false, false);
		private JButton
			closeButton = new JButton("Close");
		
		public ColorChooserButton(Channel channel) {
			this.channel = channel;
			
			GridBagConstraints c = new GridBagConstraints();
			c.weightx = 1.0;
			c.weighty = 1.0;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(2, 2, 2, 2);
			c.gridwidth = GridBagConstraints.REMAINDER;
			colorChooserPanel.setLayout(new GridBagLayout());
			colorPicker.setPreferredSize(new Dimension(150, 250));
			colorChooserPanel.add(colorPicker, c);
			c.weighty = 0.0;
			c.fill = GridBagConstraints.VERTICAL;
			c.anchor = GridBagConstraints.WEST;
			colorChooserPanel.add(closeButton, c);
			closeButton.addActionListener(this);
			colorPicker.getColorPanel().addChangeListener(this);
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			setColor(colorPicker.getColor());
			channel.color = getColor();
			hif.getActiveLayer().updateSelection();
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (this == e.getSource()) {
				colorPicker.setColor(getColor());
				switchTo(colorChooserPanel);
			} else {
				switchTo(mainPanel);
			}
		}
		
	}
	
	
	private class AlgorithmIncludeCheckBox extends JCheckBox implements ActionListener {

		private static final long 
			serialVersionUID = 1L;
		private Channel
			channel = null;
		
		public AlgorithmIncludeCheckBox(Channel channel) {
			this.channel = channel;
			addActionListener(this);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			channel.includeChannel = isSelected();
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
				channels.activeChannel = channel;
			}
			for (int i = 0; i < channelModel.getRowCount(); i++) {
				channelModel.fireTableCellUpdated(i, 2);
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
			channels.activeChannel = channels.defaultChannel;
			activationButtonGroup.remove(channel.activationButton);
			channel.layer.clearSelection(channel.channel);
			updateChannelTable();
		}
		
	}
	
	private class ChannelModel extends AbstractTableModel {

		private static final long 
			serialVersionUID = 1L;

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public int getRowCount() {
			return channels.size();
		}
		
		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
			case 1:
			case 2:
			case 3:
			case 4:
				return AbstractButton.class;
			}
			return String.class;
		}

		@Override
		public Object getValueAt(int row, int col) {
			Channel c = channels.get(row);
			switch (col) {
			case 0:
				return c;
			case 1:
				c.algorithmIncludeCheckBox.setSelected(c.includeChannel);
				return c.algorithmIncludeCheckBox;
			case 2:
				if (channels.activeChannel == c) {
					c.activationButton.setSelected(true);
				}
				return c.activationButton;
			case 3:
				c.colorChooseButton.setColor(c.color);
				return c.colorChooseButton;
			case 4:
				if (channels.defaultChannel == c) {
					c.deleteButton.setEnabled(false);
				}
				return c.deleteButton;
			}
			return null;
		}
		
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 0 && rowIndex > 0 && aValue instanceof String) {
				Channel c = channels.get(rowIndex);
				c.name = (String)aValue;
				fireTableCellUpdated(rowIndex, columnIndex);
			}
		}
		
	}
	
	private void updateChannelTable() {
		channelModel = new ChannelModel();
		channelTable.setModel(channelModel);
		channelTable.setRowHeight(22);
		channelTable.getTableHeader().setPreferredSize(new Dimension(10, 0));
		channelTable.getColumnModel().getColumn(1).setMaxWidth(30);
		channelTable.getColumnModel().getColumn(2).setMaxWidth(30);
		channelTable.getColumnModel().getColumn(3).setMaxWidth(30);
		channelTable.getColumnModel().getColumn(4).setMaxWidth(30);
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
			HalfedgeLayer layer = channels.layer;
			Integer channel = generateChannelId();
			Channel newChannel = new Channel(layer, channel, "Channel " + channel);
			channels.add(newChannel);
			channelModel.fireTableDataChanged();
		}
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.name = "Selection Interface";
		return info;
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addHalfedgeListener(this);
		hif.addSelectionListener(this);
		for (HalfedgeLayer layer : hif.getAllLayers()) {
			updateChannels(layer);
		}
		updateChannels(hif.getActiveLayer());
	}


	@Override
	public void dataChanged(HalfedgeLayer layer) {
	}
	@Override
	public void adaptersChanged(HalfedgeLayer layer) {
	}
	@Override
	public void activeLayerChanged(HalfedgeLayer old, HalfedgeLayer active) {
		updateChannels(active);
	}
	@Override
	public void layerCreated(HalfedgeLayer layer) {
	}
	@Override
	public void layerRemoved(HalfedgeLayer layer) {
		layerMap.remove(layer);
	}

	@Override
	public void selectionChanged(Selection s, HalfedgeInterface hif) {
		updateChannels(hif.getActiveLayer());
	}
	
	public void registerChannelName(Integer channel, String name) {
		channelNames.put(channel, name);
	}

}
