package com.maas.agents;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.maas.domain.Order;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

public class PriortyQueAgent extends Agent {
	public enum PriortyQueActions{
		ADD_ORDER,
		ACQUIRE_ORDER
	}
	PriorityQueue<Order> que;
	protected void setup(){
		que = new PriorityQueue<Order>(10,new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				return o1.getPriorty() - o2.getPriorty();
			}
		});
		
//		registerAsPriortyQue();
		addOrderAddingBehaviour();
		addOrderRemovingBehaviour();
	}
	protected void takeDown() 
	{
	   try { DFService.deregister(this); }
	   catch (Exception e) {}
	}
	private void registerAsPriortyQue(){
		try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName("priorty-que");
	  		sd.setType(PriortyQueAgent.class.toString());
	  		dfd.addServices(sd);
	  		
	  		DFService.register(this, dfd);
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	}
	private boolean addOrder(Order o){
		if(o != null){
			return que.add(o);
		}else{
			return false;
		}
	}
	private boolean removeOrder(Order o){
		if (o != null){
			return que.remove(o);
		}else{
			return false;
		}
	}
	private void addOrderAddingBehaviour(){
		System.out.println("Agent "+getLocalName()+" waiting for adding orders...");
		MessageTemplate template = 
//				MessageTemplate.and(MessageTemplate.MatchContent("Order"),
			  			MessageTemplate.and(
					  		MessageTemplate.MatchProtocol(PriortyQueActions.ADD_ORDER.toString()),
					  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST))/*)*/;
	  	
		addBehaviour(new AchieveREResponder(this,template){
			
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				System.out.println("Agent "+getLocalName()+": ADD REQUEST received from "+request.getSender().getName()+". Action is "+request.getContent());
				if (addOrder(Order.valueOf(request.getContent()))) {
					System.out.println("Agent "+getLocalName()+": Agree");
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				}else {
					// We refuse to perform the action
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("Unable to Add Order, " + request.getContent());
				}
			}
			
//			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
//				if (performAction()) {
//					System.out.println("Agent "+getLocalName()+": Action successfully performed");
//					ACLMessage inform = request.createReply();
//					inform.setPerformative(ACLMessage.INFORM);
//					return inform;
//				}
//				else {
//					System.out.println("Agent "+getLocalName()+": Action failed");
//					throw new FailureException("unexpected-error");
//				}	
//			}
		});
	}
	private void addOrderRemovingBehaviour(){
		System.out.println("Agent "+getLocalName()+" waiting for aquire Requests...");
		MessageTemplate template = 
//		MessageTemplate.and(MessageTemplate.MatchContent("Order"),
	  			MessageTemplate.and(
			  		MessageTemplate.MatchProtocol(PriortyQueActions.ACQUIRE_ORDER.toString()),
			  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST))/*)*/;
	
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				if(que.peek() == null){
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("check-failed");
				}
				System.out.println("Agent "+getLocalName()+": ACQUIRE_ORDER REQUEST received from "+request.getSender().getName()+". Action is "+que.peek().toString());
				Order the_order = que.peek();
				
				if (removeOrder(the_order)) {
					System.out.println("Agent "+getLocalName()+": Agree");
					informOrderHolder(the_order);
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					agree.setContent(the_order.toString());
					return agree;
				}else {
					// We refuse to perform the action
					System.out.println("Agent "+getLocalName()+": Refuse");
					throw new RefuseException("check-failed");
				}
			}
			
//			protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
//				if (performAction()) {
//					System.out.println("Agent "+getLocalName()+": Action successfully performed");
//					ACLMessage inform = request.createReply();
//					inform.setPerformative(ACLMessage.INFORM);
//					return inform;
//				}
//				else {
//					System.out.println("Agent "+getLocalName()+": Action failed");
//					throw new FailureException("unexpected-error");
//				}	
//			}
		});
	}
	private void informOrderHolder(Order o){
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.addReceiver(new AID(o.getHolderName(),AID.ISLOCALNAME));
		inform.setContent(o.toString());
		inform.setProtocol(PriortyQueActions.ACQUIRE_ORDER.toString());
		send(inform);
	}
}
