package prism;

import java.util.ArrayList;
import java.util.List;

import edit.Patch;

public class Stream {

	String id;
	Producer base;
	List<Consumer> consumers = new ArrayList<Consumer>();
	int counter = -1;  // init -1 to ignore the first save of the .mf file
	
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
		
		if(!isConsuming() && !p.getEdits().isEmpty()) {
			System.out.println("\n[DEBUG PRISM] ("+ counter + ") Patch from " + p.getSourceID());
			System.out.println(p);
			System.out.println("---------------------\n");

			consumers
			.stream()
			.filter(cons -> !cons.getId().equals(p.getSourceID())) // don't consume your own patch
			.forEach(cons -> {
				try {
					cons.consume(p);
					}
				catch(Exception e) {
					System.out.println("\n[DEBUG PRISM] This Consumer failed: " + cons.getId());
					e.printStackTrace();
				}
			});
		}
		
		counter++;
	}
	
	/*
	 * FIXME: assume consumers.size() never change
	 */
	private boolean isConsuming() {
		int numberOfCounsumer = consumers.size(); 
		
		return (counter % numberOfCounsumer) != 0;
	}
	
	public List<Consumer> getConsumers() {
		return consumers;
	}
	
}
