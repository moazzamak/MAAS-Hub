import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;

import java.util.*;

public class FSMTransportAgent extends Agent{
	
	private static final String check_request = "A";
	private static final String wait_request = "B";
	private static final String take_uncolored_object = "C";
	private static final String deliver_uncolored_object = "D";
	private static final String check_colored_object = "E";
	private static final String deliver_colored_object = "F";
	private static final String wait_painting = "G";



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
				fsm.registerFirstState(new Start(), check_request);
				
				// Register state B
				fsm.registerState(new Wait(), wait_request);
				
				// Register state C
				fsm.registerState(new TakeUncoloredObject(), take_uncolored_object);
				
				// Register state D
				fsm.registerState(new DeliverUncoloredObject(), deliver_uncolored_object);
				
				fsm.registerState(new CheckColoredObject(), check_colored_object);

				fsm.registerState(new DeliverColoredObject(), deliver_colored_object);
				fsm.registerState(new Wait(), wait_painting);

				

				// Register the transitions
				fsm.registerTransition(check_request, take_uncolored_object, 1);
				fsm.registerTransition(check_request, wait_request, 0);
				fsm.registerDefaultTransition(wait_request, check_request);				
				//fsm.registerTransition(STATE_C, STATE_C, 0);
				fsm.registerDefaultTransition(take_uncolored_object, deliver_uncolored_object);	
				fsm.registerDefaultTransition(deliver_uncolored_object, check_colored_object);	

				fsm.registerTransition(check_colored_object, wait_painting, 0);
				fsm.registerTransition(check_colored_object, deliver_colored_object, 1);
				fsm.registerDefaultTransition(wait_painting, check_colored_object);	
				fsm.registerDefaultTransition(deliver_colored_object, check_request);				


		
				
				addBehaviour(fsm);
			}
			
			
	private class Start extends OneShotBehaviour {
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
	
	private class Wait extends OneShotBehaviour {
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
	private class TakeUncoloredObject extends OneShotBehaviour {
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
	
	private class DeliverUncoloredObject extends OneShotBehaviour {
		public void action() {
			System.out.println("Deliver uncolored screw to the painting agent  ");

		}
	}
	
	
	private class CheckColoredObject extends OneShotBehaviour {
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
	
	private class DeliverColoredObject extends OneShotBehaviour {
		public void action() {
			System.out.println("Deliver colored screw from the painting agent  ");

		}
	}
	
}
	
	
	

	
	
	
			
		
