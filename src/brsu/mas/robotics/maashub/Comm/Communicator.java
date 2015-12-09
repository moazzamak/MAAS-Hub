package brsu.mas.robotics.maashub.Comm;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class Communicator extends CyclicBehaviour {
	
	private static final long serialVersionUID = -3559584894242757117L;

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive();
		
		if(msg!=null){
			// Process it
			String cap = msg.getContent();
			
			System.out.println(myAgent.getName() + ": I have the following capabilities:");
			System.out.println(myAgent.getName() + ": " + cap);
			System.out.println(myAgent.getName() + ": Therefore:");
			
			if(cap.contains("Paint")){
				System.out.println(myAgent.getName() + ": I am a Painter");
			}
			if(cap.contains("Mobile")){
				System.out.println(myAgent.getName() + ": I am a Transporter");
			}
			if(cap.contains("Manipulate")){
				System.out.println(myAgent.getName() + ": I am a Manipulator");
			}
			
			System.out.println();
		}
		else
		{
			block();
		}
		
	}

}
