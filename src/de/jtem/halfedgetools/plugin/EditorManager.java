package de.jtem.halfedgetools.plugin;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;

public class EditorManager extends Plugin {

	private JComboBox
		modeCombo = new JComboBox();
	
	
	private class ModeComboModel extends DefaultComboBoxModel {

		private static final long 
			serialVersionUID = 1L;
		
		
		
		
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
	}
	
	
}
