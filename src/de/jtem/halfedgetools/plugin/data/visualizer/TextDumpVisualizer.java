package de.jtem.halfedgetools.plugin.data.visualizer;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JPanel;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.Adapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeLayer;
import de.jtem.halfedgetools.plugin.data.AbstractDataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualization;
import de.jtem.halfedgetools.plugin.data.DataVisualizer;
import de.jtem.halfedgetools.plugin.data.DataVisualizerPlugin;
import de.jtem.halfedgetools.plugin.image.ImageHook;
import de.jtem.jrworkspace.plugin.PluginInfo;

public class TextDumpVisualizer extends DataVisualizerPlugin implements ActionListener {

	private JPanel
		optionsPanel = new JPanel();
	private JButton
		dumpButton = new JButton("Text Dump");
	private TextDumpVisualization
		activeVis = null;
	
	
	public TextDumpVisualizer() {
		optionsPanel.setLayout(new GridLayout());
		optionsPanel.add(dumpButton);
		dumpButton.addActionListener(this);
	}
	
	
	private class TextDumpVisualization extends AbstractDataVisualization {
		
		public TextDumpVisualization(
			HalfedgeLayer layer, 
			Adapter<?> source, 
			DataVisualizer visualizer, 
			NodeType type
		) {
			super(layer, source, visualizer, type);
		}

		@Override
		public void update() {
			System.out.println(this.toString() + " -------------------");
			AdapterSet aSet = getLayer().getEffectiveAdapters();
			HalfEdgeDataStructure<?, ?, ?> hds = getLayer().get();
			switch (getType()) {
			case Vertex:
				for (Vertex<?,?,?> v : hds.getVertices()) {
					Object data = getSource().get(v, aSet);
					printNode(v, data);
				}
				break;
			case Edge:
				for (Edge<?,?,?> e : hds.getEdges()) {
					Object data = getSource().get(e, aSet);
					printNode(e, data);
				}
				break;
			case Face:
				for (Face<?,?,?> f : hds.getFaces()) {
					Object data = getSource().get(f, aSet);
					printNode(f, data);
				}
				break;
			}
			System.out.println("---------------------------------------------");
		}
		
		private void printNode(Node<?,?,?> n, Object data) {
			if (data instanceof double[]) {
				data = Arrays.toString((double[])data);
			}
			if (data instanceof float[]) {
				data = Arrays.toString((float[])data);
			}
			if (data instanceof int[]) {
				data = Arrays.toString((int[])data);
			}
			if (data instanceof long[]) {
				data = Arrays.toString((long[])data);
			}
			if (data instanceof short[]) {
				data = Arrays.toString((short[])data);
			}
			System.out.println(n + "\t" + data.toString());
		}
		
		@Override
		public void remove() {
		}
		
	}
	
	@Override
	public JPanel connectUserInterfaceFor(DataVisualization visualization) {
		activeVis = (TextDumpVisualization)visualization;
		return optionsPanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		activeVis.update();
	}
	
	@Override
	public boolean canRead(Adapter<?> a, NodeType type) {
		return true;
	}
	
	@Override
	public PluginInfo getPluginInfo() {
		PluginInfo info = super.getPluginInfo();
		info.icon = ImageHook.getIcon("page_white_text.png");
		return info;
	}
	
	
	@Override
	public String getName() {
		return "Text Dump";
	}

	@Override
	public DataVisualization createVisualization(HalfedgeLayer layer, NodeType type, Adapter<?> source) {
		return new TextDumpVisualization(layer, source, this, type);
	}
	
}
