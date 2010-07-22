package de.jtem.halfedgetools.plugin.swing;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class IconCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;

	@Override
	public Component getTableCellRendererComponent(
		JTable table,
		Object value, 
		boolean isSelected, 
		boolean hasFocus, 
		int row,
		int column
	) {
		JLabel l = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); 
		if (value instanceof Icon) {
			l.setIcon((Icon)value);
			l.setText("");
		}
		return l;
	}
	
}