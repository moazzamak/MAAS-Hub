package com.maas.utils;

import java.sql.Timestamp;
import java.util.Date;

public class TimeUtils {
	private static TimeUtils instance=null;
	private TimeUtils(){
	}
	public Timestamp getCurrentTimestamp(){
		return new Timestamp(System.currentTimeMillis());
    }
	public String getCurrentTimestampString(){
		return new Timestamp(System.currentTimeMillis()).toString();
	}
	public static TimeUtils getInstance(){
		if(instance == null){
			instance = new TimeUtils();
		}
		return instance;
	}
}
