package de.jtem.halfedgetools.plugin;

import static java.awt.event.KeyEvent.VK_F1;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import de.jreality.plugin.basic.ViewMenuBar;
import de.jreality.plugin.basic.ViewToolBar;
import de.jtem.halfedgetools.plugin.modes.DefaultMode;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class EditorManager extends Plugin implements ActionListener {

	private ViewToolBar
		toolBar = null;
	private ViewMenuBar
		menuBar = null;
	private JComboBox
		modeCombo = new JComboBox();
	private List<EditorModePlugin>
		modePlugins = new LinkedList<EditorModePlugin>();
	private EditorModePlugin
		activeMode = null;
	private Controller
		controller = null;
	private JMenu
		modeMenu = new JMenu("Editor Modes");
	private Map<EditorModePlugin, Action>
		modeActionMap = new HashMap<EditorModePlugin, Action>();

	@Override
	public void actionPerformed(ActionEvent e) {
		EditorModePlugin mode = (EditorModePlugin)modeCombo.getSelectedItem();
		activateMode(mode);
	}

	
	private class ModeAction extends AbstractAction {

		private static final long 
			serialVersionUID = 1L;
		private EditorModePlugin
			mode = null;

		public ModeAction(EditorModePlugin mode) {
			this.mode = mode;
			putValue(NAME, mode.getModeName());
			int index = modePlugins.indexOf(mode);
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(VK_F1 + index, InputEvent.ALT_DOWN_MASK));
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			activateMode(mode);
		}
		
	}
	
	
	private void updateStates() {
		Vector<EditorModePlugin> pluginsVec = new Vector<EditorModePlugin>(modePlugins);
		DefaultComboBoxModel model = new DefaultComboBoxModel(pluginsVec);
		modeCombo.removeActionListener(this);
		modeCombo.setModel(model);
		modeCombo.setSelectedItem(activeMode);
		modeCombo.addActionListener(this);
		
		// rebuild menu
		modeMenu.removeAll();
		for (EditorModePlugin m : modePlugins) {
			ModeAction action = new ModeAction(m);
			modeMenu.add(action);
			modeActionMap.put(m, action);
		}
	}
	
	
	protected void addMode(EditorModePlugin m) {
		modePlugins.add(m);
		updateStates();
	}
	
	protected void removeMode(EditorModePlugin m) {
		modePlugins.remove(m);
		updateStates();
	}

	public void activateMode(EditorModePlugin mode) {
		if (activeMode == mode) return;
		activeMode.exit(controller);
		mode.enter(controller);
		activeMode = mode;
		modeCombo.setSelectedItem(activeMode);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		controller = c;
		activeMode = c.getPlugin(DefaultMode.class);
		updateStates();
		toolBar = c.getPlugin(ViewToolBar.class);
		toolBar.addTool(getClass(), 100.0, modeCombo);
		menuBar = c.getPlugin(ViewMenuBar.class);
		menuBar.addMenuSeparator(getClass(), -1.0, "Viewer");
		menuBar.addMenu(getClass(), 0.0, modeMenu, "Viewer");
	}
	
	@Override
	public void uninstall(Controller c) throws Exception {
		super.uninstall(c);
		toolBar.removeAll(getClass());
		menuBar.removeAll(getClass());
	}
	
	
}
