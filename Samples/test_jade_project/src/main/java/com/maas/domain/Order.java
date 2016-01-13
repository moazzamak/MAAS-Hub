package com.maas.domain;

import jade.util.leap.Properties;

public class Order {
	public enum ObjectColor{
		RED,
		BLUE,
		GREEN
	}
	public enum ObjectType{
		SCREW,
		NUT,
		NUT_SCREW
	}
	public Order(int id){
		this.id = id;
		this.priorty = 0;
	}
	public Order(int id, ObjectColor color, ObjectType type, String name){
		this.id = id;
		this.objectColor = color;
		this.objectType = type;
		this.priorty = 0;
		this.setHolderName(name);
	}
	public static final String[] PROPERTIES={"id","color","type"};
	private int id;
	private ObjectColor objectColor;
	private ObjectType objectType;
	private int priorty;
	private String holderName;
	
	public int getId() {
		return id;
	}
	public ObjectColor getObjectColor() {
		return objectColor;
	}
	public void setObjectColor(ObjectColor objectColor) {
		this.objectColor = objectColor;
	}
	public ObjectType getObjectType() {
		return objectType;
	}
	public void setObjectType(ObjectType objectType) {
		this.objectType = objectType;
	}
	public String getTypeString(){
//		if(objectType == ObjectType.NUT){
//			return "nut";
//		}else if(this.objectType == ObjectType.NUT_SCREW){
//			return "nut-screw";
//		}else{
//			return "screw";
//		}
		return this.objectType.toString();
	}
	public String getColorString(){
//		if(this.objectColor == ObjectColor.RED){
//			return "red";
//		}else if(this.objectColor == ObjectColor.BLUE){
//			return "blue";
//		}else{
//			return "green";
//		}
		return this.objectColor.toString();
	}
	public int getPriorty() {
		return priorty;
	}
	public void incrementPriorty(){
		priorty++;
	}
	public void setPriorty(int priorty) {
		this.priorty = priorty;
	}
	public String getHolderName() {
		return holderName;
	}
	public void setHolderName(String holderName) {
		this.holderName = holderName;
	}
	public String getPropertyString(String propertyName){
		if(propertyName.equals(PROPERTIES[0])){
			return String.valueOf(this.id);
		}else if(propertyName.equals(PROPERTIES[1])){
			return this.objectColor.toString();
		}else if(propertyName.equals(PROPERTIES[2])){
			return this.objectType.toString();
		}
		else
			return "";
	}
	public String toString(){
		return "Order:"+
				this.getId()+","+
				this.getColorString()+","+
				this.getTypeString()+","+
				this.getPriorty()+","+
				this.getHolderName();
	}
	public static Order valueOf(String orderString){
		try{
			String[] orderArgs=orderString.split(":")[1].split(",");
			Order ord = new Order(Integer.valueOf(orderArgs[0]),
					ObjectColor.valueOf(orderArgs[1]),
					ObjectType.valueOf(orderArgs[2]),
					orderArgs[4]);
			ord.setPriorty(Integer.valueOf(orderArgs[3]));
	 		return ord;
		}catch(Exception e){
			return null;
		}
	}
	public void copyValues(Order other){
		this.id = other.getId();
		this.objectColor = other.getObjectColor();
		this.objectType = other.getObjectType();
		this.priorty = other.getPriorty();
		this.holderName = other.getHolderName();
	}
	public boolean equals(Object o){
		return this.id == ((Order)o).getId(); 
	}
}
