package de.jtem.halfedgetools.tutorial;

import de.jreality.plugin.JRViewer;

public class TestApplication {

	public static void main(String[] args) {
		JRViewer v = new JRViewer();
		v.registerPlugin(new TestPlugin());
		v.startup();
	}
	
}
