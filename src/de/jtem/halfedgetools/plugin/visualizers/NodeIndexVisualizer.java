package de.jtem.halfedgetools.plugin.visualizers;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.Node;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AbstractAdapter;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.adapter.type.Label;
import de.jtem.halfedgetools.plugin.VisualizerPlugin;
import de.jtem.jrworkspace.plugin.Controller;

public class NodeIndexVisualizer extends VisualizerPlugin implements ActionListener {

		private JCheckBox	
			showVertices = new JCheckBox("V", false),
			showEdges = new JCheckBox("E", false),
			showFaces = new JCheckBox("F", true);
		private JPanel
			panel = new JPanel();
		
		public NodeIndexVisualizer() {
			panel.setLayout(new GridLayout());
			panel.add(showVertices);
			panel.add(showEdges);
			panel.add(showFaces);
			
			showVertices.addActionListener(this);
			showEdges.addActionListener(this);
			showFaces.addActionListener(this);
		}
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			updateContent();
		}
		
		@Override
		public void storeStates(Controller c) throws Exception {
			super.storeStates(c);
			c.storeProperty(getClass(), "showVertices", showVertices.isSelected());
			c.storeProperty(getClass(), "showEdges", showEdges.isSelected());
			c.storeProperty(getClass(), "showFaces", showFaces.isSelected());
		}
		
		@Override
		public void restoreStates(Controller c) throws Exception {
			super.restoreStates(c);
			showVertices.setSelected(c.getProperty(getClass(), "showVertices", showVertices.isSelected()));
			showEdges.setSelected(c.getProperty(getClass(), "showEdges", showEdges.isSelected()));
			showFaces.setSelected(c.getProperty(getClass(), "showFaces", showFaces.isSelected()));
		}
		
		

		@Label
		private class IndexLabelAdapter extends AbstractAdapter<String> {

			public IndexLabelAdapter() {
				super(String.class, true, false);
			}

			@Override
			public <N extends Node<?, ?, ?>> boolean canAccept(Class<N> nodeClass) {
				if (Vertex.class.isAssignableFrom(nodeClass)) {
					return showVertices.isSelected();
				}
				if (Edge.class.isAssignableFrom(nodeClass)) {
					return showEdges.isSelected();
				}
				if (Face.class.isAssignableFrom(nodeClass)) {
					return showFaces.isSelected();
				}
				return false;
			}

			@Override
			public double getPriority() {
				return 0;
			}

			@Override
			public <
				V extends Vertex<V, E, F>,
				E extends Edge<V, E, F>,
				F extends Face<V, E, F>,
				N extends Node<V, E, F>
			> String get(N n, AdapterSet a) {
				if (n instanceof Edge<?,?,?>) {
					int index = n.getIndex();
					int indexOpp = ((Edge<?,?,?>) n).getOppositeEdge().getIndex();
					return index + ";" + indexOpp;
				} else {
					return "" + n.getIndex();	
				}
			}
			
		}

		@Override
		public JPanel getOptionPanel() {
			return panel;
		}


		@Override
		public AdapterSet getAdapters() {
			AdapterSet result = new AdapterSet();
			result.add(new IndexLabelAdapter());
			return result;
		}


		@Override
		public String getName() {
			return "Node Index";
		}

}
