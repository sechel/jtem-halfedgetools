package de.jtem.halfedgetools.tutorial;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.sidecontainer.SideContainerPerspective;
import de.jtem.jrworkspace.plugin.sidecontainer.template.ShrinkPanelPlugin;

public class TestPlugin extends ShrinkPanelPlugin implements ActionListener {

	private HalfedgeInterface
		hif = null;
	private JButton
		button = new JButton("Go");
	
	public TestPlugin() {
		shrinkPanel.add(button);
		button.addActionListener(this);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		VHDS hds = hif.get(new VHDS());
		System.out.println("Got data structure:\n" + hds);
		double area = TestAlgorithm.doSomething(hds, hif.getAdapters());
		System.out.println("Area is " + area);
		hif.addAdapter(new TestVectorField(), false);
		hif.update();
	}
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		hif = c.getPlugin(HalfedgeInterface.class);
		hif.addAdapter(new TestPositionAdapter(), true);
	}
	
	@Override
	public Class<? extends SideContainerPerspective> getPerspectivePluginClass() {
		return View.class;
	}

}
