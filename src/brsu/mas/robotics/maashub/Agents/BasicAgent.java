package brsu.mas.robotics.maashub.Agents;

import java.io.IOException;
import java.util.Vector;

import jade.core.*;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class BasicAgent extends Agent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6324337531686569519L;

	protected void setup(){
		System.out.println("Initializing");
		
		jade.wrapper.AgentContainer cc = getContainerController();
		

		System.out.println("Created Container");
		
		try{
			Vector<AgentController> acc = new Vector<AgentController>();
			
			for (int i = 0; i < 3; i++)
			{
				acc.add(cc.createNewAgent("agent"+i, "brsu.mas.robotics.maashub.Agents.MASAgent", null));					
				acc.elementAt(i).start();
			}
			
			System.out.println("Controller: Yer a painter Harry!");
			
			///////////////////////////////////////////////////
			// Assign capabilities to agents
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("agent1", AID.ISLOCALNAME));
			msg.setLanguage("English");
			msg.setOntology("Agent-Description-Ontology");
			msg.setContent("Paint-red Paint-green Paint-blue");
			send(msg);
			
			msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(new AID("agent0", AID.ISLOCALNAME));
			msg.setLanguage("English");
			msg.setOntology("Agent-Description-Ontology");
			msg.setContent("Mobile Manipulate");
			send(msg);
			
			///////////////////////////////////////////////////

		}
		catch(StaleProxyException e){
			e.printStackTrace();
		}
	}
}
