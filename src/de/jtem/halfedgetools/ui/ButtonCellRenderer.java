package de.jtem.halfedgetools.ui;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ButtonCellRenderer extends DefaultTableCellRenderer {

	private static final long 
		serialVersionUID = 1L;
	private JButton 
		renderButton = new JButton();
	
	@Override
	public Component getTableCellRendererComponent(
		JTable table,
		Object value, 
		boolean isSelected, 
		boolean hasFocus, 
		int row,
		int column
	) {
		if (value instanceof Component) {
			Component c = (Component)value;
			if (isSelected) {
				c.setBackground(table.getSelectionBackground());
				c.setForeground(table.getSelectionForeground());
			} else {
				c.setForeground(table.getForeground());
				c.setBackground(table.getBackground());
			}
			return c;
		} else {
			return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
		}
	}
	
	@Override
	public void updateUI() {
		super.updateUI();
		if (renderButton != null) {
			renderButton.updateUI();
		}
	}
	
}