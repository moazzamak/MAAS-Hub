package comm.maas.ui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.maas.domain.Order;

public class OutwardQueGUI extends JFrame {
	
	protected static OutwardQueGUI classInstance = null; 
	private JTable orderTable; 
	private JScrollPane listScroller;
	private QueTableModel tableModel;
	protected OutwardQueGUI(){
		super("Outward Que");
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
	public void setVisible(boolean vis){
		try{
			super.setVisible(vis);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void add(Order order){
		this.tableModel.add(order);
	}
}
