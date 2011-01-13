package de.jtem.halfedgetools.tutorial;

import de.jreality.plugin.JRViewer;
import de.jtem.halfedgetools.plugin.VectorFieldManager;

public class TestApplication {

	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.addContentUI();
		v.registerPlugin(new TestPlugin());
		v.registerPlugin(new VectorFieldManager());
		v.startup();
	}
	
}
