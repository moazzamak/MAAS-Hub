package comm.maas.ui;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import com.maas.domain.Order;

public class QueTableModel extends AbstractTableModel {

	Vector<Order> orders;

	public QueTableModel() {
		orders = new Vector<Order>();
	}

	@Override
	public int getRowCount() {

		return orders.size();
	}

	@Override
	public int getColumnCount() {
		return Order.PROPERTIES.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return Order.PROPERTIES[columnIndex];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return Order.PROPERTIES.getClass();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return orders.get(rowIndex).getPropertyString(
				Order.PROPERTIES[columnIndex]);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (rowIndex < orders.size()) {
			orders.elementAt(rowIndex).copyValues((Order) aValue);
		} else {
			orders.add((Order) aValue);
		}
	}

	public void add(Order o) {
		orders.add(o);
		this.fireTableRowsInserted(getRowCount() - 1, getRowCount());
	}
}
