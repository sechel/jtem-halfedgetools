package de.jtem.halfedgetools.plugin;

import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JPanel;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public abstract class WidgetPlugin extends Plugin {

	private WidgetInterface
		customGUI = null;
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		customGUI = c.getPlugin(WidgetInterface.class);
		customGUI.addWidget(this);
	}
	
	public void repaint() {
		customGUI.getPanel().repaint();
	}
	
	public void paint(Graphics2D g, JPanel panel) {
		
	}
	
	public JComponent getWidgetComponent() {
		return null;
	}
	
}
