package fluent;

public class Transition {
	
	State container;
	String target;
	String event;
	
	public Transition(State container, String stateName) {
		this.container = container;
		target = stateName;
	}
	
	public Transition target(String stateName) {
		return container.target(stateName);
	}
	
	public State on(String event) {
		this.event = event;
		return container;
	}
	
	public State state(String name) {
		return container.state(name);
	}
	
	public Fsm end() {
		return container.end();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\t\tto: "+ target);
		if(event != null)
			sb.append(" on: "+ event);
		sb.append("\n");
		return sb.toString();
	}
}
