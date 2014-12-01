package de.jtem.halfedgetools.plugin.widget;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import de.jtem.halfedgetools.plugin.WidgetPlugin;

public class ViewSwitchWidget extends WidgetPlugin {

	private JComboBox<String>
		viewsCombo = new JComboBox<>(new String[] {"Front", "Back", "Top", "Bottom", "Left", "Right"});
	
	@Override
	public JComponent getWidgetComponent() {
		return viewsCombo;
	}

}
