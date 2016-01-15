import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;

import java.util.*;

public class FSMCentralAgent extends Agent{
	
	private static final String STATE_A = "A";
	private static final String STATE_B = "B";
	private static final String STATE_C = "C";
	private static final String STATE_D = "D";

	private ACLMessage sentMsg=new ACLMessage(ACLMessage.INFORM);;

	public int paintingRequest = 1;
	public int paint = 0;
	public boolean transportAgentStatus = true;
	public String color = "red";
	public String obj = "screw";
    
	
	protected void setup() {
		
	
	//get color and object from args
		/*
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
		color = (String) args[0];
		obj = (String) args[1];
		System.out.println("Painting a "+ color + obj);
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		*/
		//finite state machine 
				FSMBehaviour fsm = new FSMBehaviour(this){
					public int onEnd(){
						System.out.println("FSM behaviour completed.");
						//myAgent.doDelete();
						return super.onEnd();
						
					}
				};
				// Register state A (first state)
				fsm.registerFirstState(new start(), STATE_A);
				
				// Register state B
				fsm.registerState(new wait(), STATE_B);
				
				// Register state C
				fsm.registerState(new deliverScrew(), STATE_C);
				
				// Register state D
				fsm.registerState(new sendPaintRequest(), STATE_D);
				
				

				// Register the transitions
				fsm.registerTransition(STATE_A, STATE_C, 1);
				fsm.registerTransition(STATE_A, STATE_B, 0);
				fsm.registerDefaultTransition(STATE_B, STATE_A);				
				fsm.registerTransition(STATE_C, STATE_C, 0);
				fsm.registerTransition(STATE_C, STATE_D, 1);				
				fsm.registerDefaultTransition(STATE_D, STATE_A);				

				
				addBehaviour(fsm);
			}
			
			
	private class start extends OneShotBehaviour {

		public void action() {
			System.out.println("Check painting request ");
			if (color != null)
				paint = 1;
			else 
				paint = 0;
			paintingRequest = paintingRequest - 1;
		
		}
		public int onEnd() {
			return paint;
}
	}
	
	private class wait extends OneShotBehaviour {
		public void action() {
			System.out.println("Waiting for painting request  ");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	private class deliverScrew extends OneShotBehaviour {
		public void action() {
			System.out.println("Tell transport agent to take a screw");
			
			if (transportAgentStatus == true){
				paint = 1;
			sentMsg.addReceiver(new AID("FSMTransportAgent", AID.ISLOCALNAME));
			sentMsg.setContent(obj);
			send(sentMsg);
			System.out.println(sentMsg);}
			else 
				paint = 0;
		}
		public int onEnd() {
			return paint;
}
			
	}
	
	private class sendPaintRequest extends OneShotBehaviour {
		public void action() {
			System.out.println("Send request with color to painting agent  ");
			sentMsg.addReceiver(new AID("FSMPaintingAgent", AID.ISLOCALNAME));
			sentMsg.setContent(color);
			send(sentMsg);
			System.out.println(sentMsg);

		}
	}
	
	
	
	
	}

	
	
	
			
		
