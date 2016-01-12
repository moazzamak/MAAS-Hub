import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;

import java.util.*;

public class FSMTransportAgent extends Agent{
	
	private static final String STATE_A = "A";
	private static final String STATE_B = "B";
	private static final String STATE_C = "C";
	private static final String STATE_D = "D";
	private static final String STATE_E = "E";
	private static final String STATE_F = "F";
	private static final String STATE_G = "G";



	public int paintingRequest = 1;
	public String color = "red";
	public int paint = 0;
	public boolean transportAgentStatus = true;
	public boolean screwPainted = true;
	public int deliverScrew = 0;

	
	protected void setup() {
	
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
				fsm.registerState(new takeUncoloredScrew(), STATE_C);
				
				// Register state D
				fsm.registerState(new deliverUncoloredScrew(), STATE_D);
				
				fsm.registerState(new checkColoredScrew(), STATE_E);

				fsm.registerState(new deliverColoredScrew(), STATE_F);
				fsm.registerState(new wait(), STATE_G);

				

				// Register the transitions
				fsm.registerTransition(STATE_A, STATE_C, 1);
				fsm.registerTransition(STATE_A, STATE_B, 0);
				fsm.registerDefaultTransition(STATE_B, STATE_A);				
				//fsm.registerTransition(STATE_C, STATE_C, 0);
				fsm.registerDefaultTransition(STATE_C, STATE_D);	
				fsm.registerDefaultTransition(STATE_D, STATE_E);	

				fsm.registerTransition(STATE_E, STATE_G, 0);
				fsm.registerTransition(STATE_E, STATE_F, 1);
				fsm.registerDefaultTransition(STATE_G, STATE_E);	
				fsm.registerDefaultTransition(STATE_F, STATE_A);				


		
				
				addBehaviour(fsm);
			}
			
			
	private class start extends OneShotBehaviour {
		public void action() {
			System.out.println("Check painting request ");
			if (paintingRequest > 0)
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
	private class takeUncoloredScrew extends OneShotBehaviour {
		public void action() {
			System.out.println("Take uncolored screw");
			/*
			if (transportAgentStatus == true)
				paint = 1;
			else 
				paint = 0;
		}
		public int onEnd() {
			return paint;
			*/
}
			
	}
	
	private class deliverUncoloredScrew extends OneShotBehaviour {
		public void action() {
			System.out.println("Deliver uncolored screw to the painting agent  ");

		}
	}
	
	
	private class checkColoredScrew extends OneShotBehaviour {
		public void action() {
			System.out.println("Check painting request ");
			if (screwPainted)
				deliverScrew = 1;
			else 
				deliverScrew = 0;
		}
		public int onEnd() {
			return deliverScrew;
}
	}
	
	private class deliverColoredScrew extends OneShotBehaviour {
		public void action() {
			System.out.println("Deliver colored screw from the painting agent  ");

		}
	}
	
}
	
	
	

	
	
	
			
		
