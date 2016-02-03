package com.maas.agents;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import com.google.common.base.Joiner;
import com.maas.domain.Order;
import com.maas.utils.TimeUtils;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.states.MsgReceiver;

@SuppressWarnings("serial")
public class TimeKeeperAgent extends Agent {

	private Connection db = null;
	private String db_name=null;
	private static long currentId=0;
	public static final String[] fields = { "Id","Who", "Job","Discription","Timestamp" };
	public static final String serviceName ="time-keeper";
	public static final String REGISTER_EVENT="register-event";
	private final String tableName="TIMELINE";
	private boolean recordPostTime=false;
	protected void setup() {
		log("Time Keeper Started ... ");
		Object[] args = getArguments();
		if(args.length > 0 && ((String)args[0]).equals("postingTime")){
			recordPostTime=true;
		}
		init_db();
		registerAsTimeKeeper();
		addOrderEventListener();
	}
	private void addOrderEventListener(){
		log("Adding Event listener");
		addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchProtocol(REGISTER_EVENT), 
						MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				ACLMessage msg = receive(mt);
				if(msg != null){
					try{
						if(!recordPostTime){
							addEvent(msg.getSender().getLocalName(), msg.getContent(), TimeUtils.getInstance().getCurrentTimestampString());
						}else{
							addEvent(msg.getSender().getLocalName(), msg.getContent(), new Timestamp(msg.getPostTimeStamp()).toString().split(".")[0]);
						}
					}catch(Exception e){
						log(" Exception:");
						e.printStackTrace(System.out);
					}
				}
			}
		});
		log("Wainting for Events...");
	}
	private void addEvent(String who,String order,String timestamp) throws SQLException{
		Statement stmt = db.createStatement();
		Order ord = Order.valueOf(order);
		String sql="INSERT INTO "+tableName+"("+Joiner.on(",").join(fields)+")"+
				   "VALUES ("+String.valueOf(currentId)+",'"+
				   who+"','"+
				   String.valueOf(ord.getId())+"','"+
				   order+"','"+
				   timestamp+"');";
		log("SQL executed : "+sql);
		stmt.executeUpdate(sql);
		stmt.close();
//		db.commit();
		currentId++;
	}
	private void init_db() {
		log("initilizing DB");
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
//			log("Current Timestamp : "+TimeUtils.getInstance().getCurrentTimestamp());
			String time_name = TimeUtils.getInstance()
					.getCurrentTimestampString().replace(".","_")
					.replace(" ", "_").replace("-", "_").replace(":", "_");
			db_name= "timeline_"+ time_name + ".db";
			db = DriverManager.getConnection("jdbc:sqlite:"+db_name);
			log("Opened database successfully");

			stmt = db.createStatement();
			String sql = "CREATE TABLE "+tableName+"("+ 
						  fields[0]+ " INT PRIMARY KEY NOT NULL,"+
						  fields[1]+ " TEXT NOT NULL,"+
					      fields[2]+ " TEXT NOT NULL,"+
					      fields[3]+ " TEXT NOT NULL,"+
					      fields[4]+ " TIMESTAMP NOT NULL)";
			log("SQL is : "+sql);
			stmt.executeUpdate(sql);
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}
		log("Table created successfully");
	}
	private void registerAsTimeKeeper(){
		log("Registering as timekeeper");
		try {
	  		DFAgentDescription dfd = new DFAgentDescription();
	  		dfd.setName(getAID());
	  		ServiceDescription sd = new ServiceDescription();
	  		sd.setName(getLocalName());
	  		sd.setType(serviceName);
	  		dfd.addServices(sd);
	  		
	  		DFService.register(this, dfd);
	  	}
	  	catch (FIPAException fe) {
	  		log(fe.getStackTrace().toString());
	  		System.exit(0);
	  	}
	}
	private void log(String msg) {
		System.out.println("[" + getLocalName() + "]" + msg);
	}
}
