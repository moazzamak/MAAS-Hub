package comm.maas.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.maas.domain.Order;

public class OutwardQueGUI extends JFrame {
	
	private static OutwardQueGUI classInstance = null; 
	private JTable orderTable; 
	private JScrollPane listScroller;
	private QueTableModel tableModel;
	private OutwardQueGUI(){
		super("Delivery Agent");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		tableModel = new QueTableModel();
//		tableModel.add(new Order(0,ObjectColor.RED,ObjectType.NUT));
		
		orderTable = new JTable(tableModel);
		
		listScroller = new JScrollPane(orderTable);
		listScroller.setPreferredSize(new Dimension(500, 250));
		listScroller.setAlignmentX(LEFT_ALIGNMENT);
		
		getContentPane().add(listScroller);
		pack();
	}
	public static OutwardQueGUI getInstance(){
		if(classInstance == null){
			classInstance = new OutwardQueGUI();
		}
		return classInstance;
	}
	public void setVisible(){
		if(!classInstance.isVisible()){
			classInstance.setVisible(true);
		}
	}
	public void add(Order order){
		this.tableModel.add(order);
	}
}
