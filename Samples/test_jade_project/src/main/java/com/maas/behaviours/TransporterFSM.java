package com.maas.behaviours;


import com.google.common.util.concurrent.Service.State;
import com.maas.agents.PriortyQueAgent;
import com.maas.agents.SimpleWorker;
import com.maas.domain.Order;
import com.maas.domain.Order.ObjectType;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.proto.SimpleAchieveREInitiator;
import jade.util.Logger;

public class TransporterFSM extends FSMBehaviour {
	private enum states{
		Start,
		CommunicatePriortyQue,
		WaitAndCommunicate,
		LookForNextWorker,
		SearchForWorker,
		WaitAndSearch,
		End
	}
	
	private String priortyQueName;
	private Order currentJob;
//	private Logger log;
	private final long WAIT_TIME_FOR_NEXT_JOB = 1000;
	private final long WAIT_FOR_WORKER_SEARCH=500;
	
	public TransporterFSM(Agent a,String priortyQue){
		super(a);
		priortyQueName = priortyQue;
//		log = Logger.getJADELogger(getAgent().getLocalName());
		registerStates();
	}
	@SuppressWarnings("serial")
	private void registerStates(){
		registerFirstState(new OneShotBehaviour() {
			
			@Override
			public void action() {
				// TODO Auto-generated method stub
				currentJob = null;
				log("Transporter FSM starting!");
			}
		}, states.Start.toString());
		

		// Communicate Priorty Que
		registerState(new OneShotBehaviour() {
			private int response;
			@Override
			public void action() {
				ACLMessage aquire_job_msg = new ACLMessage(ACLMessage.REQUEST);
				aquire_job_msg.setProtocol(PriortyQueAgent.PriortyQueActions.ACQUIRE_ORDER
						.toString());
				aquire_job_msg.addReceiver(new AID(priortyQueName, AID.ISLOCALNAME));
//				getAgent().send(aquire_job_msg);
//				getAgent().addBehaviour(new Behaviour() {
//					
//					@Override
//					public boolean done() {
//						// TODO Auto-generated method stub
//						return false;
//					}
//					
//					@Override
//					public void action() {
//						
//						getAgent().receive()
//					}
//				});
				
				getAgent().addBehaviour(new AchieveREInitiator(getAgent(), aquire_job_msg){
					
					protected void handleAgree(ACLMessage agree) {
						log("Agent " + agree.getSender().getName()
								+ " sent " + agree.getContent());
						Order job = Order.valueOf(agree.getContent());
						job.setHolderName(getAgent().getLocalName());
						job.incrementPriorty();
						setCurrentJob(job);
						response=1;
					}

					protected void handleRefuse(ACLMessage refuse) {
//						log("Agent " + refuse.getSender().getName()
//								+ " refused!");
						response=0;
						setCurrentJob(null);
					}
					
				});
			}
			public int onEnd(){
//				log("CommunicatePriortyQue onEnd Sening response : "+response);
				return response;
			}
		}, states.CommunicatePriortyQue.toString());
		
		registerState(new WakerBehaviour(getAgent(),WAIT_TIME_FOR_NEXT_JOB) {
			protected void handleElapsedTimeout() {
				log("Waited for  "+WAIT_TIME_FOR_NEXT_JOB+ "ms searching for transportation job");
			}
			public int onEnd(){
				return 0;
			}
		}, states.WaitAndCommunicate.toString());
		
		// LookForNextWorker
		registerState(new OneShotBehaviour() {
			
			private int workerType=0;
			@Override
			public void action() {
				if(getCurrentOrder() != null){
					String nextWorker = SimpleWorker.getNextWorkerRole(getCurrentOrder());
					if(nextWorker.equals("INIT")){
						log("LookForNextWorker its Transporter");
						workerType=0;
						if (getCurrentOrder().getObjectType() == ObjectType.NUT) {
							getCurrentOrder().addToProgress(ObjectType.NUT.toString());
						} else if (getCurrentOrder().getObjectType() == ObjectType.SCREW) {
							getCurrentOrder().addToProgress(ObjectType.SCREW.toString());
						} else {
							getCurrentOrder().addToProgress(ObjectType.NUT.toString());
							getCurrentOrder().addToProgress(ObjectType.SCREW.toString());
						}
						
					}else if(nextWorker.contains("PAINTER_")){
						log("LookForNextWorker its PAINTER");
						workerType=1;
					}else{
						log("LookForNextWorker its FASTENER");
						workerType=1;
					}
				}
			}
			public int onEnd() {
				return workerType;
			}
		}, states.LookForNextWorker.toString());
		
		//SearchForWorker
		registerState(new OneShotBehaviour() {
			private boolean didAnyoneAgree=false;
			@Override
			public void action() {
				log("SearchForWorker in Action!");
				didAnyoneAgree = false;
				if(getCurrentOrder()!= null){
					String nextWorker = SimpleWorker.getNextWorkerRole(getCurrentOrder());
//					DFAgentDescription[] workers = SimpleWorker.searchForService(getAgent(), nextWorker);
					ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
					msg.setProtocol(PriortyQueAgent.PriortyQueActions.ADD_ORDER
							.toString());
//					for(DFAgentDescription worker : workers){
//						msg.addReceiver(worker.getName());
//					}
					msg.setContent(getCurrentOrder().toString());
					
					getAgent().addBehaviour(new AchieveREInitiator(getAgent(), msg){
						protected void handleAgree(ACLMessage agree) {
							log("Agent "+agree.getSender().getLocalName()+" Agreed for "+getCurrentOrder().toString());
							didAnyoneAgree=true;
						}

						protected void handleRefuse(ACLMessage refuse) {
							if(getCurrentOrder() != null)
								log("Agent "+refuse.getSender().getLocalName()+" Refused for "+getCurrentOrder().toString());
						}
					});
				}
			}
			public int onEnd(){
				if(didAnyoneAgree){
					return 1;
				}else{
					return 0;
				}
			}
		}, states.SearchForWorker.toString());
		
		registerState(new WakerBehaviour(getAgent(),WAIT_FOR_WORKER_SEARCH) {
			protected void handleElapsedTimeout() {
				log("Waited for  "+WAIT_FOR_WORKER_SEARCH+ "ms searching for next worker");
			}
		}, states.WaitAndSearch.toString());
		
		registerLastState(new OneShotBehaviour() {
			
			@Override
			public void action() {
				log("Exiting Transportr FSM");
				
			}
		}, states.End.toString());
		
		registerDefaultTransition(states.Start.toString(), states.CommunicatePriortyQue.toString());
		registerTransition(states.CommunicatePriortyQue.toString(), states.WaitAndCommunicate.toString(), 0);
		registerTransition(states.WaitAndCommunicate.toString(),states.CommunicatePriortyQue.toString(),0);
		registerTransition(states.CommunicatePriortyQue.toString(), states.LookForNextWorker.toString(), 1);
		registerTransition(states.LookForNextWorker.toString(), states.LookForNextWorker.toString(), 0);
		registerTransition(states.LookForNextWorker.toString(), states.SearchForWorker.toString(), 1);
		registerTransition(states.SearchForWorker.toString(), states.Start.toString(), 1);
		registerTransition(states.SearchForWorker.toString(), states.WaitAndSearch.toString(), 0);
		registerDefaultTransition(states.WaitAndSearch.toString(),states.SearchForWorker.toString());
		
	}
	private boolean setCurrentJob(Order job){
		currentJob = job;
		return true;
	}
	private Order getCurrentOrder(){
		return currentJob;
	}
	private void log(String msg){
		System.out.println("["+getAgent().getLocalName()+"]"+msg);
	}
}
