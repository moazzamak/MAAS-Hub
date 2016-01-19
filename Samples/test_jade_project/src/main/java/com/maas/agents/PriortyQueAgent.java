package com.maas.agents;

import java.util.Comparator;
import java.util.PriorityQueue;

import com.maas.domain.Order;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
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
				return o2.getPriorty() - o1.getPriorty();
			}
		});
//		log = Logger.getJADELogger(getLocalName());
		registerAsPriortyQue();
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
		printQue();
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
		log(" waiting for adding orders...");
		MessageTemplate template = 
//				MessageTemplate.and(MessageTemplate.MatchContent("Order"),
			  			MessageTemplate.and(
					  		MessageTemplate.MatchProtocol(PriortyQueActions.ADD_ORDER.toString()),
					  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST))/*)*/;
	  	
		addBehaviour(new AchieveREResponder(this,template){
			
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				log( "ADD REQUEST received from "+request.getSender().getLocalName()+". Action is "+request.getContent());
				if (addOrder(Order.valueOf(request.getContent()))) {
					printQue();
					log("Sending "+request.getSender().getLocalName()+ " AGREE");
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					return agree;
				}else {
					// We refuse to perform the action
					log("Sending "+request.getSender().getLocalName()+" REFUSE");
					throw new RefuseException("Unable to Add Order, " + request.getContent());
				}
			}
		});
	}
	private void addOrderRemovingBehaviour(){
		log("waiting for aquire Requests...");
		MessageTemplate template = 
//		MessageTemplate.and(MessageTemplate.MatchContent("Order"),
	  			MessageTemplate.and(
			  		MessageTemplate.MatchProtocol(PriortyQueActions.ACQUIRE_ORDER.toString()),
			  		MessageTemplate.MatchPerformative(ACLMessage.REQUEST))/*)*/;
	
		addBehaviour(new AchieveREResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
				if(que.peek() == null){
					log("Sending "+request.getSender().getLocalName()+" Refuse");
					throw new RefuseException("check-failed");
				}
				log("ACQUIRE_ORDER REQUEST received from "+request.getSender().getName()+". Action is "+que.peek().toString());
				Order the_order = que.peek();
				
				if (removeOrder(the_order)) {
					log("Sending "+request.getSender().getLocalName()+" Agree");
					informOrderHolder(the_order);
					ACLMessage agree = request.createReply();
					agree.setPerformative(ACLMessage.AGREE);
					agree.setContent(the_order.toString());
					return agree;
				}else {
					// We refuse to perform the action
					log("Sending "+request.getSender().getLocalName()+" Refuse");
					throw new RefuseException("check-failed");
				}
			}
		});
	}
	private void informOrderHolder(Order o){
		ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
		inform.addReceiver(new AID(o.getHolderName(),AID.ISLOCALNAME));
		inform.setContent(o.toString());
		inform.setProtocol(PriortyQueActions.ACQUIRE_ORDER.toString());
		send(inform);
	}
	private void printQue(){
		log("printing que : ");
		for(Order o : que){
			log(o.toString());
		}
	}
	private void log(String msg){
		System.out.println("["+getLocalName()+"]"+msg);
	}
}
