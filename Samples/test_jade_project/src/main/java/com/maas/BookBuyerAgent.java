package com.maas;


//import jade.core.AID;
import jade.core.Agent;


public class BookBuyerAgent extends Agent {
//	final String NAME="Daiem";
//	private AID id;
//	public BookBuyerAgent() {
//		id = new AID(NAME, AID.ISLOCALNAME);
//	}
	protected void setup() {
		// Printout a welcome message
		System.out.println("Hello! Buyer-agent "+getAID().getName()+" is ready.");
	}
}
