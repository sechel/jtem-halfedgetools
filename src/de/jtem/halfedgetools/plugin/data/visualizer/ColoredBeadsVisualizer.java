package de.jtem.halfedgetools.plugin.data.visualizer;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jreality.ui.LayoutFactory;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.data.color.ColorMap;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class ColoredBeadsVisualizer extends DataVisualizerPlugin implements ActionListener {
	
	private JComboBox
		colorMapCombo = new JComboBox(ColorMap.values());
	private JPanel
		optionsPanel = new JPanel();
	private ColoredBeadsVisualization
		actVis = null;
	
	public ColoredBeadsVisualizer() {
		optionsPanel.setLayout(new GridBagLayout());
		GridBagConstraints cl = LayoutFactory.createLeftConstraint();
		GridBagConstraints cr = LayoutFactory.createRightConstraint();
		optionsPanel.add(new JLabel("Color Map"), cl);
		optionsPanel.add(colorMapCombo, cr);
		
		colorMapCombo.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
	}
	
	private class ColoredBeadsVisualization extends AbstractDataVisualization {
		
		private ColorMap
			colorMap = ColorMap.Hue;
		
		public ColoredBeadsVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source, 
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
		}

		@Override
		public void update() {
		}
		
		@Override
		public void remove() {
		}
		
	}
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		actVis = (ColoredBeadsVisualization)visualization;
		colorMapCombo.setSelectedItem(actVis.colorMap);
		return optionsPanel;
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return a.checkType(Number.class);
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("bullets.png");
		return info;
	}
	
	@Override
	public String getName() {
		return "Colored Beads";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		return new ColoredBeadsVisualization(layer, source, this, type);
	}

}
