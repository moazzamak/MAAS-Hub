package brsu.mas.robotics.maashub.Agents;

import brsu.mas.robotics.maashub.Comm.Communicator;
import brsu.mas.robotics.maashub.KB.KnowledgeBase;
import jade.core.*;

public class MASAgent extends Agent {
	
	private static final long serialVersionUID = 2442898314299576536L;

	public void setup(){
		KnowledgeBase KB = new KnowledgeBase();
		
		System.out.println("Initializing " + this.getName());
		
		addBehaviour(new Communicator());
	}
}
