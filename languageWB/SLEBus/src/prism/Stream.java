package prism;

import java.util.ArrayList;
import java.util.List;

import edit.Patch;

public class Stream {

	String id;
	Producer base;
	List<Consumer> consumers = new ArrayList<Consumer>();
	int counter = 0;
	
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
		
		counter++;
		System.out.println("\n[DEBUG PRISM] ("+ counter + ") Patch from " + p.getSourceID());
		System.out.println(p);
		System.out.println("---------------------\n");
		
		if(p.getEdits().isEmpty())
			return;
		
		consumers
		.stream()
		.filter(cons -> !cons.getId().equals(p.getSourceID())) // don't consume your own patch
		.forEach(cons -> cons.consume(p));
	}
	
	public List<Consumer> getConsumers() {
		return consumers;
	}
	
}
