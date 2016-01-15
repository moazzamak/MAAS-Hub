import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.core.behaviours.*;

import java.util.*;

public class FSMPaintingAgent extends Agent{
	
	private static final String check_painting_request = "A";
	private static final String wait_painting_request = "B";
	private static final String check_available_object = "C";
	private static final String take_object = "D";
	private static final String painting = "E";
	private static final String deliver_object = "F";

	private ACLMessage receivedMsg;
	public int paintingRequest = 1;
	public String color;
	public int paint = 0;
	public int screws = 1;
    
	
	protected void setup() {
	
		//finite state machine 
				FSMBehaviour fsm = new FSMBehaviour(this){
					public int onEnd(){
						System.out.println("FSM behaviour completed, painting is done.");
						//myAgent.doDelete();
						return super.onEnd();
						
					}
				};
				// Register state A (first state)
				fsm.registerFirstState(new Satrt(), check_painting_request);
				
				// Register state B
				fsm.registerState(new Wait(), wait_painting_request);
				
				// Register state C
				fsm.registerState(new CheckObject(), check_available_object);
				
				// Register state D
				fsm.registerState(new Take(), take_object);
				
				// Register state E
				fsm.registerState(new Paint(), painting);
				
				// Register state F (final state)
				fsm.registerState(new Deliver(), deliver_object);

				// Register the transitions
				fsm.registerTransition(check_painting_request, check_available_object, 1);
				fsm.registerTransition(check_painting_request, wait_painting_request, 0);
				fsm.registerDefaultTransition(wait_painting_request, check_painting_request);				
				fsm.registerTransition(check_available_object, check_available_object, 0);
				fsm.registerTransition(check_available_object, take_object, 1);				
				fsm.registerDefaultTransition(take_object, painting);				
				fsm.registerDefaultTransition(painting, deliver_object );				
				fsm.registerDefaultTransition(deliver_object, check_painting_request);
				
				addBehaviour(fsm);
			}
			
			
	private class Satrt extends OneShotBehaviour {
		public static final String RECV_MSG = "received-message";
		public void action() {
			receivedMsg = myAgent.receive();
			System.out.println("Check painting request ");
			if (receivedMsg!= null) {
				paint = 1;
				System.out.println(receivedMsg);
				getDataStore().put(RECV_MSG, receivedMsg);
				color = receivedMsg.getContent();
				}
				else {
				paint = 0;
				}
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
	private class CheckObject extends OneShotBehaviour {
		public void action() {
			System.out.println("Check if there is screws ready to paint ");
			if (screws > 0)
				paint = 1;
			else 
				paint = 0;
		}
		public int onEnd() {
			return paint;
}
	}
	
	private class Take extends OneShotBehaviour {
		public void action() {
			System.out.println("Take a screw from transport agent  ");

		}
	}
	
	private class Paint extends OneShotBehaviour {
		public void action() {
			System.out.println("Painting  "+color + " screw");
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		}
	
	private class Deliver extends OneShotBehaviour {
		public void action() {
			System.out.println(color + " screw painted");
			
		}
		}
	}

	
	
	
			
		
