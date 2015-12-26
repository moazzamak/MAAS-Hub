package com.maas.domain;

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
	}
	public Order(int id, ObjectColor color, ObjectType type){
		this.id = id;
		this.objectColor = color;
		this.objectType = type;
		
	}
	private int id;
	private ObjectColor objectColor;
	private ObjectType objectType;
	
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
	public String toString(){
		return "Order:"+this.getId()+","+this.getColorString()+","+this.getTypeString();
	}
	public static Order valueOf(String orderString){
		String[] orderArgs=orderString.split(":")[1].split(",");
		Order ord = new Order(Integer.valueOf(orderArgs[0]),
				ObjectColor.valueOf(orderArgs[1]),
				ObjectType.valueOf(orderArgs[2]));
		
 		return ord;
	}
}
