package com.maas.agents;

import utils.RandomNumberGenrator;

import com.maas.domain.Order;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DummyWorker extends Agent {
	String orderAgentName;
	protected void setup(){
		Object[] args = getArguments();
		orderAgentName = null;
		if(args != null && args.length > 0){
			orderAgentName = (String)args[0];
		}else{
			System.out.println("Order agent name not given!");
			System.exit(-1);
		}
		lookForJob(orderAgentName);
	}
	private void lookForJob(String orderGenratorName){
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
				lookForJob(orderAgentName);
			}
		});
		System.out.println(getLocalName()+" Done with "+job.toString());
	}
	
	
}
