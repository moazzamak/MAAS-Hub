package com.maas.agents;

import com.maas.agents.PriortyQueAgent.PriortyQueActions;
import com.maas.domain.Order;
import com.maas.domain.Order.ObjectType;
import com.maas.utils.RandomNumberGenrator;

import comm.maas.ui.OutwardQueGUI;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;

public class SimpleWorker extends Agent {
	public enum AgentRole {
		TRANSPORTER, PAINTER_RED, PAINTER_BLUE, PAINTER_GREEN, FASTENER
	}

	String priortyQueAgentName;
	AgentRole role;
	boolean busy = false;

	protected void setup() {
		Object[] args = getArguments();
		priortyQueAgentName = null;
		if (args != null && args.length > 0) {
			priortyQueAgentName = (String) args[0];
		} else {
			System.out.println("Order agent name not given!");
			System.exit(-1);
		}
		if (args.length > 1) {
			registerAs((String) args[1]);
		} else {
			System.out.println("Error: role not given");
			System.exit(-1);
		}
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
		System.out.println("Agent " + getLocalName()
				+ " registering service \"" + this.role.toString()
				+ "\" of type \"weather-forecast\"");
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
				System.out.println("Agent " + agree.getSender().getName()
						+ " sent " + agree.getContent());
				Order job = Order.valueOf(agree.getContent());
				job.setHolderName(getLocalName());
				doWork(job);
				communicatePriortyQue(priortyQueName);
			}

			protected void handleRefuse(ACLMessage refuse) {
				System.out.println("Agent " + refuse.getSender().getName()
						+ " refused!");
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
		MessageTemplate template = MessageTemplate.and(MessageTemplate
				.MatchProtocol(PriortyQueActions.ADD_ORDER.toString()),
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		ACLMessage job_msg = receive(template);
		if (job_msg != null) {
			System.out.println("Recived Job from "
					+ job_msg.getSender().getLocalName() + " "
					+ job_msg.getContent());
			doWork(Order.valueOf(job_msg.getContent()));
		}
		addBehaviour(new WakerBehaviour(this, 500) {
			protected void handleElapsedTimeout() {
				waitForJob();
			}
		});
	}

	@SuppressWarnings("serial")
	private void doWork(Order job) {
		busy = true;
		int work_time = RandomNumberGenrator.getInstance().randInt(3000, 8000);
		System.out.println("Work will take " + String.valueOf(work_time)
				+ " ms");
		// addBehaviour(new WakerBehaviour(this, work_time) {
		// protected void handleElapsedTimeout() {
		if (job.isComplete()) {
			System.out.println("Job Complete : " + job.toString());
			OutwardQueGUI.getInstance().add(job);
		} else {
			String nextWorker = getNextWorkerRole(job);
			System.out.println("Next worker is : " + nextWorker);
			if (nextWorker.contains("PAINTER")) {
				DFAgentDescription[] result = searchForPainter(job
						.getObjectColor());
				if (result.length == 0) {
					System.out.println("No " + nextWorker + " Found!");
				} else {
					// TODO : Choose a painter.
					// Remove Following code.
					job.addToProgress(job.getColorString());
					job.incrementPriorty();
					job.setHolderName(getLocalName());
					sendJobToAgent(job, priortyQueAgentName);
				}
			} else if (nextWorker.equals("INIT")) {
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
				DFAgentDescription[] results = searchForService(getNextWorkerRole(job));
				sendJobToAgent(job, results[0].getName());
			} else if (nextWorker.equals(AgentRole.FASTENER.toString())) {
				DFAgentDescription[] result = searchForFastener();
				if (result.length == 0) {
					System.out.println("No " + nextWorker + " Found!");
				} else {
					// TODO : Choose a fastener.
					// Remove following code.
					if (job.getProgress().contains("NUT-SCREW")) {
						job.setProgress(job.getProgress().replace("NUT-SCREW",
								ObjectType.NUT_SCREW.toString()));
						job.incrementPriorty();
						job.setHolderName(getLocalName());
						sendJobToAgent(job, priortyQueAgentName);
					}
				}
			}

		}
		// }
		// });
		System.out.println(getLocalName() + " Done with " + job.toString());
		busy = false;
	}

	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (Exception e) {
		}
	}

	private DFAgentDescription[] searchForService(String service) {
		System.out.println("Searching For : " + service);
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(service);
		dfd.addServices(sd);

		DFAgentDescription[] result;
		try {
			result = DFService.search(this, dfd);
			System.out.println(result.length + " results");
			for (DFAgentDescription res : result)
				System.out.println("Found " + res.getName());
			return result;
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		return null;
	}

	private DFAgentDescription[] searchForPainter(Order.ObjectColor color) {
		return searchForService("PAINTER_" + color.toString());
	}

	private DFAgentDescription[] searchForFastener() {
		return searchForService(AgentRole.FASTENER.toString());
	}

	public String getNextWorkerRole(Order ord) {
		System.out.println("accessing next worker prgress is : "
				+ ord.getProgress());
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
		System.out.println(getLocalName() + " Sending JOB to " + localName);
		ACLMessage job_msg = new ACLMessage(ACLMessage.REQUEST);
		job_msg.setProtocol(PriortyQueActions.ADD_ORDER.toString());
		job_msg.setContent(ord.toString());
		job_msg.addReceiver(new AID(localName, AID.ISLOCALNAME));
		send(job_msg);
	}

	private void sendJobToAgent(Order ord, AID agentID) {
		System.out.println(getLocalName() + " Sending JOB to " + agentID);
		ACLMessage job_msg = new ACLMessage(ACLMessage.REQUEST);
		job_msg.setProtocol(PriortyQueActions.ADD_ORDER.toString());
		job_msg.setContent(ord.toString());
		job_msg.addReceiver(agentID);
		send(job_msg);
	}
}
