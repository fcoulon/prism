package slebus;

import java.util.ArrayList;
import java.util.List;

import edit.Patch;

public class Stream {

	String id;
	Producer base;
	List<Consumer> consumers = new ArrayList<Consumer>();
	
	public Stream(String id, Producer base) {
		this.id = id;
		this.base = base;
		consumers.add(base);
	}
	
	public String getId() {
		return id;
	}
	
	public void synchronize(Consumer cons) {
		Patch initState = base.synchronize();
		cons.synchronize(initState);
		consumers.add(cons);
	}
	
	public void push(Patch p) {
		consumers
		.stream()
		.filter(cons -> !cons.getId().equals(p.getSourceID())) // don't consume your own patch
		.forEach(cons -> cons.consume(p));
	}
	
	public List<Consumer> getConsumers() {
		return consumers;
	}
	
}
