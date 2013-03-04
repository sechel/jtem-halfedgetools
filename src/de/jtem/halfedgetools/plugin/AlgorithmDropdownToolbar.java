package de.jtem.halfedgetools.plugin;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JToolBar;
import javax.swing.event.ListDataListener;

import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmNameComparator;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.flavor.PerspectiveFlavor;
import de.jtem.jrworkspace.plugin.flavor.ToolBarFlavor;

public class AlgorithmDropdownToolbar extends Plugin implements ToolBarFlavor {

	private Map<AlgorithmCategory, JComboBox>
		comboMap = new HashMap<AlgorithmCategory, JComboBox>();
	private Map<AlgorithmCategory, Set<AlgorithmPlugin>>
		algoMap = new HashMap<AlgorithmCategory, Set<AlgorithmPlugin>>();
	private JToolBar
		comboToolBar = new JToolBar("Halfedge Algorithms");
	
	public AlgorithmDropdownToolbar() {
		for (AlgorithmCategory cat : AlgorithmCategory.values()) {
			JComboBox catCombo = new JComboBox();
			AlgorithmComboModel model = new AlgorithmComboModel(cat);
			catCombo.setModel(model);
			AlgorithmCellRenderer renderer = new AlgorithmCellRenderer(cat);
			catCombo.setRenderer(renderer);
			comboMap.put(cat, catCombo);
			algoMap.put(cat, new HashSet<AlgorithmPlugin>());
		}
		updateComboBoxes();
	}
	
	
	private class AlgorithmCellRenderer extends DefaultListCellRenderer {
		
		private static final long 
			serialVersionUID = 1L;
		private AlgorithmCategory
			category = AlgorithmCategory.Custom;

		public AlgorithmCellRenderer(AlgorithmCategory category) {
			this.category = category;
		}
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
			if (c instanceof JLabel) {
				JLabel l = (JLabel)c;
				if (index == -1) {
					l.setIcon(ImageHook.getIcon("color_swatch.png"));
					l.setText(category.toString());
				} else {
					List<AlgorithmPlugin> algos = getAlgorithms(category);
					AlgorithmPlugin algo = algos.get(index);
					l.setIcon(algo.getPluginInfo().icon);
					l.setText(algo.getAlgorithmName());
				}
			}
			return c;
		}
		
	}
	
	private class AlgorithmComboModel implements ComboBoxModel {

		private AlgorithmCategory
			category = AlgorithmCategory.Custom;
		
		public AlgorithmComboModel(AlgorithmCategory category) {
			this.category = category;
		}
		
		@Override
		public void addListDataListener(ListDataListener l) {
		}
		@Override
		public void removeListDataListener(ListDataListener l) {
		}

		@Override
		public Object getElementAt(int index) {
			List<AlgorithmPlugin> algos = getAlgorithms(category);
			return algos.get(index);
		}

		@Override
		public int getSize() {
			List<AlgorithmPlugin> algos = getAlgorithms(category);
			return algos.size();
		}

		@Override
		public Object getSelectedItem() {
			return null;
		}
		@Override
		public void setSelectedItem(Object selectedItem) {
			AlgorithmPlugin algo = (AlgorithmPlugin)selectedItem;
			invokeAlgorithm(algo);
		}
		
	}
	
	
	protected void invokeAlgorithm(AlgorithmPlugin algo) {
		algo.execute();
	}
	
	
	protected List<AlgorithmPlugin> getAlgorithms(AlgorithmCategory cat) {
		List<AlgorithmPlugin> result = new LinkedList<AlgorithmPlugin>();
		Set<AlgorithmPlugin> algoSet = algoMap.get(cat);
		result.addAll(algoSet);
		Collections.sort(result, new AlgorithmNameComparator());
		return result;
	}
	
	
	public void updateComboBoxes() {
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		int numCombos = 0;
		for (AlgorithmCategory cat : AlgorithmCategory.values()) {
			JComboBox combo = comboMap.get(cat);
			Set<AlgorithmPlugin> algos = algoMap.get(cat);
			if (algos.isEmpty()) continue;
			comboToolBar.add(combo, c);
			numCombos++;
		}
		int cols = 6;
		int rows = (int)Math.ceil(numCombos / 6.0);
		comboToolBar.setLayout(new GridLayout(rows, cols));
		comboToolBar.revalidate();
	}
	
	@Override
	public Component getToolBarComponent() {
		return comboToolBar;
	}

	@Override
	public Class<? extends PerspectiveFlavor> getPerspective() {
		return View.class;
	}

	@Override
	public double getToolBarPriority() {
		return 0;
	}
	
	public void addAlgorithm(AlgorithmPlugin ap) {
		Set<AlgorithmPlugin> plugins = algoMap.get(ap.getAlgorithmCategory());
		plugins.add(ap);
		updateComboBoxes();
	}
	public void removeAlgorithm(AlgorithmPlugin ap) {
		Set<AlgorithmPlugin> plugins = algoMap.get(ap.getAlgorithmCategory());
		plugins.remove(ap);
		updateComboBoxes();
	}
	
	public void setFloatable(boolean floatable) {
		comboToolBar.setFloatable(floatable);
	}
	
}
