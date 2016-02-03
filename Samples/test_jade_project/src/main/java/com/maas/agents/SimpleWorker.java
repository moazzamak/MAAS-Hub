package com.maas.agents;


import com.maas.agents.PriortyQueAgent.PriortyQueActions;
import com.maas.domain.Order;
import com.maas.domain.Order.ObjectType;
import com.maas.ui.OutwardQueGUI;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.states.MsgReceiver;

public class SimpleWorker extends Agent {
	public enum AgentRole {
		TRANSPORTER, PAINTER_RED, PAINTER_BLUE, PAINTER_GREEN, FASTENER
	}

	String priortyQueAgentName;
	AgentRole role;
	boolean busy = false;
	Order transporterCurrentJob;
	private final int TIME_TAKEN_TO_MOVE = 3000;
	private final int TIME_TAKEN_TO_PAINT = 1000;
	private final int TIME_TAKEN_TO_FASTEN = 1000;
	private AID timeKeeperAID=null;
	public SimpleWorker(){
		
	}
	protected void setup() {
		Object[] args = getArguments();
		priortyQueAgentName = null;
		if (args != null && args.length > 0) {
			priortyQueAgentName = (String) args[0];
		} else {
			log("Order agent name not given!");
			System.exit(-1);
		}
		if (args.length > 1) {
			registerAs((String) args[1]);
		} else {
			log("Error: role not given");
			System.exit(-1);
		}
		transporterCurrentJob = null;
		
		if (role == AgentRole.TRANSPORTER)
			communicatePriortyQue(priortyQueAgentName);
		else {
			waitForJob();
		}
	}

	private void registerAs(String role) {
		if (role.equals(AgentRole.TRANSPORTER.toString())) {
			this.role = AgentRole.TRANSPORTER;
		} else if (role.equals(AgentRole.PAINTER_BLUE.toString())) {
			this.role = AgentRole.PAINTER_BLUE;
		} else if (role.equals(AgentRole.PAINTER_GREEN.toString())) {
			this.role = AgentRole.PAINTER_GREEN;
		} else if (role.equals(AgentRole.PAINTER_RED.toString())) {
			this.role = AgentRole.PAINTER_RED;
		} else if (role.equals(AgentRole.FASTENER.toString())) {
			this.role = AgentRole.FASTENER;
		} else {
			System.exit(-1);
		}
		log("Agent " + getLocalName()
				+ " registering service " + this.role.toString());
		try {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setName(getLocalName());
			sd.setType(this.role.toString());
			dfd.addServices(sd);

			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	@SuppressWarnings("serial")
	private void communicatePriortyQue(String priortyQueName) {
		if (role != AgentRole.TRANSPORTER) {
			return;
		}
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setProtocol(PriortyQueAgent.PriortyQueActions.ACQUIRE_ORDER
				.toString());
		msg.addReceiver(new AID(priortyQueName, AID.ISLOCALNAME));

		this.addBehaviour(new AchieveREInitiator(this, msg) {

			protected void handleAgree(ACLMessage agree) {
				log(agree.getSender().getLocalName()
						+ " sent " + agree.getContent());
				Order job = Order.valueOf(agree.getContent());
				job.setHolderName(getLocalName());
				transporterCurrentJob=job;
				doWork(job);
//				log("sending Event To TimeKeeper");
				sendEventToTimeKeeper(job);
				String nextRole = getNextWorkerRole(job);
//				DFAgentDescription[] results = searchForService(getAgent(),nextRole);
//				sendJobToAgent(job, results[0].getName());
//				sendJobToWorkers(transporterCurrentJob, results,0);
				transportToWorker(nextRole);				
				communicatePriortyQue(priortyQueName);
			}

			protected void handleRefuse(ACLMessage refuse) {
				log(refuse.getSender().getLocalName()+ " refused!");
				getAgent().addBehaviour(new WakerBehaviour(getAgent(), 1000) {
					protected void handleElapsedTimeout() {
						communicatePriortyQue(priortyQueName);
					}
				});
			}
		});
	}

	@SuppressWarnings("serial")
	private void waitForJob() {
		addBehaviour(new Behaviour() {
			
			@Override
			public boolean done() {
				// TODO Auto-generated method stub
				return false;
			}
			
			@Override
			public void action() {
				MessageTemplate template = MessageTemplate.and(MessageTemplate
						.MatchProtocol(PriortyQueActions.ADD_ORDER.toString()),
						MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				ACLMessage job_msg = receive(template);
				if (job_msg != null) {
					log("Recived Job from "
							+ job_msg.getSender().getLocalName() + " "
							+ job_msg.getContent());
					if(!busy){
						ACLMessage reply = job_msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						send(reply);
						Order job = Order.valueOf(job_msg.getContent());
						doWork(job);
						sendEventToTimeKeeper(job);
					}else{
						ACLMessage reply = job_msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						send(reply);
					}
				}
				
			}
		});
		
		
	}

	@SuppressWarnings("serial")
	private void doWork(Order job) {
		busy = true;
		if (job.isComplete()) {
			log("Job Complete : " + job.toString());
			OutwardQueGUI.getInstance().add(job);
		} else {
			String nextWorker = getNextWorkerRole(job);
			log("Next worker is : " + nextWorker);
			if (nextWorker.contains("PAINTER") && 
				(role == AgentRole.PAINTER_BLUE || role == AgentRole.PAINTER_RED ||
				 role == AgentRole.PAINTER_GREEN)) {
					job.addToProgress(this.role.toString().split("_")[1]);
					job.incrementPriorty();
					job.setHolderName(getLocalName());
					doWait(TIME_TAKEN_TO_PAINT);
					sendJobToAgent(job, priortyQueAgentName);
//				}
			} else if (nextWorker.equals("INIT") && role == AgentRole.TRANSPORTER) {
				job.incrementPriorty();
				if (job.getObjectType() == ObjectType.NUT) {
					job.addToProgress(ObjectType.NUT.toString());
				} else if (job.getObjectType() == ObjectType.SCREW) {
					job.addToProgress(ObjectType.SCREW.toString());
				} else {
					job.addToProgress(ObjectType.NUT.toString());
					job.addToProgress(ObjectType.SCREW.toString());
				}
				// TODO: wrong , transporter should not send job to Priority
				// Que.
//				transporterCurrentJob = job;
				String nextRole = getNextWorkerRole(job);
				DFAgentDescription[] results = searchForService(nextRole);
//				sendJobToAgent(job, results[0].getName());
				sendJobToWorkers(job, results, 0);
			} else if (nextWorker.equals(AgentRole.FASTENER.toString()) && role == AgentRole.FASTENER) {
					if (job.getProgress().contains("NUT-SCREW")) {
						job.setProgress(job.getProgress().replace("NUT-SCREW",
								ObjectType.NUT_SCREW.toString()));
						job.incrementPriorty();
						job.setHolderName(getLocalName());
						doWait(TIME_TAKEN_TO_FASTEN);
						sendJobToAgent(job, priortyQueAgentName);
					}
			}else if (nextWorker.equals(AgentRole.FASTENER.toString()) && role == AgentRole.TRANSPORTER) {
//				String nextRole = getNextWorkerRole(job);
				DFAgentDescription[] results = searchForService(nextWorker);
//				sendJobToAgent(job, results[0].getName());
				sendJobToWorkers(job, results, 0);
		}

		}
		log(getLocalName() + " Done with " + job.toString());
		busy = false;
	}
	void transportToWorker(String nextWorker){
		log("Moving to "+nextWorker);
		doWait(TIME_TAKEN_TO_MOVE);
		log("Moving to inventory...");
		doWait(TIME_TAKEN_TO_MOVE);
	}
//	private void waitForJobToBeSent(){
//		doWait();
//		addBehaviour(new Behaviour() {
//			
//			@Override
//			public boolean done() {
//				// TODO Auto-generated method stub
//				return (transporterCurrentJob == null);
//			}
//			
//			@Override
//			public void action() {
//				if(transporterCurrentJob == null){
//					doWake();
//				}
//			}
//		});
//	}
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (Exception e) {
		}
	}

	public DFAgentDescription[] searchForService(String service) {
//		Logger log = Logger.getJADELogger(searcher.getLocalName());
		log("Searching For : " + service);
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service);
		dfd.addServices(sd);

		DFAgentDescription[] results;
		try {
			results = DFService.search(this, dfd);
			log(results.length + " results");
			for (DFAgentDescription result : results)
				log("Found " + result.getName());
			return results;
		} catch (FIPAException e) {
			log(e.getStackTrace().toString());
		}
		return null;
	}

	private DFAgentDescription[] searchForPainter(Order.ObjectColor color) {
		return searchForService("PAINTER_" + color.toString());
	}

	private DFAgentDescription[] searchForFastener() {
		return searchForService(AgentRole.FASTENER.toString());
	}

	public static String getNextWorkerRole(Order ord) {
		if (ord.getProgress() == null) {
			return "INIT";
		} else if (!ord.getProgress().contains(ord.getColorString())) {
			return "PAINTER_" + ord.getColorString();
		} else if (!ord.getProgress().contains(ord.getTypeString())) {
			return AgentRole.FASTENER.toString();
		} else {
			return null;
		}
	}

	private void sendJobToAgent(Order ord, String localName) {
		log(getLocalName() + " Sending JOB to " + localName);
		ACLMessage job_msg = new ACLMessage(ACLMessage.REQUEST);
		job_msg.setProtocol(PriortyQueActions.ADD_ORDER.toString());
		job_msg.setContent(ord.toString());
		job_msg.addReceiver(new AID(localName, AID.ISLOCALNAME));
		send(job_msg);
	}
	private void sendEventToTimeKeeper(Order ord){
		//TODO: still testing
		if(timeKeeperAID == null){
			DFAgentDescription[] results = searchForService(TimeKeeperAgent.serviceName);
			if(results.length > 0){
				timeKeeperAID = results[0].getName();
			}else{
				timeKeeperAID=null;
				return;
			}
		}
		log("Sending event to Timekeeper");
		ACLMessage event_msg = new ACLMessage(ACLMessage.REQUEST);
		event_msg.setProtocol(TimeKeeperAgent.REGISTER_EVENT);
		event_msg.setContent(ord.toString());
		event_msg.addReceiver(timeKeeperAID);
		send(event_msg);
		
	}
	private void sendJobToAgent(Order ord, AID agentID) {
		log(getLocalName() + " Sending JOB to " + agentID);
		ACLMessage job_msg = new ACLMessage(ACLMessage.REQUEST);
		job_msg.setProtocol(PriortyQueActions.ADD_ORDER.toString());
		job_msg.setContent(ord.toString());
		job_msg.addReceiver(agentID);
		send(job_msg);
	}
	private void sendJobToWorkers(Order ord, DFAgentDescription[] workers,int workerNumber) {
		if(workerNumber >= workers.length){
			workerNumber = 0;
		}
		if(transporterCurrentJob == null){
			return;
		}
		log(" Sending JOB to " + workers[workerNumber].getName());
		ACLMessage job_msg = new ACLMessage(ACLMessage.REQUEST);
		job_msg.setProtocol(PriortyQueActions.ADD_ORDER.toString());
		job_msg.setContent(ord.toString());
		job_msg.addReceiver(workers[workerNumber].getName());
		send(job_msg);
		MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchProtocol(PriortyQueActions.ADD_ORDER.toString()),
												 MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.AGREE),
														 MessageTemplate.MatchPerformative(ACLMessage.REFUSE)));
//		MessageTemplate mt = ;
		final int nextWorkerNumber = workerNumber+1;
		addBehaviour(new MsgReceiver(this, mt, MsgReceiver.INFINITE, null, null){
			protected void handleMessage(ACLMessage msg){
				if(msg.getPerformative() == ACLMessage.AGREE){
					log(msg.getSender().getLocalName() + " Accepted the job");
					transporterCurrentJob = null;
				}else{
					sendJobToWorkers(ord, workers, nextWorkerNumber);
				}
			}
		});
	}
	private void log(String msg){
		System.out.println("["+getLocalName()+"]"+msg);
	}
}
