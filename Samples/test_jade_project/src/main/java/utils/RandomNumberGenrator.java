package utils;

import java.util.Random;

public class RandomNumberGenrator {
	private static RandomNumberGenrator classInstance=null;
	public static RandomNumberGenrator getInstance(){
		if(classInstance == null){
			classInstance = new RandomNumberGenrator();
		}
		return classInstance;
	}
	protected RandomNumberGenrator(){
	}
	public int randInt(int min, int max) {
	    Random rand = new Random();
	    int randomNum = rand.nextInt((max - min) + 1) + min;
	    return randomNum;
	}
}
