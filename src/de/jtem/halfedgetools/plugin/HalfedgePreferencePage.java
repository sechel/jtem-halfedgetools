package de.jtem.halfedgetools.plugin;

import static javax.swing.BoxLayout.Y_AXIS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.flavor.PreferencesFlavor;

public class HalfedgePreferencePage extends Plugin implements PreferencesFlavor, ActionListener {

	private HalfedgeInterface
		hif = null;
	private JPanel
		panel = new JPanel();
	private JCheckBox
		showBoundingBoxChecker = new JCheckBox("Show Bounding Box");
	
	public HalfedgePreferencePage() {
		panel.setLayout(new BoxLayout(panel, Y_AXIS));
		panel.add(showBoundingBoxChecker);
		showBoundingBoxChecker.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Object s = e.getSource();
		if (showBoundingBoxChecker == s && hif != null) {
			hif.setShowBoundingBox(showBoundingBoxChecker.isSelected());
		}
	}
	
	
	@Override
	public void storeStates(Controller c) throws Exception {
		c.storeProperty(getClass(), "showBoundingBox", showBoundingBoxChecker.isSelected());
	}
	
	@Override
	public void restoreStates(Controller c) throws Exception {
		showBoundingBoxChecker.setSelected(c.getProperty(getClass(), "showBoundingBox", false));
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.setShowBoundingBox(c.getProperty(getClass(), "showBoundingBox", false));
	}
	
	
	@Override
	public String getMainName() {
		return "Halfedge";
	}

	@Override
	public JPanel getMainPage() {
		return panel;
	}

	@Override
	public Icon getMainIcon() {
		return null;
	}

	@Override
	public int getNumSubPages() {
		return 0;
	}
	@Override
	public String getSubPageName(int i) {
		return null;
	}
	@Override
	public JPanel getSubPage(int i) {
		return null;
	}
	@Override
	public Icon getSubPageIcon(int i) {
		return null;
	}

}
