package de.jtem.halfedgetools.ui;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class ButtonCellEditor extends AbstractCellEditor implements TableCellEditor {

	private static final long 	
		serialVersionUID = 1L;
	private JLabel
		defaultEditor = new JLabel("-");
	private Object 
		activeValue = null;
	
	@Override
	public Component getTableCellEditorComponent(
		JTable table,
		Object value, 
		boolean isSelected, 
		int row, 
		int column
	) {
		this.activeValue = value;
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
		}
		return defaultEditor;
	}
	@Override
	public Object getCellEditorValue() {
		return activeValue;
	}
	
}