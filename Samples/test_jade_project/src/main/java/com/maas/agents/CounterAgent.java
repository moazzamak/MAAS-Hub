package com.maas.agents;

import com.maas.behaviours.MessageReciverBehaviour;
import com.maas.behaviours.MessageSenderBehaviour;

import jade.core.Agent;

public class CounterAgent extends Agent {
	
	
	private MessageSenderBehaviour sender;
	private MessageReciverBehaviour reciver;
	private String name;
	private int number;
	
	protected void setup() {
		System.out.println("Setup : " + getName());
		Object[] args = getArguments();
		if(args == null){
			return;
		}
//		for(Object in : args){
//			System.out.println("Argument is : "+(String)in);
//		}
		String rec = "";
		boolean start = false;
		if (args != null && args.length > 2) {
			System.out.println("args count : " + args.length);
			name = (String)args[0];
			rec = (String)args[1];
			start = ((String)args[2]).equals("true") ? true:false;
			
			System.out.println(name + " communicating with "+ rec+" and will start first : " + start);
		}else {
			System.out.println("usage : name reciver_name true/fasle");
			doDelete();
		}
		reciver = new MessageReciverBehaviour();
		sender = new MessageSenderBehaviour(rec);
		if(!start){
			sender.block();
		}
		reciver.block();
		addBehaviour(reciver);
		addBehaviour(sender);
	}
	
	protected void takeDown() {
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
		System.out.println(name+" set number to : "+ number);
	}
	public String getGivenName(){
		return name;
	}
}
