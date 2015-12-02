import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import java.util.*;
import  jade.lang.acl.ACLMessage;


public class PaintingAgent extends Agent{
	
	private int screwNum;
	private String color;
	
	protected void setup() {

	System.out.println("Hello! Painting Agent "+getAID().getName()+" is ready.");
	
	Object[] args = getArguments();
	if (args != null && args.length > 0) {
	color = (String) args[0];
	System.out.println("Painting a "+ color + " screw");
	try {
		Thread.sleep(4000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

	}
	
	
	
	
	String nickname = "Painter";
	AID id = new AID(nickname, AID.ISLOCALNAME);
	
	

	
	Screw firstScrew= new Screw( "screw1");
	firstScrew.setColor(color);
	System.out.println(" a screw is colored with " + color);
	
	
	
	
	}
	
	
	
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Painting Agent" +getAID().getName()+" terminating.");
		}
}
