package de.jtem.halfedgetools.plugin.widget;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import de.jtem.halfedgetools.plugin.WidgetInterface;
import de.jtem.halfedgetools.plugin.WidgetPlugin;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class ContextMenuWidget extends WidgetPlugin implements PopupMenuListener {

	private JPopupMenu
		popup = new JPopupMenu("Context Menu");
	private WidgetInterface
		wi = null;
	private Controller
		controller = null;
	
	
	public ContextMenuWidget() {
		popup.addPopupMenuListener(this);
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		popup.removeAll();
		List<AlgorithmPlugin> aPlugs = controller.getPlugins(AlgorithmPlugin.class);
		Map<AlgorithmCategory, List<AlgorithmPlugin>> aMap = new HashMap<AlgorithmCategory,  List<AlgorithmPlugin>>();
		for (AlgorithmPlugin ap : aPlugs) {
			if (aMap.get(ap.getAlgorithmCategory()) == null) {
				aMap.put(ap.getAlgorithmCategory(), new LinkedList<AlgorithmPlugin>());
			}
			List<AlgorithmPlugin> aList = aMap.get(ap.getAlgorithmCategory());
			aList.add(ap);
		}
		for (AlgorithmCategory ac : aMap.keySet()) {
			List<AlgorithmPlugin> pList = aMap.get(ac);
			Collections.sort(pList);
			JMenu catMenu = new JMenu(ac.name());
			for (AlgorithmPlugin ap : pList) {
				catMenu.add(ap.getHalfedgeAction());
			}
			popup.add(catMenu);
		}
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		wi = c.getPlugin(WidgetInterface.class);
		wi.getPanel().setComponentPopupMenu(popup);
		controller = c;
	}
	
	
}
