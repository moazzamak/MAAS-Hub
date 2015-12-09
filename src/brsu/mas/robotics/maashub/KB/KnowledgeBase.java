package brsu.mas.robotics.maashub.KB;

import java.util.Dictionary;

public class KnowledgeBase {
	Dictionary<String, Integer> KB;
	
	public void set(String key, Integer value){
		KB.put(key, value);
	}
	
	public void get(String key){
		KB.get(key);
	}
	
}
