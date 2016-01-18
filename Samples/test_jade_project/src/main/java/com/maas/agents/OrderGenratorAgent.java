package com.maas.agents;

import java.util.LinkedList;

import com.maas.agents.PriortyQueAgent.PriortyQueActions;
import com.maas.domain.Order;
import com.maas.domain.Order.ObjectColor;
import com.maas.domain.Order.ObjectType;
import com.maas.utils.RandomNumberGenrator;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/*
 * Singlition For Orders Inward Que Book keeping Agent
 * */
public class OrderGenratorAgent extends Agent {

	private LinkedList<Order> orders;
	
	private int currentOrderId;
	private boolean orderInProcess;
	private String priortyQueName=null;
//	public OrderGenratorAgent(){
//		
//	}	
	/*
	 * Agent Setup
	 */
	protected void setup() {
		log("Adding ticker behaviour");
		Object[] args = getArguments();
		orders = new LinkedList<Order>();
		currentOrderId = 1;
		orderInProcess = false;
		int sec = -1;
		if(args != null && args.length > 1){
			sec = Integer.valueOf((String)args[0]);
			log("time between orders : " + String.valueOf(sec));
			priortyQueName = (String)args[1];
			log("Priorty Que Name : "+ priortyQueName);
		}else{
			System.exit(-1);
		}
		addBehaviour(new TickerBehaviour(this, sec) {
			protected void onTick() {
				Order order = genrateRandomOrder();
//				if(servicesAreAvailable(order)){
					orders.add(order);
					currentOrderId++;
//				}
				if(seeRecentlyAddedOrder() != null){
					log("Genrated "+seeRecentlyAddedOrder().toString());
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
				if(!orderInProcess && priortyQueName != null){
					
					Order ord = seeNextOrder();
					if(ord != null){
						log("Sending REQUEST to "+priortyQueName);
						ACLMessage job_msg = new ACLMessage(ACLMessage.REQUEST);
						job_msg.setProtocol(PriortyQueActions.ADD_ORDER.toString());
						job_msg.setContent(ord.toString());
						job_msg.addReceiver(new AID(priortyQueName,AID.ISLOCALNAME));
						send(job_msg);
						acquireOrder(ord);
						orderInProcess = true;
					}
				}
			}
		});
	}
	private void replyToJobSeekers(Behaviour bhv){
		//CFP = Call for Proposal
		MessageTemplate cfp_mt = MessageTemplate.and(MessageTemplate.MatchProtocol(PriortyQueActions.ACQUIRE_ORDER.toString()),
								 MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage cfp_msg = this.receive(cfp_mt);
		if(cfp_msg != null){
			log("Call for proposal recived form "+cfp_msg.getSender().getLocalName());
			orderInProcess = false;
		}
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
		return new Order(currentOrderId,col,typ,getLocalName());
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
	public boolean servicesAreAvailable(Order order){
		DFAgentDescription[] painters = searchForService("PAINTER_"+order.getColorString());
		if(painters.length == 0){
			return false;
		}
		if(order.getObjectType() == Order.ObjectType.NUT_SCREW){
			DFAgentDescription[] fasteners = searchForService("FASTENER");
			if(fasteners.length == 0){
				return false;
			}
		}
		return true;
	}
	public DFAgentDescription[] searchForService(String service) {
		log("Searching For : " + service);
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service);
		dfd.addServices(sd);

		DFAgentDescription[] results;
		try {
			results = DFService.search(this, dfd);
			log(results.length + " results");
			for (DFAgentDescription result : results)
				log("Found " + result.getName());
			return results;
		} catch (FIPAException e) {
			log(e.getStackTrace().toString());
		}
		return null;
	}
	private void log(String msg){
		System.out.println("["+getLocalName()+"]"+msg);
	}
}
