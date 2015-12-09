package brsu.mas.robotics.maashub;

import jade.core.*;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class Main {
	public static void main(String[] args){
		Runtime rt = Runtime.instance();
		
		System.out.println("Created Runtime");
		
		Profile profile = new ProfileImpl("localhost", 1099, Profile.PLATFORM_ID);

		System.out.println("Created Profile");
		
		profile.setParameter(Profile.PLATFORM_ID, "MyMainPlatform");
		profile.setParameter(Profile.GUI, "true");
		
		AgentContainer mainContainer = rt.createMainContainer(profile);
		
		System.out.println("Created Container");
		
		try{
			mainContainer.start();
			AgentController ac = mainContainer.createNewAgent("Controller", "brsu.mas.robotics.maashub.Agents.BasicAgent", null);
			ac.start();

			System.in.read();
			
			mainContainer.kill();
			rt.shutDown();
		}
		catch(StaleProxyException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
