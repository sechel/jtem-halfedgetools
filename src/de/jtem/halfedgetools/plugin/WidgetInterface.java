package de.jtem.halfedgetools.plugin;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.jreality.plugin.basic.View;
import de.jtem.halfedgetools.plugin.widget.WrapLayout;
import de.jtem.jrworkspace.plugin.Controller;
import de.jtem.jrworkspace.plugin.Plugin;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class WidgetInterface extends Plugin implements ComponentListener {

	private View 
		view = null;
	private GUIPanel
		panel = new GUIPanel();
	
	private List<WidgetPlugin>
		widgets = new LinkedList<WidgetPlugin>();
	
	
	public WidgetInterface() {
		panel.setOpaque(false);
		panel.setLayout(new WrapLayout(FlowLayout.LEADING, 2, 2));
	}
	
	
	private class GUIPanel extends JPanel {
		
		private static final long 
			serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			Graphics2D g2d = (Graphics2D)g;
			for (WidgetPlugin w : widgets) {
				w.paint(g2d, panel);
			}
			super.paint(g);
		}
		
	}
	
	
	
	private void updateLayout() {
		Component parent = panel.getParent();
		Component viewPanel = view.getViewer().getViewingComponent();
		Dimension size = view.getViewer().getViewingComponentSize();
		Point p = SwingUtilities.convertPoint(viewPanel, new Point(), parent);
		panel.setLocation(p);
		panel.setSize(size);
	}
	
	
	@Override
	public void install(Controller c) throws Exception {
		super.install(c);
		view = c.getPlugin(View.class);
		Component viewComponent = view.getViewer().getViewingComponent();
		JFrame mainFrame = (JFrame)SwingUtilities.getWindowAncestor(viewComponent);
		JLayeredPane layers = mainFrame.getLayeredPane();
		layers.add(panel, JLayeredPane.MODAL_LAYER);
		viewComponent.addComponentListener(this);
		
		EventForwarder ef = new EventForwarder(viewComponent);
		panel.setFocusable(true);
		panel.addMouseListener(ef);
		panel.addMouseMotionListener(ef);
		panel.addMouseWheelListener(ef);
		panel.addKeyListener(ef);
	}
	
	
	@Override
	public PluginInfo getPluginInfo() {
		return new PluginInfo("VaryLab GUI", "Stefan Sechelmann");
	}
	
	
	public void addWidget(WidgetPlugin wp) {
		widgets.add(wp);
		if (wp.getWidgetComponent() != null) {
			panel.add(wp.getWidgetComponent());
		}
		
	}
	
	public void removeWidgetPlugin(WidgetPlugin wp) {
		widgets.remove(wp);
		if (wp.getWidgetComponent() != null) {
			panel.remove(wp.getWidgetComponent());
		}
	}

	public JPanel getPanel() {
		return panel;
	}
	

	@Override
	public void componentResized(ComponentEvent e) {
		updateLayout();
	}


	@Override
	public void componentMoved(ComponentEvent e) {
		updateLayout();
	}


	@Override
	public void componentShown(ComponentEvent e) {
		updateLayout();
	}


	@Override
	public void componentHidden(ComponentEvent e) {
		updateLayout();
	}
	
	
	
	private class EventForwarder implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

		private Component
			target = null;
		
		public EventForwarder(Component target) {
			this.target = target;
		}

		private void forward(AWTEvent eo) {
			Component t = ((Container)target).getComponents()[0];
			t.requestFocus();
			target.dispatchEvent(eo);
		}
		
		@Override
		public void keyTyped(KeyEvent e) {
			forward(e);
		}

		@Override
		public void keyPressed(KeyEvent e) {
			forward(e);
		}

		@Override
		public void keyReleased(KeyEvent e) {
			forward(e);			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			forward(e);			
			panel.requestFocus();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			forward(e);			
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			forward(e);				
		}
		
	}
	

}
