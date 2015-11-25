package com.maas.behaviours;

import com.maas.agents.CounterAgent;

import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MessageSenderBehaviour extends CyclicBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String reciver;
	public MessageSenderBehaviour(String reciver){
		this.reciver = reciver;
	}
	
	@Override
	public void action() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(new AID(this.reciver, AID.ISLOCALNAME));
		msg.setLanguage("English");
		CounterAgent agent = (CounterAgent)myAgent;
		msg.setContent(String.valueOf(agent.getNumber()));
		myAgent.send(msg);
		System.out.println(agent.getGivenName()+
				" sent the number : "+ agent.getNumber());
		block();
	}
}
