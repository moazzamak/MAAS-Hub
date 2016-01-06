package com.maas.agents;

import com.maas.domain.Order;
import com.maas.utils.RandomNumberGenrator;

import comm.maas.ui.OutwardQueGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DummyWorker extends Agent {
	public enum AgentRole{
		TRANSPORTER,
		PAINTER_RED,
		PAINTER_BLUE,
		PAINTER_GREEN,
		TURNER
	}
	String orderAgentName;
	AgentRole role;
	protected void setup(){
		Object[] args = getArguments();
		orderAgentName = null;
		if(args != null && args.length > 0){
			orderAgentName = (String)args[0];
		}else{
			System.out.println("Order agent name not given!");
			System.exit(-1);
		}
		if(args.length > 1){
			registerAs((String)args[1]);
		}else{
			System.out.println("Error: role not given");
			System.exit(-1);
		}
		lookForJob(orderAgentName);
	}
	private void registerAs(String role){
		if(role.equals(AgentRole.TRANSPORTER.toString())){
			this.role = AgentRole.TRANSPORTER;
		}else if(role.equals(AgentRole.PAINTER_BLUE.toString())){
			this.role = AgentRole.PAINTER_BLUE;
		}else if(role.equals(AgentRole.PAINTER_GREEN.toString())){
			this.role = AgentRole.PAINTER_GREEN;
		}else if(role.equals(AgentRole.PAINTER_RED.toString())){
			this.role = AgentRole.PAINTER_RED;
		}else if(role.equals(AgentRole.TURNER.toString())){
			this.role = AgentRole.TURNER;
		}
		System.out.println("Agent "+getLocalName()+" registering service \""+this.role.toString()+"\" of type \"weather-forecast\"");
	  	try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName(getLocalName());
	  		sd.setType(this.role.toString());
	  		// Agents that want to use this service need to "know" the weather-forecast-ontology
//	  		sd.addOntologies("weather-forecast-ontology");
	  		// Agents that want to use this service need to "speak" the FIPA-SL language
//	  		sd.addOntologies(FIPANames.ContentLanguage.FIPA_SL);
//	  		sd.addProperties(new Property("country", "Italy"));
	  		dfd.addServices(sd);
	  		
	  		DFService.register(this, dfd);
	  	}
	  	catch (FIPAException fe) {
	  		fe.printStackTrace();
	  	}
	}
	private void lookForJob(String orderGenratorName){
		if(role != AgentRole.TRANSPORTER){
			return;
		}
		this.addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				ACLMessage cfp_msg = new ACLMessage(ACLMessage.CFP);
				cfp_msg.addReceiver(new AID(orderGenratorName,AID.ISLOCALNAME));
				this.getAgent().send(cfp_msg);
				
			}
		});
		this.addBehaviour(new Behaviour() {
			private boolean isDone=false;
			@Override
			public void action() {
				MessageTemplate prop_tmp = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
								MessageTemplate.MatchPerformative(ACLMessage.REFUSE));
				ACLMessage prop_msg = this.getAgent().receive(prop_tmp);
				if(prop_msg != null){
					if(prop_msg.getPerformative() == ACLMessage.PROPOSE){
						isDone = true;
						System.out.println("Recived "+prop_msg.getContent());
						acceptTheJob(Order.valueOf(prop_msg.getContent()));
					}else if(prop_msg.getPerformative() == ACLMessage.REFUSE){
						System.out.println("Refused!");
						isDone = false;
						getAgent().addBehaviour(new WakerBehaviour(getAgent(),1000) {
							protected void handleElapsedTimeout(){
								lookForJob(orderGenratorName);
							}
						});
					}
				}
			}

			@Override
			public boolean done() {
				return isDone;
			}
		});
	}
	private void acceptTheJob(Order job){
		searchForPainter(job.getObjectColor());
		System.out.println("Accepting Job : "+ getLocalName());
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				ACLMessage job_acq_msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				job_acq_msg.setContent(job.toString());
				job_acq_msg.addReceiver(new AID(orderAgentName,AID.ISLOCALNAME));
				send(job_acq_msg);
			}
		});
		addBehaviour(new Behaviour() {
			private boolean isDone=false;
			@Override
			public boolean done() {
				return isDone;
			}
			
			@Override
			public void action() {
				MessageTemplate contract_mt = 
				MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
								   MessageTemplate.MatchPerformative(ACLMessage.DISCONFIRM));
				ACLMessage contract_msg = receive(contract_mt);
				if(contract_msg != null){
					System.out.println("Recived Contract Info!");
					if(contract_msg.getPerformative() == ACLMessage.CONFIRM){
						doWork(Order.valueOf(contract_msg.getContent()));
					}else if(contract_msg.getPerformative() == ACLMessage.DISCONFIRM){
						addBehaviour(new WakerBehaviour(this.getAgent(),1000) {
							protected void handleElapsedTimeout() {
								lookForJob(orderAgentName);
							}
						});
					}
					isDone = true;
				}
			}
		});
	}
	private void doWork(Order job){
		int work_time = RandomNumberGenrator.getInstance().randInt(3000, 8000);
		System.out.println("Work will take "+String.valueOf(work_time)+" ms");
		addBehaviour(new WakerBehaviour(this, work_time) {
			protected void handleElapsedTimeout() {
				OutwardQueGUI.getInstance().add(job);
				lookForJob(orderAgentName);
			}
		});
		System.out.println(getLocalName()+" Done with "+job.toString());
	}
	
	protected void takeDown() 
    {
       try { DFService.deregister(this); }
       catch (Exception e) {}
    }
	private DFAgentDescription[] searchForPainter(Order.ObjectColor color){
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType("PAINTER_"+color.toString());
        dfd.addServices(sd);
        
        DFAgentDescription[] result;
		try {
			result = DFService.search(this, dfd);
			System.out.println(result.length + " results" );
	        if (result.length>0)
	            System.out.println(" " + result[0].getName() );
	        return result;
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
