package de.jtem.halfedgetools.plugin.algorithm.selection;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import de.jtem.halfedge.Edge;
import de.jtem.halfedge.Face;
import de.jtem.halfedge.HalfEdgeDataStructure;
import de.jtem.halfedge.Vertex;
import de.jtem.halfedgetools.adapter.AdapterSet;
import de.jtem.halfedgetools.plugin.HalfedgeInterface;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmCategory;
import de.jtem.halfedgetools.plugin.algorithm.AlgorithmDialogPlugin;

public class SelectionColor extends AlgorithmDialogPlugin {
	
	private JPanel panel;
	private JComboBox colorCombo;
	
	private Color color = Color.RED;
	
	private enum ColorEnum{
		BLACK,
		BLUE,
		CYAN,
		GRAY, 
		GREEN,
		MAGENTA,
		RED,
		WHITE,
		YELLOW
	}

	public SelectionColor(){
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		int y = 0;
		colorCombo = new JComboBox(ColorEnum.values());
		//red
		setColor(Color.RED);
		colorCombo.setSelectedIndex(6);
		colorCombo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateColor(colorCombo.getSelectedItem().toString());
			}
		});
		
		c.weighty = y++;
		panel.add(new JLabel("Color: "), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(colorCombo, c);
	}
	
	private void updateColor(String colorString) {
		for(ColorEnum c : ColorEnum.values())
			if (c.toString().equals(colorString)){
				switch(c){
					case BLACK: 
						setColor(Color.BLACK);
						break;
					case BLUE:
						setColor(Color.BLUE);
						break;
					case CYAN: 
						setColor(Color.CYAN);
						break;
					case GRAY:
						setColor(Color.GRAY);
						break;
					case GREEN: 
						setColor(Color.GREEN);
						break;
					case MAGENTA:
						setColor(Color.MAGENTA);
						break;
					case RED:
						setColor(Color.RED);
						break;	
					case WHITE: 
						setColor(Color.WHITE);
						break;
					case YELLOW:
						setColor(Color.YELLOW);
						break;
				};
			}
	}

	@Override
	public String getAlgorithmName() {
		return "Choose Color";
	}
	
	@Override
	public AlgorithmCategory getAlgorithmCategory() {
		return AlgorithmCategory.Selection;
	}
	
	@Override
	protected JPanel getDialogPanel() {
		return panel;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public <
		V extends Vertex<V, E, F>, 
		E extends Edge<V, E, F>, 
		F extends Face<V, E, F>, 
		HDS extends HalfEdgeDataStructure<V, E, F>
	> void executeAfterDialog (
		HDS hds,
		AdapterSet a,
		HalfedgeInterface hi
	){
		hi.setSelectionColor(getColor());
	}

}
