package com.maas.behaviours;

import com.maas.agents.CounterAgent;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MessageReciverBehaviour extends CyclicBehaviour {
	public MessageReciverBehaviour(){
	}
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive();
		if (msg != null) {
			int recivedNumber = Integer.parseInt(msg.getContent());
			CounterAgent agent = (CounterAgent)myAgent;
			System.out.println(agent.getGivenName()+
					" recived a numer : "+recivedNumber);
			agent.setNumber(recivedNumber+1);
		}
		block();
	}

}
