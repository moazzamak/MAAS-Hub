package com.maas.domain;

public class Order {
	public enum ObjectColor {
		RED, BLUE, GREEN
	}

	public enum ObjectType {
		SCREW, NUT, NUT_SCREW
	}

	public static final String[] PROPERTIES = { "Order", "Progress" };
	private int id;
	private ObjectColor objectColor;
	private ObjectType objectType;
	private int priorty;
	private String holderName;
	private String progress;

	public Order(int id) {
		this.id = id;
		this.priorty = 0;
		progress = null;
	}

	public Order(int id, ObjectColor color, ObjectType type, String name) {
		this.id = id;
		this.objectColor = color;
		this.objectType = type;
		this.priorty = 0;
		this.setHolderName(name);
		progress = null;
	}

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

	public String getTypeString() {
		return this.objectType.toString();
	}

	public String getColorString() {
		return this.objectColor.toString();
	}

	public int getPriorty() {
		return priorty;
	}

	public void incrementPriorty() {
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

	public String getProgress() {
		return this.progress;
	}

	public void setProgress(String prog) {
		this.progress = prog;
	}

	public void addToProgress(String step) {
		if (this.progress == null) {
			this.progress = step;
		} else {
			this.progress = this.progress + "-" + step;
		}
	}

	// public String getProgressString(){
	// String prog="";
	// for(String step : this.progress){
	// prog.concat(step+"-");
	// }
	// System.out.println("Size progress : "+String.valueOf(progress.size()));
	// if(progress.size() > 1){
	// return prog.substring(0, prog.lastIndexOf("-"));
	// }else{
	// return prog;
	// }
	// }

	public String getPropertyString(String propertyName) {
		if (propertyName.equals(PROPERTIES[0])) {
			return "Order:" + this.getId() + "," + this.getColorString() + ","
					+ this.getTypeString();
		} else if (propertyName.equals(PROPERTIES[1])) {
			return this.getProgress();
		} else {
			return "";
		}
	}

	public String toString() {
		String ord = "Order:" + this.getId() + "," + this.getColorString()
				+ "," + this.getTypeString() + "," + this.getPriorty() + ","
				+ this.getHolderName();
		if (this.getProgress() != null) {
			ord = ord + "," + this.getProgress();
		}
		return ord;
	}

	public static Order valueOf(String orderString) {
		try {
			String[] orderArgs = orderString.split(":")[1].split(",");
			Order ord = new Order(Integer.valueOf(orderArgs[0]),
					ObjectColor.valueOf(orderArgs[1]),
					ObjectType.valueOf(orderArgs[2]), orderArgs[4]);
			ord.setPriorty(Integer.valueOf(orderArgs[3]));
			if (orderArgs.length > 5) {
				ord.setProgress(orderArgs[5]);
			}
			return ord;
		} catch (Exception e) {
			System.out.println("Null Pointer Exception : " + e.getStackTrace());
			return null;
		}
	}

	public void copyValues(Order other) {
		// TODO : Incomplete
		this.id = other.getId();
		this.objectColor = other.getObjectColor();
		this.objectType = other.getObjectType();
		this.priorty = other.getPriorty();
		this.holderName = other.getHolderName();
	}

	public boolean equals(Object o) {
		return this.id == ((Order) o).getId();
	}

	public boolean isComplete() {
		if (this.progress != null) {
			return (this.progress.contains(this.getTypeString()) && this.progress
					.contains(this.getColorString()));
		} else {
			return false;
		}
	}
}
