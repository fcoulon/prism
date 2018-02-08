package slebus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edit.Patch;

public class Bus {
	
	Map<String,List<Consumer>> subscribers = new HashMap<String,List<Consumer>>();
	
	/**
	 * Subscribes 'cons' to Patchs published on 'id'
	 */
	public void subscribe(Consumer cons, String id) {
		List<Consumer> group = subscribers.get(id);
		if(group == null) {
			group = new ArrayList<Consumer>();
			subscribers.put(id, group);
		}
		group.add(cons);
	}
	
	/**
	 * Publish 'p' on 'id'
	 */
	public void publish(Patch p, String id) {
		
		System.out.println(p);
		
		List<Consumer> group = subscribers.get(id);
		if(group != null) {
			group.stream().forEach(cons -> cons.consume(p));
		}
	}
}
