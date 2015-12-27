package com.maas.agents;

import java.util.LinkedList;

import utils.RandomNumberGenrator;

import com.maas.domain.Order;
import com.maas.domain.Order.ObjectColor;
import com.maas.domain.Order.ObjectType;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Singlition For Orders Inward Que Book keeping Agent
 * */
public class OrderGenratorAgent extends Agent {

	private LinkedList<Order> orders;
	private int currentOrderId;
	public OrderGenratorAgent(){
		orders = new LinkedList<Order>();
		currentOrderId = 1;
	}	
	private Order genrateRandomOrder(){
		ObjectColor col = ObjectColor.RED;
		ObjectType typ = ObjectType.NUT;
		switch(RandomNumberGenrator.getInstance().randInt(0,2)){
		case 0:
			col = ObjectColor.RED;
			break;
		case 1:
			col = ObjectColor.BLUE;
			break;
		case 2:
			col = ObjectColor.GREEN;
			break;
		}
		switch (RandomNumberGenrator.getInstance().randInt(0, 2)){
		case 0:
			typ = ObjectType.NUT;
			break;
		case 1:
			typ = ObjectType.SCREW;
			break;
		case 2:
			typ = ObjectType.NUT_SCREW;
			break;
		}
		return new Order(currentOrderId,col,typ);
	}
	public Order seeNextOrder(){
		return orders.peekFirst();
	}
	/*
	 * Do not use to see the latest order, use seeNextOrder()
	 * */
	public Order seeRecentlyAddedOrder(){
		return orders.peekLast();
	}
	public boolean acquireOrder(Order o){
		for(Order ord : orders){
			if(ord.getId() == o.getId()){
				orders.remove(ord);
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Agent Setup
	 */
	protected void setup() {
		System.out.println("Adding ticker behaviour");
		Object[] args = getArguments();
		int sec = -1;
		if(args != null && args.length > 0){
			sec = Integer.valueOf((String)args[0]);
			System.out.println("time between orders : " + String.valueOf(sec));
		}else{
			System.exit(-1);
		}
		addBehaviour(new TickerBehaviour(this, sec) {
			protected void onTick() {
				Order order = genrateRandomOrder();
				orders.add(order);
				currentOrderId++;
				
				if(seeRecentlyAddedOrder() != null){
					System.out.println("Genrated "+
					Order.valueOf(seeRecentlyAddedOrder().toString()).toString());	
				}
			}
			
		});
		
		addBehaviour(new Behaviour() {
			
			@Override
			public boolean done() {
				return false;
			}
			
			@Override
			public void action() {
				replyToJobSeekers(this);
			}
		});
		
		addBehaviour(new Behaviour() {
			
			@Override
			public boolean done() {
				return false;
			}
			
			@Override
			public void action() {
				acceptJobProposal();
			}
		});
	}
	private void replyToJobSeekers(Behaviour bhv){
		//CFP = Call for Proposal
		MessageTemplate cfp_mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage cfp_msg = this.receive(cfp_mt);
		if(cfp_msg != null){
			System.out.println("Call for proposal recived form "+cfp_msg.getSender().getLocalName());
			ACLMessage job_msg = cfp_msg.createReply();
			if(this.seeNextOrder() != null){
				System.out.println("Job Available!");
				job_msg.setPerformative(ACLMessage.PROPOSE);
				job_msg.setContent(this.seeNextOrder().toString());
			}else{
				System.out.println("No Job Available!");
				job_msg.setPerformative(ACLMessage.REFUSE);
				job_msg.setContent("No Job Available!");
			}
			bhv.getAgent().send(job_msg);
		}
	}
	private void acceptJobProposal(){
		MessageTemplate accept_mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
		ACLMessage acc_msg= this.receive(accept_mt);
		if(acc_msg != null){
			System.out.println("Recived Acceptance from "+acc_msg.getSender().getLocalName());
			Order the_order = Order.valueOf(acc_msg.getContent());
			ACLMessage acc_reply = acc_msg.createReply();
			if(acquireOrder(the_order)){
				acc_reply.setPerformative(ACLMessage.CONFIRM);
				acc_reply.setContent(the_order.toString());
			}else{
				acc_reply.setPerformative(ACLMessage.DISCONFIRM);
			}
			send(acc_reply);
		}
	}
}
